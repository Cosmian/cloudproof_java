package com.cosmian.jna.abe;

import com.sun.jna.Library;
import com.sun.jna.ptr.IntByReference;
import com.sun.jna.Pointer;

/**
 * This maps the hybrid_gpsw-aes.rs functions in the abe_gpsw Rust library
 */
public interface FfiWrapper extends Library {



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

        int h_aes_create_encryption_cache(IntByReference cacheHandle, String policyJson, Pointer publicKeyPointer,
                        int publicKeyLength);

        int h_aes_destroy_encryption_cache(int cacheHandle);

        int h_aes_encrypt_header_using_cache(byte[] symmetricKey, IntByReference symmetricKeySize, byte[] headerBytes,
                        IntByReference headerBytesSize, int cacheHandle,
                        String attributesJson, Pointer uidPointer, int uidLen, Pointer additionalDataPointer,
                        int additionalDataLength);

        int h_aes_create_decryption_cache(IntByReference cacheHandle, Pointer userDecryptionKeyPointer,
                        int userDecryptionKeyLength);

        int h_aes_destroy_decryption_cache(int cacheHandle);

        int h_aes_decrypt_header_using_cache(byte[] symmetricKey, IntByReference symmetricKeySize, byte[] uidPointer,
                        IntByReference uidLen, byte[] additionalDataPointer, IntByReference additionalDataLength,
                        Pointer encryptedHeaderBytes, int encryptedHeaderBytesSize, int cacheHandle);

}
