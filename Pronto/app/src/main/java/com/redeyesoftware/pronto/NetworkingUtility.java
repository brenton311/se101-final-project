package com.redeyesoftware.pronto;

/**
 * Created by George on 20/11/2016.
 */

import android.content.Context;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import java.util.HashMap;
import java.util.Map;

public class NetworkingUtility {

    public static String url = "http://www.google.com";
    public static RequestQueue queue;
    public static String response;
    public static void setUpRequestQueue(Context parentActivity) {
        queue = Volley.newRequestQueue(parentActivity);
    }


    public static String get(final String key, final String request) {
        response= "ERROR";//if not rewriiten, will send back "ERROR"
        // Request a string response from the provided URL.
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        // Display the first 500 characters of the response string.
                        NetworkingUtility.response = response.substring(0, 500);
                        Log.d("Debug", "Response is: " + response.substring(0, 500));
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("Debug", "That didn't work!");
            }
        }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<String, String>();
                params.put(key, request);
                return params;
            }

            ;

        };
        // Add the request to the RequestQueue.
        queue.add(stringRequest);
        return response;
    }

    public static void send(final String key, final String msg) {
        // Request a string response from the provided URL.
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        // Display the first 500 characters of the response string.
                        Log.d("Debug", "Response is: " + response.substring(0, 500));
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("Debug", "That didn't work!");
            }
        }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<String, String>();
                params.put(key, msg);
                return params;
            }

            ;

        };
        // Add the request to the RequestQueue.
        queue.add(stringRequest);
    }
}
