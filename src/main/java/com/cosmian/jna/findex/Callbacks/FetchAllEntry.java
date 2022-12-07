package com.cosmian.jna.findex.Callbacks;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Set;

import com.cosmian.CloudproofException;
import com.cosmian.jna.findex.EntryTableValue;
import com.cosmian.jna.findex.FindexWrapper.FetchAllEntryCallback;
import com.cosmian.jna.findex.FindexWrapper.FetchAllEntryInterface;
import com.cosmian.jna.findex.Leb128Serializer;
import com.cosmian.jna.findex.Uid;
import com.sun.jna.Pointer;
import com.sun.jna.ptr.IntByReference;

public class FetchAllEntry implements FetchAllEntryCallback {
    private FetchAllEntryInterface fetch;

    private Iterator<Entry<Uid, EntryTableValue>> entrySetIterator;

    public FetchAllEntry(FetchAllEntryInterface fetch) {
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

        Set<Entry<Uid, EntryTableValue>> chunk = new HashSet<>();
        while (chunk.size() < numberOfEntries && entrySetIterator.hasNext()) {
            chunk.add(entrySetIterator.next());
        }

        if (chunk.size() > 0) {
            byte[] uidsAndValuesBytes = Leb128Serializer.serializeEntrySet(chunk);
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
