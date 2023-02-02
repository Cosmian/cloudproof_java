package com.cosmian.jna.covercrypt.structs;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import com.sun.jna.ptr.IntByReference;
import com.cosmian.jna.covercrypt.ffi.CoverCryptWrapper;
import com.cosmian.utils.CloudproofException;
import com.sun.jna.Native;

public class Ffi {

    protected static final CoverCryptWrapper instance = (CoverCryptWrapper) Native.load("cosmian_cover_crypt",
        CoverCryptWrapper.class);

    /**
     * If the result of the last FFI call is in Error, recover the last error from the native code and throw an
     * exception wrapping it.
     *
     * @param result the result of the FFI call
     * @return the result if it is different from 1
     * @throws CloudproofException in case of native library error (result is 1)
     */
    public static int unwrap(int result) throws CloudproofException {
        if (result != 0) {
            throw new CloudproofException(get_last_error(4096));
        }
        return result;
    }

    /**
     * Return the last error in a String that does not exceed 1023 bytes
     *
     * @return the last error recorded by the native library
     * @throws CloudproofException in case of native library error
     */
    public static String get_last_error() throws CloudproofException {
        return get_last_error(1024);
    }

    /**
     * Return the last error in a String that does not exceed `max_len` bytes
     *
     * @param max_len the maximum number of bytes to return
     * @throws CloudproofException in case of native library error
     * @return the error
     */
    public static String get_last_error(int max_len) throws CloudproofException {
        if (max_len < 1) {
            throw new CloudproofException("get_last_error: max_len must be at least one");
        }
        byte[] output = new byte[max_len];
        IntByReference outputSize = new IntByReference(output.length);
        int err = instance.h_get_error(output, outputSize);
        if (err == 0) {
            return new String(Arrays.copyOfRange(output, 0, outputSize.getValue()), StandardCharsets.UTF_8);
        }
        throw new CloudproofException(
            "Failed retrieving the last error with error code '" + err + "'; check the debug logs");
    }

    /**
     * Set the last error on the native lib
     *
     * @param error_msg the last error to set on the native lib
     * @throws CloudproofException n case of native library error
     */
    public void set_error(String error_msg) throws CloudproofException {
        unwrap(instance.h_set_error(error_msg + "\0"));
    }

}
