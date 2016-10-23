package org.feup.cmov.acmecafe.OrderList;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.feup.cmov.acmecafe.Models.CafeItem;
import org.feup.cmov.acmecafe.R;

import java.util.HashMap;


public class OrderAdapter extends RecyclerView.Adapter<OrderAdapter.ViewHolder> {
    private HashMap<CafeItem, Integer> mDataset;
    private final OrderFragment.OnOrderItemInteracionListener mListener;

    class ViewHolder extends RecyclerView.ViewHolder {
        final TextView mNameTextView;
        final TextView mPriceTextView;
        ViewHolder(View v) {
            super(v);
            mNameTextView = (TextView) v.findViewById(R.id.order_item_name);
            mPriceTextView = (TextView) v.findViewById(R.id.order_item_quantity);
        }
    }

    // Provide a suitable constructor (depends on the kind of dataset)
    public OrderAdapter(HashMap<CafeItem, Integer> myDataset,
                           OrderFragment.OnOrderItemInteracionListener listener) {
        mDataset = myDataset;
        this.mListener = listener;
    }

    // Create new views (invoked by the layout manager)
    @Override
    public OrderAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // create a new view
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.order_list_item, parent, false);
        ViewHolder vh = new ViewHolder(v);
        return vh;
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        final CafeItem item = (CafeItem) mDataset.keySet().toArray()[position];
        holder.mNameTextView.setText(item.getName());
        holder.mPriceTextView.setText("Quantity: " + String.valueOf(mDataset.get(item)));
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return mDataset.size();
    }
}
