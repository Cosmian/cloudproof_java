package com.cosmian.jna.findex;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.cosmian.jna.findex.ffi.FindexNativeWrapper.FetchCallback;
import com.cosmian.jna.findex.ffi.FindexNativeWrapper.UpsertCallback;
import com.cosmian.jna.findex.ffi.FindexNativeWrapper.InsertCallback;
import com.cosmian.jna.findex.ffi.FindexNativeWrapper.DeleteCallback;
import com.cosmian.jna.findex.ffi.FindexNativeWrapper.DumpTokensCallback;

import com.cosmian.jna.findex.ffi.SearchResults;
import com.cosmian.jna.findex.ffi.KeywordSet;
import com.cosmian.jna.findex.serde.Leb128Reader;
import com.cosmian.jna.findex.serde.Leb128Writer;
import com.cosmian.jna.findex.structs.IndexedValue;
import com.cosmian.jna.findex.structs.Keyword;
import com.cosmian.utils.CloudproofException;
import com.sun.jna.Memory;
import com.sun.jna.ptr.IntByReference;

public final class Findex extends FindexBase {

    static int HANDLE;

    FetchCallback entryFetcher;
    FetchCallback chainFetcher;
    UpsertCallback entryUpserter;
    InsertCallback chainInserter;
    DeleteCallback entryDeleter;
    DeleteCallback chainDeleter;
    DumpTokensCallback entryDumper;

    public void instantiateCustomBackends(byte[] key,
            byte[] label,
            int entryTableNumber,
            EntryTableDatabase entryTable,
            ChainTableDatabase chainTable)
            throws CloudproofException
    {
	    final Memory keyPointer = new Memory(key.length);
	    final Memory labelPointer = new Memory(label.length);
	    keyPointer.write(0, key, 0, key.length);
	    labelPointer.write(0, label, 0, label.length);
	    IntByReference handle = new IntByReference();

	    entryFetcher = entryTable.fetchCallback();
	    chainFetcher = chainTable.fetchCallback();
	    entryUpserter = entryTable.upsertCallback();
	    chainInserter = chainTable.insertCallback();
	    entryDeleter = entryTable.deleteCallback();
	    chainDeleter = chainTable.deleteCallback();
	    entryDumper = entryTable.dumpTokenCallback();

	    unwrap(System.currentTimeMillis(), INSTANCE.h_instantiate_with_ffi_backend(handle,
				    keyPointer, key.length,
				    labelPointer, label.length,
				    entryTableNumber,
				    entryFetcher,
				    chainFetcher,
				    entryUpserter,
				    chainInserter,
				    entryDeleter,
				    chainDeleter,
				    entryDumper));

	    HANDLE = handle.getValue();
    }

    //----------------------------------------------------------------//
    //                       Addition                                 //
    //----------------------------------------------------------------//

    public KeywordSet add(Map<IndexedValue, Set<Keyword>> additions)
            throws CloudproofException
        {

            byte[] additionsBytes = Leb128Writer.serializeMapOfSet(additions);
            final Memory additionsPointer = new Memory(additionsBytes.length);
            additionsPointer.write(0, additionsBytes, 0, additionsBytes.length);

            // Do not allocate memory. The Rust FFI function will directly
            // return after setting newKeywordsBufferSize to an upper bound on
            // the amount of memory to allocate.
            byte[] newKeywordsBuffer = new byte[0];
            IntByReference newKeywordsBufferSize = new IntByReference();

	    long start = System.currentTimeMillis();
            int ffiCode = INSTANCE.h_add(newKeywordsBuffer, newKeywordsBufferSize,
                    HANDLE,
                    additionsPointer, additionsBytes.length);

	    if (ffiCode == 1) {
		    newKeywordsBuffer = new byte[newKeywordsBufferSize.getValue()];
		    unwrap(System.currentTimeMillis(), INSTANCE.h_add(newKeywordsBuffer, newKeywordsBufferSize,
					HANDLE, additionsPointer, additionsBytes.length));
	    } else {
		    unwrap(start, ffiCode);
	    }

	    byte[] newKeywordsBytes = Arrays.copyOfRange(newKeywordsBuffer, 0, newKeywordsBufferSize.getValue());
	    return new Leb128Reader(newKeywordsBytes).readObject(KeywordSet.class);
        }

    //----------------------------------------------------------------//
    //                       Deletion                                 //
    //----------------------------------------------------------------//

    public KeywordSet deletion(Map<IndexedValue, Set<Keyword>> deletions)
            throws CloudproofException
        {
            byte[] deletionsBytes = Leb128Writer.serializeMapOfSet(deletions);
            final Memory deletionsPointer = new Memory(deletionsBytes.length);
            deletionsPointer.write(0, deletionsBytes, 0, deletionsBytes.length);

            // Do not allocate memory. The Rust FFI function will directly
            // return after setting newKeywordsBufferSize to an upper bound on
            // the amount of memory to allocate.
            byte[] newKeywordsBuffer = new byte[0];
            IntByReference newKeywordsBufferSize = new IntByReference();

	    long start = System.currentTimeMillis();
            int ffiCode = INSTANCE.h_delete(newKeywordsBuffer, newKeywordsBufferSize,
                    HANDLE,
                    deletionsPointer, deletionsBytes.length);


	    if (ffiCode == 1) {
		    newKeywordsBuffer = new byte[newKeywordsBufferSize.getValue()];
		    unwrap(System.currentTimeMillis(),
				    INSTANCE.h_delete(newKeywordsBuffer, newKeywordsBufferSize,
					    HANDLE,
					    deletionsPointer, deletionsBytes.length));
	    } else {
		    unwrap(start, ffiCode);
	    }

            byte[] newKeywordsBytes = Arrays.copyOfRange(newKeywordsBuffer, 0, newKeywordsBufferSize.getValue());
            return new Leb128Reader(newKeywordsBytes).readObject(KeywordSet.class);
        }

    //----------------------------------------------------------------//
    //                       Search                                   //
    //----------------------------------------------------------------//

    public SearchResults search(Set<Keyword> keywords, Interrupt interrupt) throws CloudproofException
    {
	    byte[] serializedKeywords = Leb128Writer.serializeCollection(keywords);
	    final Memory keywordsPointer = new Memory(serializedKeywords.length);
	    keywordsPointer.write(0, serializedKeywords, 0, serializedKeywords.length);

	    byte[] searchResultsBuffer = new byte[131072];
	    IntByReference searchResultsBufferSize = new IntByReference(searchResultsBuffer.length);

	    long start = System.currentTimeMillis();
	    int ffiCode = INSTANCE.h_search(
			    searchResultsBuffer, searchResultsBufferSize,
			    HANDLE,
			    keywordsPointer, serializedKeywords.length,
			    interrupt);

	    if (ffiCode == 1) {
		    searchResultsBuffer = new byte[searchResultsBufferSize.getValue()];
		    unwrap(System.currentTimeMillis(), INSTANCE.h_search(searchResultsBuffer,
					    searchResultsBufferSize,
					    HANDLE,
					    keywordsPointer,
					    serializedKeywords.length,
					    interrupt));
	    } else {
		    unwrap(start, ffiCode);
	    }

	    byte[] indexedValuesBytes = Arrays.copyOfRange(searchResultsBuffer, 0, searchResultsBufferSize.getValue());
	    return new Leb128Reader(indexedValuesBytes).readObject(SearchResults.class);
    }

    public SearchResults search(Set<Keyword> keywords) throws CloudproofException {
        return search(keywords, new Interrupt() {});
    }

    public SearchResults search(String[] keywords) throws CloudproofException {
		return search(Stream.of(keywords).map(keyword -> new Keyword(keyword))
				.collect(Collectors.toCollection(HashSet::new)),
				new Interrupt() { });
    }

    public SearchResults search(String[] keywords, Interrupt interrupt) throws CloudproofException {
		return search(Stream.of(keywords).map(keyword -> new Keyword(keyword))
				.collect(Collectors.toCollection(HashSet::new)),
				interrupt);
    }

    //----------------------------------------------------------------//
    //                         Compact                                //
    //----------------------------------------------------------------//

    /// `nCompactToFull`: if you compact the indexes every night
    /// this is the number of days to wait before be sure that a big portion of the
    /// indexes were checked.
    /// (see the coupon problem to understand why it's not 100% sure)
    public void compact(byte[] newKey, byte[] newLabel, int numCompactToFull, FilterLocations filter)
		    throws CloudproofException
	    {
		    final Memory newKeyPtr = new Memory(newKey.length);
		    newKeyPtr.write(0, newKey, 0, newKey.length);
		    final Memory newLabelPtr = new Memory(newLabel.length);
		    newLabelPtr.write(0, newLabel, 0, newLabel.length);

		    long start = System.currentTimeMillis();
		    unwrap(start, INSTANCE.h_compact(HANDLE,
					    newKeyPtr, newKey.length,
					    newLabelPtr, newLabel.length,
					    numCompactToFull,
					    filter));
	    }

    public void compact(byte[] newKey, byte[] newLabel, int numCompactToFull) throws CloudproofException {
        compact(newKey, newLabel, numCompactToFull, new FilterLocations() { });
    }
}
