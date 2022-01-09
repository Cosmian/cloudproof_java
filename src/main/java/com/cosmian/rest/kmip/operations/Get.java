package com.cosmian.rest.kmip.operations;

import java.util.Objects;
import java.util.Optional;

import com.cosmian.rest.kmip.data_structures.KeyWrappingData;
import com.cosmian.rest.kmip.json.KmipStruct;
import com.cosmian.rest.kmip.json.KmipStructDeserializer;
import com.cosmian.rest.kmip.json.KmipStructSerializer;
import com.cosmian.rest.kmip.types.KeyCompressionType;
import com.cosmian.rest.kmip.types.KeyFormatType;
import com.cosmian.rest.kmip.types.KeyWrapType;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

/**
 * This operation requests that the server returns the Managed Object specified
 * by its Unique Identifier.
 * 
 * Only a single object is returned. The response contains the Unique Identifier
 * of the object, along with the object itself, which MAY be wrapped using a
 * wrapping key as specified in the request.
 * 
 * The following key format capabilities SHALL be assumed by the client;
 * restrictions apply when the client requests the server to return an object in
 * a particular format:
 * 
 * · If a client registered a key in a given format, the server SHALL be able to
 * return the key during the Get operation in the same format that was used when
 * the key was registered.
 * 
 * · Any other format conversion MAY be supported by the server.
 * 
 * If Key Format Type is specified to be PKCS#12 then the response payload shall
 * be a PKCS#12 container as specified by [RFC7292]. The Unique Identifier shall
 * be either that of a private key or certificate to be included in the
 * response. The container shall be protected using the Secret Data object
 * specified via the private key or certificate’s PKCS#12 Password Link. The
 * current certificate chain shall also be included as determined by using the
 * private key’s Public Key link to get the corresponding public key (where
 * relevant), and then using that public key’s PKCS#12 Certificate Link to get
 * the base certificate, and then using each certificate’s Certificate Link to
 * build the certificate chain. It is an error if there is more than one valid
 * certificate chain.
 */
@JsonSerialize(using = KmipStructSerializer.class)
@JsonDeserialize(using = KmipStructDeserializer.class)
public class Get implements KmipStruct {

    /// Determines the object being requested. If omitted, then the ID
    /// Placeholder value is used by the server as the Unique Identifier.
    @JsonProperty(value = "UniqueIdentifier")
    private Optional<String> uniqueIdentifier = Optional.empty();

    /// Determines the key format type to be returned.
    @JsonProperty(value = "KeyFormatType")
    private Optional<KeyFormatType> keyFormatType = Optional.empty();

    /// Determines the Key Wrap Type of the returned key value.
    @JsonProperty(value = "KeyWrapType")
    private Optional<KeyWrapType> keyWrapType = Optional.empty();

    /// Determines the compression method for elliptic curve public keys.
    @JsonProperty(value = "KeyCompressionType")
    private Optional<KeyCompressionType> keyCompressionType = Optional.empty();

    /// Specifies keys and other information for wrapping the returned object.
    @JsonProperty(value = "KeyWrappingData")
    private Optional<KeyWrappingData> keyWrappingData = Optional.empty();

    public Get(String unique_identifier) {
        this(Optional.of(unique_identifier), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty());
    }

    public Get() {
    }

    public Get(Optional<String> uniqueIdentifier, Optional<KeyFormatType> keyFormatType,
            Optional<KeyWrapType> keyWrapType, Optional<KeyCompressionType> keyCompressionType,
            Optional<KeyWrappingData> keyWrappingData) {
        this.uniqueIdentifier = uniqueIdentifier;
        this.keyFormatType = keyFormatType;
        this.keyWrapType = keyWrapType;
        this.keyCompressionType = keyCompressionType;
        this.keyWrappingData = keyWrappingData;
    }

    public Optional<String> getUniqueIdentifier() {
        return this.uniqueIdentifier;
    }

    public void setUniqueIdentifier(Optional<String> uniqueIdentifier) {
        this.uniqueIdentifier = uniqueIdentifier;
    }

    public Optional<KeyFormatType> getKeyFormatType() {
        return this.keyFormatType;
    }

    public void setKeyFormatType(Optional<KeyFormatType> keyFormatType) {
        this.keyFormatType = keyFormatType;
    }

    public Optional<KeyWrapType> getKeyWrapType() {
        return this.keyWrapType;
    }

    public void setKeyWrapType(Optional<KeyWrapType> keyWrapType) {
        this.keyWrapType = keyWrapType;
    }

    public Optional<KeyCompressionType> getKeyCompressionType() {
        return this.keyCompressionType;
    }

    public void setKeyCompressionType(Optional<KeyCompressionType> keyCompressionType) {
        this.keyCompressionType = keyCompressionType;
    }

    public Optional<KeyWrappingData> getKeyWrappingData() {
        return this.keyWrappingData;
    }

    public void setKeyWrappingData(Optional<KeyWrappingData> keyWrappingData) {
        this.keyWrappingData = keyWrappingData;
    }

    public Get uniqueIdentifier(Optional<String> uniqueIdentifier) {
        setUniqueIdentifier(uniqueIdentifier);
        return this;
    }

    public Get keyFormatType(Optional<KeyFormatType> keyFormatType) {
        setKeyFormatType(keyFormatType);
        return this;
    }

    public Get keyWrapType(Optional<KeyWrapType> keyWrapType) {
        setKeyWrapType(keyWrapType);
        return this;
    }

    public Get keyCompressionType(Optional<KeyCompressionType> keyCompressionType) {
        setKeyCompressionType(keyCompressionType);
        return this;
    }

    public Get keyWrappingData(Optional<KeyWrappingData> keyWrappingData) {
        setKeyWrappingData(keyWrappingData);
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (o == this)
            return true;
        if (!(o instanceof Get)) {
            return false;
        }
        Get get = (Get) o;
        return Objects.equals(uniqueIdentifier, get.uniqueIdentifier)
                && Objects.equals(keyFormatType, get.keyFormatType) && Objects.equals(keyWrapType, get.keyWrapType)
                && Objects.equals(keyCompressionType, get.keyCompressionType)
                && Objects.equals(keyWrappingData, get.keyWrappingData);
    }

    @Override
    public int hashCode() {
        return Objects.hash(uniqueIdentifier, keyFormatType, keyWrapType, keyCompressionType, keyWrappingData);
    }

    @Override
    public String toString() {
        return "{" + " uniqueIdentifier='" + getUniqueIdentifier() + "'" + ", keyFormatType='" + getKeyFormatType()
                + "'" + ", keyWrapType='" + getKeyWrapType() + "'" + ", keyCompressionType='" + getKeyCompressionType()
                + "'" + ", keyWrappingData='" + getKeyWrappingData() + "'" + "}";
    }

}
