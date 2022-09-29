package com.cosmian.jna.findex;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import com.cosmian.CosmianException;
import com.cosmian.jna.FfiException;
import com.cosmian.jna.findex.FfiWrapper.FetchAllEntryCallback;
import com.cosmian.jna.findex.FfiWrapper.FetchChainCallback;
import com.cosmian.jna.findex.FfiWrapper.FetchEntryCallback;
import com.cosmian.jna.findex.FfiWrapper.ListRemovedLocationsCallback;
import com.cosmian.jna.findex.FfiWrapper.ProgressCallback;
import com.cosmian.jna.findex.FfiWrapper.UpdateLinesCallback;
import com.cosmian.jna.findex.FfiWrapper.UpsertChainCallback;
import com.cosmian.jna.findex.FfiWrapper.UpsertEntryCallback;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.jna.Memory;
import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.ptr.IntByReference;

public final class Ffi {

    static final FfiWrapper INSTANCE = (FfiWrapper) Native.load("cosmian_findex", FfiWrapper.class);

    /**
     * Return the last error in a String that does not exceed 1023 bytes
     *
     * @return the last error recorded by the native library
     * @throws FfiException in case of native library error
     */
    public static String get_last_error() throws FfiException {
        return get_last_error(1023);
    }

    /**
     * Return the last error in a String that does not exceed `max_len` bytes
     *
     * @param maxLen the maximum number of bytes to return
     * @throws FfiException in case of native library error
     * @return the error
     */
    public static String get_last_error(int maxLen) throws FfiException {
        if (maxLen < 1) {
            throw new FfiException("get_last_error: maxLen must be at least one");
        }
        byte[] output = new byte[maxLen + 1];
        IntByReference outputSize = new IntByReference(output.length);
        if (Ffi.INSTANCE.get_last_error(output, outputSize) == 0) {
            return new String(Arrays.copyOfRange(output, 0, outputSize.getValue()), StandardCharsets.UTF_8);
        }
        throw new FfiException("Failed retrieving the last error; check the debug logs");
    }

    /**
     * Set the last error on the native lib
     *
     * @param error_msg the last error to set on the native lib
     * @throws FfiException n case of native library error
     */
    public static void set_error(String error_msg) throws FfiException {
        unwrap(Ffi.INSTANCE.set_error(error_msg));
    }

    public static int writeOutputPointerAndSize(HashMap<byte[], byte[]> uidsAndValues, Pointer output,
        IntByReference outputSize) {
        if (uidsAndValues.size() > 0) {
            byte[] uidsAndValuesBytes = Leb128Serializer.serializeHashMap(uidsAndValues);
            if (outputSize.getValue() < uidsAndValuesBytes.length) {
                outputSize.setValue(uidsAndValuesBytes.length);
                return 1;
            }
            outputSize.setValue(uidsAndValuesBytes.length);
            output.write(0, uidsAndValuesBytes, 0, uidsAndValuesBytes.length);
        } else {
            outputSize.setValue(0);
        }
        return 0;
    }

    public static void upsert(MasterKeys masterKeys, byte[] label, HashMap<IndexedValue, Word[]> indexedValuesAndWords,
        FetchEntryCallback fetchEntry, UpsertEntryCallback upsertEntry, UpsertChainCallback upsertChain)
        throws FfiException, CosmianException {

        // For the JSON strings
        ObjectMapper mapper = new ObjectMapper();

        // Findex master keys
        String masterKeysJson;
        try {
            masterKeysJson = mapper.writeValueAsString(masterKeys);
        } catch (JsonProcessingException e) {
            throw new FfiException("Invalid master keys", e);
        }

        final Pointer labelPointer = new Memory(label.length);
        labelPointer.write(0, label, 0, label.length);

        // Findex indexed values and words
        HashMap<String, String[]> indexedValuesAndWordsString = new HashMap<>();
        for (Entry<IndexedValue, Word[]> entry : indexedValuesAndWords.entrySet()) {
            String[] words = new String[entry.getValue().length];
            int i = 0;
            for (Word word : entry.getValue()) {
                words[i++] = word.toString();
            }
            indexedValuesAndWordsString.put(entry.getKey().toString(), words);
        }

        String indexedValuesAndWordsJson;
        try {
            indexedValuesAndWordsJson = mapper.writeValueAsString(indexedValuesAndWordsString);
        } catch (JsonProcessingException e) {
            throw new FfiException("Invalid indexed values and words", e);
        }

        // Indexes creation + insertion/update
        unwrap(Ffi.INSTANCE.h_upsert(masterKeysJson, labelPointer, label.length, indexedValuesAndWordsJson, fetchEntry,
            upsertEntry, upsertChain));
    }

    public static List<byte[]> search(byte[] keyK, byte[] label, Word[] words, int loopIterationLimit, int maxDepth,
        ProgressCallback progress, FetchEntryCallback fetchEntry, FetchChainCallback fetchChain)
        throws FfiException, CosmianException {
        //
        // Prepare outputs
        //
        // start with an arbitration buffer allocation size of 131072 (around 4096 indexedValues)
        byte[] indexedValuesBuffer = new byte[131072];
        IntByReference indexedValuesBufferSize = new IntByReference(indexedValuesBuffer.length);

        // For the JSON strings
        ObjectMapper mapper = new ObjectMapper();

        // Findex master keys
        if (keyK == null) {
            throw new FfiException("Key k cannot be null");
        }
        final Pointer keyKeyPointer = new Memory(keyK.length);
        keyKeyPointer.write(0, keyK, 0, keyK.length);

        final Pointer labelPointer = new Memory(label.length);
        labelPointer.write(0, label, 0, label.length);

        // Findex words
        String[] wordsString = new String[words.length];
        int i = 0;
        for (Word word : words) {
            wordsString[i++] = word.toString();
        }
        String wordsJson;
        try {
            wordsJson = mapper.writeValueAsString(wordsString);
        } catch (JsonProcessingException e) {
            throw new FfiException("Invalid words", e);
        }

        // Indexes creation + insertion/update
        int ffiCode = Ffi.INSTANCE.h_search(indexedValuesBuffer, indexedValuesBufferSize, keyKeyPointer, keyK.length,
            labelPointer, label.length, wordsJson, loopIterationLimit, maxDepth, progress, fetchEntry, fetchChain);
        if (ffiCode != 0) {
            // Retry with correct allocated size
            indexedValuesBuffer = new byte[indexedValuesBufferSize.getValue()];
            ffiCode = Ffi.INSTANCE.h_search(indexedValuesBuffer, indexedValuesBufferSize, keyKeyPointer, keyK.length,
                labelPointer, label.length, wordsJson, loopIterationLimit, maxDepth, progress, fetchEntry, fetchChain);
            if (ffiCode != 0) {
                throw new FfiException(get_last_error(4095));
            }
        }

        byte[] indexedValuesBytes = Arrays.copyOfRange(indexedValuesBuffer, 0, indexedValuesBufferSize.getValue());

        return Leb128Serializer.deserializeList(indexedValuesBytes);
    }

    /// `number_of_reindexing_phases_before_full_set`: if you compact the indexes every night
    /// this is the number of days to wait before be sure that a big portion of the indexes were checked
    /// (see the coupon problem to understand why it's not 100% sure)
    public static void compact(int numberOfReindexingPhasesBeforeFullSet, MasterKeys masterKeys, byte[] label,
        FetchEntryCallback fetchEntry, FetchChainCallback fetchChain, FetchAllEntryCallback fetchAllEntry,
        UpdateLinesCallback updateLines, ListRemovedLocationsCallback listRemovedLocations)
        throws FfiException, CosmianException {
        // For the JSON strings
        ObjectMapper mapper = new ObjectMapper();

        // Findex master keys
        String masterKeysJson;
        try {
            masterKeysJson = mapper.writeValueAsString(masterKeys);
        } catch (JsonProcessingException e) {
            throw new FfiException("Invalid master keys", e);
        }

        final Pointer labelPointer = new Memory(label.length);
        labelPointer.write(0, label, 0, label.length);

        // Indexes creation + insertion/update
        unwrap(Ffi.INSTANCE.h_compact(numberOfReindexingPhasesBeforeFullSet, masterKeysJson, labelPointer, label.length,
            fetchEntry, fetchChain, fetchAllEntry, updateLines, listRemovedLocations));
    }

    /**
     * If the result of the last FFI call is in Error, recover the last error from the native code and throw an
     * exception wrapping it.
     *
     * @param result the result of the FFI call
     * @throws FfiException in case of native library error
     */
    public static void unwrap(int result) throws FfiException {
        if (result == 1) {
            throw new FfiException(get_last_error(4095));
        }
    }

}
