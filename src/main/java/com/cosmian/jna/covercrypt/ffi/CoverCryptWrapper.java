package com.cosmian.jna.covercrypt.ffi;

import com.sun.jna.Library;
import com.sun.jna.Pointer;
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
                             Pointer publicKeyPointer,
                             int publicKeyLength,
                             String encryptionPolicy,
                             Pointer additionalDataPointer,
                             int additionalDataLen,
                             Pointer authenticatedDataPointer,
                             int authenticatedDataLength);

    int h_decrypt_header(byte[] symmetricKey,
                             IntByReference symmetricKeySize,
                             byte[] additionalDataPointer,
                             IntByReference additionalDataLen,
                             Pointer encryptedHeaderBytes,
                             int encryptedHeaderBytesSize,
                             Pointer authenticatedDataPointer,
                             int authenticatedDataLength,
                             Pointer userDecryptionKeyPointer,
                             int userDecryptionKeyLength);

    int h_dem_encrypt(byte[] encrypted,
                            IntByReference encryptedSize,
                            Pointer symmetricKeyPointer,
                            int symmetricKeyLength,
                            Pointer associatedDatePointer,
                            int associatedDateLen,
                            Pointer dataPointer,
                            int dataLength);

    int h_dem_decrypt(byte[] clearText,
                            IntByReference clearTextSize,
                            Pointer symmetricKeyPointer,
                            int symmetricKeyLength,
                            Pointer authenticationDataPointer,
                            int authenticationDataLen,
                            Pointer clearTextPointer,
                            int clearTextLength);

    int h_create_encryption_cache(IntByReference cacheHandle,
                                  byte[] policyBytes,
                                  int policyBytesSize,
                                      Pointer publicKeyPointer,
                                      int publicKeyLength);

    int h_destroy_encryption_cache(int cacheHandle);

    int h_encrypt_header_using_cache(byte[] symmetricKey,
                                         IntByReference symmetricKeySize,
                                         byte[] headerBytes,
                                         IntByReference headerBytesSize,
                                         int cacheHandle,
                                         String encryptionPolicy,
                                         Pointer additionalDataPointer,
                                         int additionalDataLength,
                                         Pointer authenticatedPointer,
                                         int authenticatedLen);

    int h_create_decryption_cache(IntByReference cacheHandle,
                                      Pointer userDecryptionKeyPointer,
                                      int userDecryptionKeyLength);

    int h_destroy_decryption_cache(int cacheHandle);

    int h_decrypt_header_using_cache(byte[] symmetricKey,
                                         IntByReference symmetricKeySize,
                                         byte[] additionalDataPointer,
                                         IntByReference additionalDataLen,
                                         Pointer encryptedHeaderBytes,
                                         int encryptedHeaderBytesSize,
                                         Pointer authenticatedDataPointer,
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
                                   Pointer masterPrivateKeyPtr,
                                   int masterPrivateKeyLen,
                                   String userPolicy,
                                   byte[] policyBytes,
                                   int policyBytesSize);

    int h_hybrid_encrypt(byte[] ciphertext,
                      IntByReference ciphertextSize,
                      byte[] policyBytes,
                      int policyBytesSize,
                      Pointer publicKeyPointer,
                      int publicKeyLength,
                      String encryptionPolicy,
                      Pointer plaintextPointer,
                      int plaintextLen,
                      Pointer additionalDataPointer,
                      int additionalDataLen,
                      Pointer authenticatedDataPointer,
                      int authenticatedDataLength);

    int h_hybrid_decrypt(byte[] plaintext,
                      IntByReference plaintextSize,
                      byte[] additionalData,
                      IntByReference additionalDataSize,
                      Pointer ciphertextBytes,
                      int ciphertextBytesSize,
                      Pointer authenticatedDataPointer,
                      int authenticatedDataLength,
                      Pointer userDecryptionKeyPointer,
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
