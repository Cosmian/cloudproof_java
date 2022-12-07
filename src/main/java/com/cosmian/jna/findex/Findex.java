package com.cosmian.jna.findex;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.cosmian.CloudproofException;
import com.cosmian.jna.abe.MasterKeys;
import com.cosmian.jna.findex.FindexWrapper.FetchAllEntryCallback;
import com.cosmian.jna.findex.FindexWrapper.FetchChainCallback;
import com.cosmian.jna.findex.FindexWrapper.FetchEntryCallback;
import com.cosmian.jna.findex.FindexWrapper.ListRemovedLocationsCallback;
import com.cosmian.jna.findex.FindexWrapper.ProgressCallback;
import com.cosmian.jna.findex.FindexWrapper.UpdateLinesCallback;
import com.cosmian.jna.findex.FindexWrapper.UpsertChainCallback;
import com.cosmian.jna.findex.FindexWrapper.UpsertEntryCallback;
import com.cosmian.jna.findex.serde.Leb128CollectionsSerializer;
import com.cosmian.jna.findex.serde.Leb128Reader;
import com.cosmian.jna.findex.serde.Leb128Serializable;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.jna.Memory;
import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.ptr.IntByReference;

public final class Findex {

    static final FindexWrapper INSTANCE = (FindexWrapper) Native.load("cosmian_findex", FindexWrapper.class);

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
    public static <K extends com.cosmian.jna.findex.serde.Leb128Serializable, V extends Leb128Serializable> int mapToOutputPointer(Map<K, V> map,
                                                                                                                                   Pointer output,
                                                                                                                                   IntByReference outputSize)
        throws CloudproofException {
        if (map.size() > 0) {
            byte[] uidsAndValuesBytes = Leb128CollectionsSerializer.serializeMap(map);
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

    public static void upsert(
                              byte[] key,
                              byte[] label,
                              HashMap<IndexedValue, Word[]> indexedValuesAndWords,
                              FetchEntryCallback fetchEntry,
                              UpsertEntryCallback upsertEntry,
                              UpsertChainCallback upsertChain)
        throws CloudproofException {

        // For the JSON strings
        ObjectMapper mapper = new ObjectMapper();

        try (
            final Memory keyPointer = new Memory(key.length);
            final Memory labelPointer = new Memory(label.length)) {
            keyPointer.write(0, key, 0, key.length);
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
                throw new CloudproofException("Invalid indexed values and words", e);
            }

            // Indexes creation + insertion/update
            unwrap(Findex.INSTANCE.h_upsert(
                keyPointer, key.length,
                labelPointer, label.length,
                indexedValuesAndWordsJson,
                fetchEntry,
                upsertEntry,
                upsertChain));
        }
    }

    public static void graph_upsert(MasterKeys masterKeys,
                                    byte[] label,
                                    HashMap<IndexedValue, Word[]> indexedValuesAndWords,
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
                                            Word[] words,
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
            for (Word word : words) {
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

            return Leb128Reader.deserializeList(IndexedValue.class, indexedValuesBytes);
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
