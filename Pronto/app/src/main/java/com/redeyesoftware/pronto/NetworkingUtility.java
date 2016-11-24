package com.redeyesoftware.pronto;

/**
 * Created by George on 20/11/2016.
 */

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.facebook.AccessToken;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import static android.content.Context.MODE_PRIVATE;
import static bolts.Task.delay;

public class NetworkingUtility {

    public static String url = "http://www.prontoai.com:5000";
    public static RequestQueue queue;
    public static String response;
    public static String comments[][];
    public static String token;

    public static void setUpRequestQueue(MainPage parentActivity) {
        queue = Volley.newRequestQueue(parentActivity);

        SharedPreferences prefs = parentActivity.getSharedPreferences("PrefsFile", MODE_PRIVATE);
        token = prefs.getString("accessToken", "ERROR: DID NOT READ");
        Log.d("retrieved access token",token);
    }

    private static void callMethodOnFinished(String key) {
        switch (key) {
            case "fillFeed":
                FeedFragment.addCommentsToFeed();
                return;
        }
    }

    public static void getComments(final String urlEnd, final int max_messages, final String group_id, final String methodKey, final String[] tags) {
        comments = new String[max_messages][tags.length];//if not rewriiten, will send back empty array
        //Todo: consider when get less than max_messages
        String newUrl = url + urlEnd + "?max_messages=" + max_messages + "&group_id=" + group_id + "&access_token="+token ;

        Log.d("Sending to this url", newUrl);
        // Request a string response from the provided URL.
        JsonArrayRequest req = new JsonArrayRequest(newUrl,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        Log.d("The JSON was:", response.toString());

                        try {
                            // Parsing json array response, loop through each json object
                            for (int i = 0; i < response.length(); i++) {
                                JSONObject person = (JSONObject) response.get(i);

                                JSONObject comment = response.getJSONObject(i);
                                for (int j=0; j<tags.length;j++) {
                                    comments[i][j] =  comment.getString(tags[j]);
                                }
                            }
                            callMethodOnFinished(methodKey);

                        } catch (Exception e) {
                            e.printStackTrace();
                            Log.d("Debug", "Error: " + e.getMessage());
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("Debug", "That didn't work!");
            }
        });

        // Add the request to the RequestQueue.
        queue.add(req);
    }

    public static String get(final String urlEnd, final String[] keys, final String[] values) {
        response= "ERROR";//if not rewriiten, will send back "ERROR"

        StringBuilder stringBuilder = new StringBuilder(url);
        stringBuilder.append(urlEnd);
        for (int i=0; i<keys.length;i++) {
            if(i == 0) {
                stringBuilder.append("?" + keys[i] + "=" + values[i]);
            } else {
                stringBuilder.append("&" + keys[i] + "=" + values[i]);
            }
        }
        String newUrl = stringBuilder.toString();
        Log.d("Sending to this url", newUrl);
        // Request a string response from the provided URL.
        JsonArrayRequest req = new JsonArrayRequest(newUrl,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        Log.d("The JSON was:", response.toString());

                        try {
                            // Parsing json array response
                            // loop through each json object
                            String jsonResponse = "";
                            for (int i = 0; i < response.length(); i++) {

                                JSONObject person = (JSONObject) response
                                        .get(i);

                                jsonResponse += "Message #" + i + "\n";
                                JSONObject message = response.getJSONObject(i);

                                String name = message.getString("author_id");
                                String score = message.getString("msg_id");
                                String body = message.getString("text");
                                String timestamp = message.getString("timestamp");

                                jsonResponse += "author_id: " + name + "\n";
                                jsonResponse += "msgID: " + score + "\n";
                                jsonResponse += "text: " + body + "\n";
                                jsonResponse += "timestamp: " + timestamp + "\n\n";



                            }
                            Log.d("Debug", "Parsed JSON is: " + jsonResponse);

                        } catch (JSONException e) {
                            e.printStackTrace();
                            Log.d("Debug", "Error: " + e.getMessage());
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("Debug", "That didn't work!");
            }
        });

        // Add the request to the RequestQueue.
        queue.add(req);
        return response;
    }


  /*  public static String get(final String urlEnd, final String[] keys, final String[] values) {
        response= "ERROR";//if not rewriiten, will send back "ERROR"
        // Request a string response from the provided URL.
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url+urlEnd,
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
                for (int i =0; i<keys.length;i++ ) {
                    params.put(keys[i], values[i]);
                }
                return params;
            }

            ;

        };
        // Add the request to the RequestQueue.
        queue.add(stringRequest);
        return response;
    }
*/
    public static void post(final String urlEnd, final String key, final String msg) {
        // Request a string response from the provided URL.
        String newUrl = url + urlEnd;

        StringRequest stringRequest = new StringRequest(Request.Method.POST, newUrl,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.d("Debug", "Response to post is: " + response);
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
