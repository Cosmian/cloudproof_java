package com.cosmian.jna.findex.Callbacks;

import com.cosmian.jna.FfiException;
import com.cosmian.jna.findex.FfiWrapper.UpsertEntryCallback;
import com.cosmian.jna.findex.FfiWrapper.UpsertEntryInterface;
import com.sun.jna.Pointer;

public class UpsertEntry implements UpsertEntryCallback {

    private UpsertEntryInterface upsert;

    public UpsertEntry(UpsertEntryInterface upsert) {
        this.upsert = upsert;
    }

    @Override
    public int apply(Pointer uid, int uidLength, Pointer value, int valueLength) throws FfiException {
        //
        // Read `uid` until `uidLength` and `value` until `valueLength`
        //
        byte[] uidBytes = new byte[uidLength];
        uid.read(0, uidBytes, 0, uidLength);
        byte[] valueBytes = new byte[valueLength];
        value.read(0, valueBytes, 0, valueLength);

        //
        // Insert in database
        //
        this.upsert.upsert(uidBytes, valueBytes);

        return 0;
    }

}
