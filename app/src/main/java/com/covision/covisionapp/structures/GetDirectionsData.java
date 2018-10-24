package com.covision.covisionapp.structures;

import android.os.AsyncTask;

import com.covision.covisionapp.workers.DownloadUrl;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.util.HashMap;

public class GetDirectionsData extends AsyncTask<Object,String,String> {

    GoogleMap mMap;
    String url;
    String googleDirectionsData;
    String duration, distance;
    LatLng latLng;

    @Override
    protected String doInBackground(Object... objects) {
        mMap = (GoogleMap)objects[0];
        url = (String)objects[1];
        DownloadUrl dwnu = new DownloadUrl();
        latLng= (LatLng) objects[2];
        try {

            googleDirectionsData = dwnu.readUrl(url);
        }catch (IOException e){
            e.printStackTrace();
        }
        return googleDirectionsData;
    }

    @Override
    protected void onPostExecute(String s){
        HashMap<String,String> directionList= null;
        DataParser parser = new DataParser();
        directionList = parser.parseDirections(s);
        duration = directionList.get("duration");
        distance = directionList.get("distance");
        mMap.clear();
        MarkerOptions mop = new MarkerOptions();
        mop.position(latLng);
        mop.draggable(true);
        mop.title("Duration="+duration);
        mop.snippet("Distance="+distance);
        mMap.addMarker(mop);


        //FragmentManager fm = getFragmentManager();
        //VoiceFragment voice = (VoiceFragment)fm.findFragmentById(R.id.voiceFragment);
        //voice.textToVoice("Te encuentras a una distancia de "+distance+ "y a un tiempo de recorrido de "+duration);


    }

    public String getDistance() {
        return distance;
    }

    public String getDuration() {
        return duration;
    }
}
