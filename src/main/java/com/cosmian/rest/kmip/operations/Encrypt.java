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
 * This operation requests the server to perform an encryption operation on the
 * provided data using a Managed Cryptographic Object as the key for the
 * encryption operation.
 * 
 * The request contains information about the cryptographic parameters (mode and
 * padding method), the data to be encrypted, and the IV/Counter/Nonce to use.
 * The cryptographic parameters MAY be omitted from the request as they can be
 * specified as associated attributes of the Managed Cryptographic Object. The
 * IV/Counter/Nonce MAY also be omitted from the request if the cryptographic
 * parameters indicate that the server shall generate a Random IV on behalf of
 * the client or the encryption algorithm does not need an IV/Counter/Nonce. The
 * server does not store or otherwise manage the IV/Counter/Nonce.
 * 
 * If the Managed Cryptographic Object referenced has a Usage Limits attribute
 * then the server SHALL obtain an allocation from the current Usage Limits
 * value prior to performing the encryption operation. If the allocation is
 * unable to be obtained the operation SHALL return with a result status of
 * Operation Failed and result reason of Permission Denied.
 * 
 * The response contains the Unique Identifier of the Managed Cryptographic
 * Object used as the key and the result of the encryption operation.
 * 
 * The success or failure of the operation is indicated by the Result Status
 * (and if failure the Result Reason) in the response header
 */
@JsonSerialize(using = KmipStructSerializer.class)
@JsonDeserialize(using = KmipStructDeserializer.class)
public class Encrypt implements KmipStruct {

    /**
     * The Unique Identifier of the Managed
     * Cryptographic Object that is the key to
     * use for the encryption operation. If
     * omitted, then the ID Placeholder value
     * SHALL be used by the server as the
     * Unique Identifier
     */
    @JsonProperty(value = "UniqueIdentifier")
    private Optional<String> unique_identifier;

    /**
     * The Cryptographic Parameters (Block
     * Cipher Mode, Padding Method,
     * RandomIV) corresponding to the
     * particular encryption method
     * requested.
     * If there are no Cryptographic
     * Parameters associated with the
     * Managed Cryptographic Object and
     * the algorithm requires parameters then
     * the operation SHALL return with a
     * Result Status of Operation Failed.
     */
    @JsonProperty(value = "CryptographicParameters")
    private Optional<CryptographicParameters> cryptographic_parameters;

    /**
     * The data to be encrypted
     */
    @JsonProperty(value = "Data")
    private Optional<byte[]> data;

    /**
     * The initialization vector, counter or
     * nonce to be used (where appropriate).
     */
    @JsonProperty(value = "IvCounterNonce")
    private Optional<byte[]> iv_counter_nonce;

    /**
     * Specifies the existing stream or by-
     * parts cryptographic operation (as
     * returned from a previous call to this
     * operation)
     */
    @JsonProperty(value = "CorrelationValue")
    private Optional<byte[]> correlation_value;

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
     * Any additional data to be authenticated via the Authenticated Encryption
     * Tag. If supplied in multi-part encryption,
     * this data MUST be supplied on the initial Encrypt request
     */
    @JsonProperty(value = "AuthenticatedEncryptionAdditionalData")
    private Optional<byte[]> authenticated_encryption_additional_data;

    public Encrypt() {
    }

    public Encrypt(Optional<String> unique_identifier, Optional<CryptographicParameters> cryptographic_parameters,
            Optional<byte[]> data, Optional<byte[]> iv_counter_nonce, Optional<byte[]> correlation_value,
            Optional<Boolean> init_indicator, Optional<Boolean> final_indicator,
            Optional<byte[]> authenticated_encryption_additional_data) {
        this.unique_identifier = unique_identifier;
        this.cryptographic_parameters = cryptographic_parameters;
        this.data = data;
        this.iv_counter_nonce = iv_counter_nonce;
        this.correlation_value = correlation_value;
        this.init_indicator = init_indicator;
        this.final_indicator = final_indicator;
        this.authenticated_encryption_additional_data = authenticated_encryption_additional_data;
    }

    public Encrypt(String unique_identifier, byte[] data, Optional<byte[]> iv_counter_nonce,
            Optional<byte[]> authenticated_encryption_additional_data) {
        this.unique_identifier = Optional.of(unique_identifier);
        this.cryptographic_parameters = Optional.empty();
        this.data = Optional.of(data);
        this.iv_counter_nonce = iv_counter_nonce;
        this.correlation_value = Optional.empty();
        this.init_indicator = Optional.empty();
        this.final_indicator = Optional.empty();
        this.authenticated_encryption_additional_data = authenticated_encryption_additional_data;
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

    public Optional<byte[]> getCorrelation_value() {
        return this.correlation_value;
    }

    public void setCorrelation_value(Optional<byte[]> correlation_value) {
        this.correlation_value = correlation_value;
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

    public Encrypt unique_identifier(Optional<String> unique_identifier) {
        setUnique_identifier(unique_identifier);
        return this;
    }

    public Encrypt cryptographic_parameters(Optional<CryptographicParameters> cryptographic_parameters) {
        setCryptographic_parameters(cryptographic_parameters);
        return this;
    }

    public Encrypt data(Optional<byte[]> data) {
        setData(data);
        return this;
    }

    public Encrypt iv_counter_nonce(Optional<byte[]> iv_counter_nonce) {
        setIv_counter_nonce(iv_counter_nonce);
        return this;
    }

    public Encrypt correlation_value(Optional<byte[]> correlation_value) {
        setCorrelation_value(correlation_value);
        return this;
    }

    public Encrypt init_indicator(Optional<Boolean> init_indicator) {
        setInit_indicator(init_indicator);
        return this;
    }

    public Encrypt final_indicator(Optional<Boolean> final_indicator) {
        setFinal_indicator(final_indicator);
        return this;
    }

    public Encrypt authenticated_encryption_additional_data(Optional<byte[]> authenticated_encryption_additional_data) {
        setAuthenticated_encryption_additional_data(authenticated_encryption_additional_data);
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (o == this)
            return true;
        if (!(o instanceof Encrypt)) {
            return false;
        }
        Encrypt encrypt = (Encrypt) o;
        return Objects.equals(unique_identifier, encrypt.unique_identifier)
                && Objects.equals(cryptographic_parameters, encrypt.cryptographic_parameters)
                && Objects.equals(data, encrypt.data) && Objects.equals(iv_counter_nonce, encrypt.iv_counter_nonce)
                && Objects.equals(correlation_value, encrypt.correlation_value)
                && Objects.equals(init_indicator, encrypt.init_indicator)
                && Objects.equals(final_indicator, encrypt.final_indicator) && Objects.equals(
                        authenticated_encryption_additional_data, encrypt.authenticated_encryption_additional_data);
    }

    @Override
    public int hashCode() {
        return Objects.hash(unique_identifier, cryptographic_parameters, data, iv_counter_nonce, correlation_value,
                init_indicator, final_indicator, authenticated_encryption_additional_data);
    }

    @Override
    public String toString() {
        return "{" + " unique_identifier='" + getUnique_identifier() + "'" + ", cryptographic_parameters='"
                + getCryptographic_parameters() + "'" + ", data='" + getData() + "'" + ", iv_counter_nonce='"
                + getIv_counter_nonce() + "'" + ", correlation_value='" + getCorrelation_value() + "'"
                + ", init_indicator='" + getInit_indicator() + "'" + ", final_indicator='" + getFinal_indicator() + "'"
                + ", authenticated_encryption_additional_data='" + getAuthenticated_encryption_additional_data() + "'"
                + "}";
    }

}
