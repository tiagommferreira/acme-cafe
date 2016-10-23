package org.feup.cmov.acmecafe.VoucherList;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import org.feup.cmov.acmecafe.Models.Voucher;
import org.feup.cmov.acmecafe.R;

import java.util.ArrayList;

public class VoucherListAdapter extends RecyclerView.Adapter<VoucherListAdapter.ViewHolder> {
    private ArrayList<Voucher> mDataset = new ArrayList<>();
    private final VoucherListFragment.OnVoucherInteractionListener mListener;

    class ViewHolder extends RecyclerView.ViewHolder {
        final TextView mNameTextView;
        final ImageView mAddToCartImageView;
        ViewHolder(View v) {
            super(v);
            mNameTextView = (TextView) v.findViewById(R.id.voucher_item_name);
            mAddToCartImageView = (ImageView) v.findViewById(R.id.add_voucher_to_cart_icon);
        }
    }

    public VoucherListAdapter(ArrayList<Voucher> vouchers, VoucherListFragment.OnVoucherInteractionListener listener) {
        this.mListener = listener;
        this.mDataset = vouchers;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // create a new view
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.voucher_list_item, parent, false);
        VoucherListAdapter.ViewHolder vh = new VoucherListAdapter.ViewHolder(v);
        return vh;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        final Voucher item = mDataset.get(position);
        holder.mNameTextView.setText(item.getName());
        holder.mAddToCartImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mListener != null) {
                    mListener.onVoucherAdded(item);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return mDataset.size();
    }

}
