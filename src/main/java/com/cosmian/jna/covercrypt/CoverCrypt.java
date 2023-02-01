package com.cosmian.jna.covercrypt;

import java.util.Arrays;
import java.util.Optional;

import com.cosmian.jna.covercrypt.structs.DecryptedHeader;
import com.cosmian.jna.covercrypt.structs.EncryptedHeader;
import com.cosmian.jna.covercrypt.structs.Ffi;
import com.cosmian.jna.covercrypt.structs.MasterKeys;
import com.cosmian.jna.covercrypt.structs.Policy;
import com.cosmian.rest.abe.data.DecryptedData;
import com.cosmian.rest.kmip.objects.PublicKey;
import com.cosmian.utils.CloudproofException;
import com.sun.jna.ptr.IntByReference;

public final class CoverCrypt extends Ffi {

    /**
     * Create an encryption cache that can be used with {@link #encryptHeaderUsingCache(int, String)}. The cache speeds
     * up the encryption of the header. WARN: the cache MUST be destroyed after use with
     * {@link #destroyEncryptionCache(int)}.
     *
     * @param policy the {@link Policy} to cache
     * @param publicKeyBytes the public key bytes to cache
     * @return the cache handle that can be passed to the encryption routine
     * @throws CloudproofException on Rust lib errors
     */
    public static int createEncryptionCache(Policy policy,
                                            byte[] publicKeyBytes)
        throws CloudproofException {
        IntByReference cacheHandle = new IntByReference();
        byte[] policyBytes = policy.getBytes();
        unwrap(instance.h_create_encryption_cache(cacheHandle, policyBytes, policyBytes.length, publicKeyBytes,
            publicKeyBytes.length));
        return cacheHandle.getValue();
    }

    /**
     * Destroy the cache created with {@link #createEncryptionCache(Policy, byte[])}.
     *
     * @param cacheHandle the pointer to the cache to destroy
     * @throws CloudproofException on Rust lib errors
     */
    public static void destroyEncryptionCache(int cacheHandle) throws CloudproofException {
        unwrap(instance.h_destroy_encryption_cache(cacheHandle));
    }

    /**
     * Generate an encrypted header using a pre-cached Public Key and `Policy`. A symmetric key is randomly generated
     * and encrypted using the provided policy attributes.
     *
     * @param cacheHandle the pointer to the {@link int}
     * @param encryptionPolicy the encryption policy that determines the partitions to encrypt for
     * @return the encrypted header bytes and the encrypted symmetric key
     * @throws CloudproofException in case of native library error
     * @throws CloudproofException in case the {@link Policy} and key bytes cannot be recovered from the
     *             {@link PublicKey}
     */
    public static EncryptedHeader encryptHeaderUsingCache(int cacheHandle,
                                                          String encryptionPolicy)
        throws CloudproofException {
        return encryptHeaderUsingCache_(cacheHandle, encryptionPolicy, Optional.empty(), Optional.empty());
    }

    /**
     * Generate an encrypted header using a pre-cached Public Key and `Policy`. A symmetric key is randomly generated
     * and encrypted using the provided policy attributes. The `headerMetadata` is also symmetrically encrypted using
     * this key. The resulting ciphertexts are stored in the encrypted header.
     *
     * @param cacheHandle the pointer to the {@link int}
     * @param encryptionPolicy the encryption policy that determines the partitions to encrypt for
     * @param headerMetadata optional additional data to encrypt and add to the header
     * @return the encrypted header bytes and the encrypted symmetric key
     * @throws CloudproofException in case of native library error
     * @throws CloudproofException in case the {@link Policy} and key bytes cannot be recovered from the
     *             {@link PublicKey}
     */
    public static EncryptedHeader encryptHeaderUsingCache(int cacheHandle,
                                                          String encryptionPolicy,
                                                          byte[] headerMetadata)
        throws CloudproofException {
        return encryptHeaderUsingCache_(cacheHandle, encryptionPolicy, Optional.of(headerMetadata), Optional.empty());
    }

    /**
     * Generate an encrypted header using a pre-cached Public Key and `Policy`. A symmetric key is randomly generated
     * and encrypted using the provided policy attributes. The `headerMetadata` is also symmetrically encrypted using
     * this key using the `authenticationData` as AEAD associated data. The resulting ciphertexts are stored in the
     * encrypted header.
     *
     * @param cacheHandle the pointer to the {@link int}
     * @param encryptionPolicy the encryption policy that determines the partitions to encrypt for
     * @param headerMetadata optional additional data to encrypt and add to the header
     * @param authenticationData optional data used to authenticate the encryption of the additional data
     * @return the encrypted header bytes and the encrypted symmetric key
     * @throws CloudproofException in case of native library error
     * @throws CloudproofException in case the {@link Policy} and key bytes cannot be recovered from the
     *             {@link PublicKey}
     */
    public static EncryptedHeader encryptHeaderUsingCache(int cacheHandle,
                                                          String encryptionPolicy,
                                                          byte[] headerMetadata,
                                                          byte[] authenticationData)
        throws CloudproofException {
        return encryptHeaderUsingCache_(cacheHandle, encryptionPolicy, Optional.of(headerMetadata),
            Optional.of(authenticationData));
    }

    static EncryptedHeader encryptHeaderUsingCache_(int cacheHandle,
                                                    String encryptionPolicy,
                                                    Optional<byte[]> headerMetadata,
                                                    Optional<byte[]> authenticationData)
        throws CloudproofException {

        // Additional data
        int headerMetadataLength;
        byte[] headerMetadataBuffer;
        if (headerMetadata.isPresent()) {
            headerMetadataLength = headerMetadata.get().length;
            headerMetadataBuffer = headerMetadata.get();
        } else {
            headerMetadataLength = 0;
            headerMetadataBuffer = new byte[] {};
        }

        // Authenticated data
        int authenticationDataLength;
        byte[] authenticationDataBuffer;
        if (authenticationData.isPresent()) {
            authenticationDataLength = authenticationData.get().length;
            authenticationDataBuffer = authenticationData.get();
        } else {
            authenticationDataLength = 0;
            authenticationDataBuffer = new byte[] {};
        }

        // Symmetric Key OUT
        byte[] symmetricKeyBuffer = new byte[32]; // safe, the key should be 32 bytes
        IntByReference symmetricKeyBufferSize = new IntByReference(symmetricKeyBuffer.length);

        // Header Bytes OUT
        byte[] headerBytesBuffer = new byte[8192];
        IntByReference headerBytesBufferSize = new IntByReference(headerBytesBuffer.length);

        int ffiCode = instance.h_encrypt_header_using_cache(symmetricKeyBuffer, symmetricKeyBufferSize,
            headerBytesBuffer, headerBytesBufferSize, cacheHandle, encryptionPolicy, authenticationDataBuffer,
            authenticationDataLength, headerMetadataBuffer, headerMetadataLength);

        if (ffiCode != 0) {
            // retry using correct allocation size for the header
            headerBytesBuffer = new byte[headerBytesBufferSize.getValue()];
            unwrap(instance.h_encrypt_header_using_cache(symmetricKeyBuffer, symmetricKeyBufferSize,
                headerBytesBuffer, headerBytesBufferSize, cacheHandle, encryptionPolicy, authenticationDataBuffer,
                authenticationDataLength, headerMetadataBuffer, headerMetadataLength));
        }

        return new EncryptedHeader(Arrays.copyOfRange(symmetricKeyBuffer, 0, symmetricKeyBufferSize.getValue()),
            Arrays.copyOfRange(headerBytesBuffer, 0, headerBytesBufferSize.getValue()));
    }

    /**
     * Generate an hybrid encryption header. A symmetric key is randomly generated and encrypted the provided encryption
     * policy.
     *
     * @param policy the policy to use
     * @param publicKeyBytes the ABE public key bytes
     * @param encryptionPolicy the encryption policy that determines the partitions to encrypt for
     * @return the encrypted header, bytes and symmetric key
     * @throws CloudproofException in case of native library error
     */
    public static EncryptedHeader encryptHeader(Policy policy,
                                                byte[] publicKeyBytes,
                                                String encryptionPolicy)
        throws CloudproofException {
        return encryptHeader_(policy, publicKeyBytes, encryptionPolicy, Optional.empty(), Optional.empty());
    }

    /**
     * Generate an hybrid encryption header. A symmetric key is randomly generated and encrypted the provided encryption
     * policy. The `headerMetadata` is symmetrically encrypted using this key. The resulting ciphertext is appended to
     * the encrypted header.
     *
     * @param policy the policy to use
     * @param publicKeyBytes the ABE public key bytes
     * @param encryptionPolicy the encryption policy that determines the partitions to encrypt for
     * @param headerMetadata the additional data to encrypt and add to the header
     * @return the encrypted header, bytes and symmetric key
     * @throws CloudproofException in case of native library error
     */
    public static EncryptedHeader encryptHeader(Policy policy,
                                                byte[] publicKeyBytes,
                                                String encryptionPolicy,
                                                byte[] headerMetadata)
        throws CloudproofException {
        return encryptHeader_(policy, publicKeyBytes, encryptionPolicy, Optional.of(headerMetadata), Optional.empty());
    }

    /**
     * Generate an hybrid encryption header. A symmetric key is randomly generated and encrypted the provided encryption
     * policy. The `headerMetadata` is symmetrically encrypted using this key and the `authenticationData` as AEAD
     * associated data. The resulting ciphertext is appended to the encrypted header.
     *
     * @param policy the policy to use
     * @param publicKeyBytes the ABE public key bytes
     * @param encryptionPolicy the encryption policy that determines the partitions to encrypt for
     * @param headerMetadata the additional data to encrypt and add to the header
     * @param authenticationData optional data used to authenticate the encryption of the additional data
     * @return the encrypted header, bytes and symmetric key
     * @throws CloudproofException in case of native library error
     */
    public static EncryptedHeader encryptHeader(Policy policy,
                                                byte[] publicKeyBytes,
                                                String encryptionPolicy,
                                                byte[] headerMetadata,
                                                byte[] authenticationData)
        throws CloudproofException {
        return encryptHeader_(policy, publicKeyBytes, encryptionPolicy, Optional.of(headerMetadata),
            Optional.of(authenticationData));
    }

    static EncryptedHeader encryptHeader_(Policy policy,
                                          byte[] publicKeyBytes,
                                          String encryptionPolicy,
                                          Optional<byte[]> headerMetadata,
                                          Optional<byte[]> authenticationData)
        throws CloudproofException {

        // Additional data
        int headerMetadataLength;
        byte[] headerMetadataBuffer;
        if (headerMetadata.isPresent()) {
            headerMetadataLength = headerMetadata.get().length;
            headerMetadataBuffer = headerMetadata.get();
        } else {
            headerMetadataLength = 0;
            headerMetadataBuffer = new byte[] {};
        }

        // Authenticated data
        int authenticationDataLength;
        byte[] authenticationDataBuffer;
        if (authenticationData.isPresent()) {
            authenticationDataLength = authenticationData.get().length;
            authenticationDataBuffer = authenticationData.get();
        } else {
            authenticationDataLength = 0;
            authenticationDataBuffer = new byte[] {};
        }

        // Symmetric Key OUT
        byte[] symmetricKeyBuffer = new byte[32]; // safe, the key should be 32 bytes
        IntByReference symmetricKeyBufferSize = new IntByReference(symmetricKeyBuffer.length);

        // Header Bytes OUT
        byte[] headerBytesBuffer = new byte[8192 + headerMetadataLength];
        IntByReference headerBytesBufferSize = new IntByReference(headerBytesBuffer.length);

        int ffiCode = instance.h_encrypt_header(
            symmetricKeyBuffer, symmetricKeyBufferSize,
            headerBytesBuffer, headerBytesBufferSize,
            policy.getBytes(), policy.getBytes().length,
            publicKeyBytes, publicKeyBytes.length,
            encryptionPolicy,
            headerMetadataBuffer, headerMetadataLength,
            authenticationDataBuffer, authenticationDataLength);

        if (ffiCode != 0) {
            // retry with a correct allocation size for the encrypted header
            headerBytesBuffer = new byte[headerBytesBufferSize.getValue()];
            unwrap(instance.h_encrypt_header(
                symmetricKeyBuffer, symmetricKeyBufferSize,
                headerBytesBuffer, headerBytesBufferSize,
                policy.getBytes(), policy.getBytes().length,
                publicKeyBytes, publicKeyBytes.length,
                encryptionPolicy,
                headerMetadataBuffer, headerMetadataLength,
                authenticationDataBuffer, authenticationDataLength));
        }

        return new EncryptedHeader(Arrays.copyOfRange(symmetricKeyBuffer, 0, symmetricKeyBufferSize.getValue()),
            Arrays.copyOfRange(headerBytesBuffer, 0, headerBytesBufferSize.getValue()));
    }

    // -----------------------------------------------
    // Header Decryption
    // -----------------------------------------------

    /**
     * Create a decryption cache that can be used with {@link #decryptHeaderUsingCache(int, byte[], Optional)}.
     * The cache speeds up the decryption of the header. WARN: the cache MUST be destroyed after use with
     * {@link #destroyDecryptionCache(int)}
     *
     * @param userDecryptionKeyBytes the public key bytes to cache
     * @return the cache handle that can be passed to the decryption routine
     * @throws CloudproofException on Rust lib errors
     * @throws CloudproofException in case of other errors
     */
    public static int createDecryptionCache(byte[] userDecryptionKeyBytes) throws CloudproofException {
        IntByReference cacheHandle = new IntByReference();
        unwrap(instance.h_create_decryption_cache(cacheHandle, userDecryptionKeyBytes, userDecryptionKeyBytes.length));
        return cacheHandle.getValue();
    }

    /**
     * Destroy the cache created with {@link #createDecryptionCache(byte[])}.
     *
     * @param cacheHandle the pointer to the cache to destroy
     * @throws CloudproofException on Rust lib errors
     * @throws CloudproofException in case of other errors
     */
    public static void destroyDecryptionCache(int cacheHandle) throws CloudproofException {
        unwrap(instance.h_destroy_decryption_cache(cacheHandle));
    }

    /**
     * Decrypt a hybrid header using a cache, recovering the symmetric key. If some header metadata is encrypted in the
     * header, it will be decrypted using the given `authenticatedData`.
     *
     * @param cacheHandle the cache to the user decryption key
     * @param encryptedHeaderBytes the encrypted header
     * @param authenticationData optional data used to authenticate the encryption of the additional data
     * @return The decrypted header: symmetric key, uid and additional data
     * @throws CloudproofException in case of native library error
     */
    public static DecryptedHeader decryptHeaderUsingCache(int cacheHandle,
                                                          byte[] encryptedHeaderBytes,
                                                          Optional<byte[]> authenticationData)
        throws CloudproofException {

        // Symmetric Key OUT
        byte[] symmetricKeyBuffer = new byte[32]; // safe, the key should be 32-byte long
        IntByReference symmetricKeyBufferSize = new IntByReference(symmetricKeyBuffer.length);

        // Header Metadata OUT
        byte[] headerMetadataBuffer = new byte[8 * 1024];
        IntByReference headerMetadataBufferSize = new IntByReference(headerMetadataBuffer.length);

        // Authentication data
        int authenticationDataLength;
        byte[] authenticationDataBuffer;
        if (authenticationData.isPresent()) {
            authenticationDataLength = authenticationData.get().length;
            authenticationDataBuffer = authenticationData.get();
        } else {
            authenticationDataLength = 0;
            authenticationDataBuffer = new byte[] {};
        }

        int ffiCode = instance.h_decrypt_header_using_cache(
            symmetricKeyBuffer, symmetricKeyBufferSize,
            headerMetadataBuffer, headerMetadataBufferSize,
            encryptedHeaderBytes, encryptedHeaderBytes.length,
            authenticationDataBuffer, authenticationDataLength,
            cacheHandle);

        if (ffiCode != 0) {
            // retry with correct allocation size for the header metadata
            headerMetadataBuffer = new byte[headerMetadataBufferSize.getValue()];
            unwrap(instance.h_decrypt_header_using_cache(
                symmetricKeyBuffer, symmetricKeyBufferSize,
                headerMetadataBuffer, headerMetadataBufferSize,
                encryptedHeaderBytes, encryptedHeaderBytes.length,
                authenticationDataBuffer, authenticationDataLength,
                cacheHandle));
        }

        return new DecryptedHeader(Arrays.copyOfRange(symmetricKeyBuffer, 0, symmetricKeyBufferSize.getValue()),
            Arrays.copyOfRange(headerMetadataBuffer, 0, headerMetadataBufferSize.getValue()));
    }

    /**
     * Decrypt a hybrid header using a cache, recovering the symmetric key. If some header metadata is encrypted in the
     * header, it will be decrypted using the given `authenticatedData`.
     *
     * @param userDecryptionKeyBytes the ABE user decryption key bytes
     * @param encryptedHeaderBytes the encrypted header
     * @param authenticationData optional data used to authenticate the encryption of the additional data
     * @return The decrypted header: symmetric key, uid and additional data
     * @throws CloudproofException in case of native library error
     */
    public static DecryptedHeader decryptHeader(byte[] userDecryptionKeyBytes,
                                                byte[] encryptedHeaderBytes,
                                                Optional<byte[]> authenticationData)
        throws CloudproofException {

        // Symmetric Key OUT
        byte[] symmetricKeyBuffer = new byte[32]; // safe, the key should be 32-byte long
        IntByReference symmetricKeyBufferSize = new IntByReference(symmetricKeyBuffer.length);

        // Header Metadata OUT
        byte[] headerMetadataBuffer = new byte[8 * 1024];
        IntByReference headerMetadataBufferSize = new IntByReference(headerMetadataBuffer.length);

        // Authenticated data
        int authenticationDataLength;
        byte[] authenticationDataBuffer;
        if (authenticationData.isPresent()) {
            authenticationDataLength = authenticationData.get().length;
            authenticationDataBuffer = authenticationData.get();
        } else {
            authenticationDataLength = 0;
            authenticationDataBuffer = new byte[] {};
        }

        int ffiCode = instance.h_decrypt_header(
            symmetricKeyBuffer, symmetricKeyBufferSize,
            headerMetadataBuffer, headerMetadataBufferSize,
            encryptedHeaderBytes, encryptedHeaderBytes.length,
            authenticationDataBuffer, authenticationDataLength,
            userDecryptionKeyBytes, userDecryptionKeyBytes.length);

        if (ffiCode != 0) {
            // retry with the correct allocation size for the header metadata
            headerMetadataBuffer = new byte[headerMetadataBufferSize.getValue()];
            unwrap(instance.h_decrypt_header(
                symmetricKeyBuffer, symmetricKeyBufferSize,
                headerMetadataBuffer, headerMetadataBufferSize,
                encryptedHeaderBytes, encryptedHeaderBytes.length,
                authenticationDataBuffer, authenticationDataLength,
                userDecryptionKeyBytes, userDecryptionKeyBytes.length));
        }

        return new DecryptedHeader(Arrays.copyOfRange(symmetricKeyBuffer, 0, symmetricKeyBufferSize.getValue()),
            Arrays.copyOfRange(headerMetadataBuffer, 0, headerMetadataBufferSize.getValue()));
    }

    /**
     * The overhead in bytes (over the clear text) generated by the symmetric encryption scheme (AES 256 GCM)
     *
     * @return the overhead bytes
     */
    public static int symmetricEncryptionOverhead() {
        return instance.h_symmetric_encryption_overhead();
    }

    /**
     * Symmetrically encrypt a block of clear text data. Use the `authenticationData` as AEAD additional data if it is
     * given.
     *
     * @param symmetricKey The key to use to symmetrically encrypt the block
     * @param authenticationData The associated Data used to authenticate the symmetric encryption
     * @param clearText the clear text to encrypt
     * @return the encrypted block
     * @throws CloudproofException in case of native library error
     */
    public static byte[] encryptBlock(byte[] symmetricKey,
                                      Optional<byte[]> authenticationData,
                                      byte[] clearText)
        throws CloudproofException {

        // Ciphertext OUT
        byte[] ciphertextBuffer = new byte[instance.h_symmetric_encryption_overhead() + clearText.length];
        IntByReference ciphertextBufferSize = new IntByReference(ciphertextBuffer.length);

        // Authenticated data
        int authenticationDataLength;
        byte[] authenticationDataBuffer;
        if (authenticationData.isPresent()) {
            authenticationDataLength = authenticationData.get().length;
            authenticationDataBuffer = authenticationData.get();
        } else {
            authenticationDataLength = 0;
            authenticationDataBuffer = new byte[] {};
        }

        unwrap(instance.h_dem_encrypt(
            ciphertextBuffer, ciphertextBufferSize,
            symmetricKey, symmetricKey.length,
            authenticationDataBuffer, authenticationDataLength,
            clearText, clearText.length));

        return Arrays.copyOfRange(ciphertextBuffer, 0, ciphertextBufferSize.getValue());
    }

    /**
     * Symmetrically decrypt a block of encrypted data. Use the `authenticationData` as AEAD additional data if it is
     * given.
     *
     * @param symmetricKey the symmetric key to use
     * @param authenticationData The associated Data used to authenticate the symmetric encryption
     * @param encryptedBytes the encrypted block bytes
     * @return the clear text bytes
     * @throws CloudproofException in case of native library error
     */
    public static byte[] decryptBlock(byte[] symmetricKey,
                                      Optional<byte[]> authenticationData,
                                      byte[] encryptedBytes)
        throws CloudproofException {

        // Clear Text Bytes OUT
        byte[] clearTextBuffer = new byte[encryptedBytes.length]; // safe: plaintext should be smaller than ciphertext
        IntByReference clearTextBufferSize = new IntByReference(clearTextBuffer.length);

        // Authenticated data
        int authenticationDataLength;
        byte[] authenticationDataBuffer;
        if (authenticationData.isPresent()) {
            authenticationDataLength = authenticationData.get().length;
            authenticationDataBuffer = authenticationData.get();
        } else {
            authenticationDataLength = 0;
            authenticationDataBuffer = new byte[] {};
        }

        unwrap(instance.h_dem_decrypt(
            clearTextBuffer, clearTextBufferSize,
            symmetricKey, symmetricKey.length,
            authenticationDataBuffer, authenticationDataLength,
            encryptedBytes, encryptedBytes.length));

        return Arrays.copyOfRange(clearTextBuffer, 0, clearTextBufferSize.getValue());
    }

    /**
     * Generate the master private and public keys using the ABE policy
     *
     * @param policy the policy to use
     * @return the master private and public keys in raw bytes
     * @throws CloudproofException in case of native library error
     */
    public static MasterKeys generateMasterKeys(Policy policy) throws CloudproofException {
        // Master Private Key OUT
        byte[] masterPrivateKeyBuffer = new byte[8 * 1024];
        IntByReference masterPrivateKeyBufferSize = new IntByReference(masterPrivateKeyBuffer.length);

        // Master Public Key OUT
        byte[] masterPublicKeyBuffer = new byte[8 * 1024];
        IntByReference masterPublicKeyBufferSize = new IntByReference(masterPublicKeyBuffer.length);

        int ffiCode = instance.h_generate_master_keys(masterPrivateKeyBuffer, masterPrivateKeyBufferSize,
            masterPublicKeyBuffer, masterPublicKeyBufferSize, policy.getBytes(), policy.getBytes().length);

        if (ffiCode != 0) {
            // Retry with correct allocated size
            masterPrivateKeyBuffer = new byte[masterPrivateKeyBufferSize.getValue()];
            masterPublicKeyBuffer = new byte[masterPublicKeyBufferSize.getValue()];
            unwrap(instance.h_generate_master_keys(masterPrivateKeyBuffer, masterPrivateKeyBufferSize,
                masterPublicKeyBuffer, masterPublicKeyBufferSize, policy.getBytes(),
                policy.getBytes().length));
        }

        return new MasterKeys(Arrays.copyOfRange(masterPrivateKeyBuffer, 0, masterPrivateKeyBufferSize.getValue()),
            Arrays.copyOfRange(masterPublicKeyBuffer, 0, masterPublicKeyBufferSize.getValue()));
    }

    /**
     * Generate the user private key.
     *
     * @param masterPrivateKey the master private key in bytes
     * @param userPolicy the access policy of the user private key as a JSON version of an AccessPolicy instance
     * @param policy the ABE policy
     * @return the corresponding user private key
     * @throws CloudproofException in case of native library error
     */
    public static byte[] generateUserPrivateKey(byte[] masterPrivateKey,
                                                String userPolicy,
                                                Policy policy)
        throws CloudproofException {

        // User private key Bytes OUT
        byte[] userPrivateKeyBuffer = new byte[8192];
        IntByReference userPrivateKeyBufferSize = new IntByReference(userPrivateKeyBuffer.length);

        int ffiCode = instance.h_generate_user_secret_key(userPrivateKeyBuffer, userPrivateKeyBufferSize,
            masterPrivateKey, masterPrivateKey.length, userPolicy, policy.getBytes(),
            policy.getBytes().length);

        if (ffiCode != 0) {
            // Retry with the correct allocated size
            userPrivateKeyBuffer = new byte[userPrivateKeyBufferSize.getValue()];
            unwrap(instance.h_generate_user_secret_key(userPrivateKeyBuffer, userPrivateKeyBufferSize, masterPrivateKey,
                masterPrivateKey.length, userPolicy, policy.getBytes(), policy.getBytes().length));
        }

        return Arrays.copyOfRange(userPrivateKeyBuffer, 0, userPrivateKeyBufferSize.getValue());
    }

    /**
     * Generate an hybrid encryption of a plaintext. If provided, the `headerMetadata` is symmetrically encrypted and
     * appended to the encrypted header. If provided the `authenticationData` is used as AEAD associated data.
     *
     * @param policy the policy to use
     * @param publicKeyBytes the public key bytes
     * @param encryptionPolicy the encryption policy that determines the partitions to encrypt for
     * @param plaintext the plaintext to encrypt
     * @param authenticationData optional data used to authenticate the symmetric encryption
     * @param headerMetadata the additional data to encrypt and add to the header
     * @return the ciphertext
     * @throws CloudproofException in case of native library error
     */
    public static byte[] encrypt(Policy policy,
                                 byte[] publicKeyBytes,
                                 String encryptionPolicy,
                                 byte[] plaintext,
                                 Optional<byte[]> authenticationData,
                                 Optional<byte[]> headerMetadata)
        throws CloudproofException {

        // Additional data
        int headerMetadataLength;
        byte[] headerMetadataBuffer;
        if (headerMetadata.isPresent()) {
            headerMetadataLength = headerMetadata.get().length;
            headerMetadataBuffer = headerMetadata.get();
        } else {
            headerMetadataLength = 0;
            headerMetadataBuffer = new byte[] {};
        }

        // Authenticated data
        int authenticationDataLength;
        byte[] authenticationDataBuffer;
        if (authenticationData.isPresent()) {
            authenticationDataLength = authenticationData.get().length;
            authenticationDataBuffer = authenticationData.get();
        } else {
            authenticationDataLength = 0;
            authenticationDataBuffer = new byte[] {};
        }

        // Ciphertext OUT
        byte[] ciphertext =
            new byte[8192 + headerMetadataLength + plaintext.length + 2 * CoverCrypt.symmetricEncryptionOverhead()];
        IntByReference ciphertextSize = new IntByReference(ciphertext.length);

        unwrap(instance.h_hybrid_encrypt(ciphertext, ciphertextSize, policy.getBytes(), policy.getBytes().length,
            publicKeyBytes, publicKeyBytes.length, encryptionPolicy, plaintext, plaintext.length, headerMetadataBuffer,
            headerMetadataLength, authenticationDataBuffer, authenticationDataLength));

        return Arrays.copyOfRange(ciphertext, 0, ciphertextSize.getValue());
    }

    /**
     * Decrypt a hybrid encryption. If provided, the `authenticationData` is used as AEAD associated data.
     *
     * @param userDecryptionKeyBytes the ABE user decryption key bytes
     * @param ciphertext the ciphertext to decrypt
     * @param authenticationData optional data used to authenticate the symmetric encryption
     * @return the {@link DecryptedData} containing the plaintext and optional header metadata
     * @throws CloudproofException in case of native library error
     */
    public static DecryptedData decrypt(byte[] userDecryptionKeyBytes,
                                        byte[] ciphertext,
                                        Optional<byte[]> authenticationData)
        throws CloudproofException {

        // Plaintext OUT
        byte[] plaintext = new byte[ciphertext.length]; // safe: plaintext should be smaller than cipher text
        IntByReference plaintextSize = new IntByReference(plaintext.length);

        // Header Metadata OUT
        byte[] headerMetadata = new byte[8 * 1024];
        IntByReference headerMetadataSize = new IntByReference(headerMetadata.length);

        // Authenticated data
        int authenticationDataLength;
        byte[] authenticationDataBuffer;
        if (authenticationData.isPresent()) {
            authenticationDataLength = authenticationData.get().length;
            authenticationDataBuffer = authenticationData.get();
        } else {
            authenticationDataLength = 0;
            authenticationDataBuffer = new byte[] {};
        }

        int ffiCode = instance.h_hybrid_decrypt(plaintext, plaintextSize, headerMetadata, headerMetadataSize,
            ciphertext, ciphertext.length, authenticationDataBuffer, authenticationDataLength, userDecryptionKeyBytes,
            userDecryptionKeyBytes.length);

        if (ffiCode != 0) {
            // retry with correct allocation size for the header metadata
            headerMetadata = new byte[headerMetadataSize.getValue()];
            unwrap(instance.h_hybrid_decrypt(plaintext, plaintextSize, headerMetadata, headerMetadataSize, ciphertext,
                ciphertext.length, authenticationDataBuffer, authenticationDataLength, userDecryptionKeyBytes,
                userDecryptionKeyBytes.length));

        }

        return new DecryptedData(
            Arrays.copyOfRange(plaintext, 0, plaintextSize.getValue()),
            Arrays.copyOfRange(headerMetadata, 0, headerMetadataSize.getValue()));
    }
}
