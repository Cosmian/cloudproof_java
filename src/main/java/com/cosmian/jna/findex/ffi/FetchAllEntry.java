package com.cosmian.jna.findex.ffi;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Set;

import com.cosmian.jna.findex.ffi.FindexNativeWrapper.FetchAllEntriesCallback;
import com.cosmian.jna.findex.ffi.FindexUserCallbacks.DBFetchAllEntries;
import com.cosmian.jna.findex.serde.Leb128Writer;
import com.cosmian.jna.findex.structs.EntryTableValue;
import com.cosmian.jna.findex.structs.Uid32;
import com.cosmian.utils.CloudproofException;
import com.sun.jna.Pointer;
import com.sun.jna.ptr.IntByReference;

public class FetchAllEntry implements FetchAllEntriesCallback {
    private DBFetchAllEntries fetch;

    private Iterator<Entry<Uid32, EntryTableValue>> entrySetIterator;

    public FetchAllEntry(DBFetchAllEntries fetch) {
        this.fetch = fetch;
        entrySetIterator = null;
    }

    @Override
    public int apply(Pointer output,
                     IntByReference outputSize,
                     int numberOfEntries)
        throws CloudproofException {
        if (entrySetIterator == null) {
            entrySetIterator = this.fetch.fetch().entrySet().iterator();
        }

        Set<Entry<Uid32, EntryTableValue>> chunk = new HashSet<>();
        while (chunk.size() < numberOfEntries && entrySetIterator.hasNext()) {
            chunk.add(entrySetIterator.next());
        }

        if (chunk.size() > 0) {
            byte[] uidsAndValuesBytes = Leb128Writer.serializeEntryCollection(chunk);
            output.write(0, uidsAndValuesBytes, 0, uidsAndValuesBytes.length);
            outputSize.setValue(uidsAndValuesBytes.length);
        } else {
            outputSize.setValue(0);
        }

        if (entrySetIterator.hasNext()) {
            return 1;
        } else {
            entrySetIterator = null; // Reset the iterator to fetch new entries if multiple compact.
            return 0;
        }
    }

}
