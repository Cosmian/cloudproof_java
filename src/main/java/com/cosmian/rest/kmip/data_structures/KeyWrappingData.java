package com.cosmian.rest.kmip.data_structures;

import java.util.Objects;
import java.util.Optional;

import com.cosmian.rest.kmip.json.KmipStruct;
import com.cosmian.rest.kmip.types.EncodingOption;
import com.cosmian.rest.kmip.types.EncryptionKeyInformation;
import com.cosmian.rest.kmip.types.MacSignatureKeyInformation;
import com.cosmian.rest.kmip.types.WrappingMethod;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * The Key Block MAY also supply OPTIONAL information about a cryptographic key wrapping mechanism used to wrap the Key
 * Value. This consists of a Key Wrapping Data structure. It is only used inside a Key Block. This structure contains
 * fields for:
 *
 * Value Description
 *
 * Wrapping Method Indicates the method used to wrap the Key Value.
 *
 * Encryption Key Information Contains the Unique Identifier value of the encryption key and associated cryptographic
 * parameters.
 *
 * MAC/Signature Key Information Contains the Unique Identifier value of the MAC/signature key and associated
 * cryptographic parameters.
 *
 * MAC/Signature Contains a MAC or signature of the Key Value
 *
 * IV/Counter/Nonce If REQUIRED by the wrapping method.
 *
 * Encoding Option Specifies the encoding of the Key Material within the Key Value structure of the Key Block that has
 * been wrapped. If No Encoding is specified, then the Key Value structure SHALL NOT contain any attributes.
 *
 * If wrapping is used, then the whole Key Value structure is wrapped unless otherwise specified by the Wrapping Method.
 * The algorithms used for wrapping are given by the Cryptographic Algorithm attributes of the encryption key and/or
 * MAC/signature key; the block-cipher mode, padding method, and hashing algorithm used for wrapping are given by the
 * Cryptographic Parameters in the Encryption Key Information and/or MAC/Signature Key Information, or, if not present,
 * from the Cryptographic Parameters attribute of the respective key(s). Either the Encryption Key Information or the
 * MAC/Signature Key Information (or both) in the Key Wrapping Data structure SHALL be specified.
 */
public class KeyWrappingData implements KmipStruct {

    @JsonProperty("WrappingMethod")
    private WrappingMethod wrapping_method;

    @JsonProperty("EncryptionKeyInformation")
    private Optional<EncryptionKeyInformation> encryption_key_information;

    @JsonProperty("MACSignatureKeyInformation")
    private Optional<MacSignatureKeyInformation> mac_or_signature_key_information;

    @JsonProperty("MACSignature")
    private Optional<byte[]> mac_or_signature;

    @JsonProperty("IVCounterNonce")
    private Optional<byte[]> iv_counter_nonce;

    /**
     * Specifies the encoding of the Key Value Byte String. If not present, the wrapped Key Value structure SHALL be
     * TTLV encoded.
     */
    @JsonProperty("EncodingOption")
    private Optional<EncodingOption> encoding_option;

    public KeyWrappingData() {}

    public KeyWrappingData(WrappingMethod wrapping_method,
        Optional<EncryptionKeyInformation> encryption_key_information,
        Optional<MacSignatureKeyInformation> mac_or_signature_key_information, Optional<byte[]> mac_or_signature,
        Optional<byte[]> iv_counter_nonce, Optional<EncodingOption> encoding_option) {
        this.wrapping_method = wrapping_method;
        this.encryption_key_information = encryption_key_information;
        this.mac_or_signature_key_information = mac_or_signature_key_information;
        this.mac_or_signature = mac_or_signature;
        this.iv_counter_nonce = iv_counter_nonce;
        this.encoding_option = encoding_option;
    }

    public WrappingMethod getWrapping_method() {
        return this.wrapping_method;
    }

    public void setWrapping_method(WrappingMethod wrapping_method) {
        this.wrapping_method = wrapping_method;
    }

    public Optional<EncryptionKeyInformation> getEncryption_key_information() {
        return this.encryption_key_information;
    }

    public void setEncryption_key_information(Optional<EncryptionKeyInformation> encryption_key_information) {
        this.encryption_key_information = encryption_key_information;
    }

    public Optional<MacSignatureKeyInformation> getMac_or_signature_key_information() {
        return this.mac_or_signature_key_information;
    }

    public void
        setMac_or_signature_key_information(Optional<MacSignatureKeyInformation> mac_or_signature_key_information) {
        this.mac_or_signature_key_information = mac_or_signature_key_information;
    }

    public Optional<byte[]> getMac_or_signature() {
        return this.mac_or_signature;
    }

    public void setMac_or_signature(Optional<byte[]> mac_or_signature) {
        this.mac_or_signature = mac_or_signature;
    }

    public Optional<byte[]> getIv_counter_nonce() {
        return this.iv_counter_nonce;
    }

    public void setIv_counter_nonce(Optional<byte[]> iv_counter_nonce) {
        this.iv_counter_nonce = iv_counter_nonce;
    }

    public Optional<EncodingOption> getEncoding_option() {
        return this.encoding_option;
    }

    public void setEncoding_option(Optional<EncodingOption> encoding_option) {
        this.encoding_option = encoding_option;
    }

    public KeyWrappingData wrapping_method(WrappingMethod wrapping_method) {
        setWrapping_method(wrapping_method);
        return this;
    }

    public KeyWrappingData encryption_key_information(Optional<EncryptionKeyInformation> encryption_key_information) {
        setEncryption_key_information(encryption_key_information);
        return this;
    }

    public KeyWrappingData
        mac_or_signature_key_information(Optional<MacSignatureKeyInformation> mac_or_signature_key_information) {
        setMac_or_signature_key_information(mac_or_signature_key_information);
        return this;
    }

    public KeyWrappingData mac_or_signature(Optional<byte[]> mac_or_signature) {
        setMac_or_signature(mac_or_signature);
        return this;
    }

    public KeyWrappingData iv_counter_nonce(Optional<byte[]> iv_counter_nonce) {
        setIv_counter_nonce(iv_counter_nonce);
        return this;
    }

    public KeyWrappingData encoding_option(Optional<EncodingOption> encoding_option) {
        setEncoding_option(encoding_option);
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (o == this)
            return true;
        if (!(o instanceof KeyWrappingData)) {
            return false;
        }
        KeyWrappingData keyWrappingData = (KeyWrappingData)o;
        return Objects.equals(wrapping_method, keyWrappingData.wrapping_method)
            && Objects.equals(encryption_key_information, keyWrappingData.encryption_key_information)
            && Objects.equals(mac_or_signature_key_information, keyWrappingData.mac_or_signature_key_information)
            && Objects.equals(mac_or_signature, keyWrappingData.mac_or_signature)
            && Objects.equals(iv_counter_nonce, keyWrappingData.iv_counter_nonce)
            && Objects.equals(encoding_option, keyWrappingData.encoding_option);
    }

    @Override
    public int hashCode() {
        return Objects.hash(wrapping_method, encryption_key_information, mac_or_signature_key_information,
            mac_or_signature, iv_counter_nonce, encoding_option);
    }

    @Override
    public String toString() {
        return "{" + " wrapping_method='" + getWrapping_method() + "'" + ", encryption_key_information='"
            + getEncryption_key_information() + "'" + ", mac_or_signature_key_information='"
            + getMac_or_signature_key_information() + "'" + ", mac_or_signature='" + getMac_or_signature() + "'"
            + ", iv_counter_nonce='" + getIv_counter_nonce() + "'" + ", encoding_option='" + getEncoding_option() + "'"
            + "}";
    }

}
