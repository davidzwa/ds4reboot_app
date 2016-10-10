package com.davidzwart.doorbell;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.design.widget.Snackbar;
import android.text.TextUtils;
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

import static android.app.Activity.RESULT_OK;

public class VolleyBall {

    private static final String TAG = "VolleyBall";
    // supply http testdata
    private static final String OPEN_DS4_API =
            "http://10.0.4.156:8000/restapi/";
    private static final int MY_SOCKET_TIMEOUT_MS = 250;

    private static final String OPEN_BIERLIJST_URL =
            "bierlijst_action/";
    private static final String OPEN_EETLIJST_URL =
            "eetlijst_action/";
    private static final String OPEN_USERS_URL =
            "users/";
    private static final String OPEN_AUTH_URL =
            "auth/";

    /* TEST DATA ONLY - TODO remove later*/
    private static final Integer ACTION_USER = 14;
    private static final Integer ACTION_COUNT = 1;
    private static final String ACTION = "turf_bier";

    // Auth data
    public static String AUTH_FLAG_KEY = "AUTH_DONE";
    private static String AUTH_STR_KEY = "SHA_PRIVATE";

    private static boolean AUTH_FLAG;
    private static String AUTH_STR;
    private static String AUTH_STR_BASE64;
    //private static String AUTH_STR_HASH;

    private static String RecResult = "";
    private static String RecStatus = "";
    private static List<String> list;

    private View v_current;
    private Context c_current;

    private static int VolleyMethod;
    private static String LastURLRequest;

    public void setAuthDetails (String UserName, String PassWord){
        AUTH_STR = UserName + ':' + PassWord;
    }

    public void VolleyIt(Context c, View v, String UrlEnd) {

        // Update view for async tasks in volley
        v_current = v;
        c_current = c;

        // Reset volley method type
        VolleyMethod = 0;

        // Differentiate between GET and POST types
        if ( UrlEnd.equals(OPEN_BIERLIJST_URL) ||
             UrlEnd.equals(OPEN_EETLIJST_URL) ||
             UrlEnd.equals(OPEN_AUTH_URL) ) {

            // Only register Activity has
            if ( UrlEnd.equals(OPEN_AUTH_URL) ) {
                if ( c_current instanceof RegisterActivity ) {
                    RegisterActivity regAct = (RegisterActivity) c_current;
                    regAct.showProgress(true);
                }
                else {
                    Snackbar.make(v_current, "Error: signing in may only happen in RegisterActivity.", Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();
                    return;
                }
            }

            // Make sure authentication is supplied
            if (TextUtils.isEmpty(AUTH_STR)) {
                // Differentiate between register activity and others
                if ( c_current instanceof RegisterActivity ) {
                    Snackbar.make(v_current, "Error: password field wasn't processed.", Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();
                    RegisterActivity regAct = (RegisterActivity) c_current;
                    regAct.showProgress(false);
                    return;
                }
                else {
                    Snackbar.make(v_current, "Error: not signed in to DS4.", Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();
                    return;
                }
            }

            AUTH_STR_BASE64 = "Basic " + Base64.encodeToString(AUTH_STR.getBytes(), 0);
            VolleyMethod = Request.Method.POST;

        }
        else if (UrlEnd.equals(OPEN_USERS_URL)){
            VolleyMethod = Request.Method.GET;
        }
        else {
            Snackbar.make(v_current, "Unknown call URL. Inform programmer.", Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show();
            return;
        }

        // Set last URL to the given URL for ASYNC response run by Volley
        LastURLRequest = UrlEnd;

        // Instantiate the RequestQueue
        RequestQueue queue = Volley.newRequestQueue(c);

        // Generate url and auth (as json)
        String url = OPEN_DS4_API + UrlEnd + "?format=json";

        // Request a string response from the provided URL.
        StringRequest stringReq = new StringRequest(
                VolleyMethod, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {

                try {
                    // Create JSON object
                    JSONObject jsonObject = new JSONObject(response.toString());
                    // Create JSON array from object
                    RecResult = jsonObject.getString("result");
                    RecStatus = jsonObject.getString("status");
                    Respond(RecResult, RecStatus);
                } catch (JSONException e) {
                    if (LastURLRequest.equals(OPEN_USERS_URL)) {
                        // Try and solve exception by reading list instead.
                        try {
                            JSONArray arr = new JSONArray(response.toString());
                            list = new ArrayList<String>();
                            for (int i = 0; i < arr.length(); i++) {
                                list.add(arr.getJSONObject(i).getString("display_name"));
                            }
                        } catch (JSONException e2) {
                            RecResult = "Server gaf onbekende data: " + e2.toString();
                            RecStatus = "Failure";
                            Respond(RecResult, RecStatus);
                        }
                        RecResult = "Gebruikers zijn ontvangen.";
                        RecStatus = "Success";
                        Respond(RecResult, RecStatus);
                    }
                    else {
                        RecResult = "Server data kon niet verwerkt worden.";
                        RecStatus = "Failure";
                        Respond(RecResult, RecStatus);
                    }
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if (error instanceof AuthFailureError) {
                    RecResult = "Wrong credentials supplied.";
                    RecStatus = "Failure";
                }
                else {
                    RecResult = "Onbekende fout: " + error.toString();
                    RecStatus = "Failure";
                }
                Respond(RecResult, RecStatus);
            }
        }) {
            // Set parameters
            @Override
            protected Map<String, String> getParams () {
                Map<String, String> params = new HashMap<String, String>();
                if (LastURLRequest.equals(OPEN_BIERLIJST_URL) ||
                        LastURLRequest.equals(OPEN_EETLIJST_URL) ) {
                    params.put("action_user", ACTION_USER.toString());
                    params.put("action_count", ACTION_COUNT.toString());
                    params.put("action", ACTION);
                    return params;
                }
                else {
                    return params;
                }
            }

            // Set headers
            @Override
            public Map<String, String> getHeaders ()throws AuthFailureError {
                HashMap<String, String> headermap = new HashMap<String, String>();
                headermap.put("Accept", "application/json");
                if (VolleyMethod == Request.Method.POST) {
                    headermap.put("Authorization", AUTH_STR_BASE64);
                }
                headermap.put("Cache-Control", "no-cache");
                return headermap;
            }
        };

        // Setup Timeout in constructor
        stringReq.setRetryPolicy(new DefaultRetryPolicy(
                MY_SOCKET_TIMEOUT_MS,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        // Add the request to the RequestQueue.
        queue.add(stringReq);
    }

    private void Respond(String result, String status) {

        if (LastURLRequest.equals(OPEN_USERS_URL)) {
            if (status.equals("Success")) {
                Spinner userSpinner = (Spinner) ((Activity) c_current).findViewById(R.id.spinnerUsers);
                try {
                    v_current.getId();
                } catch (NullPointerException e) {
                    Log.d(TAG, "Error: " + e.toString());
                }

                try {
                    list.size();
                } catch (NullPointerException e) {
                    result = "Geen namen ontvangen van server. Controleer of je internetverbinding.";
                }

                try {
                    String[] listUsers = list.toArray(new String[list.size()]);
                    Context cont = v_current.getContext();
                    ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<String>(
                            cont, android.R.layout.simple_spinner_item, listUsers);
                    spinnerArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    userSpinner.setAdapter(spinnerArrayAdapter);
                } catch (Exception e) {
                    Log.d(TAG, "Error: " + e.toString());
                }
                Snackbar.make(v_current, result, Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            } else {
                Snackbar.make(v_current, result, Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        }
        else if (LastURLRequest.equals("auth/")) {
            if (RecStatus == "Failure") {
                RegisterActivity regAct = (RegisterActivity) c_current;
                regAct.showProgress(false);
                Snackbar.make(v_current, result, Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
            else {
                RegisterActivity regAct = (RegisterActivity) c_current;
                SharedPreferences prefs = regAct.getSharedPreferences(
                        "com.davidzwart.doorbell", Context.MODE_PRIVATE);

                if ( prefs.edit().putString(AUTH_STR_KEY, AUTH_STR).commit() ) {
                    AUTH_FLAG = true;
                    prefs.edit().putBoolean(AUTH_FLAG_KEY, AUTH_FLAG).commit();
                }
                else {
                    regAct.showProgress(false);
                    result = "Couldn't save credentials.";
                    Snackbar.make(v_current, result, Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();
                }

                Intent intentRegister = new Intent();
                intentRegister.putExtra(AUTH_FLAG_KEY, AUTH_FLAG);
                intentRegister.putExtra("Result", result);

                regAct.setResult(Activity.RESULT_OK, intentRegister);
                regAct.finish();
            }
        }
        else {
            Snackbar.make(v_current, result, Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show();
        }
    }
}
