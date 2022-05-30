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
public class DecryptResponse implements KmipStruct {

    /// The Unique Identifier of the Managed
    /// Cryptographic Object that was the key
    /// used for the decryption operation.
    @JsonProperty(value = "UniqueIdentifier")
    private String unique_identifier;

    /// The decrypted data (as a Byte String).
    @JsonProperty(value = "Data")
    private Optional<byte[]> data;

    /// Specifies the stream or by-parts value
    /// to be provided in subsequent calls to
    /// this operation for performing
    /// cryptographic operations.
    @JsonProperty(value = "CorrelationValue")
    private Optional<byte[]> correlation_value;

    public DecryptResponse() {
    }

    public DecryptResponse(String unique_identifier, Optional<byte[]> data, Optional<byte[]> correlation_value) {
        this.unique_identifier = unique_identifier;
        this.data = data;
        this.correlation_value = correlation_value;
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

    public Optional<byte[]> getCorrelation_value() {
        return this.correlation_value;
    }

    public void setCorrelation_value(Optional<byte[]> correlation_value) {
        this.correlation_value = correlation_value;
    }

    public DecryptResponse unique_identifier(String unique_identifier) {
        setUnique_identifier(unique_identifier);
        return this;
    }

    public DecryptResponse data(Optional<byte[]> data) {
        setData(data);
        return this;
    }

    public DecryptResponse correlation_value(Optional<byte[]> correlation_value) {
        setCorrelation_value(correlation_value);
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (o == this)
            return true;
        if (!(o instanceof DecryptResponse)) {
            return false;
        }
        DecryptResponse decryptResponse = (DecryptResponse) o;
        return Objects.equals(unique_identifier, decryptResponse.unique_identifier)
            && Objects.equals(data, decryptResponse.data)
            && Objects.equals(correlation_value, decryptResponse.correlation_value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(unique_identifier, data, correlation_value);
    }

    @Override
    public String toString() {
        return "{" + " unique_identifier='" + getUnique_identifier() + "'" + ", data='" + getData() + "'"
            + ", correlation_value='" + getCorrelation_value() + "'" + "}";
    }

}
