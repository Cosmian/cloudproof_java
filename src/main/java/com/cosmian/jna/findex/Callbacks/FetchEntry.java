package com.cosmian.jna.findex.Callbacks;

import java.util.List;
import java.util.Map;

import com.cosmian.CloudproofException;
import com.cosmian.jna.findex.EntryTableValue;
import com.cosmian.jna.findex.Findex;
import com.cosmian.jna.findex.FindexWrapper.FetchEntryCallback;
import com.cosmian.jna.findex.FindexWrapper.FetchEntryInterface;
import com.cosmian.jna.findex.Uid;
import com.cosmian.jna.findex.serde.Leb128Reader;
import com.sun.jna.Pointer;
import com.sun.jna.ptr.IntByReference;

public class FetchEntry implements FetchEntryCallback {
    private FetchEntryInterface fetch;

    public FetchEntry(FetchEntryInterface fetch) {
        this.fetch = fetch;
    }

    @Override
    public int apply(Pointer output,
                     IntByReference outputSize,
                     Pointer uidsPointer,
                     int uidsLength)
        throws CloudproofException {
        //
        // Read `uidsPointer` until `uidsLength`
        //
        byte[] uids = new byte[uidsLength];
        uidsPointer.read(0, uids, 0, uidsLength);

        //
        // Deserialize Entry Table uids
        //
        List<Uid> entryTableUids = Leb128Reader.deserializeList(Uid.class, uids);

        //
        // Select uids and values in EntryTable
        //
        Map<Uid, EntryTableValue> uidsAndValues = this.fetch.fetch(entryTableUids);

        //
        // Serialize results
        //
        return Findex.mapToOutputPointer(uidsAndValues, output, outputSize);
    }

}
