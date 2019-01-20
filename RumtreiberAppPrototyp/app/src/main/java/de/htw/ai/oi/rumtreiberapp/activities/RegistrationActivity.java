package de.htw.ai.oi.rumtreiberapp.activities;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.app.LoaderManager.LoaderCallbacks;

import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;

import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import de.htw.ai.oi.rumtreiberapp.R;
import de.htw.ai.oi.rumtreiberapp.network.NetworkManager;

import static android.Manifest.permission.READ_CONTACTS;

/**
 * A login screen that offers login via email/password.
 */
public class RegistrationActivity extends AppCompatActivity {

    private static final String TAG =
            RegistrationActivity.class.getSimpleName();
    public static long REGISTRATION_ID =  -1;
    public static String USER_NAME =  "Ich";

    /**
     * Keep track of the login task to ensure we can cancel it if requested.
     */
    private UserLoginTask mAuthTask = null;

    // UI references.
    private EditText mFornameView;
    private EditText mSurnameView;
    private View mProgressView;
    private View mLoginFormView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);
        REGISTRATION_ID = -1;

        // Set up the login form.
        mFornameView = (EditText) findViewById(R.id.forname);
        mSurnameView = (EditText) findViewById(R.id.surname);
        mSurnameView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == EditorInfo.IME_ACTION_DONE || id == EditorInfo.IME_NULL) {
                    attemptLogin();
                    return true;
                }
                return false;
            }
        });

        Button mEmailSignInButton = (Button) findViewById(R.id.email_sign_in_button);
        mEmailSignInButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptLogin();
            }
        });

        mLoginFormView = findViewById(R.id.login_form);
        mProgressView = findViewById(R.id.login_progress);
    }


    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    private void attemptLogin() {
        if (mAuthTask != null) {
            return;
        }

        // Reset errors.
        mFornameView.setError(null);
        mSurnameView.setError(null);

        // Store values at the time of the login attempt.
        String forname = mFornameView.getText().toString();
        String surname = mSurnameView.getText().toString();

        boolean cancel = false;
        View focusView = null;

        // Check for a valid forname address.
        if (TextUtils.isEmpty(forname)) {
            mFornameView.setError(getString(R.string.error_field_required));
            focusView = mFornameView;
            cancel = true;
        }
        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.
            showProgress(true);
            mAuthTask = new UserLoginTask(forname, surname);
            mAuthTask.execute((Void) null);
        }
    }

    /**
     * Shows the progress UI and hides the login form.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showProgress(final boolean show) {
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

    /**
     * Represents an asynchronous login/registration task used to authenticate
     * the user.
     */
    public class UserLoginTask extends AsyncTask<Void, Void, Boolean> {

        private final String forename;
        private final String surname;
        public boolean succes = false;

        UserLoginTask(String forename, String surname) {
            this.forename = forename;
            this.surname = surname;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            return NetworkManager.registrate(forename + " "+surname);
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            mAuthTask = null;
            showProgress(true);

            if (success) {
                Log.d(TAG, "UserLoginTask->onPostExecute(): AsyncTask erfolgreich, RegistrationID ist nun:   " + RegistrationActivity.REGISTRATION_ID);
                this.succes = true;
                finish();
                SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(RegistrationActivity.this);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                Log.d(TAG, "UserLoginTask->onPostExecute(): gibt es userName in den sharedPreferences?:   " + sharedPreferences.contains("userName"));
                editor.putString("registrationId",String.valueOf(RegistrationActivity.REGISTRATION_ID));
                editor.putString("userName",RegistrationActivity.USER_NAME);
                editor.apply();
                String s = sharedPreferences.getString("userName","nicht gefunden");
                Log.d(TAG, "UserLoginTask->onPostExecute(): wurde userName gespeichert?:   " + s);

                Intent intent = new Intent(RegistrationActivity.this, MapsActivity.class);
                startActivity(intent);
            } else {
                mSurnameView.setError(getString(R.string.connection_failed));
                mSurnameView.requestFocus();
            }
        }

        @Override
        protected void onCancelled() {
            mAuthTask = null;
            showProgress(false);
        }
    }
}