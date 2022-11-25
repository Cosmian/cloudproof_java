package com.cosmian.jna.findex.Callbacks;

import java.util.HashMap;

import com.cosmian.CloudproofException;
import com.cosmian.jna.findex.FindexWrapper.UpsertChainCallback;
import com.cosmian.jna.findex.FindexWrapper.UpsertChainInterface;
import com.cosmian.jna.findex.Leb128Serializer;
import com.sun.jna.Pointer;

public class UpsertChain implements UpsertChainCallback {

    private UpsertChainInterface upsert;

    public UpsertChain(UpsertChainInterface upsert) {
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
        // Deserialize the chain table items
        //
        HashMap<byte[], byte[]> uidsAndValues = Leb128Serializer.deserializeHashmap(itemsBytes);

        //
        // Insert in database
        //
        this.upsert.upsert(uidsAndValues);

        return 0;
    }
}
