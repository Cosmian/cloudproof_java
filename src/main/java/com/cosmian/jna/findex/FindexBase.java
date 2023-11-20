package com.cosmian.jna.findex;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.cosmian.jna.findex.ffi.FindexNativeWrapper;
import com.cosmian.jna.findex.structs.IndexedValue;
import com.cosmian.jna.findex.structs.Keyword;
import com.cosmian.jna.findex.structs.ToIndexedValue;
import com.cosmian.utils.CloudproofException;
import com.sun.jna.Native;
import com.sun.jna.ptr.IntByReference;

public class FindexBase {
    static final FindexNativeWrapper INSTANCE =
        (FindexNativeWrapper) Native.load("cloudproof", FindexNativeWrapper.class);

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
     */
    public static void set_error(String error_msg) {
        FindexBase.INSTANCE.set_error(error_msg);
    }

    /**
     * If the result of the last FFI call is in Error, recover the last error from the native code and throw an
     * exception wrapping it.
     *
     * @param errorCode the result of the FFI call
     * @param start the start timestamp of the FFI call (used for callback exception handling)
     * @throws CloudproofException in case of native library error
     */
    protected static void unwrap(long start,
                                 int errorCode)
        throws CloudproofException { // Parameters are evaluated
        // left to right:
        // accepting the time
        // parameter first
        // allows getting the
        // current time before
        // evaluating the
        // expression resulting
        // in the error code
        // when both are passed
        // as arguments.
        if (errorCode != 0) {
            FindexCallbackException.rethrowOnErrorCode(errorCode, start, System.currentTimeMillis());
            throw new CloudproofException(get_last_error(4095));
        }
    }

    static abstract protected class SearchRequest<SELF extends SearchRequest<SELF>> {
        protected byte[] label;

        protected Set<Keyword> keywords;

        protected int entryTableNumber = 1;

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

        public SELF setEntryTableNumber(int entryTableNumber) {
            this.entryTableNumber = entryTableNumber;
            return self();
        }
    }

    static abstract protected class IndexRequest<SELF extends IndexRequest<SELF>> {
        protected byte[] label;

        protected Map<IndexedValue, Set<Keyword>> additions = new HashMap<>();

        protected Map<IndexedValue, Set<Keyword>> deletions = new HashMap<>();

        protected int entryTableNumber = 1;

        abstract SELF self();

        public SELF add(Map<? extends ToIndexedValue, Set<Keyword>> additions) {
            for (Map.Entry<? extends ToIndexedValue, Set<Keyword>> entry : additions.entrySet()) {
                add(entry.getKey(), entry.getValue());
            }
            return self();
        }

        public SELF add(ToIndexedValue toIndexedValue,
                        Set<Keyword> keywords) {
            Set<Keyword> existingKeywords =
                additions.get(toIndexedValue.toIndexedValue());

            if (existingKeywords == null) {
                additions.put(toIndexedValue.toIndexedValue(), keywords);
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
