package com.covision.covisionapp.fragments;

import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.SurfaceTexture;
import android.graphics.drawable.BitmapDrawable;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.VolumeShaper;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.util.Size;
import android.view.LayoutInflater;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.covision.covisionapp.R;
import com.covision.covisionapp.structures.ObjectDetectionResult;
import com.covision.covisionapp.workers.ObjectDetectionWorker;

import java.util.Collections;
import java.util.List;

public class ObjectDetectionFragment extends Fragment {
    private CameraManager cameraManager;
    private CameraDevice device;
    private CameraCaptureSession cameraCaptureSession;
    private CaptureRequest.Builder captureRequestBuilder;
    private int cameraFacing;
    private String cameraId = null;
    private ImageView imageView;
    private TextureView textureView;
    private TextureView.SurfaceTextureListener surfaceTextureListener;
    private Size previewSize;

    private Handler backgroundHandler;
    private HandlerThread backgroundThread;
    private CameraDevice.StateCallback stateCallback;
    private DetectionMessageCallback detectCallback;

    private boolean cameraOpened = false;

    private Bitmap workingImage;

    public ObjectDetectionFragment() {
        // Required empty public constructor
    }

    public interface ObjectDetectionCallback {
        void onDetectionResult(List<ObjectDetectionResult> result);
        void onError(String message);
    }

    public interface DetectionMessageCallback {
        void onDetectionResult(String result);
        void onError(String message);
    }

    public void detect(DetectionMessageCallback callback, String type)
    {
        detectCallback = callback;
        if (textureView.isAvailable() && cameraId != null)
        {
            if (!cameraOpened) openCamera();
            (new Handler()).postDelayed(new Runnable() {
                @Override
                public void run() {
                    captureImage(type);
                }
            }, 1000);
        }
        else
        {
            detectCallback.onError("Ocurrio un problema al abrir la camara");
        }
    }

    private void captureImage(String type)
    {
        Bitmap image = textureView.getBitmap();
        /*switch (rotation) {
            case Surface.ROTATION_90:
                // left or counter-clockwise
                image = rotateBitmap(image, -90);
                break;
            case Surface.ROTATION_270:
                image = rotateBitmap(image, 90);
                // Rigth or clockwise
                break;
        }*/
        workingImage = image.copy(image.getConfig(), true);
        final Canvas canvas = new Canvas(workingImage);
        imageView.setImageBitmap(workingImage);
        new ObjectDetectionWorker(getContext(), image, new ObjectDetectionCallback() {
            @Override
            public void onDetectionResult(List<ObjectDetectionResult> result) {
                Paint paint = new Paint();
                paint.setARGB(1, 100, 100, 100);
                String text = "";
                ObjectDetectionResult box = null;
                for (int i = 0; i<result.size(); i++){
                    box = result.get(i);
                    if (box.getType().equals("Analysis") && type.equals("navigation")) {
                        text = box.getClassName();
                    }
                    else if (box.getType().equals("CountText") && type.equals("all")) {
                        text = box.getClassName();
                        if (text.split("\n").length == 1) text = "No encontre ningun objeto";
                    }
                    else if (box.getType().equals("BoundingBox"))
                    {
                        double[] rect = box.getBox();
                        canvas.drawRect((float)rect[0],(float)rect[1],(float)rect[2],(float)rect[3],paint);
                    }
                }
                detectCallback.onDetectionResult(text);
            }

            @Override
            public void onError(String message) {
                detectCallback.onError("Ocurrio un problema al conectarse con el servidor, vuelve a intentarlo");
            }
        }).execute();

        closeCamera();
    }

    public Bitmap rotateBitmap(Bitmap original, float degrees) {
        int width = original.getWidth();
        int height = original.getHeight();

        Matrix matrix = new Matrix();
        matrix.preRotate(degrees);

        Bitmap rotatedBitmap = Bitmap.createBitmap(original, 0, 0, width, height, matrix, true);
        Canvas canvas = new Canvas(rotatedBitmap);
        canvas.drawBitmap(original, 5.0f, 0.0f, null);

        return rotatedBitmap;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // infla el layout del fragmento
        View myView = inflater.inflate(R.layout.fragment_object_detection, container, false);

        cameraManager = (CameraManager) getActivity().getSystemService(Context.CAMERA_SERVICE);
        cameraFacing = CameraCharacteristics.LENS_FACING_BACK;
        textureView = myView.findViewById(R.id.texture_view);
        imageView = myView.findViewById(R.id.image_view);

        surfaceTextureListener = new TextureView.SurfaceTextureListener() {
            @Override
            public void onSurfaceTextureAvailable(SurfaceTexture surfaceTexture, int width, int height) {
                setUpCamera();
            }

            @Override
            public void onSurfaceTextureSizeChanged(SurfaceTexture surfaceTexture, int width, int height) {

            }

            @Override
            public boolean onSurfaceTextureDestroyed(SurfaceTexture surfaceTexture) {
                return false;
            }

            @Override
            public void onSurfaceTextureUpdated(SurfaceTexture surfaceTexture) {

            }
        };

        stateCallback = new CameraDevice.StateCallback() {
            @Override
            public void onOpened(CameraDevice cameraDevice) {
                ObjectDetectionFragment.this.device = cameraDevice;
                createPreviewSession();
            }

            @Override
            public void onDisconnected(CameraDevice cameraDevice) {
                cameraDevice.close();
                ObjectDetectionFragment.this.device = null;
            }

            @Override
            public void onError(CameraDevice cameraDevice, int error) {
                cameraDevice.close();
                ObjectDetectionFragment.this.device = null;
            }
        };

        return myView;
    }

    @Override
    public void onResume() {
        openBackgroundThread();
        if (textureView.isAvailable()) {
            setUpCamera();
        } else {
            textureView.setSurfaceTextureListener(surfaceTextureListener);
        }
        super.onResume();
    }

    private void setUpCamera() {
        try {
            for (String cameraId : cameraManager.getCameraIdList()) {
                CameraCharacteristics cameraCharacteristics =
                        cameraManager.getCameraCharacteristics(cameraId);
                if (cameraCharacteristics.get(CameraCharacteristics.LENS_FACING) ==
                        cameraFacing) {
                    StreamConfigurationMap streamConfigurationMap = cameraCharacteristics.get(
                            CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
                    previewSize = streamConfigurationMap.getOutputSizes(SurfaceTexture.class)[0];
                    this.cameraId = cameraId;
                }
            }
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private void openCamera() {
        try {
            if (ActivityCompat.checkSelfPermission(getContext(), android.Manifest.permission.CAMERA)
                    == PackageManager.PERMISSION_GRANTED) {
                cameraManager.openCamera(cameraId, stateCallback, backgroundHandler);
                cameraOpened = true;
            }
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private void openBackgroundThread() {
        backgroundThread = new HandlerThread("camera_background_thread");
        backgroundThread.start();
        backgroundHandler = new Handler(backgroundThread.getLooper());
    }

    private void createPreviewSession() {
        try {
            SurfaceTexture surfaceTexture = textureView.getSurfaceTexture();
            surfaceTexture.setDefaultBufferSize(previewSize.getWidth(), previewSize.getHeight());
            Surface previewSurface = new Surface(surfaceTexture);
            captureRequestBuilder = device.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
            captureRequestBuilder.addTarget(previewSurface);

            device.createCaptureSession(Collections.singletonList(previewSurface),
                    new CameraCaptureSession.StateCallback() {

                        @Override
                        public void onConfigured(CameraCaptureSession cameraCaptureSession) {
                            if (device == null) {
                                return;
                            }

                            try {
                                CaptureRequest captureRequest = captureRequestBuilder.build();
                                ObjectDetectionFragment.this.cameraCaptureSession = cameraCaptureSession;
                                ObjectDetectionFragment.this.cameraCaptureSession.setRepeatingRequest(captureRequest,
                                        null, backgroundHandler);
                            } catch (CameraAccessException e) {
                                e.printStackTrace();
                            }
                        }

                        @Override
                        public void onConfigureFailed(CameraCaptureSession cameraCaptureSession) {

                        }
                    }, backgroundHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        closeCamera();
        closeBackgroundThread();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        closeCamera();
        closeBackgroundThread();
    }

    private void closeCamera() {
        if (cameraCaptureSession != null) {
            cameraCaptureSession.close();
            cameraCaptureSession = null;
        }

        if (device != null) {
            device.close();
            device = null;
        }

        cameraOpened = false;
    }

    private void closeBackgroundThread() {
        if (backgroundHandler != null) {
            backgroundThread.quitSafely();
            backgroundThread = null;
            backgroundHandler = null;
        }
    }
}
