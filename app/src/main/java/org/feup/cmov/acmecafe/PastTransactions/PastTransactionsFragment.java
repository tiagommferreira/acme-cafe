package org.feup.cmov.acmecafe.PastTransactions;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;

import org.feup.cmov.acmecafe.MainActivity;
import org.feup.cmov.acmecafe.Models.Order;
import org.feup.cmov.acmecafe.Models.Product;
import org.feup.cmov.acmecafe.Models.Voucher;
import org.feup.cmov.acmecafe.R;
import org.feup.cmov.acmecafe.Utils;
import org.feup.cmov.acmecafe.VolleySingleton;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;


public class PastTransactionsFragment extends Fragment {

    private static final String AUTHENTICATE_TAG = "AUTHENTICATE";
    private static final String TRANSACTIONS_TAG = "TRANSACTIONS";

    private ArrayList<Order> mPastTransactions = new ArrayList<>();

    private OnPastTransactionInteractionListener mListener;

    private View mAuthenticateForm;
    private EditText mAskPasswordField;
    private Button mAuthenticateButton;
    private View mProgressView;
    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mPastTransactionsAdapter;

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
        View view = inflater.inflate(R.layout.fragment_past_transactions, container, false);

        mAuthenticateForm = view.findViewById(R.id.authenticate_form);
        mAskPasswordField = (EditText) mAuthenticateForm.findViewById(R.id.ask_password_input);
        mAuthenticateButton = (Button) mAuthenticateForm.findViewById(R.id.authenticate_button);
        mAuthenticateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showProgress(true, false);
                attemptAuthenticate(mAskPasswordField.getText().toString());
            }
        });
        mProgressView = view.findViewById(R.id.authentication_progress);
        mRecyclerView = (RecyclerView) view.findViewById(R.id.past_transactions_list);
        mPastTransactionsAdapter = new PastTransactionsAdapter(mPastTransactions, mListener);
        mRecyclerView.setAdapter(mPastTransactionsAdapter);

        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnPastTransactionInteractionListener) {
            mListener = (OnPastTransactionInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnPastTransactionInteractionListener");
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
        MainActivity activity = (MainActivity) getActivity();
        activity.setToolbarTitle("Past Transactions");
    }

    private void attemptAuthenticate(String password) {

        if(!Utils.hasInternetConnection(getView(), (ConnectivityManager) this.getActivity().getSystemService(Context.CONNECTIVITY_SERVICE)))
            return;

        authenticate(password);
    }

    private void authenticate(String password) {
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
                                attemptGetTransactions();
                            }
                            else {
                                showProgress(false, false);
                                mAskPasswordField.setError("Wrong Password");
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

    private void attemptGetTransactions() {

        if(!Utils.hasInternetConnection(getView(), (ConnectivityManager) this.getActivity().getSystemService(Context.CONNECTIVITY_SERVICE)))
            return;

        getTransactions();
    }

    private void getTransactions() {
        String url = "https://acme-cafe.herokuapp.com/order/" + getUserUUID();
        //String url = "https://acme-cafe.herokuapp.com/order/user-uuid-melacionismo-1234";

        JsonArrayRequest jsObjRequest = new JsonArrayRequest
                (Request.Method.GET, url, null, new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        try {
                            for(int i = 0; i < response.length(); i++) {
                                JSONObject current = response.getJSONObject(i);
                                HashMap<Product, Integer> products = new HashMap<>();
                                ArrayList<Voucher> vouchers = new ArrayList<>();

                                JSONArray productsJSON = current.getJSONArray("products");
                                for(int j = 0; j < productsJSON.length(); j++) {
                                    JSONObject currentProduct = productsJSON.getJSONObject(j);
                                    products.put(new Product(currentProduct.getInt("id"), currentProduct.getString("name"), (float) currentProduct.getDouble("price")), currentProduct.getInt("quantity"));
                                }

                                JSONArray vouchersJSON = current.getJSONArray("vouchers");
                                for(int k = 0; k < vouchersJSON.length(); k++) {
                                    JSONObject currentVoucher = vouchersJSON.getJSONObject(k);
                                    vouchers.add(new Voucher(currentVoucher.getInt("id"), currentVoucher.getInt("type"), currentVoucher.getString("name"),null));
                                }

                                Order order = new Order(current.getInt("id"), products, vouchers);
                                mPastTransactions.add(order);
                            }
                            mPastTransactionsAdapter.notifyDataSetChanged();
                            showProgress(false, true);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e("PastTransactions", "Error getting transactions");
                        getActivity().getSupportFragmentManager().popBackStackImmediate();
                    }
                });

        jsObjRequest.setTag(TRANSACTIONS_TAG);
        VolleySingleton.getInstance(this.getActivity().getApplicationContext()).addToRequestQueue(jsObjRequest);
    }

    private String getUserUUID() {
        SharedPreferences sharedPref = getActivity().getSharedPreferences(getString(R.string.user_details_prefs),Context.MODE_PRIVATE);
        String defaultValue = "Could not get UUID from shared preferences";
        return sharedPref.getString("uuid", defaultValue);
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showProgress(final boolean show, boolean success) {
        final View otherView = success ? mRecyclerView : mAuthenticateForm;
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            otherView.setVisibility(show ? View.GONE : View.VISIBLE);
            otherView.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    otherView.setVisibility(show ? View.GONE : View.VISIBLE);
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
            otherView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }

    public interface OnPastTransactionInteractionListener {
        void onOrderInteraction(Order order);
    }
}
