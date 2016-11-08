package org.feup.cmov.acmecafe.PastTransactions;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.feup.cmov.acmecafe.Models.Order;
import org.feup.cmov.acmecafe.Models.Product;
import org.feup.cmov.acmecafe.Models.Voucher;
import org.feup.cmov.acmecafe.R;
import org.feup.cmov.acmecafe.Utils;

import java.util.ArrayList;
import java.util.HashMap;

public class PastTransactionsAdapter extends RecyclerView.Adapter<PastTransactionsAdapter.ViewHolder> {
    private ArrayList<Order> mDataset;
    private final PastTransactionsFragment.OnPastTransactionInteractionListener mListener;

    class ViewHolder extends RecyclerView.ViewHolder {
        final View mCapsule;
        final TextView mIdTextView;
        final TextView mPriceTextView;
        ViewHolder(View v) {
            super(v);
            mCapsule = v;
            mIdTextView = (TextView) v.findViewById(R.id.past_transaction_id);
            mPriceTextView = (TextView) v.findViewById(R.id.past_transaction_price);
        }
    }

    // Provide a suitable constructor (depends on the kind of dataset)
    public PastTransactionsAdapter(ArrayList<Order> myDataset,
                           PastTransactionsFragment.OnPastTransactionInteractionListener listener) {
        mDataset = myDataset;
        this.mListener = listener;
    }

    // Create new views (invoked by the layout manager)
    @Override
    public PastTransactionsAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // create a new view
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.past_transactions_item, parent, false);
        PastTransactionsAdapter.ViewHolder vh = new PastTransactionsAdapter.ViewHolder(v);
        return vh;
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(PastTransactionsAdapter.ViewHolder holder, int position) {
        final Order item = mDataset.get(position);
        holder.mIdTextView.setText("Order " + item.getId());
        holder.mPriceTextView.setText(calculateOrderPrice(item.getProducts(), item.getVouchers()) + "€");
        holder.mCapsule.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.onOrderInteraction(item);
            }
        });
    }

    private float calculateOrderPrice(HashMap<Product,Integer> products, ArrayList<Voucher> vouchers) {
        float price = Utils.calculateOrderPrice(products, vouchers);

        return price;
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return mDataset.size();
    }
}