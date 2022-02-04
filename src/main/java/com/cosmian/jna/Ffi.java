package com.cosmian.jna;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;

import com.cosmian.jna.abe.DecryptedHeader;
import com.cosmian.jna.abe.EncryptedHeader;
import com.cosmian.jna.abe.FfiWrapper;
import com.cosmian.rest.abe.acccess_policy.Attr;
import com.cosmian.rest.abe.policy.Policy;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.jna.Memory;
import com.sun.jna.Pointer;
import com.sun.jna.ptr.IntByReference;

public class Ffi {

    /**
     * Return the last error in a String that does not exceed 1023 bytes
     */
    public String get_last_error() throws FfiException {
        return get_last_error(1023);
    }

    /**
     * Return the last error in a String that does not exceed `max_len` bytes
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
     * @param error_msg
     * @throws FfiException
     */
    public void set_error(String error_msg) throws FfiException {
        unwrap(FfiWrapper.INSTANCE.set_error(error_msg));
    }

    /**
     * Generate an hybrid encryption header.
     * 
     * A symmetric key is randomly generated and encrypted using the ABE schemes and
     * the provided policy attributes for the given policy
     * .
     * If provided, the resource `uid` and the `additionalData` are symmetrically
     * encrypted and appended to the encrypted header.
     * 
     * @param policy         the policy to use
     * @param publicKey      the ABE public key
     * @param attributes     the policy attributes used to encrypt the generated
     *                       symmetric key
     * @param uid            the optional resource uid
     * @param additionalData optional additional data
     * @return the encrypted header, bytes and symmetric key
     * @throws FfiException
     */
    public EncryptedHeader encryptHeader(Policy policy, byte[] publicKey, Attr[] attributes, byte[] uid,
            byte[] additionalData) throws FfiException {

        // For the JSON strings
        ObjectMapper mapper = new ObjectMapper();

        // Symmetric Key OUT
        byte[] symmetricKeyBuffer = new byte[1024];
        IntByReference symmetricKeyBufferSize = new IntByReference(symmetricKeyBuffer.length);

        // Header Bytes OUT
        byte[] headerBytesBuffer = new byte[8192 + uid.length + additionalData.length];
        IntByReference headerBytesBufferSize = new IntByReference(headerBytesBuffer.length);

        // Policy
        String policyJson;
        try {
            policyJson = mapper.writeValueAsString(policy);
        } catch (JsonProcessingException e) {
            throw new FfiException("Invalid Policy");
        }

        // Public Key
        final Pointer publicKeyPointer = new Memory(publicKey.length);
        publicKeyPointer.write(0, publicKey, 0, publicKey.length);

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
        if (uid.length == 0) {
            uidPointer = Pointer.NULL;
        } else {
            uidPointer = new Memory(uid.length);
            uidPointer.write(0, uid, 0, uid.length);
        }

        // Additional Data
        final Pointer additionalDataPointer;
        if (additionalData.length == 0) {
            additionalDataPointer = Pointer.NULL;
        } else {
            additionalDataPointer = new Memory(additionalData.length);
            additionalDataPointer.write(0, additionalData, 0, additionalData.length);
        }

        unwrap(FfiWrapper.INSTANCE.h_aes_encrypt_header(symmetricKeyBuffer, symmetricKeyBufferSize, headerBytesBuffer,
                headerBytesBufferSize, policyJson, publicKeyPointer,
                publicKey.length,
                attributesJson, uidPointer, uid.length, additionalDataPointer, additionalData.length));

        return new EncryptedHeader(Arrays.copyOfRange(symmetricKeyBuffer, 0, symmetricKeyBufferSize.getValue()),
                Arrays.copyOfRange(headerBytesBuffer, 0, headerBytesBufferSize.getValue()));
    }

    /**
     * Decrypt a hybrid header, recovering the symmetric key, and optionally, the
     * resource UID and additional data
     * 
     * @param userDecryptionKey    the ABE user decryption key
     * @param encryptedHeaderBytes the encrypted header
     * @param uidLen               the bytes length of the expected UID
     * @param additionalDataLen    the bytes length of the expected additional data
     * @return The decrypted header: symmetric key, uid and additional data
     * @throws FfiException
     */
    public DecryptedHeader decryptHeader(byte[] userDecryptionKey, byte[] encryptedHeaderBytes, int uidLen,
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
        final Pointer userDecryptionKeyPointer = new Memory(userDecryptionKey.length);
        userDecryptionKeyPointer.write(0, userDecryptionKey, 0, userDecryptionKey.length);

        // encrypted bytes
        final Pointer encryptedHeaderBytesPointer = new Memory(encryptedHeaderBytes.length);
        encryptedHeaderBytesPointer.write(0, encryptedHeaderBytes, 0, encryptedHeaderBytes.length);

        unwrap(FfiWrapper.INSTANCE.h_aes_decrypt_header(symmetricKeyBuffer, symmetricKeyBufferSize, uidBuffer,
                uidBufferSize, additionalDataBuffer, additionalDataBufferSize, encryptedHeaderBytesPointer,
                encryptedHeaderBytes.length, userDecryptionKeyPointer,
                userDecryptionKey.length));

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
     * @throws FfiException
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
     * @throws FfiException
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
     * @throws FfiException
     */
    private void unwrap(int result) throws FfiException {
        if (result == 1) {
            throw new FfiException(get_last_error(4095));
        }
    }
}
