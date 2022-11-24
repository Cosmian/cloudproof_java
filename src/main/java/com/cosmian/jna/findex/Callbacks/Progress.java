package com.cosmian.jna.findex.Callbacks;

import java.util.List;

import com.cosmian.jna.CoverCryptException;
import com.cosmian.jna.findex.FfiWrapper.ProgressCallback;
import com.cosmian.jna.findex.FfiWrapper.ProgressInterface;
import com.cosmian.jna.findex.Leb128Serializer;
import com.sun.jna.Pointer;

public class Progress implements ProgressCallback {
    private ProgressInterface progress;

    public Progress(ProgressInterface progress) {
        this.progress = progress;
    }

    @Override
    public boolean apply(Pointer searchResultsPointer, int searchResultsLength) throws CoverCryptException {
        //
        // Read `searchResultsPointer` until `searchResultsLength`
        //
        byte[] serializedSearchResults = new byte[searchResultsLength];
        searchResultsPointer.read(0, serializedSearchResults, 0, searchResultsLength);

        //
        // Deserialize search results
        //
        List<byte[]> indexedValues = Leb128Serializer.deserializeList(serializedSearchResults);

        //
        // Convert to Indexed Values list
        //
        return this.progress.list(indexedValues);
    }

}
