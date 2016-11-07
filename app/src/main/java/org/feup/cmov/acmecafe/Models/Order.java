package org.feup.cmov.acmecafe.Models;

import java.util.ArrayList;
import java.util.HashMap;

public class Order {
    private int mId;
    private HashMap<Product, Integer> mProducts = new HashMap<>();
    private ArrayList<Voucher> mVouchers = new ArrayList<>();

    public Order(int id, HashMap<Product, Integer> products, ArrayList<Voucher> vouchers) {
        this.mId = id;
        this.mProducts = products;
        this.mVouchers = vouchers;
    }

    public int getId() {
        return mId;
    }

    public HashMap<Product, Integer> getProducts() {
        return mProducts;
    }

    public ArrayList<Voucher> getVouchers() {
        return mVouchers;
    }
}
