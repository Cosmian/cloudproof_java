package com.cosmian.rest.jna;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;

import com.cosmian.rest.abe.acccess_policy.Attr;
import com.cosmian.rest.abe.policy.Policy;
import com.cosmian.rest.jna.abe.EncryptedHeader;
import com.cosmian.rest.jna.abe.FfiWrapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.jna.Memory;
import com.sun.jna.Pointer;
import com.sun.jna.ptr.IntByReference;
import com.sun.jna.ptr.PointerByReference;

public class Ffi {

    public int square(int x) {
        return FfiWrapper.INSTANCE.square(x);
    }

    // public long count_bytes(byte[] bytes) {
    // // Native.getNativeSize(Double.TYPE)
    // final Pointer ptr = new Memory(bytes.length);
    // ptr.write(0, bytes, 0, bytes.length);
    // RustLibrary lib = RustLibrary.INSTANCE;
    // return lib.count_bytes(ptr, bytes.length);

    // ,
    // }

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

        // Pubic Key
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
        final Pointer uidPointer = new Memory(uid.length);
        uidPointer.write(0, uid, 0, uid.length);

        // Additional Data
        final Pointer additionalDataPointer = new Memory(additionalData.length);
        additionalDataPointer.write(0, additionalData, 0, additionalData.length);

        unwrap(FfiWrapper.INSTANCE.encrypt_header(symmetricKeyBuffer, symmetricKeyBufferSize, headerBytesBuffer,
                headerBytesBufferSize, policyJson, publicKeyPointer,
                publicKey.length,
                attributesJson, uidPointer, uid.length, additionalDataPointer, additionalData.length));

        return new EncryptedHeader(Arrays.copyOfRange(symmetricKeyBuffer, 0, symmetricKeyBufferSize.getValue()),
                Arrays.copyOfRange(headerBytesBuffer, 0, headerBytesBufferSize.getValue()));
    }

    public int symmetricEncryptionOverhead() {
        return FfiWrapper.INSTANCE.symmetric_encryption_overhead();
    }

    public byte[] encryptBlock(byte[] symmetricKey, byte[] uid, int blockNumber,
            byte[] data) throws FfiException {

        // Header Bytes OUT
        byte[] cipherTextBuffer = new byte[FfiWrapper.INSTANCE.symmetric_encryption_overhead() + data.length];
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

        unwrap(FfiWrapper.INSTANCE.encrypt_block(cipherTextBuffer,
                cipherTextBufferSize, symmetricKeyPointer,
                symmetricKey.length,
                uidPointer, uid.length, blockNumber, dataPointer, data.length));

        return Arrays.copyOfRange(cipherTextBuffer, 0, cipherTextBufferSize.getValue());
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
    //
    // ----------------------------------------------------

    public Pointer newHybridCipher(Policy policy, byte[] publicKey, Attr[] attributes, byte[] uid,
            byte[] additionalData) throws FfiException {
        ObjectMapper mapper = new ObjectMapper();
        String policyJson;
        try {
            policyJson = mapper.writeValueAsString(policy);
        } catch (JsonProcessingException e) {
            throw new FfiException("Invalid Policy");
        }
        System.out.println("JAVA POLICY: " + policyJson);

        // Pointer to Pointer to Opaque Object
        final PointerByReference cipher = new PointerByReference();

        // Pubic Key
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
        final Pointer uidPointer = new Memory(uid.length);
        uidPointer.write(0, uid, 0, uid.length);

        // Additional Data
        final Pointer additionalDataPointer = new Memory(additionalData.length);
        additionalDataPointer.write(0, additionalData, 0, additionalData.length);

        unwrap(FfiWrapper.INSTANCE.hybrid_cipher_new(cipher, policyJson, publicKeyPointer, publicKey.length,
                attributesJson, uidPointer, uid.length, additionalDataPointer, additionalData.length));

        return cipher.getPointer();
    }

    public void destroyHybridCipher(Pointer cipherPointer) {
        FfiWrapper.INSTANCE.hybrid_cipher_destroy(cipherPointer);
    }
}

// Extract an ABE policy from attributes
// <pre>
// pub fn policy_from_attributes(attributes: &Attributes) -> KResult<Policy> {
// if let Some(bytes) = attributes.get_vendor_attribute(VENDOR_ID_COSMIAN,
// VENDOR_ATTR_ABE_POLICY)
// {
// serde_json::from_slice(bytes).map_err(|_| {
// KmsError::InvalidKmipValue(
// ErrorReason::Invalid_Attribute_Value,
// "failed deserializing the ABE Policy from the attributes".to_string(),
// )
// })
// } else {
// Err(KmsError::InvalidKmipValue(
// ErrorReason::Invalid_Attribute_Value,
// "the attributes do not contain an ABE Policy".to_string(),
// ))
// }
// }
// </pre>

// pub fn

// get_vendor_attribute(
// &self,
// vendor_identification: &str,
// attribute_name: &str,
// ) -> Option<&[u8]> {
// self.vendor_attributes.as_ref().and_then(|vas| {
// vas.iter()
// .find(|&va| {
// va.vendor_identification == vendor_identification
// && va.attribute_name == attribute_name
// })
// .map(|va| va.attribute_value.as_slice())
// })
// }

// let bytes = match key_material {
// KeyMaterial::ByteString(b) => b.clone(),
// x => {
// return Err(kms_error!(
// "Invalid Key Material for the ABE Master Public Key: {:?}",
// x
// ))
// .reason(ErrorReason::Invalid_Object_Type)
// }
// };