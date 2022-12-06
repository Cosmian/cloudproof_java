package com.cosmian.jna.findex.Callbacks;

import java.util.List;
import java.util.Map;

import com.cosmian.CloudproofException;
import com.cosmian.jna.findex.Findex;
import com.cosmian.jna.findex.FindexWrapper.FetchEntryCallback;
import com.cosmian.jna.findex.FindexWrapper.FetchEntryInterface;
import com.cosmian.jna.findex.Leb128Serializer;
import com.cosmian.jna.findex.Uid;
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
        List<Uid> entryTableUids = Leb128Serializer.deserializeList(uids);

        //
        // Select uids and values in EntryTable
        //
        Map<Uid, byte[]> uidsAndValues = this.fetch.fetch(entryTableUids);

        //
        // Serialize results
        //
        return Findex.writeOutputPointerAndSize(uidsAndValues, output, outputSize);
    }

}
