package org.feup.cmov.acmecafe.MenuList;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
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

import org.feup.cmov.acmecafe.Models.Product;
import org.feup.cmov.acmecafe.MainActivity;
import org.feup.cmov.acmecafe.R;
import org.feup.cmov.acmecafe.VolleySingleton;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class MenuListFragment extends Fragment {
    private static final String GET_MENU_TAG = "GET_MENU";

    private ArrayList<Product> mMenuItems = new ArrayList<>();

    private OnMenuListInteractionListener mListener;

    private SwipeRefreshLayout mSwipeRefreshLayout;

    private RecyclerView.Adapter mMenuListAdapter;

    public MenuListFragment() {
    }

    public static MenuListFragment newInstance() {
        return new MenuListFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_menu_list, container, false);

        // Set the adapter
        if (view instanceof SwipeRefreshLayout) {
            RecyclerView recyclerView = (RecyclerView) view.findViewById(R.id.menu_list);
            recyclerView.setLayoutManager(new LinearLayoutManager(recyclerView.getContext()));
            mMenuListAdapter = new MenuListAdapter(mMenuItems, mListener);
            recyclerView.setAdapter(mMenuListAdapter);

            mMenuItems.addAll(Product.listAll(Product.class));

            mSwipeRefreshLayout = (SwipeRefreshLayout) view;
            mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
                @Override
                public void onRefresh() {
                    attemptGetMenu();
                }
            });

            //attemptGetMenu();
        }

        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnMenuListInteractionListener) {
            mListener = (OnMenuListInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnMenuListInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
        //If the fragment is detached and there is a menu request in queue, cancel it
        if (VolleySingleton.getInstance(this.getActivity().getApplicationContext()).getRequestQueue() != null) {
            VolleySingleton.getInstance(this.getActivity().getApplicationContext()).getRequestQueue().cancelAll(GET_MENU_TAG);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        MainActivity activity = (MainActivity) getActivity();
        activity.setToolbarTitle("Menu");
    }

    public void attemptGetMenu() {
        //If the user does not have an Internet connection, do not try to get the Menu list
        ConnectivityManager connectivityManager = (ConnectivityManager) this.getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        if(activeNetworkInfo == null || !activeNetworkInfo.isConnected()) {
            mSwipeRefreshLayout.setRefreshing(false);
            Snackbar.make(getActivity().getCurrentFocus(), "Check your Internet connection", Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show();
            return;
        }

        getMenu();
    }

    public void getMenu() {
        String url = "https://acme-cafe.herokuapp.com/menu";
        JsonArrayRequest jsObjRequest = new JsonArrayRequest
                (Request.Method.GET, url, null, new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        mMenuItems.clear();
                        Product.deleteAll(Product.class);
                        for(int i = 0; i < response.length(); i++) {
                            try {
                                JSONObject object = (JSONObject) response.get(i);
                                Product item = new Product(object.getInt("id"), object.getString("name"), (float) object.getDouble("price"));
                                item.save();
                                mMenuItems.add(item);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                        mMenuListAdapter.notifyDataSetChanged();
                        mSwipeRefreshLayout.setRefreshing(false);
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e("Menu", "Error getting menu");
                    }
                });

        jsObjRequest.setTag(GET_MENU_TAG);
        VolleySingleton.getInstance(this.getActivity().getApplicationContext()).addToRequestQueue(jsObjRequest);
    }

    public interface OnMenuListInteractionListener {
        void onMenuListInteraction(Product item);
    }

}
