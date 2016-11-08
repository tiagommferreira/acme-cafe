package org.feup.cmov.acmecafe;

import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.design.widget.Snackbar;
import android.view.View;

import org.feup.cmov.acmecafe.Models.Product;
import org.feup.cmov.acmecafe.Models.Voucher;

import java.util.ArrayList;
import java.util.HashMap;

public final class Utils {
    public static float calculateOrderPrice(HashMap<Product, Integer> products, ArrayList<Voucher> vouchers) {
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

    public static boolean hasInternetConnection(View v, ConnectivityManager connectivityManager) {
        //If the user does not have an Internet connection, do not try to register
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        if(activeNetworkInfo == null || !activeNetworkInfo.isConnected()) {
            Snackbar.make(v, "Check your Internet connection", Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show();
            return false;
        }

        return true;
    }
}
