package org.feup.cmov.acmecafe.OrderList;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import org.feup.cmov.acmecafe.Models.Voucher;
import org.feup.cmov.acmecafe.R;

import java.util.ArrayList;

public class OrderVoucherAdapter extends RecyclerView.Adapter<OrderVoucherAdapter.ViewHolder> {
    private ArrayList<Voucher> mDataset;
    private TextView mPriceTV;
    private OrderFragment.OnOrderVoucherInteractionListener mListener;

    class ViewHolder extends RecyclerView.ViewHolder {
        final TextView mNameTextView;
        final ImageView mRemoveImageView;
        ViewHolder(View v) {
            super(v);
            mNameTextView = (TextView) v.findViewById(R.id.voucher_item_name);
            mRemoveImageView = (ImageView) v.findViewById(R.id.remove_voucher_to_cart_icon);
        }
    }

    public OrderVoucherAdapter(ArrayList<Voucher> vouchers, OrderFragment.OnOrderVoucherInteractionListener listener, TextView mPriceTextView) {
        this.mDataset = vouchers;
        this.mListener = listener;
        this.mPriceTV = mPriceTextView;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // create a new view
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.order_voucher_list_item, parent, false);
        OrderVoucherAdapter.ViewHolder vh = new OrderVoucherAdapter.ViewHolder(v);
        return vh;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, final int position) {
        final Voucher voucher = mDataset.get(position);
        holder.mNameTextView.setText(voucher.getName());

        final RecyclerView.Adapter adapter = this;
        if(mListener != null) {
            holder.mRemoveImageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mListener.onVoucherRemove(voucher, position, adapter, mPriceTV);
                }
            });
        }
        else {
            holder.mRemoveImageView.setBackground(null);
        }

    }

    @Override
    public int getItemCount() {
        return mDataset.size();
    }
}
