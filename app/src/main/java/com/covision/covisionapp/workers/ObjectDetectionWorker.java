package com.covision.covisionapp.workers;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.util.Base64;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.covision.covisionapp.fragments.ObjectDetectionFragment;
import com.covision.covisionapp.structures.ObjectDetectionRequest;
import com.covision.covisionapp.structures.ObjectDetectionResult;

import java.io.ByteArrayOutputStream;
import java.util.List;

public class ObjectDetectionWorker extends AsyncTask<Void, Void, Void> {
    private final static String SERVER_URL = "http://35.227.86.190:8080/detect";

    private Context context;
    private Bitmap image;
    private ObjectDetectionFragment.ObjectDetectionCallback callback;


    public ObjectDetectionWorker(Context context, Bitmap imageToProcess, ObjectDetectionFragment.ObjectDetectionCallback callback)
    {
        this.context = context;
        this.image = imageToProcess;
        this.callback = callback;
    }

    @Override
    public Void doInBackground(Void... params)
    {
        String encodedImage = encodeToBase64(image);

        ObjectDetectionRequest request = new ObjectDetectionRequest(SERVER_URL, "navigation", encodedImage, new Response.Listener<List<ObjectDetectionResult>>() {
            @Override
            public void onResponse(List<ObjectDetectionResult> response) {
                ObjectDetectionWorker.this.callback.onDetectionResult(response);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                ObjectDetectionWorker.this.callback.onError(error.getMessage());
            }
        });

        RestRequestQueue.getInstance(this.context).addToRequestQueue(request);

        return null;
    }


    public static String encodeToBase64(Bitmap image)
    {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        image.compress(Bitmap.CompressFormat.JPEG, 80, baos);
        byte[] b = baos.toByteArray();
        String imageEncoded = Base64.encodeToString(b,Base64.DEFAULT);
        return imageEncoded;
    }
}
