package org.feup.cmov.acmecafe.Models;

import com.orm.SugarRecord;

import java.io.Serializable;

public class Product extends SugarRecord implements Serializable {
    private int productId;
    private String name;
    private float price;

    public Product() {
    }

    public Product(int id, String name, float price) {
        this.productId = id;
        this.name = name;
        this.price = price;
    }

    public int getProductId() {
        return productId;
    }

    public String getName() {
        return this.name;
    }

    public float getPrice() {
        return this.price;
    }

}
