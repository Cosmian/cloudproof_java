package com.cosmian.rest.jna;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;

import com.cosmian.rest.abe.acccess_policy.Attr;
import com.cosmian.rest.abe.policy.Policy;
import com.cosmian.rest.jna.abe.DecryptedHeader;
import com.cosmian.rest.jna.abe.EncryptedHeader;
import com.cosmian.rest.jna.abe.FfiWrapper;
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

    public void set_error(String error_msg) throws FfiException {
        unwrap(FfiWrapper.INSTANCE.set_error(error_msg));
    }

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
        System.out.println("JAVA POLICY: " + policyJson);

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
        System.out.println("JAVA ATTRIBUTES: " + attributesJson);

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
        System.out.println("Ffi.decryptHeader() udk len " + userDecryptionKey.length);

        // encrypted bytes
        final Pointer encryptedHeaderBytesPointer = new Memory(encryptedHeaderBytes.length);
        encryptedHeaderBytesPointer.write(0, encryptedHeaderBytes, 0, encryptedHeaderBytes.length);
        System.out.println("Ffi.decryptHeader() encrypted header len " + encryptedHeaderBytes.length);

        unwrap(FfiWrapper.INSTANCE.h_aes_decrypt_header(symmetricKeyBuffer, symmetricKeyBufferSize, uidBuffer,
                uidBufferSize, additionalDataBuffer, additionalDataBufferSize, encryptedHeaderBytesPointer,
                encryptedHeaderBytes.length, userDecryptionKeyPointer,
                userDecryptionKey.length));

        return new DecryptedHeader(
                Arrays.copyOfRange(symmetricKeyBuffer, 0, symmetricKeyBufferSize.getValue()),
                Arrays.copyOfRange(uidBuffer, 0, uidBufferSize.getValue()),
                Arrays.copyOfRange(additionalDataBuffer, 0, additionalDataBufferSize.getValue()));
    }

    public int symmetricEncryptionOverhead() {
        return FfiWrapper.INSTANCE.h_aes_symmetric_encryption_overhead();
    }

    public byte[] encryptBlock(byte[] symmetricKey, byte[] uid, int blockNumber,
            byte[] data) throws FfiException {

        // Header Bytes OUT
        byte[] cipherTextBuffer = new byte[FfiWrapper.INSTANCE.h_aes_symmetric_encryption_overhead() + data.length];
        IntByReference cipherTextBufferSize = new IntByReference(cipherTextBuffer.length);

        // Symmetric Key
        final Pointer symmetricKeyPointer = new Memory(symmetricKey.length);
        symmetricKeyPointer.write(0, symmetricKey, 0, symmetricKey.length);

        // Uid
        final Pointer uidPointer = new Memory(uid.length);
        uidPointer.write(0, uid, 0, uid.length);

        // Additional Data
        final Pointer dataPointer = new Memory(data.length);
        dataPointer.write(0, data, 0, data.length);

        unwrap(FfiWrapper.INSTANCE.h_aes_encrypt_block(cipherTextBuffer,
                cipherTextBufferSize, symmetricKeyPointer,
                symmetricKey.length,
                uidPointer, uid.length, blockNumber, dataPointer, data.length));

        return Arrays.copyOfRange(cipherTextBuffer, 0, cipherTextBufferSize.getValue());
    }

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

    // ----------------------------------------------------
    // TODO Creates strange SIGSEV on Rust Side - deactivated
    // ----------------------------------------------------

    // public Pointer newHybridCipher(Policy policy, byte[] publicKey, Attr[]
    // attributes, byte[] uid,
    // byte[] additionalData) throws FfiException {
    // ObjectMapper mapper = new ObjectMapper();
    // String policyJson;
    // try {
    // policyJson = mapper.writeValueAsString(policy);
    // } catch (JsonProcessingException e) {
    // throw new FfiException("Invalid Policy");
    // }
    // System.out.println("JAVA POLICY: " + policyJson);

    // // Pointer to Pointer to Opaque Object
    // final PointerByReference cipher = new PointerByReference();

    // // Pubic Key
    // final Pointer publicKeyPointer = new Memory(publicKey.length);
    // publicKeyPointer.write(0, publicKey, 0, publicKey.length);

    // // Attributes
    // // The value must be the JSON array of the String representation of the Attrs
    // ArrayList<String> attributesArray = new ArrayList<String>();
    // for (Attr attr : attributes) {
    // attributesArray.add(attr.toString());
    // }
    // String attributesJson;
    // try {
    // attributesJson = mapper.writeValueAsString(attributesArray);
    // } catch (JsonProcessingException e) {
    // throw new FfiException("Invalid Policy");
    // }
    // System.out.println("JAVA ATTRIBUTES: " + attributesJson);

    // // Uid
    // final Pointer uidPointer = new Memory(uid.length);
    // uidPointer.write(0, uid, 0, uid.length);

    // // Additional Data
    // final Pointer additionalDataPointer = new Memory(additionalData.length);
    // additionalDataPointer.write(0, additionalData, 0, additionalData.length);

    // unwrap(FfiWrapper.INSTANCE.h_aes_hybrid_cipher_new(cipher, policyJson,
    // publicKeyPointer, publicKey.length,
    // attributesJson, uidPointer, uid.length, additionalDataPointer,
    // additionalData.length));

    // return cipher.getPointer();
    // }

    // public void destroyHybridCipher(Pointer cipherPointer) {
    // FfiWrapper.INSTANCE.h_aes_hybrid_cipher_destroy(cipherPointer);
    // }
}
