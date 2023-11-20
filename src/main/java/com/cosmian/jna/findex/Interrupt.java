package com.cosmian.jna.findex;

import java.util.Map;
import java.util.Set;

import com.cosmian.jna.findex.ffi.FindexNativeWrapper.InterruptCallback;
import com.cosmian.jna.findex.ffi.IntermediateResults;
import com.cosmian.jna.findex.serde.Leb128Reader;
import com.cosmian.jna.findex.structs.IndexedValue;
import com.cosmian.jna.findex.structs.Keyword;
import com.cosmian.utils.CloudproofException;
import com.sun.jna.Pointer;

public interface Interrupt extends InterruptCallback {
    /**
     * Function called at each recursion level of the search operation. It is fed with the results from the current
     * recursion level. The search is interrupted is true is returned.
     *
     * @param intermediateResults a {@link Map} of {@link Keyword} to {@link Set} of {@link IndexedValue}
     * @return true to interrupt the search
     * @throws CloudproofException if anything goes wrong
     */
    default boolean interrupt(Map<Keyword, Set<IndexedValue>> intermediateResults) throws CloudproofException {
        return false;
    }

    @Override
    default boolean callback(Pointer intermediateResultsPointer,
                             int intermediateResultsLength) {
        try {
            //
            // Read `searchResultsPointer` until `searchResultsLength`
            //
            byte[] serializedSearchResults = new byte[intermediateResultsLength];
            intermediateResultsPointer.read(0, serializedSearchResults, 0, intermediateResultsLength);

            //
            // Deserialize search results
            //
            IntermediateResults results =
                new Leb128Reader(serializedSearchResults).readObject(IntermediateResults.class);

            //
            // Convert to Indexed Values list
            //
            return interrupt(results.getResults());

        } catch (CloudproofException e) {
            FindexCallbackException.record(e);
            return true; // Interrupt search upon error
        }
    }
}
