package com.cosmian.rest.kmip.types;

import java.util.Objects;
import java.util.Optional;

import com.cosmian.rest.kmip.json.KmipStruct;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * The following subsections describe the attributes that are associated with Managed Objects. Attributes that an object
 * MAY have multiple instances of are referred to as multi-instance attributes. All instances of an attribute SHOULD
 * have a different value. Similarly, attributes which an object SHALL only have at most one instance of are referred to
 * as single-instance attributes. Attributes are able to be obtained by a client from the server using the Get Attribute
 * operation. Some attributes are able to be set by the Add Attribute operation or updated by the Modify Attribute
 * operation, and some are able to be deleted by the Delete Attribute operation if they no longer apply to the Managed
 * Object. Read-only attributes are attributes that SHALL NOT be modified by either server or client, and that SHALL NOT
 * be deleted by a client. When attributes are returned by the server (e.g., via a Get Attributes operation), the
 * attribute value returned SHALL NOT differ for different clients unless specifically noted against each attribute. The
 * first table in each subsection contains the attribute name in the first row. This name is the canonical name used
 * when managing attributes using the Get Attributes, Get Attribute List, Add Attribute, Modify Attribute, and Delete
 * Attribute operations. A server SHALL NOT delete attributes without receiving a request from a client until the object
 * is destroyed. After an object is destroyed, the server MAY retain all, some or none of the object attributes,
 * depending on the object type and server policy.
 */
public class Attributes implements KmipStruct {
    /**
     * The Activation Date attribute contains the date and time when the Managed Object MAY begin to be used. This time
     * corresponds to state transition. The object SHALL NOT be used for any cryptographic purpose before the Activation
     * Date has been reached. Once the state transition from Pre-Active has occurred, then this attribute SHALL NOT be
     * changed or deleted before the object is destroyed.
     */
    @JsonProperty(value = "ActivationDate")
    private Optional<Integer> activationDate = Optional.empty(); // epoch millis

    /**
     * The Cryptographic Algorithm of an object. The Cryptographic Algorithm of a Certificate object identifies the
     * algorithm for the public key contained within the Certificate. The digital signature algorithm used to sign the
     * Certificate is identified in the Digital Signature Algorithm attribute. This attribute SHALL be set by the server
     * when the object is created or registered and then SHALL NOT be changed or deleted before the object is destroyed.
     */
    @JsonProperty(value = "CryptographicAlgorithm")
    private Optional<CryptographicAlgorithm> cryptographicAlgorithm = Optional.empty();

    /**
     * For keys, Cryptographic Length is the length in bits of the clear-text cryptographic key material of the Managed
     * Cryptographic Object. For certificates, Cryptographic Length is the length in bits of the public key contained
     * within the Certificate. This attribute SHALL be set by the server when the object is created or registered, and
     * then SHALL NOT be changed or deleted before the object is destroyed.
     */
    @JsonProperty(value = "CryptographicLength")
    private Optional<Integer> cryptographicLength = Optional.empty();

    /**
     * The Cryptographic Domain Parameters attribute is a structure that contains fields that MAY need to be specified
     * in the Create Key Pair Request Payload. Specific fields MAY only pertain to certain types of Managed
     * Cryptographic Objects. The domain parameter Q-length corresponds to the bit length of parameter Q (refer to
     * [RFC7778],[SEC2]and [SP800-56A]).
     */
    @JsonProperty(value = "CryptographicDomainParameters")
    private Optional<CryptographicDomainParameters> cryptographicDomainParameters = Optional.empty();

    /**
     * @see CryptographicParameters
     */
    @JsonProperty(value = "CryptographicParameters")
    private Optional<CryptographicParameters> cryptographicParameters = Optional.empty();

    /**
     * The Cryptographic Usage Mask attribute defines the cryptographic usage of a key. This is a bit mask that
     * indicates to the client which cryptographic functions MAY be performed using the key, and which ones SHALL NOT be
     * performed.
     *
     * @see CryptographicUsageMask
     */
    @JsonProperty(value = "CryptographicUsageMask")
    private Optional<Integer> cryptographicUsageMask = Optional.empty();

    /**
     * 4.26 The Key Format Type attribute is a required attribute of a Cryptographic Object. It is set by the server,
     * but a particular Key Format Type MAY be requested by the client if the cryptographic material is produced by the
     * server (i.e., Create, Create Key Pair, Create Split Key, Re-key, Re-key Key Pair, Derive Key) on the client's
     * behalf. The server SHALL comply with the client's requested format or SHALL fail the request. When the server
     * calculates a Digest for the object, it SHALL compute the digest on the data in the assigned Key Format Type, as
     * well as a digest in the default KMIP Key Format Type for that type of key and the algorithm requested (if a
     * non-default value is specified).
     */
    @JsonProperty(value = "KeyFormatType")
    private Optional<KeyFormatType> keyFormatType = Optional.empty();

    /**
     * The Link attribute is a structure used to create a link from one Managed Cryptographic Object to another, closely
     * related target Managed Cryptographic Object. The link has a type, and the allowed types differ, depending on the
     * Object Type of the Managed Cryptographic Object, as listed below. The Linked Object Identifier identifies the
     * target Managed Cryptographic Object by its Unique Identifier. The link contains information about the association
     * between the Managed Objects (e.g., the private key corresponding to a public key; the parent certificate for a
     * certificate in a chain; or for a derived symmetric key, the base key from which it was derived). The Link
     * attribute SHOULD be present for private keys and public keys for which a certificate chain is stored by the
     * server, and for certificates in a certificate chain. Note that it is possible for a Managed Object to have
     * multiple instances of the Link attribute (e.g., a Private Key has links to the associated certificate, as well as
     * the associated public key; a Certificate object has links to both the public key and to the certificate of the
     * certification authority (CA) that signed the certificate). It is also possible that a Managed Object does not
     * have links to associated cryptographic objects. This MAY occur in cases where the associated key material is not
     * available to the server or client (e.g., the registration of a CA Signer certificate with a server, where the
     * corresponding private key is held in a different manner)
     */
    @JsonProperty(value = "Link")
    private Optional<Link[]> link = Optional.empty();

    /**
     * The Object Typeof a Managed Object (e.g., public key, private key, symmetric key, etc.) SHALL be set by the
     * server when the object is created or registered and then SHALL NOT be changed or deleted before the object is
     * destroyed.
     */
    @JsonProperty(value = "ObjectType")
    private ObjectType objectType = ObjectType.Opaque_Object;

    /**
     * A vendor specific Attribute is a structure used for sending and receiving a Managed Object attribute. The Vendor
     * Identification and Attribute Name are text-strings that are used to identify the attribute. The Attribute Value
     * is either a primitive data type or structured object, depending on the attribute. Vendor identification values
     * "x" and "y" are reserved for KMIP v2.0 and later implementations referencing KMIP v1.x Custom Attributes. Vendor
     * Attributes created by the client with Vendor Identification "x" are not created (provided during object
     * creation), set, added, adjusted, modified or deleted by the server. Vendor Attributes created by the server with
     * Vendor Identification "y" are not created (provided during object creation), set, added, adjusted, modified or
     * deleted by the client.
     */
    @JsonProperty(value = "VendorAttributes")
    private Optional<VendorAttribute[]> vendorAttributes = Optional.empty();

    public Attributes(ObjectType object_type, Optional<CryptographicAlgorithm> cryptographic_algorithm) {
        super();
        this.objectType = object_type;
        this.cryptographicAlgorithm = cryptographic_algorithm;

    }

    public Attributes() {
    }

    public Attributes(Optional<Integer> activationDate, Optional<CryptographicAlgorithm> cryptographicAlgorithm,
        Optional<Integer> cryptographicLength, Optional<CryptographicDomainParameters> cryptographicDomainParameters,
        Optional<CryptographicParameters> cryptographicParameters, Optional<Integer> cryptographicUsageMask,
        Optional<KeyFormatType> keyFormatType, Optional<Link[]> link, ObjectType objectType,
        Optional<VendorAttribute[]> vendorAttributes) {
        this.activationDate = activationDate;
        this.cryptographicAlgorithm = cryptographicAlgorithm;
        this.cryptographicLength = cryptographicLength;
        this.cryptographicDomainParameters = cryptographicDomainParameters;
        this.cryptographicParameters = cryptographicParameters;
        this.cryptographicUsageMask = cryptographicUsageMask;
        this.keyFormatType = keyFormatType;
        this.link = link;
        this.objectType = objectType;
        this.vendorAttributes = vendorAttributes;
    }

    public Optional<Integer> getActivationDate() {
        return this.activationDate;
    }

    public void setActivationDate(Optional<Integer> activationDate) {
        this.activationDate = activationDate;
    }

    public Optional<CryptographicAlgorithm> getCryptographicAlgorithm() {
        return this.cryptographicAlgorithm;
    }

    public void setCryptographicAlgorithm(Optional<CryptographicAlgorithm> cryptographicAlgorithm) {
        this.cryptographicAlgorithm = cryptographicAlgorithm;
    }

    public Optional<Integer> getCryptographicLength() {
        return this.cryptographicLength;
    }

    public void setCryptographicLength(Optional<Integer> cryptographicLength) {
        this.cryptographicLength = cryptographicLength;
    }

    public Optional<CryptographicDomainParameters> getCryptographicDomainParameters() {
        return this.cryptographicDomainParameters;
    }

    public void setCryptographicDomainParameters(
        Optional<CryptographicDomainParameters> cryptographicDomainParameters) {
        this.cryptographicDomainParameters = cryptographicDomainParameters;
    }

    public Optional<CryptographicParameters> getCryptographicParameters() {
        return this.cryptographicParameters;
    }

    public void setCryptographicParameters(Optional<CryptographicParameters> cryptographicParameters) {
        this.cryptographicParameters = cryptographicParameters;
    }

    public Optional<Integer> getCryptographicUsageMask() {
        return this.cryptographicUsageMask;
    }

    public void setCryptographicUsageMask(Optional<Integer> cryptographicUsageMask) {
        this.cryptographicUsageMask = cryptographicUsageMask;
    }

    public Optional<KeyFormatType> getKeyFormatType() {
        return this.keyFormatType;
    }

    public void setKeyFormatType(Optional<KeyFormatType> keyFormatType) {
        this.keyFormatType = keyFormatType;
    }

    public Optional<Link[]> getLink() {
        return this.link;
    }

    public void setLink(Optional<Link[]> link) {
        this.link = link;
    }

    public ObjectType getObjectType() {
        return this.objectType;
    }

    public void setObjectType(ObjectType objectType) {
        this.objectType = objectType;
    }

    public Optional<VendorAttribute[]> getVendorAttributes() {
        return this.vendorAttributes;
    }

    public void setVendorAttributes(Optional<VendorAttribute[]> vendorAttributes) {
        this.vendorAttributes = vendorAttributes;
    }

    public Attributes activationDate(Optional<Integer> activationDate) {
        setActivationDate(activationDate);
        return this;
    }

    public Attributes cryptographicAlgorithm(Optional<CryptographicAlgorithm> cryptographicAlgorithm) {
        setCryptographicAlgorithm(cryptographicAlgorithm);
        return this;
    }

    public Attributes cryptographicLength(Optional<Integer> cryptographicLength) {
        setCryptographicLength(cryptographicLength);
        return this;
    }

    public Attributes cryptographicDomainParameters(
        Optional<CryptographicDomainParameters> cryptographicDomainParameters) {
        setCryptographicDomainParameters(cryptographicDomainParameters);
        return this;
    }

    public Attributes cryptographicParameters(Optional<CryptographicParameters> cryptographicParameters) {
        setCryptographicParameters(cryptographicParameters);
        return this;
    }

    public Attributes cryptographicUsageMask(Optional<Integer> cryptographicUsageMask) {
        setCryptographicUsageMask(cryptographicUsageMask);
        return this;
    }

    public Attributes keyFormatType(Optional<KeyFormatType> keyFormatType) {
        setKeyFormatType(keyFormatType);
        return this;
    }

    public Attributes link(Optional<Link[]> link) {
        setLink(link);
        return this;
    }

    public Attributes objectType(ObjectType objectType) {
        setObjectType(objectType);
        return this;
    }

    public Attributes vendorAttributes(Optional<VendorAttribute[]> vendorAttributes) {
        setVendorAttributes(vendorAttributes);
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (o == this)
            return true;
        if (!(o instanceof Attributes)) {
            return false;
        }
        Attributes attributes = (Attributes) o;
        return Objects.equals(activationDate, attributes.activationDate)
            && Objects.equals(cryptographicAlgorithm, attributes.cryptographicAlgorithm)
            && Objects.equals(cryptographicLength, attributes.cryptographicLength)
            && Objects.equals(cryptographicDomainParameters, attributes.cryptographicDomainParameters)
            && Objects.equals(cryptographicParameters, attributes.cryptographicParameters)
            && Objects.equals(cryptographicUsageMask, attributes.cryptographicUsageMask)
            && Objects.equals(keyFormatType, attributes.keyFormatType) && Objects.equals(link, attributes.link)
            && Objects.equals(objectType, attributes.objectType)
            && Objects.equals(vendorAttributes, attributes.vendorAttributes);
    }

    @Override
    public int hashCode() {
        return Objects.hash(activationDate, cryptographicAlgorithm, cryptographicLength, cryptographicDomainParameters,
            cryptographicParameters, cryptographicUsageMask, keyFormatType, link, objectType, vendorAttributes);
    }

    @Override
    public String toString() {
        return "{" + " activationDate='" + getActivationDate() + "'" + ", cryptographicAlgorithm='"
            + getCryptographicAlgorithm() + "'" + ", cryptographicLength='" + getCryptographicLength() + "'"
            + ", cryptographicDomainParameters='" + getCryptographicDomainParameters() + "'"
            + ", cryptographicParameters='" + getCryptographicParameters() + "'" + ", cryptographicUsageMask='"
            + getCryptographicUsageMask() + "'" + ", keyFormatType='" + getKeyFormatType() + "'" + ", link='"
            + getLink() + "'" + ", objectType='" + getObjectType() + "'" + ", vendorAttributes='"
            + getVendorAttributes() + "'" + "}";
    }

}
