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

    // In case a custom backend is created, objects holding the callbacks need
    // to be stored to prevent them from being garbage collected.
    FetchCallback entryFetcher;
    FetchCallback chainFetcher;
    UpsertCallback entryUpserter;
    InsertCallback chainInserter;
    DeleteCallback entryDeleter;
    DeleteCallback chainDeleter;
    DumpTokensCallback entryDumper;

    //----------------------------------------------------------------//
    //                       Instantiation                            //
    //----------------------------------------------------------------//

    /**
     * Instantiate Findex using a custom backend.
     * <p>
     * The implementation of both the Entry Table and the Chain Table passed as
     * arguments is used in to manipulate the index.
     *
     * @param key Findex key used to encrypt the index
     * @param label a public label used to allow compact operation without key
     * rotation
     * @param entryTableNumber the number of Entry Table used as backend
     * @param entryTable Entry Table implementation
     * @param chainTable Chain Table implementation
     * @throws {@link CloudproofException} if anything goes wrong
     */
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

    /**
     * Instantiate Findex using a REST backend.
     * <p>
     * All manipulation of indexes is delegated to a server implementing the
     * Findex REST API.
     *
     * @param label a public label used to allow compact operation without key
     * rotation
     * @param token token used for authentication to the Findex REST server
     * @param url URL of the Findex REST server
     * @throws {@link CloudproofException} if anything goes wrong
     */
    public void instantiateRestBackend(byte[] label, String token, String url)
            throws CloudproofException
    {
	    final Memory labelPointer = new Memory(label.length);
	    labelPointer.write(0, label, 0, label.length);

	    IntByReference handle = new IntByReference();

	    unwrap(System.currentTimeMillis(),
		   INSTANCE.h_instantiate_with_rest_backend(handle,
							    labelPointer, label.length,
							    token, url));
	    HANDLE = handle.getValue();
    }

    //----------------------------------------------------------------//
    //                       Addition                                 //
    //----------------------------------------------------------------//

    /**
     * Add the given associations to the index.
     *
     * @param associations {@link Map} of {@link IndexedValue} to {@link Set} of {@link Keyword}
     * @return the {@link KeywordSet} of new keywords added to the index
     * @throws {@link CloudproofException} if anything goes wrong
     */
    public KeywordSet add(Map<IndexedValue, Set<Keyword>> associations)
            throws CloudproofException
        {

            byte[] additionsBytes = Leb128Writer.serializeMapOfSet(associations);
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

    /**
     * Remove the given associations from the index.
     *
     * @param associations {@link Map} of {@link IndexedValue} to {@link Set} of {@link Keyword}
     * @return the {@link KeywordSet} of new keywords added to the index
     * @throws {@link CloudproofException} if anything goes wrong
     */
    public KeywordSet deletion(Map<IndexedValue, Set<Keyword>> associations)
            throws CloudproofException
        {
            byte[] deletionsBytes = Leb128Writer.serializeMapOfSet(associations);
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

    /**
     * Search the index for the given keywords.
     *
     * @param keywords a {@link Set} of {@link Keyword} to search
     * @param interrupt an implementation of the {@link Interrupt} interface
     * @return the {@link SearchResults}
     * @throws {@link CloudproofException} if anything goes wrong
     */
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

    /**
     * Search the index for the given keywords, without interruption.
     *
     * @param keywords a {@link Set} of {@link Keyword} to search
     * @return the {@link SearchResults}
     * @throws {@link CloudproofException} if anything goes wrong
     */
    public SearchResults search(Set<Keyword> keywords) throws CloudproofException {
        return search(keywords, new Interrupt() {});
    }

    /**
     * Search the index for the given keywords, without interruption.
     *
     * @param keywords an array of {@link String} representing the keywords to search
     * @return the {@link SearchResults}
     * @throws {@link CloudproofException} if anything goes wrong
     */
    public SearchResults search(String[] keywords) throws CloudproofException {
		return search(Stream.of(keywords).map(keyword -> new Keyword(keyword))
				.collect(Collectors.toCollection(HashSet::new)),
				new Interrupt() { });
    }

    /**
     * Search the index for the given keywords.
     *
     * @param keywords an array of {@link String} representing the keywords to search
     * @param interrupt an implementation of the {@link Interrupt} interface
     * @return the {@link SearchResults}
     * @throws {@link CloudproofException} if anything goes wrong
     */
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
    /**
     * Compact the index.
     * <p>
     * At least one of the Findex key or label needs to be changed during this
     * operation.
     *
     * @param newKey key to use as replacement to the current Findex key.
     * @param newLabel label to use as replacement to the current Findex label.
     * @param numCompactToFull number of compact operation to run before going throught the entire Chain Table (on average).
     * @param filter implementation of the {@link FilterLocations} interface
     * @throws {@link CloudproofException} if anything goes wrong
     */
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

    /**
     * Compact the index.
     * <p>
     * At least one of the Findex key or label needs to be changed during this
     * operation.
     * <p>
     * No filter is applied on the list of locations collected during the compact
     * operation.
     *
     * @param newKey key to use as replacement to the current Findex key.
     * @param newLabel label to use as replacement to the current Findex label.
     * @param numCompactToFull number of compact operation to run before going throught the entire Chain Table (on average).
     * @throws {@link CloudproofException} if anything goes wrong
     */
    public void compact(byte[] newKey, byte[] newLabel, int numCompactToFull) throws CloudproofException {
        compact(newKey, newLabel, numCompactToFull, new FilterLocations() { });
    }
}
