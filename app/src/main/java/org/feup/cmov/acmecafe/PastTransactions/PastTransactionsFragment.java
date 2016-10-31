package org.feup.cmov.acmecafe.PastTransactions;

import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.text.Editable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;

import org.feup.cmov.acmecafe.R;
import org.feup.cmov.acmecafe.VolleySingleton;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


public class PastTransactionsFragment extends Fragment {

    private static final String AUTHENTICATE_TAG = "AUTHENTICATE";

    private OnPastTranscationInteractionListener mListener;

    public PastTransactionsFragment() {
    }

    public static PastTransactionsFragment newInstance() {
        return new PastTransactionsFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_past_transactions, container, false);
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onOrderInteraction(uri);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnPastTranscationInteractionListener) {
            mListener = (OnPastTranscationInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnPastTranscationInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
        //If the fragment is detached and there is a authenticate request in queue, cancel it
        if (VolleySingleton.getInstance(this.getActivity().getApplicationContext()).getRequestQueue() != null) {
            VolleySingleton.getInstance(this.getActivity().getApplicationContext()).getRequestQueue().cancelAll(AUTHENTICATE_TAG);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        askUserForPassword();
    }

    private void askUserForPassword() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        LayoutInflater inflater = this.getActivity().getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_ask_password, null);

        final EditText passwordEditText = (EditText) dialogView.findViewById(R.id.ask_password_input);

        builder.setTitle(R.string.dialog_ask_password_title)
                .setView(dialogView)
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        attemptAuthenticate(dialog, passwordEditText.getText().toString());
                    }
                }).setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        getActivity().getSupportFragmentManager().popBackStackImmediate();
                    }
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void attemptAuthenticate(DialogInterface dialog, String password) {
        //If the user does not have an Internet connection, do not try to get the Voucher list
        ConnectivityManager connectivityManager = (ConnectivityManager) this.getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        if(activeNetworkInfo == null || !activeNetworkInfo.isConnected()) {
            Snackbar.make(getView(), "Check your Internet connection", Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show();
            dialog.dismiss();
            getActivity().getSupportFragmentManager().popBackStackImmediate();
            return;
        }

        authenticate(dialog, password);
    }

    private void authenticate(final DialogInterface dialog, String password) {
        String url = "https://acme-cafe.herokuapp.com/authenticate";

        JSONObject body = new JSONObject();
        try {
            body.put("uuid", getUserUUID());
            body.put("password", password);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        JsonObjectRequest jsObjRequest = new JsonObjectRequest
                (Request.Method.POST, url, body, new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            if(response.getBoolean("success")) {
                                dialog.dismiss();
                            }
                            else {
                                //TODO: notify user wrong password
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e("PastTransactions", "Error authenticating");
                        getActivity().getSupportFragmentManager().popBackStackImmediate();
                    }
                });

        jsObjRequest.setTag(AUTHENTICATE_TAG);
        VolleySingleton.getInstance(this.getActivity().getApplicationContext()).addToRequestQueue(jsObjRequest);
    }

    private String getUserUUID() {
        SharedPreferences sharedPref = getActivity().getSharedPreferences(getString(R.string.user_details_prefs),Context.MODE_PRIVATE);
        String defaultValue = "Could not get UUID from shared preferences";
        return sharedPref.getString("uuid", defaultValue);
    }

    public interface OnPastTranscationInteractionListener {
        // TODO: Update argument type and name
        void onOrderInteraction(Uri uri);
    }
}
