package com.cosmian.rest.abe;

public enum Implementation {
    GPSW(0x01),
    CoverCrypt(0x02);

    private final int code;

    private Implementation(int code) {
        this.code = code;
    }

    public int getCode() {
        return this.code;
    }
}
