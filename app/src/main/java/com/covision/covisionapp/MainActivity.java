package com.covision.covisionapp;

import android.Manifest;
import android.app.FragmentTransaction;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.covision.covisionapp.fragments.MapsFragment;
import com.covision.covisionapp.fragments.ObjectDetectionFragment;
import com.covision.covisionapp.fragments.VoiceFragment;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{

    public static final int CAMERA_REQUEST_CODE = 200;
    public static final int REQUEST_RECORD_PERMISSION = 100;
    public VoiceFragment voiceFragment;
    public ObjectDetectionFragment fragment;
    public MapsFragment mapFragment;
    private Button speakButton;
    private String TAG="replace fragment";

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

        mapFragment = new MapsFragment();




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
                   //voiceFragment.recordSpeak();
                } else {
                    Toast.makeText(getApplicationContext(), "Tu dispositivo no permite la función de text to speech", Toast.LENGTH_SHORT).show();
                }
            }

            // los otros casos pueden ser otros permisos que la palicación necesite verificar que tiene
        }
    }

    /**
     * Change the current displayed fragment by a new one.
     * - if the fragment is in backstack, it will pop it
     * - if the fragment is already displayed (trying to change the fragment with the same), it will not do anything
     *
     * @param frag            the new fragment to display
     * @param saveInBackstack if we want the fragment to be in backstack
     * @param animate         if we want a nice animation or not
     */
    public void changeFragment(Fragment frag, boolean saveInBackstack, boolean animate) {
        String backStateName = ((Object) frag).getClass().getName();

        try {
            FragmentManager manager = getSupportFragmentManager();
            boolean fragmentPopped = manager.popBackStackImmediate(backStateName, 0);

            if (!fragmentPopped && manager.findFragmentByTag(backStateName) == null) {
                //fragment not in back stack, create it.
                android.support.v4.app.FragmentTransaction transaction = manager.beginTransaction();

                if (animate) {
                    Log.d(TAG, "Change Fragment: animate");
                    transaction.setCustomAnimations(R.anim.slide_in_left, R.anim.slide_out_left, R.anim.slide_in_left, R.anim.slide_out_right);
                }

                transaction.replace(R.id.container, frag, backStateName);

                if (saveInBackstack) {
                    Log.d(TAG, "Change Fragment: addToBackTack " + backStateName);
                    transaction.addToBackStack(backStateName);
                } else {
                    Log.d(TAG, "Change Fragment: NO addToBackTack");
                }

                transaction.commit();
            } else {
                // custom effect if fragment is already instanciated
            }
        } catch (IllegalStateException exception) {
            Log.w(TAG, "Unable to commit fragment, could be activity as been killed in background. " + exception.toString());
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
