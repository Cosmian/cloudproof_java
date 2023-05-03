package com.cosmian.jna.findex;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Base64;
import java.util.Base64.Encoder;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.Set;

import com.cosmian.jna.findex.ffi.FindexNativeWrapper;
import com.cosmian.jna.findex.structs.IndexedValue;
import com.cosmian.jna.findex.structs.Keyword;
import com.cosmian.jna.findex.structs.ToIndexedValue;
import com.cosmian.utils.CloudproofException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.jna.Native;
import com.sun.jna.ptr.IntByReference;

public class FindexBase {
    static final FindexNativeWrapper INSTANCE =
        (FindexNativeWrapper) Native.load("cloudproof_findex", FindexNativeWrapper.class);

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
        if (FindexBase.INSTANCE.get_last_error(output, outputSize) == 0) {
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
        unwrap(FindexBase.INSTANCE.set_error(error_msg));
    }

    protected static String indexedValuesToJson(Map<IndexedValue, Set<Keyword>> indexedValuesAndWords)
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

    protected static String keywordsToJson(Set<Keyword> keyWords) throws CloudproofException {
        // For the JSON strings
        ObjectMapper mapper = new ObjectMapper();

        // Findex words
        Base64.Encoder encoder = Base64.getEncoder();
        String[] wordsString = new String[keyWords.size()];
        int i = 0;
        for (Keyword keyword : keyWords) {
            wordsString[i++] = encoder.encodeToString(keyword.getBytes());
        }

        try {
            return mapper.writeValueAsString(wordsString);
        } catch (JsonProcessingException e) {
            throw new CloudproofException("Invalid words", e);
        }
    }

    /**
     * If the result of the last FFI call is in Error, recover the last error from the native code and throw an
     * exception wrapping it.
     *
     * @param errorCode the result of the FFI call
     * @param start the start timestamp of the FFI call (used for callback exception handling)
     * @throws CloudproofException in case of native library error
     */
    protected static void unwrap(int errorCode, long start) throws CloudproofException {
        FindexCallbackException.rethrowOnErrorCode(errorCode, start, System.currentTimeMillis());

        unwrap(errorCode);
    }

    /**
     * If the result of the last FFI call is in Error, recover the last error from the native code and throw an
     * exception wrapping it.
     *
     * @param errorCode the result of the FFI call
     * @throws CloudproofException in case of native library error
     */
    protected static void unwrap(int errorCode) throws CloudproofException {
        if (errorCode != 0) {
            throw new CloudproofException(get_last_error(4095));
        }
    }

    static abstract protected class SearchRequest<SELF extends SearchRequest<SELF>> {
        protected byte[] label;

        protected Set<Keyword> keywords;

        abstract SELF self();

        public SELF keywords(Set<Keyword> keywords) {
            this.keywords = keywords;
            return self();
        }

        public SELF keywords(String[] keywords) {
            this.keywords =
                Stream.of(keywords).map(keyword -> new Keyword(keyword)).collect(Collectors.toCollection(HashSet::new));
            return self();
        }
    }

    static abstract protected class IndexRequest<SELF extends IndexRequest<SELF>> {
        protected byte[] label;

        protected Map<IndexedValue, Set<Keyword>> indexedValuesAndWords = new HashMap<>();

        abstract SELF self();

        public SELF add(Map<? extends ToIndexedValue, Set<Keyword>> indexedValuesAndWords) {
            for (Map.Entry<? extends ToIndexedValue, Set<Keyword>> entry : indexedValuesAndWords.entrySet()) {
                add(entry.getKey(), entry.getValue());
            }
            return self();
        }

        public SELF add(ToIndexedValue toIndexedValue,
                        Set<Keyword> keywords) {
            Set<Keyword> existingKeywords =
                indexedValuesAndWords.get(toIndexedValue.toIndexedValue());

            if (existingKeywords == null) {
                indexedValuesAndWords.put(toIndexedValue.toIndexedValue(), keywords);
            } else {
                existingKeywords.addAll(keywords);
            }

            return self();
        }

        public SELF add(ToIndexedValue toIndexedValue,
                        String[] keywords) {
            return add(
                toIndexedValue.toIndexedValue(),
                Stream.of(keywords).map(keyword -> new Keyword(keyword))
                    .collect(Collectors.toCollection(HashSet::new)));
        }
    }

}
