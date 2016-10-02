package com.github.qezt.phonenumberidentifier;


import android.content.Context;

import com.android.volley.Cache;
import com.android.volley.Network;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.BasicNetwork;
import com.android.volley.toolbox.DiskBasedCache;
import com.android.volley.toolbox.HurlStack;

import java.io.File;

public class RequestManager {
    private static RequestManager requestManager;
    public static final RequestManager instance() {
        if (requestManager != null) return requestManager;
        synchronized (RequestManager.class) {
            if (requestManager != null) return requestManager;
            requestManager = new RequestManager(Application.instance());
        }
        return requestManager;
    }

    private Context context;
    private RequestQueue requestQueue;

    private RequestManager(Context context) {
        this.context = context.getApplicationContext();
        Cache cache = new DiskBasedCache(
                new File(context.getCacheDir(), "volley"),
                30 * 1024 * 1024); // 30MB tops
        // Set up the network to use HttpURLConnection as the HTTP client.
        Network network = new BasicNetwork(new HurlStack());
        // Instantiate the RequestQueue with the cache and network.
        requestQueue = new RequestQueue(cache, network);

        // Start the queue
        requestQueue.start();
    }

    public void addRequest(Request request) {
        requestQueue.add(request);
    }
}
