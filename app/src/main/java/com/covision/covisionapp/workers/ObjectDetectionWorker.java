package com.covision.covisionapp.workers;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Base64;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.covision.covisionapp.fragments.ObjectDetectionFragment;
import com.covision.covisionapp.structures.BoundingBox;
import com.covision.covisionapp.structures.ObjectDetectionResult;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;

public class ObjectDetectionWorker extends Thread {
    private final static String SERVER_URL = "http://201.244.214.60:5000";

    private Context context;
    private Bitmap image;
    private ObjectDetectionFragment.ObjectDetectionCallback callback;


    public ObjectDetectionWorker(Context context, Bitmap imageToProcess, ObjectDetectionFragment.ObjectDetectionCallback callback)
    {
        super("ObjectDetection");
        this.context = context;
        this.image = imageToProcess;
        this.callback = callback;
    }

    public void run()
    {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        image.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);
        byte[] byteArray = byteArrayOutputStream .toByteArray();
        final int byteCount = byteArray.length;

        JSONObject requestBody = new JSONObject();
        try {
            requestBody.put("mode", "navigation");
            requestBody.put("image", Base64.encode(byteArray, Base64.DEFAULT));
        }
        catch (JSONException e)
        {
            this.callback.onError(e.getMessage());
        }
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, SERVER_URL, requestBody, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    ObjectDetectionResult result = new ObjectDetectionResult();
                    String text = response.getString("count_text");
                    result.addText(text);
                    JSONArray classes = response.getJSONArray("objects");
                    for (int i = 0; i<classes.length(); i++)
                    {
                        JSONObject currentClass = classes.getJSONObject(i);
                        String className = currentClass.getString("class");
                        JSONArray boxes = currentClass.getJSONArray("boxes");
                        for (int j = 0; j<boxes.length(); j++)
                        {
                            JSONObject box = boxes.getJSONObject(j);
                            double score = box.getDouble("score");
                            JSONArray points = box.getJSONArray("box");
                            double[] boxPoints = new double[points.length()];
                            for (int k = 0; k<points.length(); k++){
                                boxPoints[k] = points.getDouble(k);
                            }
                            result.addBox(new BoundingBox(className, score, boxPoints));
                        }
                    }
                    ObjectDetectionWorker.this.callback.onDetectionResult(result);
                }
                catch (JSONException e)
                {
                    ObjectDetectionWorker.this.callback.onError(e.getMessage());
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                ObjectDetectionWorker.this.callback.onError(error.getMessage());
            }
        });

        RestRequestQueue.getInstance(this.context).addToRequestQueue(request);
    }

}
