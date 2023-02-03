package com.cosmian.rest.kmip.operations;

import java.util.Objects;
import java.util.Optional;

import com.cosmian.rest.kmip.json.KmipStruct;
import com.cosmian.rest.kmip.json.KmipStructDeserializer;
import com.cosmian.rest.kmip.json.KmipStructSerializer;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

@JsonSerialize(using = KmipStructSerializer.class)
@JsonDeserialize(using = KmipStructDeserializer.class)
public class EncryptResponse implements KmipStruct {

    /// The Unique Identifier of the Managed
    /// Cryptographic Object that was the key
    /// used for the encryption operation.
    @JsonProperty(value = "UniqueIdentifier")
    private String unique_identifier;

    /// The encrypted data (as a Byte String).
    @JsonProperty(value = "Data")
    private Optional<byte[]> data;

    /// The value used if the Cryptographic
    /// Parameters specified Random IV and
    /// the IV/Counter/Nonce value was not
    /// provided in the request and the
    /// algorithm requires the provision of an
    /// IV/Counter/Nonce.
    @JsonProperty(value = "IvCounterNonce")
    private Optional<byte[]> iv_counter_nonce;

    /// Specifies the stream or by-parts value
    /// to be provided in subsequent calls to
    /// this operation for performing
    /// cryptographic operations.
    @JsonProperty(value = "CorrelationValue")
    private Optional<byte[]> correlation_value;

    /// Specifies the tag that will be needed to
    /// authenticate the decrypted data (and
    /// any "additional data"). Only returned on
    /// completion of the encryption of the last
    /// of the plaintext by an authenticated
    /// encryption cipher.
    @JsonProperty(value = "AuthenticatedEncryptionTag")
    private Optional<byte[]> authenticated_encryption_tag;

    public EncryptResponse() {
    }

    public EncryptResponse(String unique_identifier, Optional<byte[]> data, Optional<byte[]> iv_counter_nonce,
        Optional<byte[]> correlation_value, Optional<byte[]> authenticated_encryption_tag) {
        this.unique_identifier = unique_identifier;
        this.data = data;
        this.iv_counter_nonce = iv_counter_nonce;
        this.correlation_value = correlation_value;
        this.authenticated_encryption_tag = authenticated_encryption_tag;
    }

    public String getUnique_identifier() {
        return this.unique_identifier;
    }

    public void setUnique_identifier(String unique_identifier) {
        this.unique_identifier = unique_identifier;
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

    public Optional<byte[]> getAuthenticated_encryption_tag() {
        return this.authenticated_encryption_tag;
    }

    public void setAuthenticated_encryption_tag(Optional<byte[]> authenticated_encryption_tag) {
        this.authenticated_encryption_tag = authenticated_encryption_tag;
    }

    public EncryptResponse unique_identifier(String unique_identifier) {
        setUnique_identifier(unique_identifier);
        return this;
    }

    public EncryptResponse data(Optional<byte[]> data) {
        setData(data);
        return this;
    }

    public EncryptResponse iv_counter_nonce(Optional<byte[]> iv_counter_nonce) {
        setIv_counter_nonce(iv_counter_nonce);
        return this;
    }

    public EncryptResponse correlation_value(Optional<byte[]> correlation_value) {
        setCorrelation_value(correlation_value);
        return this;
    }

    public EncryptResponse authenticated_encryption_tag(Optional<byte[]> authenticated_encryption_tag) {
        setAuthenticated_encryption_tag(authenticated_encryption_tag);
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (o == this)
            return true;
        if (!(o instanceof EncryptResponse)) {
            return false;
        }
        EncryptResponse encryptResponse = (EncryptResponse) o;
        return Objects.equals(unique_identifier, encryptResponse.unique_identifier)
            && Objects.equals(data, encryptResponse.data)
            && Objects.equals(iv_counter_nonce, encryptResponse.iv_counter_nonce)
            && Objects.equals(correlation_value, encryptResponse.correlation_value)
            && Objects.equals(authenticated_encryption_tag, encryptResponse.authenticated_encryption_tag);
    }

    @Override
    public int hashCode() {
        return Objects.hash(unique_identifier, data, iv_counter_nonce, correlation_value, authenticated_encryption_tag);
    }

    @Override
    public String toString() {
        return "{" + " unique_identifier='" + getUnique_identifier() + "'" + ", data='" + getData() + "'"
            + ", iv_counter_nonce='" + getIv_counter_nonce() + "'" + ", correlation_value='" + getCorrelation_value()
            + "'" + ", authenticated_encryption_tag='" + getAuthenticated_encryption_tag() + "'" + "}";
    }

}
