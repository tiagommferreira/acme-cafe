package org.feup.cmov.acmecafe.Models;

import java.io.Serializable;

public class Voucher implements Serializable {
    private int mId;
    private int mType;
    private String mName;
    private String mSignature;
    private boolean mIsUsed;

    public Voucher(int id, int type, String name, String signature) {
        this.mId = id;
        this.mType = type;
        this.mName = name;
        this.mSignature = signature;
        this.mIsUsed = false;
    }

    public int getId() {
        return mId;
    }

    public int getType() {
        return mType;
    }

    public String getName() {
        return mName;
    }

    public String getSignature() {
        return mSignature;
    }

    public boolean getIsUsed() {
        return  mIsUsed;
    }

    public void setIsUsed(boolean isUsed) {
        this.mIsUsed = isUsed;
    }

}
