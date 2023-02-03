package com.cosmian.jna.covercrypt.ffi;

import com.sun.jna.Library;
import com.sun.jna.ptr.IntByReference;

/**
 * This maps the hybrid_gpsw-aes.rs functions in the abe_gpsw Rust library
 */
public interface CoverCryptWrapper extends Library {

    //
    // Error management
    //

    int h_set_error(String errorMsg);

    int h_get_error(byte[] output,
                    IntByReference outputSize);

    //
    // CoverCrypt APIs
    //

    int h_symmetric_encryption_overhead();

    int h_encrypt_header(byte[] symmetricKey,
                         IntByReference symmetricKeySize,
                         byte[] headerBytes,
                         IntByReference headerBytesSize,
                         byte[] policyBytes,
                         int policyBytesSize,
                         byte[] publicKeyBuffer,
                         int publicKeyLength,
                         String encryptionPolicy,
                         byte[] additionalDataBuffer,
                         int additionalDataLen,
                         byte[] authenticatedDataBuffer,
                         int authenticatedDataLength);

    int h_decrypt_header(byte[] symmetricKey,
                         IntByReference symmetricKeySize,
                         byte[] additionalDataBuffer,
                         IntByReference additionalDataLen,
                         byte[] encryptedHeaderBytes,
                         int encryptedHeaderBytesSize,
                         byte[] authenticatedDataBuffer,
                         int authenticatedDataLength,
                         byte[] userDecryptionKeyBuffer,
                         int userDecryptionKeyLength);

    int h_dem_encrypt(byte[] encrypted,
                      IntByReference encryptedSize,
                      byte[] symmetricKeyBuffer,
                      int symmetricKeyLength,
                      byte[] associatedDateBuffer,
                      int associatedDateLen,
                      byte[] dataBuffer,
                      int dataLength);

    int h_dem_decrypt(byte[] clearText,
                      IntByReference clearTextSize,
                      byte[] symmetricKeyBuffer,
                      int symmetricKeyLength,
                      byte[] authenticationDataBuffer,
                      int authenticationDataLen,
                      byte[] clearTextBuffer,
                      int clearTextLength);

    int h_create_encryption_cache(IntByReference cacheHandle,
                                  byte[] policyBytes,
                                  int policyBytesSize,
                                  byte[] publicKeyBuffer,
                                  int publicKeyLength);

    int h_destroy_encryption_cache(int cacheHandle);

    int h_encrypt_header_using_cache(byte[] symmetricKey,
                                     IntByReference symmetricKeySize,
                                     byte[] headerBytes,
                                     IntByReference headerBytesSize,
                                     int cacheHandle,
                                     String encryptionPolicy,
                                     byte[] additionalDataBuffer,
                                     int additionalDataLength,
                                     byte[] authenticatedBuffer,
                                     int authenticatedLen);

    int h_create_decryption_cache(IntByReference cacheHandle,
                                  byte[] userDecryptionKeyBuffer,
                                  int userDecryptionKeyLength);

    int h_destroy_decryption_cache(int cacheHandle);

    int h_decrypt_header_using_cache(byte[] symmetricKey,
                                     IntByReference symmetricKeySize,
                                     byte[] additionalDataBuffer,
                                     IntByReference additionalDataLen,
                                     byte[] encryptedHeaderBytes,
                                     int encryptedHeaderBytesSize,
                                     byte[] authenticatedDataBuffer,
                                     int authenticatedDataLength,
                                     int cacheHandle);

    int h_generate_master_keys(byte[] masterPrivateKeyBuffer,
                               IntByReference masterPrivateKeyBufferSize,
                               byte[] masterPublicKeyBuffer,
                               IntByReference masterPublicKeyBufferSize,
                               byte[] policyBytes,
                               int policyBytesSize);

    int h_generate_user_secret_key(byte[] userPrivateKeyPtr,
                                   IntByReference userPrivateKeySize,
                                   byte[] masterPrivateKeyPtr,
                                   int masterPrivateKeyLen,
                                   String userPolicy,
                                   byte[] policyBytes,
                                   int policyBytesSize);

    int h_hybrid_encrypt(byte[] ciphertext,
                         IntByReference ciphertextSize,
                         byte[] policyBytes,
                         int policyBytesSize,
                         byte[] publicKeyBuffer,
                         int publicKeyLength,
                         String encryptionBuffer,
                         byte[] plaintextBuffer,
                         int plaintextLen,
                         byte[] additionalDataBuffer,
                         int additionalDataLen,
                         byte[] authenticatedDataBuffer,
                         int authenticatedDataLength);

    int h_hybrid_decrypt(byte[] plaintext,
                         IntByReference plaintextSize,
                         byte[] additionalData,
                         IntByReference additionalDataSize,
                         byte[] ciphertextBytes,
                         int ciphertextBytesSize,
                         byte[] authenticatedDataBuffer,
                         int authenticatedDataLength,
                         byte[] userDecryptionKeyBuffer,
                         int userDecryptionKeyLength);

    //
    // Policy APIs
    //

    int h_policy(byte[] policyBuffer,
                 IntByReference policyBufferSize,
                 int maxAttributeCreations);

    int h_add_policy_axis(byte[] updatedPolicyBuffer,
                          IntByReference updatedPolicyBufferSize,
                          byte[] currentPolicyBuffer,
                          int currentPolicyBufferSize,
                          String axis);

    int h_rotate_attribute(byte[] updatedPolicyBuffer,
                           IntByReference updatedPolicyBufferSize,
                           byte[] currentPolicyBuffer,
                           int currentPolicyBufferSize,
                           String attribute);

    int h_validate_boolean_expression(String booleanExpression);

    int h_validate_attribute(String booleanExpression);
}
