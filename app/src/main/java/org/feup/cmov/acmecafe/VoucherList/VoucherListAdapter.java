package org.feup.cmov.acmecafe.VoucherList;

import android.content.Context;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
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
    private Context mContext;

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
        this.mContext = null;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        this.mContext = parent.getContext();
        // create a new view
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.voucher_list_item, parent, false);
        VoucherListAdapter.ViewHolder vh = new VoucherListAdapter.ViewHolder(v);
        return vh;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, final int position) {
        final Voucher voucher = mDataset.get(position);
        holder.mNameTextView.setText(voucher.getName());

        Log.d("VoucherAdapter", "Voucher " + voucher.getName() + " with IsUsed " + voucher.getIsUsed());
        if(!voucher.getIsUsed()) {
            final RecyclerView.Adapter adapter = this;
            holder.mAddToCartImageView.setBackground(ResourcesCompat.getDrawable(mContext.getResources(),R.mipmap.ic_add_to_order,null));
            holder.mAddToCartImageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(mListener != null) {
                        mListener.onVoucherAdded(voucher, position, adapter);
                    }
                }
            });
        }
        else {
            holder.mAddToCartImageView.setBackground(null);
            holder.mAddToCartImageView.setOnClickListener(null);
        }
    }

    @Override
    public int getItemCount() {
        return mDataset.size();
    }

}
