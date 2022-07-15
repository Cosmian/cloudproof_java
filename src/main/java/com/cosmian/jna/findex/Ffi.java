package com.cosmian.jna.findex;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;

import com.cosmian.CosmianException;
import com.cosmian.jna.FfiException;
import com.cosmian.jna.findex.FfiWrapper.FetchChainCallback;
import com.cosmian.jna.findex.FfiWrapper.FetchEntryCallback;
import com.cosmian.jna.findex.FfiWrapper.UpsertChainCallback;
import com.cosmian.jna.findex.FfiWrapper.UpsertEntryCallback;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.jna.Native;
import com.sun.jna.ptr.IntByReference;

public final class Ffi {

    static final FfiWrapper INSTANCE = (FfiWrapper) Native.load("findex", FfiWrapper.class);

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

    public static void upsert(MasterKeys masterKeys, HashMap<String, String[]> dbUidsAndWords,
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

        // Findex db UIDS and words
        String dbUidsAndWordsJson;
        try {
            dbUidsAndWordsJson = mapper.writeValueAsString(dbUidsAndWords);
        } catch (JsonProcessingException e) {
            throw new FfiException("Invalid db uids and words", e);
        }

        // Indexes creation + insertion/update
        unwrap(Ffi.INSTANCE.h_upsert(masterKeysJson, dbUidsAndWordsJson, fetchEntry, upsertEntry, upsertChain));
    }

    public static String[] search(MasterKeys masterKeys, String[] words, int loopIterationLimit,
        FetchEntryCallback fetchEntry, FetchChainCallback fetchChain) throws FfiException, CosmianException {
        //
        // Prepare outputs
        //
        byte[] dbUidsBuffer = new byte[8192];
        IntByReference dbUidsBufferSize = new IntByReference(dbUidsBuffer.length);

        // For the JSON strings
        ObjectMapper mapper = new ObjectMapper();

        // Findex master keys
        String masterKeysJson;
        try {
            masterKeysJson = mapper.writeValueAsString(masterKeys);
        } catch (JsonProcessingException e) {
            throw new FfiException("Invalid master keys", e);
        }

        // Findex words
        String wordsJson;
        try {
            wordsJson = mapper.writeValueAsString(words);
        } catch (JsonProcessingException e) {
            throw new FfiException("Invalid words", e);
        }

        String[] dbUids = null;

        // Indexes creation + insertion/update
        unwrap(Ffi.INSTANCE.h_search(dbUidsBuffer, dbUidsBufferSize, masterKeysJson, wordsJson, loopIterationLimit,
            fetchEntry, fetchChain));

        byte[] dbUidsBytes = Arrays.copyOfRange(dbUidsBuffer, 0, dbUidsBufferSize.getValue());
        try {
            dbUids = mapper.readValue(dbUidsBytes, String[].class);
        } catch (IOException e) {
            throw new FfiException("DB UIDs deserialization failed", e);
        }
        return dbUids;
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
