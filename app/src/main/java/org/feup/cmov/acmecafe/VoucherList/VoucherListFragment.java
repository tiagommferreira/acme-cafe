package org.feup.cmov.acmecafe.VoucherList;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;

import org.feup.cmov.acmecafe.MainActivity;
import org.feup.cmov.acmecafe.MenuList.MenuListAdapter;
import org.feup.cmov.acmecafe.Models.CafeItem;
import org.feup.cmov.acmecafe.Models.Voucher;
import org.feup.cmov.acmecafe.R;
import org.feup.cmov.acmecafe.VolleySingleton;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;


public class VoucherListFragment extends Fragment {
    private static final String GET_VOUCHERS_TAG = "GET_VOUCHERS";

    private OnVoucherInteractionListener mListener;

    private ArrayList<Voucher> mVouchers = new ArrayList<>();
    private VoucherListAdapter mVoucherListAdapter;
    private SwipeRefreshLayout mSwipeRefreshLayout;

    public VoucherListFragment() {
    }


    // TODO: Rename and change types and number of parameters
    public static VoucherListFragment newInstance() {
        return new VoucherListFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_voucher_list, container, false);

        // Set the adapter
        if (view instanceof SwipeRefreshLayout) {
            RecyclerView recyclerView = (RecyclerView) view.findViewById(R.id.voucher_list);
            recyclerView.setLayoutManager(new LinearLayoutManager(recyclerView.getContext()));
            mVoucherListAdapter = new VoucherListAdapter(mVouchers, mListener);
            recyclerView.setAdapter(mVoucherListAdapter);

            mSwipeRefreshLayout = (SwipeRefreshLayout) view;
            mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
                @Override
                public void onRefresh() {
                    attemptGetVouchers();
                }
            });

            attemptGetVouchers();
        }

        return view;
    }

    private void attemptGetVouchers() {
        //If the user does not have an Internet connection, do not try to get the Voucher list
        ConnectivityManager connectivityManager = (ConnectivityManager) this.getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        if(activeNetworkInfo == null || !activeNetworkInfo.isConnected()) {
            mSwipeRefreshLayout.setRefreshing(false);
            Snackbar.make(getActivity().getCurrentFocus(), "Check your Internet connection", Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show();
            return;
        }

        getVouchers();
    }

    private void getVouchers() {
        String url = "http://10.0.2.2:8080/vouchers/" + getUserUUID();
        JsonArrayRequest jsObjRequest = new JsonArrayRequest
                (Request.Method.GET, url, null, new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        mVouchers.clear();
                        for(int i = 0; i < response.length(); i++) {
                            try {
                                JSONObject object = (JSONObject) response.get(i);
                                Voucher voucher = new Voucher(object.getInt("voucher_id"), object.getInt("type"), object.getString("name"), object.getString("signature"));
                                mVouchers.add(voucher);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                        mVoucherListAdapter.notifyDataSetChanged();
                        mSwipeRefreshLayout.setRefreshing(false);
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e("Menu", "Error getting vouchers");
                    }
                });

        jsObjRequest.setTag(GET_VOUCHERS_TAG);
        VolleySingleton.getInstance(this.getActivity().getApplicationContext()).addToRequestQueue(jsObjRequest);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnVoucherInteractionListener) {
            mListener = (OnVoucherInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnVoucherInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
        //If the fragment is detached and there is a menu request in queue, cancel it
        if (VolleySingleton.getInstance(this.getActivity().getApplicationContext()).getRequestQueue() != null) {
            VolleySingleton.getInstance(this.getActivity().getApplicationContext()).getRequestQueue().cancelAll(GET_VOUCHERS_TAG);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        MainActivity activity = (MainActivity) getActivity();
        activity.setToolbarTitle("Vouchers");
    }

    private String getUserUUID() {
        SharedPreferences sharedPref = getActivity().getSharedPreferences(getString(R.string.user_details_prefs),Context.MODE_PRIVATE);
        String defaultValue = "Could not get UUID from shared preferences";
        return sharedPref.getString("uuid", defaultValue);
    }

    public interface OnVoucherInteractionListener {
        void onVoucherAdded(Voucher voucher);
    }
}
