package com.cosmian.rest.jna;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;

// import com.sun.jna.Memory;
// import com.sun.jna.Pointer;
import com.sun.jna.ptr.IntByReference;

public class Ffi {

    public int square(int x) {
        return AbeWrapper.INSTANCE.square(x);
    }

    // public long count_bytes(byte[] bytes) {
    // // Native.getNativeSize(Double.TYPE)
    // final Pointer ptr = new Memory(bytes.length);
    // ptr.write(0, bytes, 0, bytes.length);
    // RustLibrary lib = RustLibrary.INSTANCE;
    // return lib.count_bytes(ptr, bytes.length);

    // ,
    // }

    /**
     * Return the last error in a String that does not exceed 1023 bytes
     */
    public String get_last_error() throws FfiException {
        return get_last_error(1023);
    }

    /**
     * Return the last error in a String that does not exceed `max_len` bytes
     */
    public String get_last_error(int max_len) throws FfiException {
        if (max_len < 1) {
            throw new FfiException("get_last_error: max_lem must be at least one");
        }
        byte[] output = new byte[max_len + 1];
        IntByReference outputSize = new IntByReference(output.length);
        if (AbeWrapper.INSTANCE.get_last_error(output, outputSize) == 0) {
            return new String(Arrays.copyOfRange(output, 0, outputSize.getValue()), StandardCharsets.UTF_8);
        }
        throw new FfiException("Failed retrieving the last error; check the debug logs");
    }

    public void set_error(String error_msg) throws FfiException {
        unwrap(AbeWrapper.INSTANCE.set_error(error_msg));
    }

    private void unwrap(int result) throws FfiException {
        if (result == 1) {
            throw new FfiException(get_last_error(4095));
        }
    }
}
