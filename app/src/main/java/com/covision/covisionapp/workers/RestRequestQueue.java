package com.covision.covisionapp.workers;

import android.content.Context;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;

public class RestRequestQueue {
    private static RestRequestQueue instance;

    private RequestQueue queue;
    private static Context ctx;

    private RestRequestQueue(Context context)
    {
        ctx = context;
        queue = getRequestQueue();
    }

    public RequestQueue getRequestQueue() {
        if (queue == null) {
            // getApplicationContext() is key, it keeps you from leaking the
            // Activity or BroadcastReceiver if someone passes one in.
            queue = Volley.newRequestQueue(ctx.getApplicationContext());
        }
        return queue;
    }

    public static synchronized RestRequestQueue getInstance(Context context) {
        if (instance == null) {
            instance = new RestRequestQueue(context);
        }
        return instance;
    }

    public <T> void addToRequestQueue(Request<T> req) {
        getRequestQueue().add(req);
    }
}
