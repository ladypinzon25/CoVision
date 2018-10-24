package com.covision.covisionapp.workers;

import android.graphics.Color;
import android.os.AsyncTask;

import com.covision.covisionapp.structures.DataParser;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.maps.android.PolyUtil;

import java.io.IOException;

public class GetDirectionsDataRoutes extends AsyncTask<Object,String,String> {

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

        String[] directionList;
        DataParser parser = new DataParser();
        directionList=parser.parseDirectionsPaint(s);
        if(directionList!=null)
        displayDirection(directionList);

    }

    public void displayDirection(String[] directionsList){
        int count=directionsList.length;
        for (int i =0; i<count;i++){
            PolylineOptions options=new PolylineOptions();
            options.color(Color.RED);
            options.width(10);
            options.addAll(PolyUtil.decode(directionsList[i]));
            mMap.addPolyline(options);


        }
    }
}
