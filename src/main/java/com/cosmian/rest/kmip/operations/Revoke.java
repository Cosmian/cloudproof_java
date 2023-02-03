package com.cosmian.rest.kmip.operations;

import java.util.Objects;
import java.util.Optional;

import com.cosmian.rest.kmip.json.KmipStruct;
import com.cosmian.rest.kmip.json.KmipStructDeserializer;
import com.cosmian.rest.kmip.json.KmipStructSerializer;
import com.cosmian.rest.kmip.types.RevocationReason;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

/**
 * This operation requests the server to revoke a Managed Cryptographic Object or an Opaque Object. The request contains
 * a reason for the revocation (e.g., "key compromise", "cessation of operation", etc.). The operation has one of two
 * effects. If the revocation reason is "key compromise" or "CA compromise", then the object is placed into the
 * "compromised" state; the Date is set to the current date and time; and the Compromise Occurrence Date is set to the
 * value (if provided) in the Revoke request and if a value is not provided in the Revoke request then Compromise
 * Occurrence Date SHOULD be set to the Initial Date for the object. If the revocation reason is neither "key
 * compromise" nor "CA compromise", the object is placed into the "deactivated" state, and the Deactivation Date is set
 * to the current date and time.
 */
@JsonSerialize(using = KmipStructSerializer.class)
@JsonDeserialize(using = KmipStructDeserializer.class)
public class Revoke implements KmipStruct {

    /**
     * Determines the object being revoked. If omitted, then the ID Placeholder value is used by the server as the
     * Unique Identifier.
     */
    @JsonProperty(value = "UniqueIdentifier")
    private Optional<String> uniqueIdentifier = Optional.empty();

    /**
     * Specifies the reason for revocation.
     */
    @JsonProperty(value = "RevocationReason")
    private RevocationReason revocation_reason;

    /**
     * SHOULD be specified if the Revocation Reason is 'key compromise' or 'CA compromise' and SHALL NOT be specified
     * for other Revocation Reason enumerations.
     */
    @JsonProperty(value = "CompromiseOccurrenceDate")
    private Optional<Long> compromise_occurrence_date = Optional.empty();

    public Revoke() {
    }

    public Revoke(Optional<String> uniqueIdentifier, RevocationReason revocation_reason,
        Optional<Long> compromise_occurrence_date) {
        this.uniqueIdentifier = uniqueIdentifier;
        this.revocation_reason = revocation_reason;
        this.compromise_occurrence_date = compromise_occurrence_date;
    }

    public Optional<String> getUniqueIdentifier() {
        return this.uniqueIdentifier;
    }

    public void setUniqueIdentifier(Optional<String> uniqueIdentifier) {
        this.uniqueIdentifier = uniqueIdentifier;
    }

    public RevocationReason getRevocation_reason() {
        return this.revocation_reason;
    }

    public void setRevocation_reason(RevocationReason revocation_reason) {
        this.revocation_reason = revocation_reason;
    }

    public Optional<Long> getCompromise_occurrence_date() {
        return this.compromise_occurrence_date;
    }

    public void setCompromise_occurrence_date(Optional<Long> compromise_occurrence_date) {
        this.compromise_occurrence_date = compromise_occurrence_date;
    }

    public Revoke uniqueIdentifier(Optional<String> uniqueIdentifier) {
        setUniqueIdentifier(uniqueIdentifier);
        return this;
    }

    public Revoke revocation_reason(RevocationReason revocation_reason) {
        setRevocation_reason(revocation_reason);
        return this;
    }

    public Revoke compromise_occurrence_date(Optional<Long> compromise_occurrence_date) {
        setCompromise_occurrence_date(compromise_occurrence_date);
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (o == this)
            return true;
        if (!(o instanceof Revoke)) {
            return false;
        }
        Revoke revoke = (Revoke) o;
        return Objects.equals(uniqueIdentifier, revoke.uniqueIdentifier)
            && Objects.equals(revocation_reason, revoke.revocation_reason)
            && Objects.equals(compromise_occurrence_date, revoke.compromise_occurrence_date);
    }

    @Override
    public int hashCode() {
        return Objects.hash(uniqueIdentifier, revocation_reason, compromise_occurrence_date);
    }

    @Override
    public String toString() {
        return "{" + " uniqueIdentifier='" + getUniqueIdentifier() + "'" + ", revocation_reason='"
            + getRevocation_reason() + "'" + ", compromise_occurrence_date='" + getCompromise_occurrence_date() + "'"
            + "}";
    }

}
