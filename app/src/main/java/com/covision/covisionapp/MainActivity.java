package com.covision.covisionapp;

import android.content.Context;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.covision.covisionapp.fragments.MapsFragment;
import com.covision.covisionapp.fragments.ObjectDetectionFragment;
import com.covision.covisionapp.fragments.VoiceFragment;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{

    public static final int REQUEST_ALL = 100;
    public static final int REQUEST_CAMERA = 200;
    public static final int REQUEST_RECORD = 300;
    public static final int REQUEST_LOCATION = 400;

    VoiceFragment voice;
    MapsFragment maps;
    ObjectDetectionFragment objectDetection;
    FragmentManager fragmentManager;

    private Button speakButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Boton principal
        speakButton =  findViewById(R.id.btnMic);
        speakButton.setOnClickListener(this);

        // Fragmentos
        fragmentManager = getSupportFragmentManager();

        if (savedInstanceState == null) {
            voice = new VoiceFragment();
            fragmentManager.beginTransaction().add(R.id.voiceFragment, voice).commit();
            maps = new MapsFragment();
            fragmentManager.beginTransaction().add(R.id.mapsFragment, maps).commit();
            findViewById(R.id.mapsFragment).setVisibility(View.INVISIBLE);
            objectDetection = new ObjectDetectionFragment();
            fragmentManager.beginTransaction().add(R.id.objectDetectionFragment, objectDetection).commit();
            findViewById(R.id.objectDetectionFragment).setVisibility(View.INVISIBLE);
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
    public void onClick(View v) {
        if (v.getId() == R.id.btnMic) {
            voice.recordSpeak(new VoiceFragment.VoiceCallback() {
                @Override
                public void onSpeechResult(VoiceFragment.VoiceResult result, String... params) {
                    switch (result)
                    {
                        case Location:
                            voice.textToVoice(maps.showCurrentPlace());
                            showMaps();
                            break;
                        case Route:
                            voice.textToVoice("Calculando ruta hacia "+ params[0]);
                            showMaps();
                            maps.geoLocate(params[0]);
                            break;
                        case Detection:
                            voice.textToVoice("Iniciando análisis de imagen");
                            showObjectDetection();
                            objectDetection.detect();
                            break;

                    }
                }

                @Override
                public void onError(String message) {
                    voice.textToVoice(message);
                }
            });
        }
    }

    private void showMaps()
    {
        findViewById(R.id.mapsFragment).setVisibility(View.VISIBLE);
        FragmentTransaction ft = fragmentManager.beginTransaction();
        ft.setCustomAnimations(R.anim.slide_in_right,0);
        ft.show(maps);
        ft.commit();
    }

    private void showObjectDetection()
    {
        findViewById(R.id.objectDetectionFragment).setVisibility(View.VISIBLE);
        FragmentTransaction ft = fragmentManager.beginTransaction();
        ft.setCustomAnimations(R.anim.slide_in_right,0);
        ft.show(objectDetection);
        ft.commit();
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
}
