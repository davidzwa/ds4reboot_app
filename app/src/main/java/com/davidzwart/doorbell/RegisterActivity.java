package com.davidzwart.doorbell;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.Context;
import android.support.design.widget.Snackbar;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

/**
 * A signup screen that offers login via GCM token.
 */
public class RegisterActivity extends AppCompatActivity {

    /**
     * Keep track of the login task to ensure we can cancel it if requested.
     */

    // UI references.
    private Spinner mNameView;
    private TextView pWordView;
    private View mProgressView;
    private View mLoginFormView;

    private String TAG = "REGISTER";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // my_child_toolbar is defined in the layout file
        Toolbar myChildToolbar =
                (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(myChildToolbar);

        // Get a support ActionBar corresponding to this toolbar
        ActionBar action_bar = getSupportActionBar();

        // Enable the Up button
        action_bar.setDisplayHomeAsUpEnabled(true);

        // Set up the login form.
        mNameView = (Spinner) findViewById(R.id.spinnerUsers);
        mNameView.getSelectedItem();
        pWordView = (TextView) findViewById(R.id.passWord);

        Button mNameSignInButton = (Button) findViewById(R.id.name_sign_in_button);
        mNameSignInButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                requestAPIaccess();
            }
        });

        mLoginFormView = findViewById(R.id.login_form);
        mProgressView = findViewById(R.id.login_progress);
    }

    @Override
    protected void onStart(){
        super.onStart();
        RetrieveUsers(findViewById(R.id.login_layout));
    }

    /** GCM beer-turf code **/
    public void RetrieveUsers(View v) {
        Context c = getApplicationContext();

        // TODO carry this across whole app (Singleton)
        VolleyBall request = new VolleyBall();

        // Execute HTTP Volley StringRequest
        request.VolleyIt(this, v, "users/");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.activity_login_toolbar_quickaccess, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.refreshUsers) {
            RetrieveUsers(findViewById(R.id.login_layout));
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * Attempts to request user access for this device by returning GCM token.
     * If no username is selected or no usernames were previously supplied,
     * gives error.
     */
    private void requestAPIaccess() {
        String AuthName;

        // Store values at the time of the signup attempt.
        try {
            AuthName = mNameView.getSelectedItem().toString();
        } catch (NullPointerException e) {
            Log.d(TAG, "Empty list! " + e.toString());
            Snackbar.make(findViewById(R.id.login_layout), "Please sync users and select your name.", Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show();
            return;
        } catch (Exception e){
            Log.d(TAG, "requestAPIaccess: " + e.toString());
            Snackbar.make(findViewById(R.id.login_layout), "Unkown username error. Can't login.", Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show();
            return;
        }

        String AuthWord;
        try {
            AuthWord = pWordView.getText().toString();
        } catch (Exception e){
            Log.d(TAG, "requestAPIaccess: " + e.toString());
            Snackbar.make(findViewById(R.id.login_layout), "Unknown password error. Can't login.", Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show();
            return;
        }

        boolean cancel = false;
        View focusView = null;

        // Check for a non-empty name
        if (TextUtils.isEmpty(AuthName)) {
            focusView = mNameView;
            cancel = true;
        } else if (!isNameValid(AuthName)) {
            focusView = mNameView;
            cancel = true;
        }

        if (TextUtils.isEmpty(AuthWord)) {
            focusView = pWordView;
            cancel = true;
        } else if (!isNameValid(AuthWord)) {
            focusView = pWordView;
            cancel = true;
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            Snackbar.make(findViewById(R.id.login_layout), "Couldn't sign in.", Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show();
            if (focusView == pWordView) {
                pWordView.setError("Empty field.");
            }
            focusView.requestFocus();
            return;
        } else {
            // Give Volley control over UI, assume activity is known
            AuthUser(findViewById(R.id.login_layout), AuthName, AuthWord);
        }
    }

    /** Authenticate user on server **/
    public void AuthUser(View v, String UserName, String PassWord) {

        // Make sure LowerCase characters are sent.
        UserName = UserName.toLowerCase();

        // Execute HTTP Volley StringRequest
        VolleyBall request = new VolleyBall();
        request.setAuthDetails(UserName, PassWord);
        request.VolleyIt(this, v, "auth/");
    }

    private boolean isNameValid(String name) {
        //Should be non-numeric and longer than 2
        return name.length() > 2; //name.isNumeric(); //name.contains("@");
    }

    /**
     * Shows the progress UI and hides the login form.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    public void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
            mLoginFormView.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
                }
            });

            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mProgressView.animate().setDuration(shortAnimTime).alpha(
                    show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            });
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }

}

