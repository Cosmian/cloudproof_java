package com.cosmian.jna.abe;

import com.sun.jna.Library;
import com.sun.jna.Pointer;
import com.sun.jna.ptr.IntByReference;

/**
 * This maps the hybrid_gpsw-aes.rs functions in the abe_gpsw Rust library
 */
public interface FfiWrapper extends Library {

    int set_error(String errorMsg);

    int get_last_error(byte[] output, IntByReference outputSize);

    int h_aes_symmetric_encryption_overhead();

    int h_aes_encrypt_header(
            byte[] symmetricKey, IntByReference symmetricKeySize,
            byte[] headerBytes, IntByReference headerBytesSize,
            String policyJson,
            Pointer publicKeyPointer, int publicKeyLength,
            String encryptionPolicy,
            Pointer additionalDataPointer, int additionalDataLen,
            Pointer authenticatedDataPointer, int authenticatedDataLength);

    int h_aes_decrypt_header(
            byte[] symmetricKey, IntByReference symmetricKeySize,
            byte[] additionalDataPointer, IntByReference additionalDataLen,
            Pointer encryptedHeaderBytes, int encryptedHeaderBytesSize,
            Pointer authenticatedDataPointer, int authenticatedDataLength,
            Pointer userDecryptionKeyPointer, int userDecryptionKeyLength);

    int h_aes_encrypt_block(
            byte[] encrypted, IntByReference encryptedSize,
            Pointer symmetricKeyPointer, int symmetricKeyLength,
            Pointer associatedDatePointer, int associatedDateLen,
            Pointer dataPointer, int dataLength);

    int h_aes_decrypt_block(
            byte[] clearText, IntByReference clearTextSize,
            Pointer symmetricKeyPointer, int symmetricKeyLength,
            Pointer authenticationDataPointer, int authenticationDataLen,
            Pointer clearTextPointer, int clearTextLength);

    int h_aes_create_encryption_cache(IntByReference cacheHandle, String policyJson, Pointer publicKeyPointer,
            int publicKeyLength);

    int h_aes_destroy_encryption_cache(int cacheHandle);

    int h_aes_encrypt_header_using_cache(byte[] symmetricKey, IntByReference symmetricKeySize, byte[] headerBytes,
            IntByReference headerBytesSize, int cacheHandle, String encryptionPolicy, Pointer additionalDataPointer,
            int additionalDataLength, Pointer authenticatedPointer,
            int authenticatedLen);

    int h_aes_create_decryption_cache(IntByReference cacheHandle, Pointer userDecryptionKeyPointer,
            int userDecryptionKeyLength);

    int h_aes_destroy_decryption_cache(int cacheHandle);

    int h_aes_decrypt_header_using_cache(byte[] symmetricKey, IntByReference symmetricKeySize,
            byte[] additionalDataPointer, IntByReference additionalDataLen,
            Pointer encryptedHeaderBytes, int encryptedHeaderBytesSize,
            Pointer authenticatedDataPointer, int authenticatedDataLength,
            int cacheHandle);

    int h_generate_master_keys(byte[] masterKeys, IntByReference masterKeysSize, String policyJson);

    int h_generate_user_secret_key(byte[] userPrivateKeyPtr, IntByReference userPrivateKeySize,
            Pointer masterPrivateKeyPtr, int masterPrivateKeyLen, String accessPolicyJson, String policyJson);

    int h_rotate_attributes(byte[] policyBuffer, IntByReference policyBufferSize, String attributesJson,
            String policyJson);

    int h_aes_encrypt(
            byte[] ciphertext, IntByReference ciphertextSize,
            String policyJson,
            Pointer publicKeyPointer, int publicKeyLength,
            String encryptionPolicy,
            Pointer plaintextPointer, int plaintextLen,
            Pointer additionalDataPointer, int additionalDataLen,
            Pointer authenticatedDataPointer, int authenticatedDataLength);

    int h_aes_decrypt(
            byte[] plaintext, IntByReference plaintextSize,
            byte[] additionalData, IntByReference additionalDataSize,
            Pointer ciphertextBytes, int ciphertextBytesSize,
            Pointer authenticatedDataPointer, int authenticatedDataLength,
            Pointer userDecryptionKeyPointer, int userDecryptionKeyLength);

    int h_access_policy_expression_to_json(
            byte[] jsonExpr, IntByReference jsonExprSize,
            String booleanExpression);
}
