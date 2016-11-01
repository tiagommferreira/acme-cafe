package org.feup.cmov.acmecafe.OrderList;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import org.feup.cmov.acmecafe.Models.Product;
import org.feup.cmov.acmecafe.R;

import java.util.HashMap;


public class OrderAdapter extends RecyclerView.Adapter<OrderAdapter.ViewHolder> {
    private HashMap<Product, Integer> mDataset;
    private TextView mPriceTV;
    private final OrderFragment.OnOrderItemInteractionListener mListener;

    class ViewHolder extends RecyclerView.ViewHolder {
        final TextView mNameTextView;
        final TextView mPriceTextView;
        final ImageView mRemoveImageView;
        ViewHolder(View v) {
            super(v);
            mNameTextView = (TextView) v.findViewById(R.id.order_item_name);
            mPriceTextView = (TextView) v.findViewById(R.id.order_item_quantity);
            mRemoveImageView = (ImageView) v.findViewById(R.id.remove_to_cart_icon);
        }
    }

    // Provide a suitable constructor (depends on the kind of dataset)
    public OrderAdapter(HashMap<Product, Integer> myDataset,
                           OrderFragment.OnOrderItemInteractionListener listener, TextView priceTV) {
        mDataset = myDataset;
        mListener = listener;
        mPriceTV = priceTV;
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
    public void onBindViewHolder(ViewHolder holder, final int position) {
        final Product item = (Product) mDataset.keySet().toArray()[position];
        final RecyclerView.Adapter adapter = this;
        holder.mNameTextView.setText(item.getName());
        holder.mPriceTextView.setText("Quantity: " + String.valueOf(mDataset.get(item)));
        holder.mRemoveImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.onItemRemove(item, position, adapter,mPriceTV);
            }
        });
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return mDataset.size();
    }
}
