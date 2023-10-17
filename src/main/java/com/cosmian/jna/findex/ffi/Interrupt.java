package com.cosmian.jna.findex.ffi;

import com.cosmian.jna.findex.ffi.FindexNativeWrapper.InterruptCallback;
import com.cosmian.jna.findex.ffi.FindexUserCallbacks.SearchInterrupt;
import com.cosmian.jna.findex.serde.Leb128Reader;
import com.cosmian.utils.CloudproofException;
import com.sun.jna.Pointer;

public class Interrupt implements InterruptCallback {
    private SearchInterrupt interrupt;

    public Interrupt(SearchInterrupt interrupt) {
        this.interrupt = interrupt;
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
        ProgressResults results = new Leb128Reader(serializedSearchResults).readObject(ProgressResults.class);

        //
        // Convert to Indexed Values list
        //
        return this.interrupt.notify(results);
    }

}
