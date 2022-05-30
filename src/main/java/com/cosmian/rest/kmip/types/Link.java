package com.cosmian.rest.kmip.types;

import java.util.Objects;

import com.cosmian.rest.kmip.json.KmipStruct;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * The Link attribute is a structure used to create a link from one Managed Cryptographic Object to another, closely
 * related target Managed Cryptographic Object. The link has a type, and the allowed types differ, depending on the
 * Object Type of the Managed Cryptographic Object, as listed below. The Linked Object Identifier identifies the target
 * Managed Cryptographic Object by its Unique Identifier. The link contains information about the association between
 * the Managed Objects (e.g., the private key corresponding to a public key; the parent certificate for a certificate in
 * a chain; or for a derived symmetric key, the base key from which it was derived). The Link attribute SHOULD be
 * present for private keys and public keys for which a certificate chain is stored by the server, and for certificates
 * in a certificate chain. Note that it is possible for a Managed Object to have multiple instances of the Link
 * attribute (e.g., a Private Key has links to the associated certificate, as well as the associated public key; a
 * Certificate object has links to both the public key and to the certificate of the certification authority (CA) that
 * signed the certificate). It is also possible that a Managed Object does not have links to associated cryptographic
 * objects. This MAY occur in cases where the associated key material is not available to the server or client (e.g.,
 * the registration of a CA Signer certificate with a server, where the corresponding private key is held in a different
 * manner).
 */
public class Link implements KmipStruct {

    @JsonProperty("LinkType")
    private LinkType link_type;

    @JsonProperty("LinkedObjectIdentifier")
    private LinkedObjectIdentifier linked_object_identifier;

    public static Link empty() {
        return new Link();
    }

    public Link() {
    }

    public Link(LinkType link_type, LinkedObjectIdentifier linked_object_identifier) {
        this.link_type = link_type;
        this.linked_object_identifier = linked_object_identifier;
    }

    public LinkType getLink_type() {
        return this.link_type;
    }

    public void setLink_type(LinkType link_type) {
        this.link_type = link_type;
    }

    public LinkedObjectIdentifier getLinked_object_identifier() {
        return this.linked_object_identifier;
    }

    public void setLinked_object_identifier(LinkedObjectIdentifier linked_object_identifier) {
        this.linked_object_identifier = linked_object_identifier;
    }

    @Override
    public boolean equals(Object o) {
        if (o == this)
            return true;
        if (!(o instanceof Link)) {
            return false;
        }
        Link link = (Link) o;
        return Objects.equals(link_type, link.link_type)
            && Objects.equals(linked_object_identifier, link.linked_object_identifier);
    }

    @Override
    public int hashCode() {
        return Objects.hash(link_type, linked_object_identifier);
    }

}
