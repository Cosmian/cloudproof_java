package com.cosmian.jna;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Optional;

import com.cosmian.CosmianException;
import com.cosmian.jna.abe.DecryptedHeader;
import com.cosmian.jna.abe.EncryptedHeader;
import com.cosmian.jna.abe.FfiWrapper;
import com.cosmian.rest.abe.acccess_policy.Attr;
import com.cosmian.rest.abe.policy.Policy;
import com.cosmian.rest.kmip.objects.PrivateKey;
import com.cosmian.rest.kmip.objects.PublicKey;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.jna.Memory;
import com.sun.jna.Pointer;
import com.sun.jna.ptr.IntByReference;

public class Ffi {

    /**
     * Return the last error in a String that does not exceed 1023 bytes
     * 
     * @return the last error recorded by the native library
     * @throws FfiException in case of native library error
     */
    public String get_last_error() throws FfiException {
        return get_last_error(1023);
    }

    /**
     * Return the last error in a String that does not exceed `max_len` bytes
     * 
     * @param max_len the maximum number of bytes to return
     * @throws FfiException in case of native library error
     * @return the error
     */
    public String get_last_error(int max_len) throws FfiException {
        if (max_len < 1) {
            throw new FfiException("get_last_error: max_lem must be at least one");
        }
        byte[] output = new byte[max_len + 1];
        IntByReference outputSize = new IntByReference(output.length);
        if (FfiWrapper.INSTANCE.get_last_error(output, outputSize) == 0) {
            return new String(Arrays.copyOfRange(output, 0, outputSize.getValue()), StandardCharsets.UTF_8);
        }
        throw new FfiException("Failed retrieving the last error; check the debug logs");
    }

    /**
     * Set the last error on the native lib
     * 
     * @param error_msg the last error to set on the native lib
     * @throws FfiException n case of native library error
     */
    public void set_error(String error_msg) throws FfiException {
        unwrap(FfiWrapper.INSTANCE.set_error(error_msg));
    }

    /**
     * Generate an hybrid encryption header.
     * 
     * A symmetric key is randomly generated and encrypted using the ABE schemes and
     * the provided policy attributes for the given policy
     * 
     * @param publicKey  the ABE public key also holds the {@link Policy}
     * @param attributes the policy attributes used to encrypt the generated
     *                   symmetric key
     * @return the encrypted header, bytes and symmetric key
     * @throws FfiException     in case of native library error
     * @throws CosmianException in case the {@link Policy} and key bytes cannot be
     *                          recovered from the {@link PublicKey}
     */
    public EncryptedHeader encryptHeader(PublicKey publicKey, Attr[] attributes) throws FfiException, CosmianException {
        byte[] publicKeyBytes = publicKey.bytes();
        Policy policy = Policy.fromVendorAttributes(publicKey.attributes());
        return encryptHeader(policy, publicKeyBytes, attributes, Optional.empty(), Optional.empty());
    }

    /**
     * Generate an hybrid encryption header.
     * 
     * A symmetric key is randomly generated and encrypted using the ABE schemes and
     * the provided policy attributes for the given policy.
     * .
     * If provided, the resource `uid` and the `additionalData` are symmetrically
     * encrypted and appended to the encrypted header.
     * 
     * @param publicKey      the ABE public key also holds the {@link Policy}
     * @param attributes     the policy attributes used to encrypt the generated
     *                       symmetric key
     * @param uid            the optional resource uid
     * @param additionalData optional additional data
     * @return the encrypted header, bytes and symmetric key
     * @throws FfiException     in case of native library error
     * @throws CosmianException in case the {@link Policy} and key bytes cannot be
     *                          recovered from the {@link PublicKey}
     */
    public EncryptedHeader encryptHeader(PublicKey publicKey, Attr[] attributes, Optional<byte[]> uid,
            Optional<byte[]> additionalData) throws FfiException, CosmianException {
        byte[] publicKeyBytes = publicKey.bytes();
        Policy policy = Policy.fromVendorAttributes(publicKey.attributes());
        return encryptHeader(policy, publicKeyBytes, attributes, uid, additionalData);
    }

    /**
     * Generate an hybrid encryption header.
     * 
     * A symmetric key is randomly generated and encrypted using the ABE schemes and
     * the provided policy attributes for the given policy.
     * 
     * If provided, the resource `uid` and the `additionalData` are symmetrically
     * encrypted and appended to the encrypted header.
     * 
     * @param policy         the policy to use
     * @param publicKeyBytes the ABE public key bytes
     * @param attributes     the policy attributes used to encrypt the generated
     *                       symmetric key
     * @param uid            the optional resource uid
     * @param additionalData optional additional data
     * @return the encrypted header, bytes and symmetric key
     * @throws FfiException in case of native library error
     */
    public EncryptedHeader encryptHeader(Policy policy, byte[] publicKeyBytes, Attr[] attributes, Optional<byte[]> uid,
            Optional<byte[]> additionalData) throws FfiException {

        // Is a resource UID supplied
        int uidLength;
        if (uid.isPresent()) {
            uidLength = uid.get().length;
        } else {
            uidLength = 0;
        }

        // Are additional data supplied
        int adLength;
        if (uid.isPresent()) {
            adLength = additionalData.get().length;
        } else {
            adLength = 0;
        }

        // For the JSON strings
        ObjectMapper mapper = new ObjectMapper();

        // Symmetric Key OUT
        byte[] symmetricKeyBuffer = new byte[1024];
        IntByReference symmetricKeyBufferSize = new IntByReference(symmetricKeyBuffer.length);

        // Header Bytes OUT
        byte[] headerBytesBuffer = new byte[8192 + uidLength + adLength];
        IntByReference headerBytesBufferSize = new IntByReference(headerBytesBuffer.length);

        // Policy
        String policyJson;
        try {
            policyJson = mapper.writeValueAsString(policy);
        } catch (JsonProcessingException e) {
            throw new FfiException("Invalid Policy");
        }

        // Public Key
        final Pointer publicKeyPointer = new Memory(publicKeyBytes.length);
        publicKeyPointer.write(0, publicKeyBytes, 0, publicKeyBytes.length);

        // Attributes
        // The value must be the JSON array of the String representation of the Attrs
        ArrayList<String> attributesArray = new ArrayList<String>();
        for (Attr attr : attributes) {
            attributesArray.add(attr.toString());
        }
        String attributesJson;
        try {
            attributesJson = mapper.writeValueAsString(attributesArray);
        } catch (JsonProcessingException e) {
            throw new FfiException("Invalid Policy");
        }

        // Uid
        final Pointer uidPointer;
        if (uidLength == 0) {
            uidPointer = Pointer.NULL;
        } else {
            uidPointer = new Memory(uidLength);
            uidPointer.write(0, uid.get(), 0, uidLength);
        }

        // Additional Data
        final Pointer additionalDataPointer;
        if (adLength == 0) {
            additionalDataPointer = Pointer.NULL;
        } else {
            additionalDataPointer = new Memory(adLength);
            additionalDataPointer.write(0, additionalData.get(), 0, adLength);
        }

        unwrap(FfiWrapper.INSTANCE.h_aes_encrypt_header(symmetricKeyBuffer, symmetricKeyBufferSize, headerBytesBuffer,
                headerBytesBufferSize, policyJson, publicKeyPointer,
                publicKeyBytes.length,
                attributesJson, uidPointer, uidLength, additionalDataPointer, adLength));

        return new EncryptedHeader(Arrays.copyOfRange(symmetricKeyBuffer, 0, symmetricKeyBufferSize.getValue()),
                Arrays.copyOfRange(headerBytesBuffer, 0, headerBytesBufferSize.getValue()));
    }

    /**
     * Decrypt a hybrid header, recovering the symmetric key
     * 
     * @param userDecryptionKey    the ABE user decryption key
     * @param encryptedHeaderBytes the encrypted header
     * @return The decrypted header: symmetric key, uid and additional data
     * @throws FfiException     in case of native library error
     * @throws CosmianException in case the key bytes cannot be recovered from the
     *                          {@link PrivateKey}
     */
    public DecryptedHeader decryptHeader(PrivateKey userDecryptionKey, byte[] encryptedHeaderBytes)
            throws FfiException, CosmianException {
        return decryptHeader(userDecryptionKey.bytes(), encryptedHeaderBytes, 0, 0);
    }

    /**
     * Decrypt a hybrid header, recovering the symmetric key, and optionally, the
     * resource UID and additional data
     * 
     * @param userDecryptionKey    the ABE user decryption key
     * @param encryptedHeaderBytes the encrypted header
     * @param uidLen               the maximum bytes length of the expected UID
     * @param additionalDataLen    the maximum bytes length of the expected
     *                             additional
     *                             data
     * @return The decrypted header: symmetric key, uid and additional data
     * @throws FfiException     in case of native library error
     * @throws CosmianException in case the key bytes cannot be recovered from the
     *                          {@link PrivateKey}
     */
    public DecryptedHeader decryptHeader(PrivateKey userDecryptionKey, byte[] encryptedHeaderBytes, int uidLen,
            int additionalDataLen) throws FfiException, CosmianException {
        return decryptHeader(userDecryptionKey.bytes(), encryptedHeaderBytes, uidLen, additionalDataLen);
    }

    /**
     * Decrypt a hybrid header, recovering the symmetric key, and optionally, the
     * resource UID and additional data
     * 
     * @param userDecryptionKeyBytes the ABE user decryption key bytes
     * @param encryptedHeaderBytes   the encrypted header
     * @param uidLen                 the maximum bytes length of the expected UID
     * @param additionalDataLen      the maximum bytes length of the expected
     *                               additional
     *                               data
     * @return The decrypted header: symmetric key, uid and additional data
     * @throws FfiException in case of native library error
     */
    public DecryptedHeader decryptHeader(byte[] userDecryptionKeyBytes, byte[] encryptedHeaderBytes, int uidLen,
            int additionalDataLen) throws FfiException {

        // Symmetric Key OUT
        byte[] symmetricKeyBuffer = new byte[1024];
        IntByReference symmetricKeyBufferSize = new IntByReference(symmetricKeyBuffer.length);

        // UID OUT
        byte[] uidBuffer = new byte[uidLen];
        IntByReference uidBufferSize = new IntByReference(uidBuffer.length);

        // Additional Data OUT
        byte[] additionalDataBuffer = new byte[additionalDataLen];
        IntByReference additionalDataBufferSize = new IntByReference(additionalDataBuffer.length);

        // User Decryption Key
        final Pointer userDecryptionKeyPointer = new Memory(userDecryptionKeyBytes.length);
        userDecryptionKeyPointer.write(0, userDecryptionKeyBytes, 0, userDecryptionKeyBytes.length);

        // encrypted bytes
        final Pointer encryptedHeaderBytesPointer = new Memory(encryptedHeaderBytes.length);
        encryptedHeaderBytesPointer.write(0, encryptedHeaderBytes, 0, encryptedHeaderBytes.length);

        unwrap(FfiWrapper.INSTANCE.h_aes_decrypt_header(symmetricKeyBuffer, symmetricKeyBufferSize, uidBuffer,
                uidBufferSize, additionalDataBuffer, additionalDataBufferSize, encryptedHeaderBytesPointer,
                encryptedHeaderBytes.length, userDecryptionKeyPointer,
                userDecryptionKeyBytes.length));

        return new DecryptedHeader(
                Arrays.copyOfRange(symmetricKeyBuffer, 0, symmetricKeyBufferSize.getValue()),
                Arrays.copyOfRange(uidBuffer, 0, uidBufferSize.getValue()),
                Arrays.copyOfRange(additionalDataBuffer, 0, additionalDataBufferSize.getValue()));
    }

    /**
     * The overhead in bytes (over the clear text) generated by the symmetric
     * encryption scheme (AES 256 GCM)
     * 
     * @return the overhead bytes
     */
    public int symmetricEncryptionOverhead() {
        return FfiWrapper.INSTANCE.h_aes_symmetric_encryption_overhead();
    }

    /**
     * Symmetrically encrypt a block of clear text data.
     * 
     * The UID and Block Number are part of the AEAD of the symmetric scheme.
     * 
     * @param symmetricKey The key to use to symmetrically encrypt the block
     * @param uid          The resource UID
     * @param blockNumber  the block number when the resource is split in multiple
     *                     blocks
     * @param clearText    the clear text to encrypt
     * @return the encrypted block
     * @throws FfiException in case of native library error
     */
    public byte[] encryptBlock(byte[] symmetricKey, byte[] uid, int blockNumber,
            byte[] clearText) throws FfiException {

        // Header Bytes OUT
        byte[] cipherTextBuffer = new byte[FfiWrapper.INSTANCE.h_aes_symmetric_encryption_overhead()
                + clearText.length];
        IntByReference cipherTextBufferSize = new IntByReference(cipherTextBuffer.length);

        // Symmetric Key
        final Pointer symmetricKeyPointer = new Memory(symmetricKey.length);
        symmetricKeyPointer.write(0, symmetricKey, 0, symmetricKey.length);

        // Uid
        final Pointer uidPointer = new Memory(uid.length);
        uidPointer.write(0, uid, 0, uid.length);

        // Additional Data
        final Pointer dataPointer = new Memory(clearText.length);
        dataPointer.write(0, clearText, 0, clearText.length);

        unwrap(FfiWrapper.INSTANCE.h_aes_encrypt_block(cipherTextBuffer,
                cipherTextBufferSize, symmetricKeyPointer,
                symmetricKey.length,
                uidPointer, uid.length, blockNumber, dataPointer, clearText.length));

        return Arrays.copyOfRange(cipherTextBuffer, 0, cipherTextBufferSize.getValue());
    }

    /**
     * Symmetrically decrypt a block of encrypted data.
     * 
     * The resource UID and block Number must match those supplied on encryption or
     * decryption will fail.
     * 
     * @param symmetricKey   the symmetric key to use
     * @param uid            the resource UID
     * @param blockNumber    the block number of the resource
     * @param encryptedBytes the encrypted block bytes
     * @return the clear text bytes
     * @throws FfiException in case of native library error
     */
    public byte[] decryptBlock(byte[] symmetricKey, byte[] uid, int blockNumber, byte[] encryptedBytes)
            throws FfiException {

        // Clear Text Bytes OUT
        byte[] clearTextBuffer = new byte[encryptedBytes.length
                - FfiWrapper.INSTANCE.h_aes_symmetric_encryption_overhead()];
        IntByReference clearTextBufferSize = new IntByReference(clearTextBuffer.length);

        // Symmetric Key
        final Pointer symmetricKeyPointer = new Memory(symmetricKey.length);
        symmetricKeyPointer.write(0, symmetricKey, 0, symmetricKey.length);

        // Uid
        final Pointer uidPointer = new Memory(uid.length);
        uidPointer.write(0, uid, 0, uid.length);

        // Encrypted Data
        final Pointer encryptedBytesPointer = new Memory(encryptedBytes.length);
        encryptedBytesPointer.write(0, encryptedBytes, 0, encryptedBytes.length);

        unwrap(FfiWrapper.INSTANCE.h_aes_decrypt_block(clearTextBuffer, clearTextBufferSize, symmetricKeyPointer,
                symmetricKey.length, uidPointer, uid.length, blockNumber,
                encryptedBytesPointer, encryptedBytes.length));

        return Arrays.copyOfRange(clearTextBuffer, 0, clearTextBufferSize.getValue());
    }

    /**
     * If the result of the last FFI call is in Error, recover the last error from
     * the native code and throw an exception wrapping it.
     * 
     * @param result the result of the FFI call
     * @throws FfiException in case of native library error
     */
    private void unwrap(int result) throws FfiException {
        if (result == 1) {
            throw new FfiException(get_last_error(4095));
        }
    }
}
