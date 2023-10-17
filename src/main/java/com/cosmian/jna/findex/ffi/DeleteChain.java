package com.cosmian.jna.findex.ffi;

import java.util.List;

import com.cosmian.jna.findex.FindexCallbackException;
import com.cosmian.jna.findex.ffi.FindexNativeWrapper.DeleteChainCallback;
import com.cosmian.jna.findex.ffi.FindexUserCallbacks.DBDeleteChain;
import com.cosmian.jna.findex.serde.Leb128Reader;
import com.cosmian.jna.findex.structs.Uid32;
import com.cosmian.utils.CloudproofException;
import com.sun.jna.Pointer;

public class DeleteChain implements DeleteChainCallback {

    private DBDeleteChain delete;

    public DeleteChain(DBDeleteChain delete) {
        this.delete = delete;
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
            List<Uid32> uids =
                Leb128Reader.deserializeCollection(Uid32.class,  itemsBytes);

            //
            // Insert in database
            //
            this.delete.delete(uids);

            return 0;
        } catch (CloudproofException e) {
            return FindexCallbackException.record(e);
        }
    }
}
