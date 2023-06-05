package com.cosmian.jna.findex.ffi;

import java.util.List;
import java.util.Map;

import com.cosmian.jna.findex.FindexCallbackException;
import com.cosmian.jna.findex.ffi.FindexNativeWrapper.UpdateLinesCallback;
import com.cosmian.jna.findex.ffi.FindexUserCallbacks.DBUpdateLines;
import com.cosmian.jna.findex.serde.Leb128Reader;
import com.cosmian.jna.findex.structs.ChainTableValue;
import com.cosmian.jna.findex.structs.EntryTableValue;
import com.cosmian.jna.findex.structs.Uid32;
import com.cosmian.utils.CloudproofException;
import com.sun.jna.Pointer;

public class UpdateLines implements UpdateLinesCallback {

    private DBUpdateLines update;

    public UpdateLines(DBUpdateLines update) {
        this.update = update;
    }

    @Override
    public int apply(Pointer removedChainsPointer,
                     int removedChainsLength,
                     Pointer newEntriesPointer,
                     int newEntriesLength,
                     Pointer newChainsPointer,
                     int newChainsLength)
        throws CloudproofException {
        try {
            byte[] removedChainsBytes = new byte[removedChainsLength];
            removedChainsPointer.read(0, removedChainsBytes, 0, removedChainsLength);
            List<Uid32> removedChains = Leb128Reader.deserializeCollection(Uid32.class, removedChainsBytes);
    
            byte[] newEntriesBytes = new byte[newEntriesLength];
            newEntriesPointer.read(0, newEntriesBytes, 0, newEntriesLength);
            Map<Uid32, EntryTableValue> newEntries =
                Leb128Reader.deserializeMap(Uid32.class, EntryTableValue.class, newEntriesBytes);
    
            byte[] newChainsBytes = new byte[newChainsLength];
            newChainsPointer.read(0, newChainsBytes, 0, newChainsLength);
            Map<Uid32, ChainTableValue> newChains =
                Leb128Reader.deserializeMap(Uid32.class, ChainTableValue.class, newChainsBytes);
    
            this.update.update(removedChains, newEntries, newChains);
            return 0;
        } catch (CloudproofException e) {
            return FindexCallbackException.record(e);
        }
    }

}
