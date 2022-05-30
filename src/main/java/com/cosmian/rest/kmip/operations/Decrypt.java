package com.cosmian.rest.kmip.operations;

import java.util.Objects;
import java.util.Optional;

import com.cosmian.rest.kmip.json.KmipStruct;
import com.cosmian.rest.kmip.json.KmipStructDeserializer;
import com.cosmian.rest.kmip.json.KmipStructSerializer;
import com.cosmian.rest.kmip.types.CryptographicParameters;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

/**
 * This operation requests the server to perform a decryption operation on the provided data using a Managed
 * Cryptographic Object as the key for the decryption operation. The request contains information about the
 * cryptographic parameters (mode and padding method), the data to be decrypted, and the IV/Counter/Nonce to use. The
 * cryptographic parameters MAY be omitted from the request as they can be specified as associated attributes of the
 * Managed Cryptographic Object. The initialization vector/counter/nonce MAY also be omitted from the request if the
 * algorithm does not use an IV/Counter/Nonce. The response contains the Unique Identifier of the Managed Cryptographic
 * Object used as the key and the result of the decryption operation. The success or failure of the operation is
 * indicated by the Result Status (and if failure the Result Reason) in the response header.
 */
@JsonSerialize(using = KmipStructSerializer.class)
@JsonDeserialize(using = KmipStructDeserializer.class)
public class Decrypt implements KmipStruct {

    /**
     * The Unique Identifier of the Managed Cryptographic Object that is the key to use for the decryption operation. If
     * omitted, then the ID Placeholder value SHALL be used by the server as the Unique Identifier.
     */
    @JsonProperty(value = "UniqueIdentifier")
    private Optional<String> unique_identifier;

    /**
     * The Cryptographic Parameters (Block Cipher Mode, Padding Method) corresponding to the particular decryption
     * method requested. If there are no Cryptographic Parameters associated with the Managed Cryptographic Object and
     * the algorithm requires parameters then the operation SHALL return with a Result Status of Operation Failed.
     */
    @JsonProperty(value = "CryptographicParameters")
    private Optional<CryptographicParameters> cryptographic_parameters;

    /**
     * The data to be decrypted.
     */
    @JsonProperty(value = "Data")
    private Optional<byte[]> data;

    /**
     * The initialization vector, counter or nonce to be used (where appropriate)
     */
    @JsonProperty(value = "IvCounterNonce")
    private Optional<byte[]> iv_counter_nonce;

    /**
     * Initial operation as Boolean
     */
    @JsonProperty(value = "InitIndicator")
    private Optional<Boolean> init_indicator;

    /**
     * Final operation as Boolean
     */
    @JsonProperty(value = "FinalIndicator")
    private Optional<Boolean> final_indicator;

    /**
     * Additional data to be authenticated via the Authenticated Encryption Tag. If supplied in multi-part decryption,
     * this data MUST be supplied on the initial Decrypt request
     */
    @JsonProperty(value = "AuthenticatedEncryptionAdditionalData")
    private Optional<byte[]> authenticated_encryption_additional_data;

    /**
     * Specifies the tag that will be needed to authenticate the decrypted data and the additional authenticated data.
     * If supplied in multi-part decryption, this data MUST be supplied on the initial Decrypt request
     */
    @JsonProperty(value = "AuthenticatedEncryptionTag")
    private Optional<byte[]> authenticated_encryption_tag;

    public Decrypt() {
    }

    public Decrypt(String userDecryptionKeyIdentifier, byte[] encrypted_data,
        Optional<byte[]> authenticated_encryption_additional_data) {
        this.unique_identifier = Optional.of(userDecryptionKeyIdentifier);
        this.cryptographic_parameters = Optional.empty();
        this.data = Optional.of(encrypted_data);
        this.iv_counter_nonce = Optional.empty();
        this.init_indicator = Optional.empty();
        this.final_indicator = Optional.empty();
        this.authenticated_encryption_additional_data = authenticated_encryption_additional_data;
        this.authenticated_encryption_tag = Optional.empty();
    }

    public Decrypt(Optional<String> unique_identifier, Optional<CryptographicParameters> cryptographic_parameters,
        Optional<byte[]> data, Optional<byte[]> iv_counter_nonce, Optional<Boolean> init_indicator,
        Optional<Boolean> final_indicator, Optional<byte[]> authenticated_encryption_additional_data,
        Optional<byte[]> authenticated_encryption_tag) {
        this.unique_identifier = unique_identifier;
        this.cryptographic_parameters = cryptographic_parameters;
        this.data = data;
        this.iv_counter_nonce = iv_counter_nonce;
        this.init_indicator = init_indicator;
        this.final_indicator = final_indicator;
        this.authenticated_encryption_additional_data = authenticated_encryption_additional_data;
        this.authenticated_encryption_tag = authenticated_encryption_tag;
    }

    public Optional<String> getUnique_identifier() {
        return this.unique_identifier;
    }

    public void setUnique_identifier(Optional<String> unique_identifier) {
        this.unique_identifier = unique_identifier;
    }

    public Optional<CryptographicParameters> getCryptographic_parameters() {
        return this.cryptographic_parameters;
    }

    public void setCryptographic_parameters(Optional<CryptographicParameters> cryptographic_parameters) {
        this.cryptographic_parameters = cryptographic_parameters;
    }

    public Optional<byte[]> getData() {
        return this.data;
    }

    public void setData(Optional<byte[]> data) {
        this.data = data;
    }

    public Optional<byte[]> getIv_counter_nonce() {
        return this.iv_counter_nonce;
    }

    public void setIv_counter_nonce(Optional<byte[]> iv_counter_nonce) {
        this.iv_counter_nonce = iv_counter_nonce;
    }

    public Optional<Boolean> getInit_indicator() {
        return this.init_indicator;
    }

    public void setInit_indicator(Optional<Boolean> init_indicator) {
        this.init_indicator = init_indicator;
    }

    public Optional<Boolean> getFinal_indicator() {
        return this.final_indicator;
    }

    public void setFinal_indicator(Optional<Boolean> final_indicator) {
        this.final_indicator = final_indicator;
    }

    public Optional<byte[]> getAuthenticated_encryption_additional_data() {
        return this.authenticated_encryption_additional_data;
    }

    public void setAuthenticated_encryption_additional_data(Optional<byte[]> authenticated_encryption_additional_data) {
        this.authenticated_encryption_additional_data = authenticated_encryption_additional_data;
    }

    public Optional<byte[]> getAuthenticated_encryption_tag() {
        return this.authenticated_encryption_tag;
    }

    public void setAuthenticated_encryption_tag(Optional<byte[]> authenticated_encryption_tag) {
        this.authenticated_encryption_tag = authenticated_encryption_tag;
    }

    public Decrypt unique_identifier(Optional<String> unique_identifier) {
        setUnique_identifier(unique_identifier);
        return this;
    }

    public Decrypt cryptographic_parameters(Optional<CryptographicParameters> cryptographic_parameters) {
        setCryptographic_parameters(cryptographic_parameters);
        return this;
    }

    public Decrypt data(Optional<byte[]> data) {
        setData(data);
        return this;
    }

    public Decrypt iv_counter_nonce(Optional<byte[]> iv_counter_nonce) {
        setIv_counter_nonce(iv_counter_nonce);
        return this;
    }

    public Decrypt init_indicator(Optional<Boolean> init_indicator) {
        setInit_indicator(init_indicator);
        return this;
    }

    public Decrypt final_indicator(Optional<Boolean> final_indicator) {
        setFinal_indicator(final_indicator);
        return this;
    }

    public Decrypt authenticated_encryption_additional_data(Optional<byte[]> authenticated_encryption_additional_data) {
        setAuthenticated_encryption_additional_data(authenticated_encryption_additional_data);
        return this;
    }

    public Decrypt authenticated_encryption_tag(Optional<byte[]> authenticated_encryption_tag) {
        setAuthenticated_encryption_tag(authenticated_encryption_tag);
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (o == this)
            return true;
        if (!(o instanceof Decrypt)) {
            return false;
        }
        Decrypt decrypt = (Decrypt) o;
        return Objects.equals(unique_identifier, decrypt.unique_identifier)
            && Objects.equals(cryptographic_parameters, decrypt.cryptographic_parameters)
            && Objects.equals(data, decrypt.data) && Objects.equals(iv_counter_nonce, decrypt.iv_counter_nonce)
            && Objects.equals(init_indicator, decrypt.init_indicator)
            && Objects.equals(final_indicator, decrypt.final_indicator)
            && Objects.equals(authenticated_encryption_additional_data,
                decrypt.authenticated_encryption_additional_data)
            && Objects.equals(authenticated_encryption_tag, decrypt.authenticated_encryption_tag);
    }

    @Override
    public int hashCode() {
        return Objects.hash(unique_identifier, cryptographic_parameters, data, iv_counter_nonce, init_indicator,
            final_indicator, authenticated_encryption_additional_data, authenticated_encryption_tag);
    }

    @Override
    public String toString() {
        return "{" + " unique_identifier='" + getUnique_identifier() + "'" + ", cryptographic_parameters='"
            + getCryptographic_parameters() + "'" + ", data='" + getData() + "'" + ", iv_counter_nonce='"
            + getIv_counter_nonce() + "'" + ", init_indicator='" + getInit_indicator() + "'" + ", final_indicator='"
            + getFinal_indicator() + "'" + ", authenticated_encryption_additional_data='"
            + getAuthenticated_encryption_additional_data() + "'" + ", authenticated_encryption_tag='"
            + getAuthenticated_encryption_tag() + "'" + "}";
    }

}
