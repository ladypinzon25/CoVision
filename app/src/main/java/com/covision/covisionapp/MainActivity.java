package com.covision.covisionapp;

import android.Manifest;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.covision.covisionapp.fragments.ObjectDetectionFragment;
import com.covision.covisionapp.fragments.VoiceFragment;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{

    public static final int CAMERA_REQUEST_CODE = 200;
    public static final int REQUEST_RECORD_PERMISSION = 100;
    VoiceFragment voiceFragment;
    ObjectDetectionFragment fragment;
    private Button speakButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //boton principal
        speakButton =  findViewById(R.id.btnMic);
        speakButton.setOnClickListener(this);

        voiceFragment = new VoiceFragment();
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction().add(R.id.container, voiceFragment).commit();
        }

        /*// crea el fragmento de voz
        fragment = new ObjectDetectionFragment();
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction().add(R.id.container, fragment).commit();
        }*/

        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, CAMERA_REQUEST_CODE);
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO}, REQUEST_RECORD_PERMISSION);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        switch (requestCode) {
            //verific si hay permiso para usar el microfono
            case CAMERA_REQUEST_CODE: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    //voiceFragment.recordSpeak();
                } else {
                    Toast.makeText(getApplicationContext(), "Tu dispositivo no permite el uso de la camara", Toast.LENGTH_SHORT).show();
                }
            }

            case REQUEST_RECORD_PERMISSION: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    voiceFragment.recordSpeak();
                } else {
                    Toast.makeText(getApplicationContext(), "Tu dispositivo no permite la función de text to speech", Toast.LENGTH_SHORT).show();
                }
            }

            // los otros casos pueden ser otros permisos que la palicación necesite verificar que tiene
        }
    }


    /*
    onClick del boton principal
     */
    public void onClick(View v) {
        if (v.getId() == R.id.btnMic) {
            voiceFragment.recordSpeak();
            //fragment.detect();
        }
    }
}
