package com.cosmian.jna.covercrypt;

import java.util.Arrays;
import java.util.Optional;

import com.cosmian.jna.covercrypt.ffi.CoverCryptWrapper;
import com.cosmian.jna.covercrypt.structs.AccessPolicy;
import com.cosmian.jna.covercrypt.structs.DecryptedHeader;
import com.cosmian.jna.covercrypt.structs.EncryptedHeader;
import com.cosmian.jna.covercrypt.structs.Ffi;
import com.cosmian.jna.covercrypt.structs.MasterKeys;
import com.cosmian.jna.covercrypt.structs.Policy;
import com.cosmian.rest.abe.data.DecryptedData;
import com.cosmian.rest.kmip.objects.PrivateKey;
import com.cosmian.rest.kmip.objects.PublicKey;
import com.cosmian.utils.CloudproofException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.jna.Memory;
import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.ptr.IntByReference;

public final class CoverCrypt extends Ffi {

    // For the JSON strings
    private final static ObjectMapper mapper = new ObjectMapper();

    final private CoverCryptWrapper instance;

    /**
     * Instantiate a {@link CoverCrypt} instance by loading the native library `cosmian_cover_crypt`. The library must
     * be on the classpath. Native libraries are already included for darwin-x86-64, linux-x86-64 and win32-x86-64
     */
    public CoverCrypt() {
        this((CoverCryptWrapper) Native.load("cosmian_cover_crypt",
            CoverCryptWrapper.class));
    }

    public CoverCrypt(CoverCryptWrapper instance) {
        this.instance = instance;
    }

    /**
     * Create an encryption cache that can be used with {@link #encryptHeaderUsingCache(int, String)} se of the cache
     * speeds up the encryption of the header. WARN: the cache MUST be destroyed after use with
     * {@link #destroyEncryptionCache(int)}
     *
     * @param publicKey the public key to cache
     * @return the cache handle that can be passed to the encryption routine
     * @throws CloudproofException on Rust lib errors
     * @throws CloudproofException in case of other errors
     */
    public int createEncryptionCache(PublicKey publicKey) throws CloudproofException {
        byte[] publicKeyBytes = publicKey.bytes();
        Policy policy = Policy.fromAttributes(publicKey.attributes());
        return createEncryptionCache(policy, publicKeyBytes);
    }

    /**
     * Create an encryption cache that can be used with {@link #encryptHeaderUsingCache(int, String)} Use of the cache
     * speeds up the encryption of the header. WARN: the cache MUST be destroyed after use with
     * {@link #destroyEncryptionCache(int)}
     *
     * @param policy the {@link Policy} to cache
     * @param publicKeyBytes the public key bytes to cache
     * @return the cache handle that can be passed to the encryption routine
     * @throws CloudproofException on Rust lib errors
     * @throws CloudproofException in case of other errors
     */
    public int createEncryptionCache(Policy policy,
                                     byte[] publicKeyBytes)
        throws CloudproofException {
        // Public Key
        final Pointer publicKeyPointer = new Memory(publicKeyBytes.length);
        publicKeyPointer.write(0, publicKeyBytes, 0, publicKeyBytes.length);

        // Cache Handle
        IntByReference cacheHandle = new IntByReference();

        // cache ptr ptr
        unwrap(this.instance.h_create_encryption_cache(cacheHandle, policy.getBytes(),
            policy.getBytes().length,
            publicKeyPointer,
            publicKeyBytes.length));

        return cacheHandle.getValue();
    }

    /**
     * Destroy the cache created with {@link #createEncryptionCache(Policy, byte[])}
     *
     * @param cacheHandle the pointer to the cache to destroy
     * @throws CloudproofException on Rust lib errors
     * @throws CloudproofException in case of other errors
     */
    public void destroyEncryptionCache(int cacheHandle) throws CloudproofException {
        unwrap(this.instance.h_destroy_encryption_cache(cacheHandle));
    }

    /**
     * Generate an hybrid encryption header using a pre-cached Public Key and Policy. A symmetric key is randomly
     * generated and encrypted using the ABEschemes and the provided policy attributes for the given policy
     *
     * @param cacheHandle the pointer to the {@link int}
     * @param encryptionPolicy the encryption policy that determines the partitions to encrypt for
     * @return the encrypted header, bytes and symmetric key
     * @throws CloudproofException in case of native library error
     * @throws CloudproofException in case the {@link Policy} and key bytes cannot be recovered from the
     *             {@link PublicKey}
     */
    public EncryptedHeader encryptHeaderUsingCache(int cacheHandle,
                                                   String encryptionPolicy)
        throws CloudproofException {
        return encryptHeaderUsingCache(cacheHandle, encryptionPolicy, Optional.empty(), Optional.empty());
    }

    /**
     * Generate an hybrid encryption header using a pre-cached Public Key and Policy. A symmetric key is randomly
     * generated and encrypted using the ABE schemes and the provided policy attributes for the given policy. . If
     * provided, the resource `uid` and the `additionalData` are symmetrically encrypted and appended to the encrypted
     * header.
     *
     * @param cacheHandle the pointer to the {@link int}
     * @param encryptionPolicy the encryption policy that determines the partitions to encrypt for
     * @param additionalData optional additional data to encrypt and add to the header
     * @param authenticationData optional data used to authenticate the encryption of the additional data
     * @return the encrypted header, bytes and symmetric key
     * @throws CloudproofException in case of native library error
     * @throws CloudproofException in case the {@link Policy} and key bytes cannot be recovered from the
     *             {@link PublicKey}
     */
    public EncryptedHeader encryptHeaderUsingCache(int cacheHandle,
                                                   String encryptionPolicy,
                                                   Optional<byte[]> additionalData,
                                                   Optional<byte[]> authenticationData)
        throws CloudproofException {
        // Is a resource UID supplied
        int authenticationDataLength;
        if (authenticationData.isPresent()) {
            authenticationDataLength = authenticationData.get().length;
        } else {
            authenticationDataLength = 0;
        }

        // Are additional data supplied
        int additionalDataLength;
        if (additionalData.isPresent()) {
            additionalDataLength = additionalData.get().length;
        } else {
            additionalDataLength = 0;
        }

        // Symmetric Key OUT
        byte[] symmetricKeyBuffer = new byte[1024];
        IntByReference symmetricKeyBufferSize = new IntByReference(symmetricKeyBuffer.length);

        // Header Bytes OUT
        byte[] headerBytesBuffer = new byte[8192 + authenticationDataLength + additionalDataLength];
        IntByReference headerBytesBufferSize = new IntByReference(headerBytesBuffer.length);

        // Uid
        final Pointer uidPointer;
        if (authenticationDataLength == 0) {
            uidPointer = Pointer.NULL;
        } else {
            uidPointer = new Memory(authenticationDataLength);
            uidPointer.write(0, authenticationData.get(), 0, authenticationDataLength);
        }

        // Additional Data
        final Pointer additionalDataPointer;
        if (additionalDataLength == 0) {
            additionalDataPointer = Pointer.NULL;
        } else {
            additionalDataPointer = new Memory(additionalDataLength);
            additionalDataPointer.write(0, additionalData.get(), 0, additionalDataLength);
        }

        try {
            unwrap(this.instance.h_encrypt_header_using_cache(
                symmetricKeyBuffer, symmetricKeyBufferSize,
                headerBytesBuffer, headerBytesBufferSize,
                cacheHandle,
                encryptionPolicy,
                uidPointer, authenticationDataLength,
                additionalDataPointer, additionalDataLength));
        } catch (Throwable e) {
            e.printStackTrace();
            throw e;
        }

        return new EncryptedHeader(Arrays.copyOfRange(symmetricKeyBuffer, 0, symmetricKeyBufferSize.getValue()),
            Arrays.copyOfRange(headerBytesBuffer, 0, headerBytesBufferSize.getValue()));
    }

    /**
     * Generate an hybrid encryption header. A symmetric key is randomly generated and encrypted using the ABE schemes
     * and the provided encryption policy for the given policy
     *
     * @param publicKey the ABE public key also holds the {@link Policy}
     * @param encryptionPolicy the encryption policy that determines the partitions to encrypt for
     * @return the encrypted header, bytes and symmetric key
     * @throws CloudproofException in case of native library error
     * @throws CloudproofException in case the {@link Policy} and key bytes cannot be recovered from the
     *             {@link PublicKey}
     */
    public EncryptedHeader encryptHeader(PublicKey publicKey,
                                         String encryptionPolicy)
        throws CloudproofException {
        byte[] publicKeyBytes = publicKey.bytes();
        Policy policy = Policy.fromAttributes(publicKey.attributes());
        return encryptHeader(policy, publicKeyBytes, encryptionPolicy, Optional.empty(), Optional.empty());
    }

    /**
     * Generate an hybrid encryption header. A symmetric key is randomly generated and encrypted using the ABE schemes
     * and the provided encryption policy for the given policy. . If provided, the resource `uid` and the
     * `additionalData` are symmetrically encrypted and appended to the encrypted header.
     *
     * @param publicKey the ABE public key also holds the {@link Policy}
     * @param encryptionPolicy the encryption policy that determines the partitions to encrypt for
     * @param additionalData the additional data to encrypt and add to the header
     * @param authenticationData optional data used to authenticate the encryption of the additional data
     * @return the encrypted header, bytes and symmetric key
     * @throws CloudproofException in case of native library error
     * @throws CloudproofException in case the {@link Policy} and key bytes cannot be recovered from the
     *             {@link PublicKey}
     */
    public EncryptedHeader encryptHeader(PublicKey publicKey,
                                         String encryptionPolicy,
                                         Optional<byte[]> additionalData,
                                         Optional<byte[]> authenticationData)
        throws CloudproofException {
        byte[] publicKeyBytes = publicKey.bytes();
        Policy policy = Policy.fromAttributes(publicKey.attributes());
        return encryptHeader(policy, publicKeyBytes, encryptionPolicy, additionalData, authenticationData);
    }

    /**
     * Generate an hybrid encryption header. A symmetric key is randomly generated and encrypted using the ABE schemes
     * and the provided encryption policy for the given policy.
     *
     * @param policy the policy to use
     * @param publicKeyBytes the ABE public key bytes
     * @param encryptionPolicy the encryption policy that determines the partitions to encrypt for
     * @return the encrypted header, bytes and symmetric key
     * @throws CloudproofException in case of native library error
     */
    public EncryptedHeader encryptHeader(Policy policy,
                                         byte[] publicKeyBytes,
                                         String encryptionPolicy)
        throws CloudproofException {
        return encryptHeader(policy, publicKeyBytes, encryptionPolicy, Optional.empty(), Optional.empty());
    }

    /**
     * Generate an hybrid encryption header. A symmetric key is randomly generated and encrypted using the ABE schemes
     * and the provided encryption policy for the given policy. If provided, the additionalData` are symmetrically
     * encrypted and appended to the encrypted header. If provided the `authenticationData` are used as part of the
     * authentication of the symmetric encryption scheme.
     *
     * @param policy the policy to use
     * @param publicKeyBytes the ABE public key bytes
     * @param encryptionPolicy the encryption policy that determines the partitions to encrypt for
     * @param additionalData the additional data to encrypt and add to the header
     * @param authenticationData optional data used to authenticate the encryption of the additional data
     * @return the encrypted header, bytes and symmetric key
     * @throws CloudproofException in case of native library error
     */
    public EncryptedHeader encryptHeader(Policy policy,
                                         byte[] publicKeyBytes,
                                         String encryptionPolicy,
                                         Optional<byte[]> additionalData,
                                         Optional<byte[]> authenticationData)
        throws CloudproofException {

        // Is additional data supplied
        int additionalDataLength;
        if (additionalData.isPresent()) {
            additionalDataLength = additionalData.get().length;
        } else {
            additionalDataLength = 0;
        }

        // Is authenticated data supplied
        int authenticationDataLength;
        if (authenticationData.isPresent()) {
            authenticationDataLength = authenticationData.get().length;
        } else {
            authenticationDataLength = 0;
        }

        // Symmetric Key OUT
        byte[] symmetricKeyBuffer = new byte[1024];
        IntByReference symmetricKeyBufferSize = new IntByReference(symmetricKeyBuffer.length);

        // Header Bytes OUT
        byte[] headerBytesBuffer = new byte[8192 + additionalDataLength + authenticationDataLength];
        IntByReference headerBytesBufferSize = new IntByReference(headerBytesBuffer.length);

        // Public Key
        final Pointer publicKeyPointer = new Memory(publicKeyBytes.length);
        publicKeyPointer.write(0, publicKeyBytes, 0, publicKeyBytes.length);

        // Uid
        final Pointer additionalDataPointer;
        if (additionalDataLength == 0) {
            additionalDataPointer = Pointer.NULL;
        } else {
            additionalDataPointer = new Memory(additionalDataLength);
            additionalDataPointer.write(0, additionalData.get(), 0, additionalDataLength);
        }

        // Additional Data
        final Pointer authenticationDataPointer;
        if (authenticationDataLength == 0) {
            authenticationDataPointer = Pointer.NULL;
        } else {
            authenticationDataPointer = new Memory(authenticationDataLength);
            authenticationDataPointer.write(0, authenticationData.get(), 0, authenticationDataLength);
        }

        unwrap(this.instance.h_encrypt_header(
            symmetricKeyBuffer, symmetricKeyBufferSize,
            headerBytesBuffer, headerBytesBufferSize,
            policy.getBytes(),
            policy.getBytes().length,
            publicKeyPointer, publicKeyBytes.length,
            encryptionPolicy,
            additionalDataPointer, additionalDataLength,
            authenticationDataPointer, authenticationDataLength));

        return new EncryptedHeader(Arrays.copyOfRange(symmetricKeyBuffer, 0, symmetricKeyBufferSize.getValue()),
            Arrays.copyOfRange(headerBytesBuffer, 0, headerBytesBufferSize.getValue()));
    }

    // -----------------------------------------------
    // Header Decryption
    // -----------------------------------------------

    /**
     * Create an decryption cache that can be used with {@link #decryptHeaderUsingCache(int, byte[])} Use of the cache
     * speeds up decryption of the header WARN: the cache MUST be destroyed after use with
     * {@link #destroyDecryptionCache(int)}
     *
     * @param userDecryptionKey the public key to cache
     * @return the cache handle that can be passed to the decryption routine
     * @throws CloudproofException on Rust lib errors
     * @throws CloudproofException in case of other errors
     */
    public int createDecryptionCache(PrivateKey userDecryptionKey) throws CloudproofException {
        byte[] userDecryptionKeyBytes = userDecryptionKey.bytes();
        return createDecryptionCache(userDecryptionKeyBytes);
    }

    /**
     * Create a decryption cache that can be used with {@link #decryptHeaderUsingCache(int, byte[])} Use of the cache
     * speeds up the decryption of the header. WARN: the cache MUST be destroyed after use with
     * {@link #destroyDecryptionCache(int)}
     *
     * @param userDecryptionKeyBytes the public key bytes to cache
     * @return the cache handle that can be passed to the decryption routine
     * @throws CloudproofException on Rust lib errors
     * @throws CloudproofException in case of other errors
     */
    public int createDecryptionCache(byte[] userDecryptionKeyBytes) throws CloudproofException {

        // Public Key
        final Pointer userDecryptionKeyPointer = new Memory(userDecryptionKeyBytes.length);
        userDecryptionKeyPointer.write(0, userDecryptionKeyBytes, 0, userDecryptionKeyBytes.length);

        // Cache Handle
        IntByReference cacheHandle = new IntByReference();

        unwrap(this.instance.h_create_decryption_cache(cacheHandle, userDecryptionKeyPointer,
            userDecryptionKeyBytes.length));

        return cacheHandle.getValue();
    }

    /**
     * Destroy the cache created with {@link #createDecryptionCache(byte[])}
     *
     * @param cacheHandle the pointer to the cache to destroy
     * @throws CloudproofException on Rust lib errors
     * @throws CloudproofException in case of other errors
     */
    public void destroyDecryptionCache(int cacheHandle) throws CloudproofException {
        unwrap(this.instance.h_destroy_decryption_cache(cacheHandle));
    }

    /**
     * Decrypt a hybrid header using a cache, recovering the symmetric key
     *
     * @param cacheHandle the cache to the user decryption key
     * @param encryptedHeaderBytes the encrypted header
     * @return The decrypted header: symmetric key, uid and additional data
     * @throws CloudproofException in case of native library error
     * @throws CloudproofException in case the key bytes cannot be recovered from the {@link PrivateKey}
     */
    public DecryptedHeader decryptHeaderUsingCache(int cacheHandle,
                                                   byte[] encryptedHeaderBytes)
        throws CloudproofException {
        return decryptHeaderUsingCache(cacheHandle, encryptedHeaderBytes, 0, Optional.empty());
    }

    /**
     * Decrypt a hybrid header using a cache, recovering the symmetric key, and optionally, the resource UID and
     * additional data
     *
     * @param cacheHandle the cache to the user decryption key
     * @param encryptedHeaderBytes the encrypted header
     * @param additionalDataLen the maximum bytes length of the expected additional data
     * @param authenticationData optional data used to authenticate the encryption of the additional data
     * @return The decrypted header: symmetric key, uid and additional data
     * @throws CloudproofException in case of native library error
     */
    public DecryptedHeader decryptHeaderUsingCache(int cacheHandle,
                                                   byte[] encryptedHeaderBytes,
                                                   int additionalDataLen,
                                                   Optional<byte[]> authenticationData)
        throws CloudproofException {

        // Symmetric Key OUT
        byte[] symmetricKeyBuffer = new byte[1024];
        IntByReference symmetricKeyBufferSize = new IntByReference(symmetricKeyBuffer.length);

        // Additional Data OUT
        byte[] additionalDataBuffer = new byte[additionalDataLen];
        IntByReference additionalDataBufferSize = new IntByReference(additionalDataBuffer.length);

        // encrypted bytes
        final Pointer encryptedHeaderBytesPointer = new Memory(encryptedHeaderBytes.length);
        encryptedHeaderBytesPointer.write(0, encryptedHeaderBytes, 0, encryptedHeaderBytes.length);

        // Is authenticated data supplied
        final Pointer authenticationDataPointer;
        final int authenticationDataLen;
        if (additionalDataLen > 0) {
            if (authenticationData.isPresent()) {
                authenticationDataLen = authenticationData.get().length;
                authenticationDataPointer = new Memory(authenticationDataLen);
                authenticationDataPointer.write(0, authenticationData.get(), 0, authenticationDataLen);
            } else {
                authenticationDataPointer = Pointer.NULL;
                authenticationDataLen = 0;
            }
        } else {
            authenticationDataPointer = Pointer.NULL;
            authenticationDataLen = 0;
        }

        unwrap(this.instance.h_decrypt_header_using_cache(
            symmetricKeyBuffer, symmetricKeyBufferSize,
            additionalDataBuffer, additionalDataBufferSize,
            encryptedHeaderBytesPointer, encryptedHeaderBytes.length,
            authenticationDataPointer, authenticationDataLen,
            cacheHandle));

        return new DecryptedHeader(
            Arrays.copyOfRange(symmetricKeyBuffer, 0, symmetricKeyBufferSize.getValue()),
            authenticationDataLen > 0 ? authenticationData.get() : new byte[] {},
            additionalDataLen > 0 ? Arrays.copyOfRange(additionalDataBuffer, 0, additionalDataBufferSize.getValue())
                : new byte[] {});
    }

    /**
     * Decrypt a hybrid header, recovering the symmetric key
     *
     * @param userDecryptionKey the ABE user decryption key
     * @param encryptedHeaderBytes the encrypted header
     * @return The decrypted header: symmetric key, uid and additional data
     * @throws CloudproofException in case of native library error
     * @throws CloudproofException in case the key bytes cannot be recovered from the {@link PrivateKey}
     */
    public DecryptedHeader decryptHeader(PrivateKey userDecryptionKey,
                                         byte[] encryptedHeaderBytes)
        throws CloudproofException {
        return decryptHeader(userDecryptionKey.bytes(), encryptedHeaderBytes, 0, Optional.empty());
    }

    /**
     * Decrypt a hybrid header, recovering the symmetric key, and optionally, the resource UID and additional data
     *
     * @param userDecryptionKey the ABE user decryption key
     * @param encryptedHeaderBytes the encrypted header
     * @param additionalDataLen the maximum bytes length of the expected additional data
     * @param authenticationData optional data used to authenticate the encryption of the additional data
     * @return The decrypted header: symmetric key, uid and additional data
     * @throws CloudproofException in case of native library error
     * @throws CloudproofException in case the key bytes cannot be recovered from the {@link PrivateKey}
     */
    public DecryptedHeader decryptHeader(PrivateKey userDecryptionKey,
                                         byte[] encryptedHeaderBytes,
                                         int additionalDataLen,
                                         Optional<byte[]> authenticationData)
        throws CloudproofException {
        return decryptHeader(userDecryptionKey.bytes(), encryptedHeaderBytes, additionalDataLen, authenticationData);
    }

    /**
     * Decrypt a hybrid header, recovering the symmetric key, and optionally, the resource UID and additional data
     *
     * @param userDecryptionKeyBytes the ABE user decryption key bytes
     * @param encryptedHeaderBytes the encrypted header
     * @return The decrypted header: symmetric key, uid and additional data
     * @throws CloudproofException in case of native library error
     */
    public DecryptedHeader decryptHeader(byte[] userDecryptionKeyBytes,
                                         byte[] encryptedHeaderBytes)
        throws CloudproofException {
        return decryptHeader(userDecryptionKeyBytes, encryptedHeaderBytes, 0, Optional.empty());
    }

    /**
     * Decrypt a hybrid header, recovering the symmetric key, and optionally, the resource UID and additional data
     *
     * @param userDecryptionKeyBytes the ABE user decryption key bytes
     * @param encryptedHeaderBytes the encrypted header
     * @param additionalDataLen the maximum bytes length of the expected additional data
     * @param authenticationData optional data used to authenticate the encryption of the additional data
     * @return The decrypted header: symmetric key, uid and additional data
     * @throws CloudproofException in case of native library error
     */
    public DecryptedHeader decryptHeader(byte[] userDecryptionKeyBytes,
                                         byte[] encryptedHeaderBytes,
                                         int additionalDataLen,
                                         Optional<byte[]> authenticationData)
        throws CloudproofException {

        // Symmetric Key OUT
        byte[] symmetricKeyBuffer = new byte[1024];
        IntByReference symmetricKeyBufferSize = new IntByReference(symmetricKeyBuffer.length);

        // Additional Data OUT
        byte[] additionalDataBuffer = new byte[additionalDataLen];
        IntByReference additionalDataBufferSize = new IntByReference(additionalDataBuffer.length);

        // encrypted bytes
        final Pointer encryptedHeaderBytesPointer = new Memory(encryptedHeaderBytes.length);
        encryptedHeaderBytesPointer.write(0, encryptedHeaderBytes, 0, encryptedHeaderBytes.length);

        // Is authenticated data supplied
        final Pointer authenticationDataPointer;
        final int authenticationDataLen;
        if (additionalDataLen > 0) {
            if (authenticationData.isPresent()) {
                authenticationDataLen = authenticationData.get().length;
                authenticationDataPointer = new Memory(authenticationDataLen);
                authenticationDataPointer.write(0, authenticationData.get(), 0, authenticationDataLen);
            } else {
                authenticationDataPointer = Pointer.NULL;
                authenticationDataLen = 0;
            }
        } else {
            authenticationDataPointer = Pointer.NULL;
            authenticationDataLen = 0;
        }

        // User Decryption Key
        final Pointer userDecryptionKeyPointer = new Memory(userDecryptionKeyBytes.length);
        userDecryptionKeyPointer.write(0, userDecryptionKeyBytes, 0, userDecryptionKeyBytes.length);

        unwrap(this.instance.h_decrypt_header(
            symmetricKeyBuffer, symmetricKeyBufferSize,
            additionalDataBuffer, additionalDataBufferSize,
            encryptedHeaderBytesPointer, encryptedHeaderBytes.length,
            authenticationDataPointer, authenticationDataLen,
            userDecryptionKeyPointer, userDecryptionKeyBytes.length));

        return new DecryptedHeader(Arrays.copyOfRange(symmetricKeyBuffer, 0, symmetricKeyBufferSize.getValue()),
            authenticationDataLen > 0 ? authenticationData.get() : new byte[] {},
            additionalDataLen > 0 ? Arrays.copyOfRange(additionalDataBuffer, 0, additionalDataBufferSize.getValue())
                : new byte[] {});
    }

    /**
     * The overhead in bytes (over the clear text) generated by the symmetric encryption scheme (AES 256 GCM)
     *
     * @return the overhead bytes
     */
    public int symmetricEncryptionOverhead() {
        return this.instance.h_symmetric_encryption_overhead();
    }

    /**
     * Symmetrically encrypt a block of clear text data. No resource UID is used for authentication and the block number
     * is assumed to be zero
     *
     * @param symmetricKey The key to use to symmetrically encrypt the block
     * @param clearText the clear text to encrypt
     * @return the encrypted block
     * @throws CloudproofException in case of native library error
     */
    public byte[] encryptBlock(byte[] symmetricKey,
                               byte[] clearText)
        throws CloudproofException {
        return encryptBlock(symmetricKey, new byte[] {}, clearText);
    }

    /**
     * Symmetrically encrypt a block of clear text data. The UID and Block Number are part of the AEAD of the symmetric
     * scheme.
     *
     * @param symmetricKey The key to use to symmetrically encrypt the block
     * @param authenticationData The associated Data used to authenticate the symmetric encryption
     * @param clearText the clear text to encrypt
     * @return the encrypted block
     * @throws CloudproofException in case of native library error
     */
    public byte[] encryptBlock(byte[] symmetricKey,
                               byte[] authenticationData,
                               byte[] clearText)
        throws CloudproofException {

        // Ciphertext OUT
        byte[] ciphertextBuffer = new byte[this.instance.h_symmetric_encryption_overhead() + clearText.length];
        IntByReference ciphertextBufferSize = new IntByReference(ciphertextBuffer.length);

        // Symmetric Key
        final Pointer symmetricKeyPointer = new Memory(symmetricKey.length);
        symmetricKeyPointer.write(0, symmetricKey, 0, symmetricKey.length);

        // Uid
        final Pointer associatedDataPointer;
        if (authenticationData.length > 0) {
            associatedDataPointer = new Memory(authenticationData.length);
            associatedDataPointer.write(0, authenticationData, 0, authenticationData.length);
        } else {
            associatedDataPointer = Pointer.NULL;
        }

        // Additional Data
        final Pointer dataPointer = new Memory(clearText.length);
        dataPointer.write(0, clearText, 0, clearText.length);

        unwrap(this.instance.h_dem_encrypt(
            ciphertextBuffer, ciphertextBufferSize,
            symmetricKeyPointer, symmetricKey.length,
            associatedDataPointer, authenticationData.length,
            dataPointer, clearText.length));

        return Arrays.copyOfRange(ciphertextBuffer, 0, ciphertextBufferSize.getValue());
    }

    /**
     * Symmetrically decrypt a block of encrypted data. No resource UID is used for authentication and the block number
     * is assumed to be zero
     *
     * @param symmetricKey the symmetric key to use
     * @param encryptedBytes the encrypted block bytes
     * @return the clear text bytes
     * @throws CloudproofException in case of native library error
     */
    public byte[] decryptBlock(byte[] symmetricKey,
                               byte[] encryptedBytes)
        throws CloudproofException {

        return decryptBlock(symmetricKey, new byte[] {}, encryptedBytes);
    }

    /**
     * Symmetrically decrypt a block of encrypted data. The resource UID and block Number must match those supplied on
     * encryption or decryption will fail.
     *
     * @param symmetricKey the symmetric key to use
     * @param authenticationData The associated Data used to authenticate the symmetric encryption
     * @param encryptedBytes the encrypted block bytes
     * @return the clear text bytes
     * @throws CloudproofException in case of native library error
     */
    public byte[] decryptBlock(byte[] symmetricKey,
                               byte[] authenticationData,
                               byte[] encryptedBytes)
        throws CloudproofException {

        // Clear Text Bytes OUT
        byte[] clearTextBuffer = new byte[encryptedBytes.length - this.instance.h_symmetric_encryption_overhead()];
        IntByReference clearTextBufferSize = new IntByReference(clearTextBuffer.length);

        // Symmetric Key
        final Pointer symmetricKeyPointer = new Memory(symmetricKey.length);
        symmetricKeyPointer.write(0, symmetricKey, 0, symmetricKey.length);

        // Uid
        final Pointer authenticationDataPointer;
        if (authenticationData.length > 0) {
            authenticationDataPointer = new Memory(authenticationData.length);
            authenticationDataPointer.write(0, authenticationData, 0, authenticationData.length);
        } else {
            authenticationDataPointer = Pointer.NULL;
        }

        // Encrypted Data
        final Pointer encryptedBytesPointer = new Memory(encryptedBytes.length);
        encryptedBytesPointer.write(0, encryptedBytes, 0, encryptedBytes.length);

        unwrap(this.instance.h_dem_decrypt(
            clearTextBuffer, clearTextBufferSize,
            symmetricKeyPointer, symmetricKey.length,
            authenticationDataPointer, authenticationData.length,
            encryptedBytesPointer, encryptedBytes.length));

        return Arrays.copyOfRange(clearTextBuffer, 0, clearTextBufferSize.getValue());
    }

    /**
     * Generate the master private and public keys using the ABE policy
     *
     * @param policy the policy to use
     * @return the master private and public keys in raw bytes
     * @throws CloudproofException in case of native library error
     */
    public MasterKeys generateMasterKeys(Policy policy) throws CloudproofException {
        // Master keys Bytes OUT
        byte[] masterPrivateKeyBuffer = new byte[8 * 1024];
        IntByReference masterPrivateKeyBufferSize = new IntByReference(masterPrivateKeyBuffer.length);
        byte[] masterPublicKeyBuffer = new byte[8 * 1024];
        IntByReference masterPublicKeyBufferSize = new IntByReference(masterPublicKeyBuffer.length);

        int ffiCode = this.instance.h_generate_master_keys(masterPrivateKeyBuffer, masterPrivateKeyBufferSize,
            masterPublicKeyBuffer, masterPublicKeyBufferSize, policy.getBytes(), policy.getBytes().length);

        if (ffiCode != 0) {
            // Retry with correct allocated size
            masterPrivateKeyBuffer = new byte[masterPrivateKeyBufferSize.getValue()];
            masterPublicKeyBuffer = new byte[masterPublicKeyBufferSize.getValue()];
            unwrap(this.instance.h_generate_master_keys(masterPrivateKeyBuffer, masterPrivateKeyBufferSize,
                masterPublicKeyBuffer, masterPublicKeyBufferSize, policy.getBytes(),
                policy.getBytes().length));
        }

        byte[] privateKey = Arrays.copyOfRange(masterPrivateKeyBuffer, 0, masterPrivateKeyBufferSize.getValue());
        byte[] publicKey = Arrays.copyOfRange(masterPublicKeyBuffer, 0, masterPublicKeyBufferSize.getValue());

        return new MasterKeys(privateKey, publicKey);
    }

    /**
     * Generates a user private key based on a master private key, a user policy string, and a policy object.
     *
     * @param masterPrivateKey the master private key used to generate the user private key
     * @param userPolicy the string representation of the user policy
     * @param policy the policy object to be used in the generation of the user private key
     * @return the generated user private key
     * @throws CloudproofException if the user private key cannot be generated
     */
    public byte[] generateUserPrivateKey(byte[] masterPrivateKey,
                                         String userPolicy,
                                         Policy policy)
        throws CloudproofException {

        return generateUserPrivateKey_(masterPrivateKey, userPolicy, policy);
    }

    /**
     * Generate the user private key
     *
     * @param masterPrivateKey the master private key in bytes
     * @param accessPolicy the access policy of the user private key as an AccessPolicy instance
     * @param policy the ABE policy
     * @return the corresponding user private key
     * @throws CloudproofException in case of native library error
     */
    public byte[] generateUserPrivateKey(byte[] masterPrivateKey,
                                         AccessPolicy accessPolicy,
                                         Policy policy)
        throws CloudproofException {

        // Access Policy
        String accessPolicyJson;
        try {
            accessPolicyJson = mapper.writeValueAsString(accessPolicy);
        } catch (JsonProcessingException e) {
            throw new CloudproofException("Invalid Access Policy", e);
        }

        return generateUserPrivateKey_(masterPrivateKey, accessPolicyJson, policy);

    }

    /**
     * Generate the user private key
     *
     * @param masterPrivateKey the master private key in bytes
     * @param userPolicy the access policy of the user private key as a JSON version of an AccessPolicy instance
     * @param policy the ABE policy
     * @return the corresponding user private key
     * @throws CloudproofException in case of native library error
     */
    byte[] generateUserPrivateKey_(byte[] masterPrivateKey,
                                   String userPolicy,
                                   Policy policy)
        throws CloudproofException {
        // User private key Bytes OUT
        byte[] userPrivateKeyBuffer = new byte[8192];
        IntByReference userPrivateKeyBufferSize = new IntByReference(userPrivateKeyBuffer.length);

        // Master private key
        try (final Memory masterPrivateKeyPointer = new Memory(masterPrivateKey.length)) {
            masterPrivateKeyPointer.write(0, masterPrivateKey, 0, masterPrivateKey.length);

            int ffiCode = this.instance.h_generate_user_secret_key(userPrivateKeyBuffer, userPrivateKeyBufferSize,
                masterPrivateKeyPointer, masterPrivateKey.length, userPolicy, policy.getBytes(),
                policy.getBytes().length);
            if (ffiCode != 0) {
                // Retry with correct allocated size
                userPrivateKeyBuffer = new byte[userPrivateKeyBufferSize.getValue()];
                ffiCode = this.instance.h_generate_user_secret_key(userPrivateKeyBuffer, userPrivateKeyBufferSize,
                    masterPrivateKeyPointer, masterPrivateKey.length, userPolicy, policy.getBytes(),
                    policy.getBytes().length);
                if (ffiCode != 0) {
                    throw new CloudproofException(get_last_error(4096));
                }
            }
            return Arrays.copyOfRange(userPrivateKeyBuffer, 0, userPrivateKeyBufferSize.getValue());
        }
    }

    /**
     * Generate an hybrid encryption of a plaintext.
     *
     * @param policy the policy to use
     * @param publicKeyBytes the ABE public key bytes
     * @param encryptionPolicy the encryption policy that determines the partitions to encrypt for
     * @param plaintext the plaintext to encrypt
     * @return the ciphertext
     * @throws CloudproofException in case of native library error
     */
    public byte[] encrypt(Policy policy,
                          byte[] publicKeyBytes,
                          String encryptionPolicy,
                          byte[] plaintext)
        throws CloudproofException {

        return encrypt(policy, publicKeyBytes, encryptionPolicy, plaintext, Optional.empty(),
            Optional.empty());
    }

    /**
     * Generate an hybrid encryption of a plaintext. The `authenticationData` are used as part of the authentication of
     * the symmetric encryption scheme.
     *
     * @param policy the policy to use
     * @param publicKeyBytes the ABE public key bytes
     * @param encryptionPolicy the encryption policy that determines the partitions to encrypt for
     * @param plaintext the plaintext to encrypt
     * @param authenticationData data used to authenticate the symmetric encryption
     * @return the ciphertext
     * @throws CloudproofException in case of native library error
     */
    public byte[] encrypt(Policy policy,
                          byte[] publicKeyBytes,
                          String encryptionPolicy,
                          byte[] plaintext,
                          byte[] authenticationData)
        throws CloudproofException {

        return encrypt(policy, publicKeyBytes, encryptionPolicy, plaintext, Optional.of(authenticationData),
            Optional.empty());
    }

    /**
     * Generate an hybrid encryption of a plaintext. The `authenticationData` are used as part of the authentication of
     * the symmetric encryption scheme.
     *
     * @param policy the policy to use
     * @param publicKeyBytes the ABE public key bytes
     * @param encryptionPolicy the encryption policy that determines the partitions to encrypt for
     * @param plaintext the plaintext to encrypt
     * @param authenticationData data used to authenticate the symmetric encryption
     * @param headerMetadata additional data to encrypt and add to the header
     * @return the ciphertext
     * @throws CloudproofException in case of native library error
     */
    public byte[] encrypt(Policy policy,
                          byte[] publicKeyBytes,
                          String encryptionPolicy,
                          byte[] plaintext,
                          byte[] authenticationData,
                          byte[] headerMetadata)
        throws CloudproofException {

        return encrypt(policy, publicKeyBytes, encryptionPolicy, plaintext, Optional.of(
            authenticationData), Optional.of(headerMetadata));
    }

    /**
     * Generate an hybrid encryption of a plaintext. If provided, the additionalData` are symmetrically encrypted and
     * appended to the encrypted header. If provided the `authenticationData` are used as part of the authentication of
     * the symmetric encryption scheme.
     *
     * @param policy the policy to use
     * @param publicKeyBytes the ABE public key bytes
     * @param encryptionPolicy the encryption policy that determines the partitions to encrypt for
     * @param plaintext the plaintext to encrypt
     * @param authenticationData optional data used to authenticate the symmetric encryption
     * @param headerMetadata the additional data to encrypt and add to the header
     * @return the ciphertext
     * @throws CloudproofException in case of native library error
     */
    byte[] encrypt(Policy policy,
                   byte[] publicKeyBytes,
                   String encryptionPolicy,
                   byte[] plaintext,
                   Optional<byte[]> authenticationData,
                   Optional<byte[]> headerMetadata)
        throws CloudproofException {

        // Is additional data supplied
        int additionalDataLength;
        if (headerMetadata.isPresent()) {
            additionalDataLength = headerMetadata.get().length;
        } else {
            additionalDataLength = 0;
        }

        // Is authenticated data supplied
        int authenticationDataLength;
        if (authenticationData.isPresent()) {
            authenticationDataLength = authenticationData.get().length;
        } else {
            authenticationDataLength = 0;
        }

        // Ciphertext OUT
        byte[] ciphertext = new byte[8192 + plaintext.length];
        IntByReference ciphertextSize = new IntByReference(ciphertext.length);

        // Public Key
        final Pointer publicKeyPointer = new Memory(publicKeyBytes.length);
        publicKeyPointer.write(0, publicKeyBytes, 0, publicKeyBytes.length);

        // plaintext
        final Pointer plaintextPointer = new Memory(plaintext.length);
        plaintextPointer.write(0, plaintext, 0, plaintext.length);

        // additional data
        final Pointer additionalDataPointer;
        if (additionalDataLength == 0) {
            additionalDataPointer = Pointer.NULL;
        } else {
            additionalDataPointer = new Memory(additionalDataLength);
            additionalDataPointer.write(0, headerMetadata.get(), 0, additionalDataLength);
        }

        // Additional Data
        final Pointer authenticationDataPointer;
        if (authenticationDataLength == 0) {
            authenticationDataPointer = Pointer.NULL;
        } else {
            authenticationDataPointer = new Memory(authenticationDataLength);
            authenticationDataPointer.write(0, authenticationData.get(), 0, authenticationDataLength);
        }

        unwrap(instance.h_hybrid_encrypt(
            ciphertext, ciphertextSize,
            policy.getBytes(),
            policy.getBytes().length,
            publicKeyPointer, publicKeyBytes.length,
            encryptionPolicy,
            plaintextPointer, plaintext.length,
            additionalDataPointer, additionalDataLength,
            authenticationDataPointer, authenticationDataLength));

        return Arrays.copyOfRange(ciphertext, 0, ciphertextSize.getValue());
    }

    /**
     * Decrypt a hybrid encryption
     *
     * @param userDecryptionKeyBytes the ABE user decryption key bytes
     * @param ciphertext the ciphertext to decrypt
     * @return the {@link DecryptedData} containing the plaintext and optional header metadata
     * @throws CloudproofException in case of native library error
     */
    public DecryptedData decrypt(byte[] userDecryptionKeyBytes,
                                 byte[] ciphertext)
        throws CloudproofException {
        return decrypt(userDecryptionKeyBytes, ciphertext, Optional.empty());
    }

    /**
     * Decrypt a hybrid encryption
     *
     * @param userDecryptionKeyBytes the ABE user decryption key bytes
     * @param ciphertext the ciphertext to decrypt
     * @param authenticationData data used to authenticate the symmetric encryption
     * @return the {@link DecryptedData} containing the plaintext and optional header metadata
     * @throws CloudproofException in case of native library error
     */
    public DecryptedData decrypt(byte[] userDecryptionKeyBytes,
                                 byte[] ciphertext,
                                 byte[] authenticationData)
        throws CloudproofException {
        return decrypt(userDecryptionKeyBytes, ciphertext, Optional.of(authenticationData));
    }

    /**
     * Decrypt a hybrid encryption
     *
     * @param userDecryptionKeyBytes the ABE user decryption key bytes
     * @param ciphertext the ciphertext to decrypt
     * @param authenticationData optional data used to authenticate the symmetric encryption
     * @return the {@link DecryptedData} containing the plaintext and optional header metadata
     * @throws CloudproofException in case of native library error
     */
    DecryptedData decrypt(byte[] userDecryptionKeyBytes,
                          byte[] ciphertext,
                          Optional<byte[]> authenticationData)
        throws CloudproofException {

        // plaintext OUT
        byte[] plaintext = new byte[ciphertext.length]; // safe: plaintext should be smaller than cipher text
        IntByReference plaintextSize = new IntByReference(plaintext.length);

        // additional OUT
        byte[] additionalData = new byte[4096 * 4]; // size ?
        IntByReference additionalDataSize = new IntByReference(additionalData.length);

        // encrypted bytes
        final Pointer ciphertextPointer = new Memory(ciphertext.length);
        ciphertextPointer.write(0, ciphertext, 0, ciphertext.length);

        // Is authenticated data supplied
        final Pointer authenticationDataPointer;
        final int authenticationDataLen;
        if (authenticationData.isPresent() && authenticationData.get().length > 0) {
            authenticationDataLen = authenticationData.get().length;
            authenticationDataPointer = new Memory(authenticationDataLen);
            authenticationDataPointer.write(0, authenticationData.get(), 0, authenticationDataLen);
        } else {
            authenticationDataPointer = Pointer.NULL;
            authenticationDataLen = 0;
        }

        // User Decryption Key
        final Pointer userDecryptionKeyPointer = new Memory(userDecryptionKeyBytes.length);
        userDecryptionKeyPointer.write(0, userDecryptionKeyBytes, 0, userDecryptionKeyBytes.length);

        unwrap(this.instance.h_hybrid_decrypt(
            plaintext, plaintextSize,
            additionalData, additionalDataSize,
            ciphertextPointer, ciphertext.length,
            authenticationDataPointer, authenticationDataLen,
            userDecryptionKeyPointer, userDecryptionKeyBytes.length));

        return new DecryptedData(
            Arrays.copyOfRange(plaintext, 0, plaintextSize.getValue()),
            Arrays.copyOfRange(additionalData, 0, additionalDataSize.getValue()));
    }
}
