package com.covision.covisionapp.structures;

import java.util.HashMap;
import android.util.Log;

import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;


public class DataParser {


    private HashMap<String, String> getDuration (JSONArray googleDirectionsJson){
        HashMap<String, String> googleDirectionsMap = new HashMap<>();
        String duration="";
        String distance="";

        if(googleDirectionsJson!=null) {
            Log.d("Json RESPONSE ", googleDirectionsJson.toString());
            try {
                duration = googleDirectionsJson.getJSONObject(0).getJSONObject("duration").getString("text");
                distance = googleDirectionsJson.getJSONObject(0).getJSONObject("distance").getString("text");
                googleDirectionsMap.put("duration", duration);
                googleDirectionsMap.put("distance", distance);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }else{
            googleDirectionsMap.put("duration"," No hay duracion disponible ");
            googleDirectionsMap.put("distance"," No hay ruta exacta disponible ");
        }
        return googleDirectionsMap;
    }
    private HashMap<String, String> getPlace(JSONObject googlePlaceJson)
    {
        HashMap<String, String> googlePlaceMap = new HashMap<>();
        String placeName = "--NA--";
        String vicinity= "--NA--";
        String latitude= "";
        String longitude="";
        String reference="";

        Log.d("DataParser","jsonobject ="+googlePlaceJson.toString());


        try {
            if (!googlePlaceJson.isNull("name")) {
                placeName = googlePlaceJson.getString("name");
            }
            if (!googlePlaceJson.isNull("vicinity")) {
                vicinity = googlePlaceJson.getString("vicinity");
            }

            latitude = googlePlaceJson.getJSONObject("geometry").getJSONObject("location").getString("lat");
            longitude = googlePlaceJson.getJSONObject("geometry").getJSONObject("location").getString("lng");

            reference = googlePlaceJson.getString("reference");

            googlePlaceMap.put("place_name", placeName);
            googlePlaceMap.put("vicinity", vicinity);
            googlePlaceMap.put("lat", latitude);
            googlePlaceMap.put("lng", longitude);
            googlePlaceMap.put("reference", reference);


        }
        catch (JSONException e) {
            e.printStackTrace();
        }
        return googlePlaceMap;

    }
    private List<HashMap<String, String>>getPlaces(JSONArray jsonArray)
    {
        int count = jsonArray.length();
        List<HashMap<String, String>> placelist = new ArrayList<>();
        HashMap<String, String> placeMap = null;

        for(int i = 0; i<count;i++)
        {
            try {
                placeMap = getPlace((JSONObject) jsonArray.get(i));
                placelist.add(placeMap);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return placelist;
    }
    public HashMap<String,String> parseDirections(String jsonData){
        JSONArray jsonArray = null;
        JSONObject jsonObject;

        try {
            jsonObject = new JSONObject(jsonData);
            jsonArray= jsonObject.getJSONArray("routes").getJSONObject(0).getJSONArray("legs");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return getDuration(jsonArray);

    }

    public String[] parseDirectionsPaint(String jsonData){
        JSONArray jsonArray = null;
        JSONObject jsonObject;

        try {
            jsonObject = new JSONObject(jsonData);
            jsonArray= jsonObject.getJSONArray("routes").getJSONObject(0).getJSONArray("legs").getJSONObject(0).getJSONArray("steps");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return getPaths(jsonArray);

    }

    public String[] getPaths(JSONArray googleStepsJson){
        String[] polylines = new String[0];
        if(googleStepsJson!=null){
            int count= googleStepsJson.length();
             polylines = new String[count];
            for(int i =0 ; i<count;i++){
                try {
                    polylines[i]=getPath(googleStepsJson.getJSONObject(i));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }else{
           return null;
        }

        return polylines;
    }

    public String getPath(JSONObject googlePathJson){

        String polyline ="";
        try {
             polyline = googlePathJson.getJSONObject("polyline").getString("points");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return polyline;
    }


    public List<HashMap<String, String>> parse(String jsonData)
    {
        JSONArray jsonArray = null;
        JSONObject jsonObject;

        Log.d("json data", jsonData);

        try {
            jsonObject = new JSONObject(jsonData);
            jsonArray = jsonObject.getJSONArray("results");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return getPlaces(jsonArray);
    }
}