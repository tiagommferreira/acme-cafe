package org.feup.cmov.acmecafe;

import java.io.Serializable;

public class CafeItem implements Serializable {
    private String mName;
    private float mPrice;

    public CafeItem(String name, float price) {
        this.mName = name;
        this.mPrice = price;
    }

    public String getName() {
        return this.mName;
    }

    public float getPrice() {
        return this.mPrice;
    }

}
