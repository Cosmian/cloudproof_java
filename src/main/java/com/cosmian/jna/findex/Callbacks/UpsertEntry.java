package com.cosmian.jna.findex.Callbacks;

import java.util.HashMap;

import com.cosmian.jna.CloudproofException;
import com.cosmian.jna.findex.FindexWrapper.UpsertEntryCallback;
import com.cosmian.jna.findex.FindexWrapper.UpsertEntryInterface;
import com.cosmian.jna.findex.Leb128Serializer;
import com.sun.jna.Pointer;

public class UpsertEntry implements UpsertEntryCallback {

    private UpsertEntryInterface upsert;

    public UpsertEntry(UpsertEntryInterface upsert) {
        this.upsert = upsert;
    }

    @Override
    public int apply(Pointer items, int itemsLength) throws CloudproofException {
        //
        // Read `items` until `itemsLength`
        //
        byte[] itemsBytes = new byte[itemsLength];
        items.read(0, itemsBytes, 0, itemsLength);

        //
        // Deserialize the entry table items
        //
        HashMap<byte[], byte[]> uidsAndValues = Leb128Serializer.deserializeHashmap(itemsBytes);

        //
        // Insert in database
        //
        this.upsert.upsert(uidsAndValues);

        return 0;
    }

}
