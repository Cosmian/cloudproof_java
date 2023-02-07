package com.cosmian.jna.findex.ffi;


import com.cosmian.jna.findex.ffi.FindexNativeWrapper.ProgressCallback;
import com.cosmian.jna.findex.ffi.FindexUserCallbacks.SearchProgress;
import com.cosmian.jna.findex.serde.Leb128Reader;
import com.cosmian.utils.CloudproofException;
import com.sun.jna.Pointer;

public class Progress implements ProgressCallback {
    private SearchProgress progress;

    public Progress(SearchProgress progress) {
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
        ProgressResults indexedValues = new Leb128Reader(serializedSearchResults).readObject(ProgressResults.class);

        //
        // Convert to Indexed Values list
        //
        return this.progress.notify(indexedValues);
    }

}
