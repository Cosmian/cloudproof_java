package com.cosmian.rest.jna;

import com.sun.jna.Native;
import com.sun.jna.Library;
import com.sun.jna.ptr.IntByReference;

public interface AbeWrapper extends Library {

    AbeWrapper INSTANCE = (AbeWrapper) Native.load("abe_gpsw", AbeWrapper.class);

    int square(int x);

    int set_error(String errorMsg);

    int get_last_error(byte[] output, IntByReference outputSize);

}
