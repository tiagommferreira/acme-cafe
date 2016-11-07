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

import java.util.ArrayList;
import java.util.HashMap;

public class PastTransactionsAdapter extends RecyclerView.Adapter<PastTransactionsAdapter.ViewHolder> {
    private ArrayList<Order> mDataset;
    private final PastTransactionsFragment.OnPastTransactionInteractionListener mListener;

    class ViewHolder extends RecyclerView.ViewHolder {
        final TextView mIdTextView;
        final TextView mPriceTextView;
        ViewHolder(View v) {
            super(v);
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
    }

    private float calculateOrderPrice(HashMap<Product,Integer> products, ArrayList<Voucher> vouchers) {
        float price = 0f;
        float popcornPrice = 0f;
        float coffeePrice = 0f;

        for(Product p : products.keySet()) {
            price += p.getPrice() * products.get(p);
            if(p.getName().equals("Popcorn")) {
                popcornPrice = p.getPrice();
            }
            else if(p.getName().equals("Coffee")) {
                coffeePrice = p.getPrice();
            }
        }

        for(Voucher v : vouchers) {
            if(v.getType() == 1) {
                price -= popcornPrice;
            }
            else if(v.getType() == 2) {
                price -= coffeePrice;
            }
            else if(v.getType() == 3) {
                price -= ((5*price)/100);
            }
        }

        return price;
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return mDataset.size();
    }
}