package com.covision.covisionapp.structures;

import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.HttpHeaderParser;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ObjectDetectionRequest extends Request<List<ObjectDetectionResult>> {

    private Gson gson;
    private Map<String, String> params;
    private Response.Listener<List<ObjectDetectionResult>> listener;

    public ObjectDetectionRequest(String url, String mode, String image, Response.Listener<List<ObjectDetectionResult>> listener, Response.ErrorListener errorListener){
        super(Method.POST, url, errorListener);
        gson = new Gson();
        this.listener = listener;
        params = new HashMap();
        params.put("mode", mode);
        params.put("image", image);
    }

    @Override
    public Map<String, String> getParams() {
        return params;
    }

    @Override
    protected Response<List<ObjectDetectionResult>> parseNetworkResponse(NetworkResponse response) {
        try {
            String json = new String(response.data, HttpHeaderParser.parseCharset(response.headers));
            Type listType = new TypeToken<ArrayList<ObjectDetectionResult>>(){}.getType();
            List<ObjectDetectionResult> result = gson.fromJson(json, listType);
            return Response.success(result, HttpHeaderParser.parseCacheHeaders(response));
        }
        catch (UnsupportedEncodingException e) {
            return Response.error(new VolleyError(e.getMessage()));
        } catch (JsonSyntaxException e) {
            return Response.error(new VolleyError(e.getMessage()));
        }
    }

    @Override
    protected void deliverResponse(List<ObjectDetectionResult> response) {
        listener.onResponse(response);
    }
}
