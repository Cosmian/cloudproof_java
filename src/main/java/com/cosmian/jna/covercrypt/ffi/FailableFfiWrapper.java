package com.cosmian.jna.covercrypt.ffi;

import com.sun.jna.Library;
import com.sun.jna.ptr.IntByReference;

public interface FailableFfiWrapper extends Library {
    int set_error(String errorMsg);

    int get_error(byte[] output,
                  IntByReference outputSize);
}
