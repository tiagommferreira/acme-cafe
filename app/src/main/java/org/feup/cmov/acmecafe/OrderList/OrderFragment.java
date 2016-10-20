package org.feup.cmov.acmecafe.OrderList;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.feup.cmov.acmecafe.CafeItem;
import org.feup.cmov.acmecafe.R;

import java.util.HashMap;


public class OrderFragment extends Fragment {
    private static final String ARG_ORDER_CONTENT = "order_content";

    private HashMap<CafeItem, Integer> mCurrentOrder;

    private OnOrderItemInteracionListener mListener;

    private RecyclerView.Adapter mOrderListAdapter;


    public OrderFragment() {
    }

    public static OrderFragment newInstance(HashMap<CafeItem, Integer> currentOrder) {
        OrderFragment fragment = new OrderFragment();
        Bundle args = new Bundle();
        args.putSerializable(ARG_ORDER_CONTENT, currentOrder);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mCurrentOrder = (HashMap<CafeItem, Integer>) getArguments().getSerializable(ARG_ORDER_CONTENT);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_order, container, false);

        Log.d("OrderFragment", "Size: " + String.valueOf(mCurrentOrder.size()));

        RecyclerView recyclerView = (RecyclerView) view;
        recyclerView.setLayoutManager(new LinearLayoutManager(recyclerView.getContext()));
        mOrderListAdapter = new OrderAdapter(mCurrentOrder, mListener);
        recyclerView.setAdapter(mOrderListAdapter);

        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnOrderItemInteracionListener) {
            mListener = (OnOrderItemInteracionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnOrderItemInteracionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public interface OnOrderItemInteracionListener {
        void onItemInteraction(CafeItem item);
    }
}
