package com.covision.covisionapp;

import android.Manifest;
import android.animation.ValueAnimator;
import android.app.PendingIntent;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.hardware.SensorManager;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.provider.ContactsContract;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.support.design.widget.Snackbar;

import com.covision.covisionapp.dtos.ContactDTO;
import com.covision.covisionapp.fragments.MapsFragment;
import com.covision.covisionapp.fragments.NavigationFragment;
import com.covision.covisionapp.fragments.ObjectDetectionFragment;
import com.covision.covisionapp.fragments.VoiceFragment;
import com.mapbox.geojson.Point;
import com.mapbox.mapboxsdk.annotations.MarkerOptions;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.tomer.fadingtextview.FadingTextView;

import java.text.DecimalFormat;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{

    public static final int REQUEST_ALL = 100;
    public static final int REQUEST_CAMERA = 200;
    public static final int REQUEST_RECORD = 300;
    public static final int REQUEST_LOCATION = 400;
    private static final int PERMISSION_REQUEST_CONTACTS = 500;
    private static final int PERMISSION_REQUEST_SEND_SMS = 600;

    private VoiceFragment voice;
    private MapsFragment maps;
    private ObjectDetectionFragment objectDetection;
    private NavigationFragment navigFrag;

    private FragmentManager fragmentManager;

    private FrameLayout mapView;
    private FrameLayout navView;
    private FrameLayout detectionView;
    private FadingTextView fadingTextView;
    private View mLayout;

    private Button speakButton;
    private ArrayList<ContactDTO> contacts;
    private ContactDTO selectedContact;

    private boolean mapsHidden = true;
    private boolean detectionHidden = true;
    private boolean navigationHidden = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        contacts = new ArrayList<>();
        mLayout = findViewById(R.id.main_layout);

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
                navigFrag = new NavigationFragment();
                fragmentManager.beginTransaction().add(R.id.navigationFragment,navigFrag).commit();

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
                android.Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.SEND_SMS,
                Manifest.permission.READ_CONTACTS
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
        navView = findViewById(R.id.navigationFragment);
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
                if (!navigationHidden) hideNavigation();

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
                                String les=maps.geoLocate(params[0]);
                                if(!les.equals("error")){
                                    String[] res = les.split("&");
                                    Double longtud= Double.valueOf(res[2]);
                                    Double latisnd= Double.valueOf(res[1]);
                                    Double latiOri= Double.valueOf(res[3]);
                                    Double longOri= Double.valueOf(res[4]);
                                    navigFrag.setLocationDestination(new LatLng(latisnd,longtud));
                                    navigFrag.setLocationOrigin(new LatLng(latiOri,longOri));
                                    les = res[0].replace(".","=");
                                    String[]d = les.split("=");
                                    voice.textToVoice( "estas a una distancia de "+ d[0] + " metros");
                                    hideMaps();
                                    showNavigation();
                                    navigFrag.startTheNavRes();
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
                            case SendMessage:
                                sendMessageToEmergencyContact(params[0]);
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

    private void sendMessageToEmergencyContact(String name){
        loadContacts();
        for (int i = 0; i < contacts.size() && selectedContact == null; i++) {
            if (contacts.get(i).getName().toLowerCase().contains(name.toLowerCase())) {
                selectedContact = contacts.get(i);
            }
        }

        String[] PERMISSIONS = {
                Manifest.permission.SEND_SMS,
                Manifest.permission.READ_CONTACTS
        };

        if(!hasPermissions(this, PERMISSIONS)){
            Snackbar.make(mLayout, R.string.permissions_denied, Snackbar.LENGTH_SHORT).show();
            ActivityCompat.requestPermissions(this, PERMISSIONS, REQUEST_ALL);
        }
        if (hasPermissions(this, PERMISSIONS)) {
            if (selectedContact != null) {
                String message = "Hola " + selectedContact.getName() + ", el usuario ha llegado a su destino";
                sendMessage(selectedContact.getNumber(), message);
            } else {
                voice.textToVoice("No se ha encontrado al contacto. Intenta de nuevo.");
            }
        }
    }

    private void loadContacts() {
        ContentResolver contentResolver = getContentResolver();
        Cursor cursor = contentResolver.query(ContactsContract.Contacts.CONTENT_URI, null, null, null, null);
        while (cursor.moveToNext()) {
            if (cursor.getCount() > 0) {
                String id = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts._ID));
                String name = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
                int hasPhoneNumber = Integer.parseInt(cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER)));

                if (hasPhoneNumber > 0) {
                    Cursor cursor2 = contentResolver.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                            null,
                            ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?",
                            new String[]{id}, null);
                    while (cursor2.moveToNext()) {
                        String phoneNumber = cursor2.getString(cursor2.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                        ContactDTO contact = new ContactDTO(name, phoneNumber);
                        contacts.add(contact);
                    }
                    cursor2.close();
                }
            }
        }
        cursor.close();
    }

    public void sendMessage(String number, String message) {
        PendingIntent sent = PendingIntent.getBroadcast(getBaseContext(), 0, new Intent("sent"), 0);
        PendingIntent deliver = PendingIntent.getBroadcast(getBaseContext(), 0, new Intent("delivered"), 0);

        SmsManager smsManager = SmsManager.getDefault();
         smsManager.sendTextMessage(number, null, message, sent, deliver);
        voice.textToVoice("M.");
        voice.textToVoice("Se ha avisado al contacto que llegaste al destino");

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

    private void hideNavigation()
    {
        ValueAnimator animX = ValueAnimator.ofFloat(0, 2* navView.getWidth());
        animX.setDuration(500);
        animX.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {

            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                navView.setTranslationX((float) animation.getAnimatedValue());
            }
        });
        animX.start();
        navigationHidden = true;
    }

    private void showNavigation()
    {
        ValueAnimator animX = ValueAnimator.ofFloat(2* navView.getWidth(), 0);
        animX.setDuration(500);
        animX.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {

            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                navView.setTranslationX((float) animation.getAnimatedValue());
            }
        });
        animX.start();
        navigationHidden = false;
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
