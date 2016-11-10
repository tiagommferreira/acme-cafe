package org.feup.cmov.acmecafe;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.widget.TextView;

import org.feup.cmov.acmecafe.Models.Order;
import org.feup.cmov.acmecafe.Models.Product;
import org.feup.cmov.acmecafe.Models.Voucher;
import org.feup.cmov.acmecafe.OrderList.OrderAdapter;
import org.feup.cmov.acmecafe.OrderList.OrderVoucherAdapter;

import java.util.ArrayList;
import java.util.HashMap;

public class OrderDetailsActivity extends AppCompatActivity {

    private HashMap<Product, Integer> mProducts = new HashMap<>();
    private ArrayList<Voucher> mVouchers = new ArrayList<>();

    private RecyclerView mProductsRecyclerView;
    private RecyclerView mVouchersRecyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_details);

        Intent intent = getIntent();
        Order order = (Order) intent.getSerializableExtra("order");

        setTitle("Order " + order.getId());
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mProducts = order.getProducts();
        mVouchers = order.getVouchers();

        mProductsRecyclerView = (RecyclerView) findViewById(R.id.order_details_products_list);
        mProductsRecyclerView.setAdapter(new OrderAdapter(mProducts, null, null, null));

        mVouchersRecyclerView = (RecyclerView) findViewById(R.id.order_details_voucher_list);
        mVouchersRecyclerView.setAdapter(new OrderVoucherAdapter(mVouchers, null, null, null));

        TextView priceTV = (TextView) findViewById(R.id.order_details_price);
        priceTV.setText("Total: " + calculateOrderPrice(mProducts, mVouchers) + "€");

    }

    private float calculateOrderPrice(HashMap<Product,Integer> products, ArrayList<Voucher> vouchers) {
        float price = Utils.calculateOrderPrice(products, vouchers);

        return price;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        //getMenuInflater().inflate(R.menu.main, menu);
        return false;
    }
}
