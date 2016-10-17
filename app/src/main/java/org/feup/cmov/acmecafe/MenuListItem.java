package org.feup.cmov.acmecafe;

public class MenuListItem {
    private String mName;
    private float mPrice;

    public MenuListItem(String name, float price) {
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
