package com.davidzwart.doorbell;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import org.json.JSONObject;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Base64;
import android.util.Log;

/**
 * Created by david on 02-Oct-16.
 */
public class RemoteFetch extends AsyncTask<String, Void, String> {

    // supply http testdata
    private static final String OPEN_DS4_API =
            "http://10.0.4.131:8000/restapi/";
    //"http://www.ds4.nl/restapi/";
    private static final String OPEN_BIERLIJST_URL =
            "bierlijst_action/";
    private static final String OPEN_EETLIJST_URL =
            "eetlijst_action/";
    private static final Integer ACTION_USER = 14;
    private static final Integer ACTION_COUNT = 14;
    private static final String ACTION = "turf_bier";
    private static final String AUTH_STR = "david:Davido";

    @Override
    protected String doInBackground(String... urls) {
        try {
            URL url = new URL(String.format(OPEN_DS4_API, OPEN_BIERLIJST_URL));
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            String basicAuth = "Basic " + Base64.encodeToString(AUTH_STR.getBytes(),0);
            connection.setRequestMethod("POST");
            connection.setRequestProperty ("Authorization", basicAuth);
            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(connection.getInputStream()));

            StringBuffer json = new StringBuffer(1024);
            String tmp="";
            while((tmp=reader.readLine())!=null)
                json.append(tmp).append("\n");
            reader.close();

            JSONObject data = new JSONObject(json.toString());

            // This value will be 404 if the request was not
            // successful
            if(data.getInt("cod") != 200){
                return null;
            }

            return data.toString();
        }catch(Exception e){
            Log.v("API", e.toString());
            return null;
        }
    }

/*    public static JSONObject getJSON(Context context, String actionTopic){
        try {

            //connection.addRequestProperty("x-api-key", context.getString(R.string.ds4_app_id));
            //connection.addRequestProperty("action-user", ACTION_USER.toString());
            //connection.addRequestProperty("action-count", ACTION_COUNT.toString());
            //connection.addRequestProperty("action", ACTION);


    }*/
}
