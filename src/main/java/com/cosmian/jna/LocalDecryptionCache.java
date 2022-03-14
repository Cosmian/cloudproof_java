package com.cosmian.jna;

import java.io.Serializable;

// import com.sun.jna.ptr.PointerByReference;
import com.sun.jna.PointerType;


/**
 * A pointer to a local decryption cache created Rust side
 * which holds the user decryption key
 */
public class LocalDecryptionCache extends PointerType implements Serializable{
}
