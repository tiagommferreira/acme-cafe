package org.feup.cmov.acmecafe.Models;

import com.orm.SugarRecord;

import java.io.Serializable;

public class Voucher extends SugarRecord implements Serializable {
    private int voucherId;
    private int type;
    private String name;
    private String signature;
    private boolean isUsed;

    public Voucher() {

    }

    public Voucher(int id, int type, String name, String signature) {
        this.voucherId = id;
        this.type = type;
        this.name = name;
        this.signature = signature;
        this.isUsed = false;
    }

    public int getVoucherId() {
        return voucherId;
    }

    public int getType() {
        return type;
    }

    public String getName() {
        return name;
    }

    public String getSignature() {
        return signature;
    }

    public boolean getIsUsed() {
        return isUsed;
    }

    public void setIsUsed(boolean isUsed) {
        this.isUsed = isUsed;
    }

}
