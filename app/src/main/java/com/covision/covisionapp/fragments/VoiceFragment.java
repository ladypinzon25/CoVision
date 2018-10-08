package com.covision.covisionapp.fragments;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.covision.covisionapp.MainActivity;
import com.covision.covisionapp.R;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Locale;

public class VoiceFragment extends Fragment {
    private ProgressBar progressBar;

    private TextToSpeech toSpeech;
    private int res;
    private SpeechRecognizer sr;

    private static final String TAG = "MainFragment";
    private String LOG_TAG = "SpeechToTextActivity";
    private String[] options = {"llevame a", "donde estoy", "frente"};

    private VoiceCallback callback;

    public enum VoiceResult {
        Location,
        Route,
        Detection
    }

    public VoiceFragment() {
        // Required empty public constructor
    }

    public interface VoiceCallback {
        void onSpeechResult(VoiceResult result, String... params);
        void onError(String message);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // infla el layout del fragmento
        View myView = inflater.inflate(R.layout.fragment_voice, container, false);
        progressBar = myView.findViewById(R.id.progressBar);
        progressBar.setVisibility(View.INVISIBLE);

        //crea el SpeechRecognizer y su listener
        sr = SpeechRecognizer.createSpeechRecognizer(getActivity());
        sr.setRecognitionListener(new listener());
        return myView;
    }

    @Override
    public void onDestroy() {
        sr.destroy();
        sr = null;
        super.onDestroy();
    }

    /*
     * listener del SpeechRecognizer.
     */
    class listener implements RecognitionListener {
        public void onReadyForSpeech(Bundle params) {
            Log.d(TAG, "onReadyForSpeech");
        }

        public void onBeginningOfSpeech() {

            Log.d(TAG, "onBeginningOfSpeech");
            progressBar.setIndeterminate(false);
            progressBar.setMax(10);
        }

        public void onRmsChanged(float rmsdB) {
            Log.d(TAG, "onRmsChanged");
            progressBar.setProgress((int) rmsdB);
        }

        public void onBufferReceived(byte[] buffer) {
            Log.d(TAG, "onBufferReceived");
        }

        public void onEndOfSpeech() {
            Log.d(TAG, "onEndofSpeech");
            progressBar.setIndeterminate(true);
            progressBar.setVisibility(View.INVISIBLE);
        }

        public void onError(int error) {
            Log.d(TAG, "error " + error);
        }

        public void onResults(Bundle results) {

            Log.d(TAG, "onResults " + results);
            // Lista de resultados obtenidos por el SpeechRecognizer
            ArrayList<String> matches = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
            //guarda el primer resultado
            if(matches !=null && !matches.isEmpty())
                logthis(matches.get(0));

        }

        public void onPartialResults(Bundle partialResults) {
            Log.d(TAG, "onPartialResults");
        }

        public void onEvent(int eventType, Bundle params) {
            Log.d(TAG, "onEvent " + eventType);
        }
    }

    public void recordSpeak(VoiceCallback callback) {
        this.callback = callback;
        //si la app no tiene permiso para usar microfono, lo pide
        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            Log.v(TAG, "asking for permissions");
            ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.RECORD_AUDIO}, MainActivity.REQUEST_RECORD);
        } else {
            progressBar.setVisibility(View.VISIBLE);
            progressBar.setIndeterminate(true);

            //se crea el intent para escuchar al usuario
            Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "es");
            intent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3);

            sr.startListening(intent);
            Log.i(TAG, "Intent sent");
        }
    }

    /*
     * método para añadir la información resivida al TextView, analizarla y reproducirla
     */
    public void logthis(String newinfo) {
        if (newinfo.compareTo("") != 0) {
            analizeSpeech(newinfo);
        }
    }
    /*
     * método que debe revisar el comando recibido por el usuario
     */
    public void analizeSpeech(String speech){
        speech = Normalizer.normalize(speech, Normalizer.Form.NFD).replaceAll("\\p{InCombiningDiacriticalMarks}+", "");
        speech = speech.toLowerCase();
        int opt = -1;
        Log.i(LOG_TAG, "analize: normalizó a "+ speech);
        for (int i=0; i<options.length && opt ==-1; i++){
            if(speech.contains(options[i])) opt = i;
        }
        Log.i(LOG_TAG, "analize: opcion "+ opt);
        switch (opt){
            case 0:
                if(speech.contains(" a ")){
                    //puede ser a, al, a la
                    String[] div = speech.split(" a ");
                    this.callback.onSpeechResult(VoiceResult.Route, div[1]);
                }
                break;
            case 1:
                this.callback.onSpeechResult(VoiceResult.Location);
                break;
            case 2:
                this.callback.onSpeechResult(VoiceResult.Detection);
                break;

            default:
                this.callback.onError("Lo siento, esa no es una opción disponible. Intenta de nuevo porfavor");
                break;
        }
    }

    /*
     * método para pasar de texto a voz
     */
    public void textToVoice(final String message){
        toSpeech = new TextToSpeech(getContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if(status == TextToSpeech.SUCCESS){
                    res = toSpeech.setLanguage(Locale.getDefault());
                }
                if(res == TextToSpeech.LANG_MISSING_DATA || res == TextToSpeech.LANG_NOT_SUPPORTED){
                    Toast.makeText(getActivity(), "Tu dispositivo no soporta la función de text to speech", Toast.LENGTH_SHORT).show();
                }
                else if (message !=null){
                    Log.i(LOG_TAG, "entra else textToSpeach");
                    toSpeech.speak(message, TextToSpeech.QUEUE_FLUSH, null, null);
                }
            }
        });
    }
}
