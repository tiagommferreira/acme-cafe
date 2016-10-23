package org.feup.cmov.acmecafe.Models;

public class Voucher {
    private int mId;
    private int mType;
    private String mName;
    private String mSignature;

    public Voucher(int id, int type, String name, String signature) {
        this.mId = id;
        this.mType = type;
        this.mName = name;
        this.mSignature = signature;
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


}
