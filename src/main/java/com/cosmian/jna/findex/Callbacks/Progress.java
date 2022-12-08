package com.cosmian.jna.findex.Callbacks;

import java.util.List;

import com.cosmian.CloudproofException;
import com.cosmian.jna.findex.FindexWrapper.ProgressCallback;
import com.cosmian.jna.findex.FindexWrapper.ProgressInterface;
import com.cosmian.jna.findex.IndexedValue;
import com.cosmian.jna.findex.serde.Leb128Reader;
import com.sun.jna.Pointer;

public class Progress implements ProgressCallback {
    private ProgressInterface progress;

    public Progress(ProgressInterface progress) {
        this.progress = progress;
    }

    @Override
    public boolean apply(Pointer searchResultsPointer,
                         int searchResultsLength)
        throws CloudproofException {
        //
        // Read `searchResultsPointer` until `searchResultsLength`
        //
        byte[] serializedSearchResults = new byte[searchResultsLength];
        searchResultsPointer.read(0, serializedSearchResults, 0, searchResultsLength);

        //
        // Deserialize search results
        //
        List<IndexedValue> indexedValues =
            Leb128Reader.deserializeCollection(IndexedValue.class, serializedSearchResults);

        //
        // Convert to Indexed Values list
        //
        return this.progress.list(indexedValues);
    }

}
