package org.feup.cmov.acmecafe;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
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
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import static android.Manifest.permission.READ_CONTACTS;

/**
 * A login screen that offers login via email/password.
 */
public class RegisterActivity extends AppCompatActivity {

    /**
     * Keep track of the register request to ensure we can cancel it if requested.
     */
    public static final String REGISTER_TAG = "RegisterTag";

    // UI references.
    private EditText mNameView;
    private EditText mUsernameView;
    private EditText mCreditcardView;
    private EditText mPasswordView;
    private View mProgressView;
    private View mRegisterFormView;

    private String mUUID;
    private int mPin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //If the user has already registered, skip the registration
        SharedPreferences sharedPreferences = this.getPreferences(Context.MODE_PRIVATE);

        if (sharedPreferences.contains("uuid") && sharedPreferences.contains("pin")) {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    goToMainActivity();
                }
            }, 10);
        }



        setContentView(R.layout.activity_register);
        // Set up the register form.
        mNameView = (EditText) findViewById(R.id.name);
        mUsernameView = (EditText) findViewById(R.id.username);
        mCreditcardView = (EditText) findViewById(R.id.creditcard);

        mPasswordView = (EditText) findViewById(R.id.password);
        mPasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == R.id.register || id == EditorInfo.IME_NULL) {
                    attemptRegister();
                    return true;
                }
                return false;
            }
        });

        Button mRegisterButton = (Button) findViewById(R.id.register_button);
        mRegisterButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                //close the keyboard
                if (view != null) {
                    InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                }
                //perform the registration
                attemptRegister();
            }
        });

        mRegisterFormView = findViewById(R.id.register_form);
        mProgressView = findViewById(R.id.register_progress);
    }

    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    private void attemptRegister() {
        //if (mAuthTask != null) {
        //    return;
        //}

        //If the user does not have an Internet connection, do not try to register
        ConnectivityManager connectivityManager = (ConnectivityManager) this.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        if(activeNetworkInfo == null || !activeNetworkInfo.isConnected()) {
            Snackbar.make(getCurrentFocus(), "Check your Internet connection", Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show();
            return;
        }

        // Reset errors.
        mNameView.setError(null);
        mUsernameView.setError(null);
        mCreditcardView.setError(null);
        mPasswordView.setError(null);

        // Store values at the time of the login attempt.
        String name = mNameView.getText().toString();
        String username = mUsernameView.getText().toString();
        String creditcard = mCreditcardView.getText().toString();
        String password = mPasswordView.getText().toString();

        boolean cancel = false;
        View focusView = null;

        // Check for a valid password, if the user entered one.
        if (!TextUtils.isEmpty(password) && !isPasswordValid(password)) {
            mPasswordView.setError(getString(R.string.error_invalid_password));
            focusView = mPasswordView;
            cancel = true;
        }

        // Check for a valid name.
        if (TextUtils.isEmpty(name)) {
            mNameView.setError(getString(R.string.error_field_required));
            focusView = mNameView;
            cancel = true;
        } else if (!isNameValid(name)) {
            mNameView.setError(getString(R.string.error_field_length));
            focusView = mNameView;
            cancel = true;
        }

        // Check for a valid username.
        if (TextUtils.isEmpty(username)) {
            mUsernameView.setError(getString(R.string.error_field_required));
            if(focusView == mPasswordView || focusView == null)
                focusView = mNameView;
            cancel = true;
        } else if (!isNameValid(username)) {
            mUsernameView.setError(getString(R.string.error_field_length));
            if(focusView == mPasswordView || focusView == null)
                focusView = mUsernameView;
            cancel = true;
        }

        // Check for a valid credit card.
        if (TextUtils.isEmpty(creditcard)) {
            mCreditcardView.setError(getString(R.string.error_field_required));
            if(focusView == mPasswordView || focusView == null)
                focusView = mCreditcardView;

            cancel = true;
        } else if (!isNameValid(creditcard)) {
            mCreditcardView.setError(getString(R.string.error_field_length));
            if(focusView == mPasswordView || focusView == null)
                focusView = mCreditcardView;
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
            register(name,username,creditcard,password);
        }
    }

    private void register(String name, String username, String creditcard, String password) {
        String url = "http://10.0.2.2:8080/register";
        JSONObject body = new JSONObject();
        try {
            body.put("name", name);
            body.put("username", username);
            body.put("creditcard", creditcard);
            body.put("password", password);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        JsonObjectRequest jsObjRequest = new JsonObjectRequest
                (Request.Method.POST, url, body, new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            mUUID = response.getString("uuid");
                            mPin = response.getInt("pin");
                            saveUserData();
                            showPINDialog();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    }
                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // TODO Auto-generated method stub
                        Log.d("Register", error.getMessage());
                    }
                });
        jsObjRequest.setTag(REGISTER_TAG);

        VolleySingleton.getInstance(this.getApplicationContext()).addToRequestQueue(jsObjRequest);
    }

    private void saveUserData() {
        SharedPreferences sharedPreferences = this.getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("uuid", mUUID);
        editor.putInt("pin", mPin);
        editor.apply();
    }

    private void showPINDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        LayoutInflater inflater = this.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_pin, null);

        TextView pinTextView = (TextView) dialogView.findViewById(R.id.pin_textview);
        pinTextView.setText(String.valueOf(mPin));

        builder.setMessage(R.string.dialog_PIN_message)
                .setTitle(R.string.dialog_PIN_title)
                .setView(dialogView)
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        goToMainActivity();
                    }
                });

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private boolean isNameValid(String name) {
        return name.length() >= 4;
    }

    private boolean isPasswordValid(String password) {
        return password.length() > 4;
    }

    /**
     * Shows the progress UI and hides the register form.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            mRegisterFormView.setVisibility(show ? View.GONE : View.VISIBLE);
            mRegisterFormView.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mRegisterFormView.setVisibility(show ? View.GONE : View.VISIBLE);
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
            mRegisterFormView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }

    @Override
    protected void onStop () {
        super.onStop();
        if (VolleySingleton.getInstance(this.getApplicationContext()).getRequestQueue() != null) {
            VolleySingleton.getInstance(this.getApplicationContext()).getRequestQueue().cancelAll(REGISTER_TAG);
        }
    }

    private void goToMainActivity() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }

}

