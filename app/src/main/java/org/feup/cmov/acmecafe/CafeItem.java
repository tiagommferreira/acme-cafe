package org.feup.cmov.acmecafe;

import java.io.Serializable;

public class CafeItem implements Serializable {
    private int mId;
    private String mName;
    private float mPrice;

    public CafeItem(int id, String name, float price) {
        this.mId = id;
        this.mName = name;
        this.mPrice = price;
    }

    public int getId() {
        return mId;
    }

    public String getName() {
        return this.mName;
    }

    public float getPrice() {
        return this.mPrice;
    }

}
