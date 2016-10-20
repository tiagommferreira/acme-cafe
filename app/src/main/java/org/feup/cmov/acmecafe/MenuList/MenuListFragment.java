package org.feup.cmov.acmecafe.MenuList;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.GridLayoutManager;
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

import org.feup.cmov.acmecafe.CafeItem;
import org.feup.cmov.acmecafe.R;
import org.feup.cmov.acmecafe.VolleySingleton;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class MenuListFragment extends Fragment {
    private static final String GET_MENU_TAG = "GET_MENU";

    private ArrayList<CafeItem> mMenuItems = new ArrayList<>();

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
            Log.d("MenuListFragment", "inside SwipeRefreshLayout");
            RecyclerView recyclerView = (RecyclerView) view.findViewById(R.id.menu_list);
            recyclerView.setLayoutManager(new LinearLayoutManager(recyclerView.getContext()));
            mMenuListAdapter = new MenuListAdapter(mMenuItems, mListener);
            recyclerView.setAdapter(mMenuListAdapter);

            mSwipeRefreshLayout = (SwipeRefreshLayout) view;
            mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
                @Override
                public void onRefresh() {
                    getMenu();
                }
            });

            Log.d("MenuListFragment", "on create view");

            getMenu();

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
    }

    public void getMenu() {
        String url = "http://10.0.2.2:8080/menu";
        JsonArrayRequest jsObjRequest = new JsonArrayRequest
                (Request.Method.GET, url, null, new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        mMenuItems.clear();
                        for(int i = 0; i < response.length(); i++) {
                            try {
                                JSONObject object = (JSONObject) response.get(i);
                                CafeItem item = new CafeItem(object.getString("name"), (float) object.getDouble("price"));
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
                        // TODO Auto-generated method stub
                        Log.e("Menu", "Error getting menu");
                    }
                });

        jsObjRequest.setTag(GET_MENU_TAG);
        VolleySingleton.getInstance(this.getActivity().getApplicationContext()).addToRequestQueue(jsObjRequest);
    }

    public interface OnMenuListInteractionListener {
        void onMenuListInteraction(CafeItem item);
    }

}
