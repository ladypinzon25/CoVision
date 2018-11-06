package com.covision.covisionapp;

import android.animation.ValueAnimator;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.hardware.SensorManager;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.covision.covisionapp.fragments.MapsFragment;
import com.covision.covisionapp.fragments.ObjectDetectionFragment;
import com.covision.covisionapp.fragments.VoiceFragment;
import com.tomer.fadingtextview.FadingTextView;

import java.text.DecimalFormat;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{

    public static final int REQUEST_ALL = 100;
    public static final int REQUEST_CAMERA = 200;
    public static final int REQUEST_RECORD = 300;
    public static final int REQUEST_LOCATION = 400;

    private VoiceFragment voice;
    private MapsFragment maps;
    private ObjectDetectionFragment objectDetection;

    private FragmentManager fragmentManager;

    private FrameLayout mapView;
    private FrameLayout detectionView;
    private FadingTextView fadingTextView;

    private Button speakButton;

    private boolean mapsHidden = true;
    private boolean detectionHidden = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Boton principal
        speakButton =  findViewById(R.id.btnMic);
        speakButton.setOnClickListener(this);

        // fading text on start
        fadingTextView = findViewById(R.id.fading_text_view);

        // Fragmentos
        fragmentManager = getSupportFragmentManager();

        if(!isNetworkAvailable()){
            displayContextualInfoOnNoInternet();
            turnOnWifiRequest();
        }
        if (savedInstanceState == null) {
            voice = new VoiceFragment();
            fragmentManager.beginTransaction().add(R.id.voiceFragment, voice).commit();
            if (checkInternet()) {
                maps = new MapsFragment();
                fragmentManager.beginTransaction().add(R.id.mapsFragment, maps).commit();

                objectDetection = new ObjectDetectionFragment();
                fragmentManager.beginTransaction().add(R.id.objectDetectionFragment, objectDetection).commit();
            }
            else {
                if(voice!=null)
                    voice.textToVoice("No tienes conexión a internet. Intenta más tarde");
            }
        }

        String[] PERMISSIONS = {
                android.Manifest.permission.RECORD_AUDIO,
                android.Manifest.permission.CAMERA,
                android.Manifest.permission.ACCESS_FINE_LOCATION,
                android.Manifest.permission.ACCESS_COARSE_LOCATION
        };

        if(!hasPermissions(this, PERMISSIONS)){
            ActivityCompat.requestPermissions(this, PERMISSIONS, REQUEST_ALL);
        }
    }

    private void displayContextualInfoOnNoInternet(){
        String[] t ={"Revisa tu conexion a internet","Prende el Wifi","Sal del sotano","App no apta para ascensores"};
        fadingTextView.setTexts(t);
        fadingTextView.setTextSize(38);
        Toast.makeText(this,
                "Please check your internet connection state", Toast.LENGTH_LONG).show();
        if(voice!=null){
            voice.textToVoice("No tienes conexion a internet. Intenta más tarde");
        }
    }


    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    @Override
    public void onStart(){
        mapView = findViewById(R.id.mapsFragment);
        detectionView = findViewById(R.id.objectDetectionFragment);
        super.onStart();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_CAMERA:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Granted
                } else {
                    // Not granted
                }
                break;
            case REQUEST_RECORD:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Granted
                } else {
                    // Not granted
                }
                break;
            case REQUEST_LOCATION:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Granted
                } else {
                    // Not granted
                }
                break;
        }
    }


    /*
    onClick del boton principal
     */
    public void onClick(final View v) {
        if(isNetworkAvailable()){
            String[] f= {""};
            fadingTextView.setTexts(f);

        if (v.getId() == R.id.btnMic) {
            if (checkInternet()){
                if (!mapsHidden) hideMaps();
                if (!detectionHidden) hideObjectDetection();
                voice.recordSpeak(new VoiceFragment.VoiceCallback() {
                    @Override
                    public void onSpeechResult(VoiceFragment.VoiceResult result, String... params) {
                        switch (result) {
                            case Location:
                                voice.textToVoice(maps.showCurrentPlace());
                                showMaps();
                                break;
                            case Route:
                                voice.textToVoice("Calculando ruta hacia " + params[0]);
                                showMaps();
                                String res=maps.geoLocate(params[0]);
                                if(!res.equals("error")){
                                    res = res.replace(".","=");
                                    String[]d = res.split("=");
                                    Log.d("QUE FOCO ",res);
                                    voice.textToVoice( "estas a una distancia de "+ d[0] + " metros");
                                }else{
                                    voice.textToVoice("No se pudo calcular la distancia hasta su destino");
                                }
                                break;
                            case Detection:
                                voice.textToVoice("Iniciando análisis de imagen");
                                objectDetection.detect(new ObjectDetectionFragment.DetectionMessageCallback() {
                                    @Override
                                    public void onDetectionResult(String result) {
                                        showObjectDetection();
                                        voice.textToVoice(result);
                                    }

                                    @Override
                                    public void onError(String message) {
                                        voice.textToVoice(message);
                                    }
                                }, params[0]);
                                break;
                        }
                    }

                    @Override
                    public void onError(String message) {
                        voice.textToVoice(message);
                    }
                });
            }
            else {
                if(voice!=null)
                    voice.textToVoice("No tienes conexion a internet. Intenta más tarde");
            }
         }
        }
        else{
            displayContextualInfoOnNoInternet();
            turnOnWifiRequest();
            if(isNetworkAvailable()){
                Toast.makeText(this,
                        "Detected Internet Conection - Back Online!", Toast.LENGTH_LONG).show();
            }
        }
    }

    private void hideMaps()
    {
        ValueAnimator animX = ValueAnimator.ofFloat(0, 2* mapView.getWidth());
        animX.setDuration(500);
        animX.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {

            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                mapView.setTranslationX((float) animation.getAnimatedValue());
            }
        });
        animX.start();
        mapsHidden = true;
    }

    private void showMaps()
    {
        ValueAnimator animX = ValueAnimator.ofFloat(2* mapView.getWidth(), 0);
        animX.setDuration(500);
        animX.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {

            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                mapView.setTranslationX((float) animation.getAnimatedValue());
            }
        });
        animX.start();
        mapsHidden = false;
    }

    private void hideObjectDetection()
    {
        ValueAnimator animX = ValueAnimator.ofFloat(0, -2* detectionView.getWidth());
        animX.setDuration(500);
        animX.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {

            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                detectionView.setTranslationX((float) animation.getAnimatedValue());
            }
        });
        animX.start();
        detectionHidden = true;
    }

    private void showObjectDetection()
    {
        ValueAnimator animX = ValueAnimator.ofFloat(-2* detectionView.getWidth(), 0);
        animX.setDuration(500);
        animX.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {

            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                detectionView.setTranslationX((float) animation.getAnimatedValue());
            }
        });
        animX.start();
        detectionHidden = false;
    }

    public static void setMargins (View v, int l, int t, int r, int b) {
        if (v.getLayoutParams() instanceof ViewGroup.MarginLayoutParams) {
            ViewGroup.MarginLayoutParams p = (ViewGroup.MarginLayoutParams) v.getLayoutParams();
            p.setMargins(l, t, r, b);
            v.requestLayout();
        }
    }

    public static boolean hasPermissions(Context context, String... permissions) {
        if (context != null && permissions != null) {
            for (String permission : permissions) {
                if (ActivityCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
            }
        }
        return true;
    }

    private boolean checkInternet (){
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        if(connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState() == NetworkInfo.State.CONNECTED ||
                connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState() == NetworkInfo.State.CONNECTED){
            return true;
        }
        return false;
    }
    private void turnOnWifiRequest(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Do you want to turn WIFI ON?")
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        WifiManager wifi = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
                        wifi.setWifiEnabled(true);// true or false to activate/deactivate wifi
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // Do Nothing or Whatever you want.
                        dialog.cancel();
                    }
                });
        AlertDialog alert = builder.create();
        alert.show();
    }
}
