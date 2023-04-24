package com.cosmian.jna.findex.ffi;

import java.util.Map;

import com.cosmian.jna.findex.FindexCallbackException;
import com.cosmian.jna.findex.ffi.FindexNativeWrapper.UpsertChainCallback;
import com.cosmian.jna.findex.ffi.FindexUserCallbacks.DBUpsertChain;
import com.cosmian.jna.findex.serde.Leb128Reader;
import com.cosmian.jna.findex.structs.ChainTableValue;
import com.cosmian.jna.findex.structs.Uid32;
import com.cosmian.utils.CloudproofException;
import com.sun.jna.Pointer;

public class UpsertChain implements UpsertChainCallback {

    private DBUpsertChain upsert;

    public UpsertChain(DBUpsertChain upsert) {
        this.upsert = upsert;
    }

    @Override
    public int apply(Pointer items,
                     int itemsLength)
        throws CloudproofException {
        try {
            //
            // Read `items` until `itemsLength`
            //
            byte[] itemsBytes = new byte[itemsLength];
            items.read(0, itemsBytes, 0, itemsLength);
    
            //
            // Deserialize the chain table items
            //
            Map<Uid32, ChainTableValue> uidsAndValues =
                Leb128Reader.deserializeMap(Uid32.class, ChainTableValue.class, itemsBytes);
    
            //
            // Insert in database
            //
            this.upsert.upsert(uidsAndValues);
    
            return 0;
        } catch (CloudproofException e) {
            return FindexCallbackException.record(e);
        }
    }
}
