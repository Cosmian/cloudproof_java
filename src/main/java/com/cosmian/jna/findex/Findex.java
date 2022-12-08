package com.cosmian.jna.findex;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Base64;
import java.util.Base64.Encoder;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.cosmian.jna.covercrypt.structs.MasterKeys;
import com.cosmian.jna.findex.ffi.FindexNativeWrapper;
import com.cosmian.jna.findex.ffi.FindexNativeWrapper.FetchAllEntryCallback;
import com.cosmian.jna.findex.ffi.FindexNativeWrapper.FetchChainCallback;
import com.cosmian.jna.findex.ffi.FindexNativeWrapper.FetchEntryCallback;
import com.cosmian.jna.findex.ffi.FindexNativeWrapper.ListRemovedLocationsCallback;
import com.cosmian.jna.findex.ffi.FindexNativeWrapper.ProgressCallback;
import com.cosmian.jna.findex.ffi.FindexNativeWrapper.UpdateLinesCallback;
import com.cosmian.jna.findex.ffi.FindexNativeWrapper.UpsertChainCallback;
import com.cosmian.jna.findex.ffi.FindexNativeWrapper.UpsertEntryCallback;
import com.cosmian.jna.findex.serde.Leb128Reader;
import com.cosmian.jna.findex.serde.Leb128Serializable;
import com.cosmian.jna.findex.serde.Leb128Writer;
import com.cosmian.jna.findex.structs.IndexedValue;
import com.cosmian.jna.findex.structs.Keyword;
import com.cosmian.jna.findex.structs.NextKeyword;
import com.cosmian.utils.CloudproofException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.jna.Memory;
import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.ptr.IntByReference;

public final class Findex {

    static final FindexNativeWrapper INSTANCE =
        (FindexNativeWrapper) Native.load("cosmian_findex", FindexNativeWrapper.class);

    /**
     * Return the last error in a String that does not exceed 1023 bytes
     *
     * @return the last error recorded by the native library
     * @throws CloudproofException in case of native library error
     */
    public static String get_last_error() throws CloudproofException {
        return get_last_error(1023);
    }

    /**
     * Return the last error in a String that does not exceed `max_len` bytes
     *
     * @param maxLen the maximum number of bytes to return
     * @throws CloudproofException in case of native library error
     * @return the error
     */
    public static String get_last_error(int maxLen) throws CloudproofException {
        if (maxLen < 1) {
            throw new CloudproofException("get_last_error: maxLen must be at least one");
        }
        byte[] output = new byte[maxLen + 1];
        IntByReference outputSize = new IntByReference(output.length);
        if (Findex.INSTANCE.get_last_error(output, outputSize) == 0) {
            return new String(Arrays.copyOfRange(output, 0, outputSize.getValue()), StandardCharsets.UTF_8);
        }
        throw new CloudproofException("Failed retrieving the last error; check the debug logs");
    }

    /**
     * Set the last error on the native lib
     *
     * @param error_msg the last error to set on the native lib
     * @throws CloudproofException n case of native library error
     */
    public static void set_error(String error_msg) throws CloudproofException {
        unwrap(Findex.INSTANCE.set_error(error_msg));
    }

    /**
     * Serialize a map to a memory location specified by the Pointer; set its actual size in the pointed int.
     * 
     * @param <K> the map key type. Must be
     *            {@link com.cosmian.jna.findex.serde.Leb128CollectionsSerializer.Leb128Serializable}
     * @param <V> the map value type. Must be
     *            {@link com.cosmian.jna.findex.serde.Leb128CollectionsSerializer.Leb128Serializable}
     * @param map the map to serialize and export
     * @param output the output Pointer
     * @param outputSize the output byte size
     * @return 0 on success, 1 if the pre-allocated memory is too small. The outputSized contains the required size to
     *         hold the map.
     * @throws CloudproofException
     */
    public static <K extends Leb128Serializable, V extends Leb128Serializable> int mapToOutputPointer(Map<K, V> map,
                                                                                                      Pointer output,
                                                                                                      IntByReference outputSize)
        throws CloudproofException {
        byte[] uidsAndValuesBytes = Leb128Writer.serializeMap(map);
        if (outputSize.getValue() < uidsAndValuesBytes.length) {
            outputSize.setValue(uidsAndValuesBytes.length);
            return 1;
        }
        outputSize.setValue(uidsAndValuesBytes.length);
        output.write(0, uidsAndValuesBytes, 0, uidsAndValuesBytes.length);
        return 0;
    }

    public static void upsert(
                              byte[] key,
                              byte[] label,
                              Map<IndexedValue, Set<Keyword>> indexedValuesAndWords,
                              Database db)
        throws CloudproofException {

        try (
            final Memory keyPointer = new Memory(key.length);
            final Memory labelPointer = new Memory(label.length)) {
            keyPointer.write(0, key, 0, key.length);
            labelPointer.write(0, label, 0, label.length);

            // Indexes creation + insertion/update
            unwrap(Findex.INSTANCE.h_upsert(
                keyPointer, key.length,
                labelPointer, label.length,
                indexedValuesToJson(indexedValuesAndWords),
                db.fetchEntryCallback(),
                db.upsertEntryCallback(),
                db.upsertChainCallback()));
        }
    }

    private static String indexedValuesToJson(Map<IndexedValue, Set<Keyword>> indexedValuesAndWords)
        throws CloudproofException {
        // For the JSON strings
        ObjectMapper mapper = new ObjectMapper();
        Encoder encoder = Base64.getEncoder();
        HashMap<String, String[]> indexedValuesAndWordsString = new HashMap<>();
        for (Entry<IndexedValue, Set<Keyword>> entry : indexedValuesAndWords.entrySet()) {
            String[] words = new String[entry.getValue().size()];
            int i = 0;
            for (Keyword word : entry.getValue()) {
                words[i++] = encoder.encodeToString(word.getBytes());
            }
            indexedValuesAndWordsString.put(
                encoder.encodeToString(entry.getKey().getBytes()),
                words);
        }

        String indexedValuesAndWordsJson;
        try {
            indexedValuesAndWordsJson = mapper.writeValueAsString(indexedValuesAndWordsString);
        } catch (JsonProcessingException e) {
            throw new CloudproofException("Invalid indexed values and words", e);
        }
        return indexedValuesAndWordsJson;
    }

    public static void graph_upsert(MasterKeys masterKeys,
                                    byte[] label,
                                    HashMap<IndexedValue, Set<Keyword>> indexedValuesAndWords,
                                    FetchEntryCallback fetchEntry,
                                    UpsertEntryCallback upsertEntry,
                                    UpsertChainCallback upsertChain)
        throws CloudproofException {

        // For the JSON strings
        ObjectMapper mapper = new ObjectMapper();

        // Findex master keys
        String masterKeysJson;
        try {
            masterKeysJson = mapper.writeValueAsString(masterKeys);
        } catch (JsonProcessingException e) {
            throw new CloudproofException("Invalid master keys", e);
        }

        try (final Memory labelPointer = new Memory(label.length)) {
            labelPointer.write(0, label, 0, label.length);

            // Findex indexed values and words
            HashMap<String, String[]> indexedValuesAndWordsString = new HashMap<>();
            for (Entry<IndexedValue, Set<Keyword>> entry : indexedValuesAndWords.entrySet()) {
                String[] words = new String[entry.getValue().size()];
                int i = 0;
                for (Keyword word : entry.getValue()) {
                    words[i++] = word.toString();
                }
                indexedValuesAndWordsString.put(entry.getKey().toString(), words);
            }

            String indexedValuesAndWordsJson;
            try {
                indexedValuesAndWordsJson = mapper.writeValueAsString(indexedValuesAndWordsString);
            } catch (JsonProcessingException e) {
                throw new CloudproofException("Invalid indexed values and words", e);
            }

            // Indexes creation + insertion/update
            unwrap(Findex.INSTANCE.h_graph_upsert(masterKeysJson, labelPointer, label.length, indexedValuesAndWordsJson,
                fetchEntry,
                upsertEntry, upsertChain));
        }
    }

    public static List<IndexedValue> search(byte[] keyK,
                                            byte[] label,
                                            NextKeyword[] words,
                                            int loopIterationLimit,
                                            int maxDepth,
                                            ProgressCallback progress,
                                            FetchEntryCallback fetchEntry,
                                            FetchChainCallback fetchChain)
        throws CloudproofException {
        //
        // Prepare outputs
        //
        // start with an arbitration buffer allocation size of 131072 (around 4096
        // indexedValues)
        byte[] indexedValuesBuffer = new byte[131072];
        IntByReference indexedValuesBufferSize = new IntByReference(indexedValuesBuffer.length);

        // For the JSON strings
        ObjectMapper mapper = new ObjectMapper();

        // Findex master keys
        if (keyK == null) {
            throw new CloudproofException("Key k cannot be null");
        }
        try (final Memory keyKeyPointer = new Memory(keyK.length);
            final Memory labelPointer = new Memory(label.length)) {
            keyKeyPointer.write(0, keyK, 0, keyK.length);

            labelPointer.write(0, label, 0, label.length);

            // Findex words
            String[] wordsString = new String[words.length];
            int i = 0;
            for (NextKeyword word : words) {
                wordsString[i++] = word.toString();
            }
            String wordsJson;
            try {
                wordsJson = mapper.writeValueAsString(wordsString);
            } catch (JsonProcessingException e) {
                throw new CloudproofException("Invalid words", e);
            }

            // Indexes creation + insertion/update
            int ffiCode = Findex.INSTANCE.h_search(indexedValuesBuffer, indexedValuesBufferSize, keyKeyPointer,
                keyK.length,
                labelPointer, label.length, wordsJson, loopIterationLimit, maxDepth, progress, fetchEntry,
                fetchChain);
            if (ffiCode != 0) {
                // Retry with correct allocated size
                indexedValuesBuffer = new byte[indexedValuesBufferSize.getValue()];
                ffiCode = Findex.INSTANCE.h_search(indexedValuesBuffer, indexedValuesBufferSize, keyKeyPointer,
                    keyK.length,
                    labelPointer, label.length, wordsJson, loopIterationLimit, maxDepth, progress, fetchEntry,
                    fetchChain);
                if (ffiCode != 0) {
                    throw new CloudproofException(get_last_error(4095));
                }
            }

            byte[] indexedValuesBytes = Arrays.copyOfRange(indexedValuesBuffer, 0, indexedValuesBufferSize.getValue());

            return Leb128Reader.deserializeCollection(IndexedValue.class, indexedValuesBytes);
        }
    }

    /// `number_of_reindexing_phases_before_full_set`: if you compact the indexes
    /// every night
    /// this is the number of days to wait before be sure that a big portion of the
    /// indexes were checked
    /// (see the coupon problem to understand why it's not 100% sure)
    public static void compact(int numberOfReindexingPhasesBeforeFullSet,
                               MasterKeys masterKeys,
                               byte[] label,
                               FetchEntryCallback fetchEntry,
                               FetchChainCallback fetchChain,
                               FetchAllEntryCallback fetchAllEntry,
                               UpdateLinesCallback updateLines,
                               ListRemovedLocationsCallback listRemovedLocations)
        throws CloudproofException {
        // For the JSON strings
        ObjectMapper mapper = new ObjectMapper();

        // Findex master keys
        String masterKeysJson;
        try {
            masterKeysJson = mapper.writeValueAsString(masterKeys);
        } catch (JsonProcessingException e) {
            throw new CloudproofException("Invalid master keys", e);
        }

        final Pointer labelPointer = new Memory(label.length);
        labelPointer.write(0, label, 0, label.length);

        // Indexes creation + insertion/update
        unwrap(Findex.INSTANCE.h_compact(numberOfReindexingPhasesBeforeFullSet, masterKeysJson, labelPointer,
            label.length,
            fetchEntry, fetchChain, fetchAllEntry, updateLines, listRemovedLocations));
    }

    /**
     * If the result of the last FFI call is in Error, recover the last error from the native code and throw an
     * exception wrapping it.
     *
     * @param result the result of the FFI call
     * @throws CloudproofException in case of native library error
     */
    public static void unwrap(int result) throws CloudproofException {
        if (result == 1) {
            throw new CloudproofException(get_last_error(4095));
        }
    }

}
