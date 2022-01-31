package com.cosmian.rest.jna.abe;

import com.sun.jna.Native;
import com.sun.jna.Library;
import com.sun.jna.ptr.IntByReference;
import com.sun.jna.Pointer;

public interface FfiWrapper extends Library {

        FfiWrapper INSTANCE = (FfiWrapper) Native.load("abe_gpsw", FfiWrapper.class);

        int set_error(String errorMsg);

        int get_last_error(byte[] output, IntByReference outputSize);

        int h_aes_symmetric_encryption_overhead();

        int h_aes_encrypt_header(byte[] symmetricKey, IntByReference symmetricKeySize, byte[] headerBytes,
                        IntByReference headerBytesSize, String policyJson, Pointer publicKeyPointer,
                        int publicKeyLength,
                        String attributesJson, Pointer uidPointer, int uidLen, Pointer additionalDataPointer,
                        int additionalDataLength);

        int h_aes_decrypt_header(byte[] symmetricKey, IntByReference symmetricKeySize, byte[] uidPointer,
                        IntByReference uidLen, byte[] additionalDataPointer, IntByReference additionalDataLength,
                        Pointer encryptedHeaderBytes, int encryptedHeaderBytesSize, Pointer userDecryptionKeyPointer,
                        int userDecryptionKeyLength);

        int h_aes_encrypt_block(byte[] encrypted, IntByReference encryptedSize, Pointer symmetricKeyPointer,
                        int symmetricKeyLength, Pointer uidPointer, int uidLen, int blockNumber, Pointer dataPointer,
                        int dataLength);

        int h_aes_decrypt_block(byte[] clearText, IntByReference clearTextSize, Pointer symmetricKeyPointer,
                        int symmetricKeyLength, Pointer uidPointer, int uidLen, int blockNumber,
                        Pointer clearTextPointer, int clearTextLength);

        // ----------------------------------------------------
        // Creates strange SIGSEGV Rust side: deactivated
        // ----------------------------------------------------

        // int hybrid_cipher_new(PointerByReference cipher, String policyJson, Pointer
        // publicKeyPointer,
        // int publicKeyLength,
        // String attributesJson, Pointer uidPointer, int uidLen, Pointer
        // additionalDataPointer,
        // int additionalDataLength);

        // void hybrid_cipher_destroy(Pointer cipherPointer);

}
