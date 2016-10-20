package org.feup.cmov.acmecafe.MenuList;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import org.feup.cmov.acmecafe.CafeItem;
import org.feup.cmov.acmecafe.R;

import java.util.ArrayList;

public class MenuListAdapter extends RecyclerView.Adapter<MenuListAdapter.ViewHolder> {
    private ArrayList<CafeItem> mDataset;
    private final MenuListFragment.OnMenuListInteractionListener mListener;

    class ViewHolder extends RecyclerView.ViewHolder {
        final TextView mNameTextView;
        final TextView mPriceTextView;
        final ImageView mAddToCartImageView;
        ViewHolder(View v) {
            super(v);
            mNameTextView = (TextView) v.findViewById(R.id.menu_item_name);
            mPriceTextView = (TextView) v.findViewById(R.id.menu_item_price);
            mAddToCartImageView = (ImageView) v.findViewById(R.id.add_to_cart_icon);
        }
    }

    // Provide a suitable constructor (depends on the kind of dataset)
    public MenuListAdapter(ArrayList<CafeItem> myDataset,
                           MenuListFragment.OnMenuListInteractionListener listener) {
        mDataset = myDataset;
        this.mListener = listener;
    }

    // Create new views (invoked by the layout manager)
    @Override
    public MenuListAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // create a new view
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.menu_list_item, parent, false);
        ViewHolder vh = new ViewHolder(v);
        return vh;
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        final CafeItem item = mDataset.get(position);
        holder.mNameTextView.setText(item.getName());
        holder.mPriceTextView.setText(String.valueOf(item.getPrice()) + "€");
        holder.mAddToCartImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mListener != null) {
                    mListener.onMenuListInteraction(item);
                }
            }
        });
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return mDataset.size();
    }
}