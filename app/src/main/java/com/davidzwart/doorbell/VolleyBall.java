package com.davidzwart.doorbell;

import android.app.Activity;
import android.content.Context;
import android.support.design.widget.Snackbar;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import com.android.volley.*;
import com.android.volley.toolbox.*;
import org.json.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class VolleyBall {

    private static final String TAG = "VolleyBall";
    // supply http testdata
    private static final String OPEN_DS4_API =
            "http://10.0.4.131:8000/restapi/";
    private static final String OPEN_BIERLIJST_URL =
            "bierlijst_action/";
    private static final String OPEN_EETLIJST_URL =
            "eetlijst_action/";
    private static final String OPEN_USERS_URL =
            "users/";
    private static final Integer ACTION_USER = 14;
    private static final Integer ACTION_COUNT = 1;
    private static final String ACTION = "turf_bier";
    private static final String AUTH_STR = "David:Davido12";

    private static String RecResult = "";
    private static String RecStatus = "";
    private static List<String> list;

    private View v_current;
    private Context c_current;
    private static int VolleyMethod = 0;

    public void VolleyIt(Context c, View v, String UrlEnd) {
        VolleyMethod = 0;
        if (UrlEnd.equals(OPEN_BIERLIJST_URL)) {
            VolleyMethod = Request.Method.POST;
        }
        else if (UrlEnd.equals(OPEN_EETLIJST_URL)){
            VolleyMethod = Request.Method.POST;
        }
        else if (UrlEnd.equals(OPEN_USERS_URL)){
            VolleyMethod = Request.Method.GET;
        }
        else {
            return;
        }

        // Update view for async tasks in volley
        v_current = v;
        c_current = c;

        // Instantiate the RequestQueue
        RequestQueue queue = Volley.newRequestQueue(c);

        // Generate url and auth
        String url = OPEN_DS4_API + UrlEnd + "?format=json";

        // Request a string response from the provided URL.
        StringRequest stringReq = new StringRequest(
                VolleyMethod, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {

                try {
                    // Create JSON object
                    JSONObject jsonObject= new JSONObject(response.toString());
                    // Create JSON array from object
                    RecResult = jsonObject.getString("result");
                    RecStatus = jsonObject.getString("status");
                } catch (JSONException e) {
                    RecResult = "Gebruikers zijn ontvangen.";
                    RecStatus = "success";
                    try {
                        JSONArray arr = new JSONArray(response.toString());
                        list = new ArrayList<String>();
                        for(int i = 0; i < arr.length(); i++){
                            list.add(arr.getJSONObject(i).getString("display_name"));
                        }
                    } catch (JSONException e2) {
                        RecResult = "Server gaf onbekende data: " + e2.toString();
                    }
                }
                Respond(RecResult);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                RecResult = "Kon niet met server verbinden: " + error.toString();
                Respond(RecResult);
            }
        }) {
            // Set parameters
            @Override
            protected Map<String, String> getParams () {
                Map<String, String> params = new HashMap<String, String>();
                if (VolleyMethod == Request.Method.POST) {
                    params.put("action_user", ACTION_USER.toString());
                    params.put("action_count", ACTION_COUNT.toString());
                    params.put("action", ACTION);
                    return params;
                }
                else {
                    return params;
                }
            }
            String basicAuth = "Basic " + Base64.encodeToString(AUTH_STR.getBytes(), 0);
            // Set headers
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> headermap = new HashMap<String, String>();
                headermap.put("Accept", "application/json");
                if (VolleyMethod == Request.Method.POST) {
                    headermap.put("Authorization", basicAuth);
                }
                headermap.put("Cache-Control", "no-cache");
                return headermap;
            }
        };
        // Add the request to the RequestQueue.
        queue.add(stringReq);
    }

    private void Respond(String result) {

        Spinner userSpinner = (Spinner) ((Activity) c_current).findViewById(R.id.spinnerUsers);
        try {
            Log.d(TAG, "Respond: "+ v_current.getId());
        } catch (NullPointerException e){
            Log.d(TAG, "Respond: "+e.toString());
        }

        try {
            list.size();
        } catch (NullPointerException e){
            result = "Een lege lijst is ontvangen. Informeer de programmeur.";
        }

        try {
            String[] listUsers = list.toArray(new String[list.size()]);
            Context cont = v_current.getContext();
            ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<String>(
                    cont, android.R.layout.simple_spinner_item, listUsers);
            spinnerArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

            userSpinner.setAdapter(spinnerArrayAdapter);
        } catch (Exception e) {
            Log.d(TAG, "Respond: " + e.toString());
        }

        Snackbar.make(v_current, result, Snackbar.LENGTH_LONG)
                .setAction("Action", null).show();

    }
}
