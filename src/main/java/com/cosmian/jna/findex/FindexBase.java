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
import com.cosmian.utils.CloudproofException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.jna.Native;
import com.sun.jna.ptr.IntByReference;

public class FindexBase {
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

<<<<<<< HEAD
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

=======
>>>>>>> 5dcc9e4 (WIP)
    /**
     * If the result of the last FFI call is in Error, recover the last error from the native code and throw an
     * exception wrapping it.
     *
     * @param result the result of the FFI call
     * @throws CloudproofException in case of native library error
     */
    protected static void unwrap(int result) throws CloudproofException {
        if (result == 1) {
            throw new CloudproofException(get_last_error(4095));
        }
    }

    static abstract public class SearchParams<SELF extends SearchParams<SELF>> {
        protected byte[] label;

        protected Set<Keyword> keywords;

        protected int maxResultsPerKeyword = 0;

        protected int maxDepth = -1;

        protected int insecureFetchChainsBatchSize = 0;

        abstract SELF self();

        public SELF label(byte[] label) {
            this.label = label;
            return self();
        }

        public SELF label(String label) {
            return this.label(label.getBytes(StandardCharsets.UTF_8));
        }

        public SELF keywords(Set<Keyword> keywords) {
            this.keywords = keywords;
            return self();
        }

        public SELF keywords(String[] keywords) {
            this.keywords =
                Stream.of(keywords).map(keyword -> new Keyword(keyword)).collect(Collectors.toCollection(HashSet::new));
            return self();
        }

        public SELF maxResultsPerKeyword(int maxResultsPerKeyword) {
            this.maxResultsPerKeyword = maxResultsPerKeyword;
            return self();
        }

        public SELF maxDepth(int maxDepth) {
            this.maxDepth = maxDepth;
            return self();
        }

        public SELF insecureFetchChainsBatchSize(int insecureFetchChainsBatchSize) {
            this.insecureFetchChainsBatchSize = insecureFetchChainsBatchSize;
            return self();
        }
    }

}
