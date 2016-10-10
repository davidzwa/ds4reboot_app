package com.davidzwart.doorbell;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;

import static com.davidzwart.doorbell.VolleyBall.AUTH_FLAG_KEY;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener{

    // Request GCM & log-tag
    private static final String TAG = "MainActivity";
    // GCM activity elements & setup variables
    private BroadcastReceiver mRegistrationBroadcastReceiver;
    private boolean isReceiverRegistered;
    private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;

    // Login status
    private static boolean MAIN_AUTH_FLAG;
    LinearLayout RegisterForm;
    MenuItem ToolbarConnected;

    // UDP client
    UDPClient UDPjetser = new UDPClient();

    // Other activity ID's
    private static final int REGISTER_ACTIVITY_ID = 2;
    private static final int OTHER_ID = 3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        RegisterForm = (LinearLayout) findViewById(R.id.RegisterContainer);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        //drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        //GCM setup & items
        mRegistrationBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
                boolean sentToken = sharedPreferences
                        .getBoolean(QuickstartPreferences.SENT_TOKEN_TO_SERVER, false);
                if (sentToken) {
                    Snackbar.make(findViewById(R.id.content_struct), "Connected to GCM.", Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();
                } else {
                    Snackbar.make(findViewById(R.id.content_struct), "Could not connect to GCM", Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();
                }
            }
        };
        registerReceiver(); // Register BroadcastReceiver

        if (checkPlayServices()) {
            // Start IntentService to register this application with webserver.
            Intent intent = new Intent(this, RegistrationIntentService.class);
            startService(intent);
        }
    };

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        Log.d(TAG, "onCreateOptionsMenu: " + "Im running");
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.activity_main_toolbar_quickaccess, menu);

        ToolbarConnected = menu.findItem(R.id.server_connected);

        // Solve problem of onCreatoptionsmenu being run after onresume & updateRegisterForm.
        if (! MAIN_AUTH_FLAG) {
            ToolbarConnected.setIcon(R.drawable.ic_cred_not);
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.toolbar_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_camera) {
            // Handle the camera action
        } else if (id == R.id.nav_gallery) {

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Check if user is signed in
        updateRegisterForm();

        registerReceiver();
    }

    @Override
    protected void onPause() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mRegistrationBroadcastReceiver);
        isReceiverRegistered = false;
        super.onPause();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch(requestCode) {
            case (REGISTER_ACTIVITY_ID) : {
                if (resultCode == Activity.RESULT_OK) {
                    String result = data.getStringExtra("Result");
                    MAIN_AUTH_FLAG = data.getBooleanExtra(AUTH_FLAG_KEY, false);
                    Snackbar.make(findViewById(R.id.content_struct), result, Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();
                    updateRegisterForm();
                }
                else {
                    Snackbar.make(findViewById(R.id.content_struct), "Unknown intent code returned.", Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();
                }
                break;
            }
        }
    }

    // Update signed in status
    private void updateRegisterStatus(){

        SharedPreferences prefs = this.getSharedPreferences(
                "com.davidzwart.doorbell", Context.MODE_PRIVATE);

        // Check if AUTH_FLAG is set in shared preferences
        MAIN_AUTH_FLAG = prefs.getBoolean(AUTH_FLAG_KEY, false);

        // TODO check if auth is still valid
    }

    // Update Register form
    private void updateRegisterForm(){

        // Get shared preference for sign-in status
        updateRegisterStatus();

        // Apply (in)visibility when signed in or not
        if (MAIN_AUTH_FLAG == true) {
            RegisterForm.setVisibility(View.GONE);

            // If calling function is onResume the menuitem is null
            try {
                ToolbarConnected.setVisible(true);
            } catch (Exception e) {
                Log.d(TAG, "updateRegisterForm: ToolbarConnected not initialized yet");
            }
        }
        else {
            RegisterForm.setVisibility(View.VISIBLE);
            try {
                ToolbarConnected.setVisible(false);
            } catch (Exception e) {
                Log.d(TAG, "updateRegisterForm: ToolbarConnected not initialized yet");
            }
        }
    }

    /** Open Register intent+activity **/
    public void registerUser(View v) {
        //Register intent
        Intent intentLogin = new Intent(MainActivity.this, RegisterActivity.class);
        startActivityForResult(intentLogin, REGISTER_ACTIVITY_ID);
    }

    /** UDP code **/
    public void fabUDPClick(View v) {
        Snackbar.make(v, "Doorbel-aanvraag verzonden.", Snackbar.LENGTH_LONG)
                .setAction("Action", null).show();
        try {
            UDPjetser.sendBroadcast("doorbellapp-broadcast");
        } catch (Exception e){
            Snackbar.make(v, "App kan geen lokaal netwerk vinden.", Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show();
        }
    }

    /** GCM beer-turf code **/
    public void fabTurfBeer(View v) {
        Context c = getApplicationContext();

        // Execute HTTP Volley StringRequest
        VolleyBall request = new VolleyBall();
        request.VolleyIt(this, v, "bierlijst_action/");
    }

    /** GCM code **/
    private void registerReceiver(){
        if(!isReceiverRegistered) {
            LocalBroadcastManager.getInstance(this).registerReceiver(mRegistrationBroadcastReceiver,
                    new IntentFilter(QuickstartPreferences.REGISTRATION_COMPLETE));
            isReceiverRegistered = true;
        }
    }

    /**
     * Check the device to make sure it has the Google Play Services APK. If
     * it doesn't, display a dialog that allows users to download the APK from
     * the Google Play Store or enable it in the device's system settings.
     */
    private boolean checkPlayServices() {
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        int resultCode = apiAvailability.isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (apiAvailability.isUserResolvableError(resultCode)) {
                apiAvailability.getErrorDialog(this, resultCode, PLAY_SERVICES_RESOLUTION_REQUEST)
                        .show();
            } else {
                Log.i(TAG, "This device is not supported.");
                finish();
            }
            return false;
        }
        return true;
    }

}
