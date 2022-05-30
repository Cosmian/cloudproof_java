package com.cosmian.rest.kmip.types;

import java.util.Objects;
import java.util.Optional;

import com.cosmian.rest.kmip.json.KmipStruct;
import com.fasterxml.jackson.annotation.JsonProperty;

public class EncryptionKeyInformation implements KmipStruct {

    @JsonProperty("UniqueIdentifier")
    private String unique_identifier;

    @JsonProperty("CryptographicParameters")
    private Optional<CryptographicParameters> cryptographic_parameters;

    public EncryptionKeyInformation() {
    }

    public EncryptionKeyInformation(String unique_identifier,
        Optional<CryptographicParameters> cryptographic_parameters) {
        this.unique_identifier = unique_identifier;
        this.cryptographic_parameters = cryptographic_parameters;
    }

    public String getUnique_identifier() {
        return this.unique_identifier;
    }

    public void setUnique_identifier(String unique_identifier) {
        this.unique_identifier = unique_identifier;
    }

    public Optional<CryptographicParameters> getCryptographic_parameters() {
        return this.cryptographic_parameters;
    }

    public void setCryptographic_parameters(Optional<CryptographicParameters> cryptographic_parameters) {
        this.cryptographic_parameters = cryptographic_parameters;
    }

    public EncryptionKeyInformation unique_identifier(String unique_identifier) {
        setUnique_identifier(unique_identifier);
        return this;
    }

    public EncryptionKeyInformation cryptographic_parameters(
        Optional<CryptographicParameters> cryptographic_parameters) {
        setCryptographic_parameters(cryptographic_parameters);
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (o == this)
            return true;
        if (!(o instanceof EncryptionKeyInformation)) {
            return false;
        }
        EncryptionKeyInformation encryptionKeyInformation = (EncryptionKeyInformation) o;
        return Objects.equals(unique_identifier, encryptionKeyInformation.unique_identifier)
            && Objects.equals(cryptographic_parameters, encryptionKeyInformation.cryptographic_parameters);
    }

    @Override
    public int hashCode() {
        return Objects.hash(unique_identifier, cryptographic_parameters);
    }

    @Override
    public String toString() {
        return "{" + " unique_identifier='" + getUnique_identifier() + "'" + ", cryptographic_parameters='"
            + getCryptographic_parameters() + "'" + "}";
    }

}
