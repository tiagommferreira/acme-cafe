package org.feup.cmov.acmecafe.VoucherList;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.os.Bundle;
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
import org.feup.cmov.acmecafe.Models.Voucher;
import org.feup.cmov.acmecafe.R;
import org.feup.cmov.acmecafe.Utils;
import org.feup.cmov.acmecafe.VolleySingleton;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class VoucherListFragment extends Fragment {
    private static final String GET_VOUCHERS_TAG = "GET_VOUCHERS";
    private static final String ARG_USER_VOUCHERS = "USER_VOUCHERS";

    private OnVoucherInteractionListener mListener;

    private ArrayList<Voucher> mVouchers = new ArrayList<>();
    private VoucherListAdapter mVoucherListAdapter;
    private SwipeRefreshLayout mSwipeRefreshLayout;

    public VoucherListFragment() {
    }

    public static VoucherListFragment newInstance() {
        VoucherListFragment fragment = new VoucherListFragment();
        //Bundle args = new Bundle();
        //args.putSerializable(ARG_USER_VOUCHERS, vouchers);
        //fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            //mVouchers = (ArrayList<Voucher>) getArguments().getSerializable(ARG_USER_VOUCHERS);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_voucher_list, container, false);

        mVouchers.clear();
        mVouchers.addAll(Voucher.listAll(Voucher.class));

        Log.d("VoucherListFragment", "Vouchers size: " + mVouchers.size());

        if(mVouchers.size() == 0)
            attemptGetVouchers();

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

        }

        return view;
    }

    private void attemptGetVouchers() {

        if(!Utils.hasInternetConnection(getView(), (ConnectivityManager) this.getActivity().getSystemService(Context.CONNECTIVITY_SERVICE)))
            return;

        getVouchers();
    }

    private void getVouchers() {
        String url = "https://acme-cafe.herokuapp.com/vouchers/" + getUserUUID();
        JsonArrayRequest jsObjRequest = new JsonArrayRequest
                (Request.Method.GET, url, null, new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        mVouchers.clear();
                        Voucher.deleteAll(Voucher.class);
                        for(int i = 0; i < response.length(); i++) {
                            try {
                                JSONObject object = (JSONObject) response.get(i);
                                Voucher voucher = new Voucher(object.getInt("voucher_id"), object.getInt("type"), object.getString("name"), object.getString("signature"));
                                voucher.save();
                                mVouchers.add(voucher);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                        mVoucherListAdapter.toggleDiscountVouchers(false);
                        mVoucherListAdapter.notifyDataSetChanged();
                        mListener.onVoucherRefreshed();
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
        void onVoucherAdded(Voucher voucher, int pos, VoucherListAdapter adapter);
        void onVoucherRefreshed();
    }
}
