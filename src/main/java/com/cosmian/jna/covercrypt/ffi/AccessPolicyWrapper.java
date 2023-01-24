package com.cosmian.jna.covercrypt.ffi;

import com.sun.jna.Library;
import com.sun.jna.ptr.IntByReference;

/**
 * This maps the hybrid_gpsw-aes.rs functions in the abe_gpsw Rust library
 */
public interface AccessPolicyWrapper extends Library {
    int h_parse_boolean_access_policy(byte[] accessPolicyBuffer,
                                      IntByReference accessPolicyBufferSize,
                                      String booleanExpression);
}
