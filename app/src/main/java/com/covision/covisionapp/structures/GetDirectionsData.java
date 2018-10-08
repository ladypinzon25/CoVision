package com.covision.covisionapp.structures;

import android.os.AsyncTask;
import android.util.Log;

import com.google.android.gms.maps.GoogleMap;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;

public class GetDirectionsData extends AsyncTask<Object,String,String> {

    GoogleMap mMap;
    String url;
    String googleDirectionsData;

    @Override
    protected String doInBackground(Object... objects) {
        mMap = (GoogleMap)objects[0];
        url = (String)objects[1];
        DownloadUrl  dwnu = new DownloadUrl();

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


    }
}
