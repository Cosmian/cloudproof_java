package com.cosmian.rest.kmip.operations;

import java.util.Objects;
import java.util.Optional;

import com.cosmian.rest.kmip.json.KmipStruct;
import com.cosmian.rest.kmip.json.KmipStructDeserializer;
import com.cosmian.rest.kmip.json.KmipStructSerializer;
import com.cosmian.rest.kmip.types.Attributes;
import com.cosmian.rest.kmip.types.ObjectGroupMember;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

/**
 * This operation requests that the server search for one or more Managed
 * Objects, depending on the attributes specified in the request. All attributes
 * are allowed to be used. The request MAY contain a Maximum Items field, which
 * specifies the maximum number of objects to be returned. If the Maximum Items
 * field is omitted, then the server MAY return all objects matched, or MAY
 * impose an internal maximum limit due to resource limitations.
 * 
 * The request MAY contain an Offset Items field, which specifies the number of
 * objects to skip that satisfy the identification criteria specified in the
 * request. An Offset Items field of 0 is the same as omitting the Offset Items
 * field. If both Offset Items and Maximum Items are specified in the request,
 * the server skips Offset Items objects and returns up to Maximum Items
 * objects.
 * 
 * If more than one object satisfies the identification criteria specified in
 * the request, then the response MAY contain Unique Identifiers for multiple
 * Managed Objects. Responses containing Unique Identifiers for multiple objects
 * SHALL be returned in descending order of object creation (most recently
 * created object first). Returned objects SHALL match all of the attributes in
 * the request. If no objects match, then an empty response payload is returned.
 * If no attribute is specified in the request, any object SHALL be deemed to
 * match the Locate request. The response MAY include Located Items which is the
 * count of all objects that satisfy the identification criteria.
 * 
 * The server returns a list of Unique Identifiers of the found objects, which
 * then MAY be retrieved using the Get operation. If the objects are archived,
 * then the Recover and Get operations are REQUIRED to be used to obtain those
 * objects. If a single Unique Identifier is returned to the client, then the
 * server SHALL copy the Unique Identifier returned by this operation into the
 * ID Placeholder variable. If the Locate operation matches more than one
 * object, and the Maximum Items value is omitted in the request, or is set to a
 * value larger than one, then the server SHALL empty the ID Placeholder,
 * causing any subsequent operations that are batched with the Locate, and which
 * do not specify a Unique Identifier explicitly, to fail. This ensures that
 * these batched operations SHALL proceed only if a single object is returned by
 * Locate.
 * 
 * The Date attributes in the Locate request (e.g., Initial Date, Activation
 * Date, etc.) are used to specify a time or a time range for the search. If a
 * single instance of a given Date attribute is used in the request (e.g., the
 * Activation Date), then objects with the same Date attribute are considered to
 * be matching candidate objects. If two instances of the same Date attribute
 * are used (i.e., with two different values specifying a range), then objects
 * for which the Date attribute is inside or at a limit of the range are
 * considered to be matching candidate objects. If a Date attribute is set to
 * its largest possible value, then it is equivalent to an undefined attribute.
 * 
 * When the Cryptographic Usage Mask attribute is specified in the request,
 * candidate objects are compared against this field via an operation that
 * consists of a logical AND of the requested mask with the mask in the
 * candidate object, and then a comparison of the resulting value with the
 * requested mask. For example, if the request contains a mask value of
 * 10001100010000, and a candidate object mask contains 10000100010000, then the
 * logical AND of the two masks is 10000100010000, which is compared against the
 * mask value in the request (10001100010000) and the match fails. This means
 * that a matching candidate object has all of the bits set in its mask that are
 * set in the requested mask, but MAY have additional bits set.
 * 
 * When the Usage Limits attribute is specified in the request, matching
 * candidate objects SHALL have a Usage Limits Count and Usage Limits Total
 * equal to or larger than the values specified in the request.
 * 
 * When an attribute that is defined as a structure is specified, all of the
 * structure fields are not REQUIRED to be specified. For instance, for the Link
 * attribute, if the Linked Object Identifier value is specified without the
 * Link Type value, then matching candidate objects have the Linked Object
 * Identifier as specified, irrespective of their Link Type.
 * 
 * When the Object Group attribute and the Object Group Member flag are
 * specified in the request, and the value specified for Object Group Member is
 * ‘Group Member Fresh’, matching candidate objects SHALL be fresh objects from
 * the object group. If there are no more fresh objects in the group, the server
 * MAY choose to generate a new object on-the-fly, based on server policy. If
 * the value specified for Object Group Member is ‘Group Member Default’, the
 * server locates the default object as defined by server policy.
 * 
 * The Storage Status Mask field is used to indicate whether on-line objects
 * (not archived or destroyed), archived objects, destroyed objects or any
 * combination of the above are to be searched.The server SHALL NOT return
 * unique identifiers for objects that are destroyed unless the Storage Status
 * Mask field includes the Destroyed Storage indicator. The server SHALL NOT
 * return unique identifiers for objects that are archived unless the Storage
 * Status Mask field includes the Archived Storage indicator.
 */
@JsonSerialize(using = KmipStructSerializer.class)
@JsonDeserialize(using = KmipStructDeserializer.class)
public class Locate implements KmipStruct {

    /**
     * An Integer object that indicates the maximum number of object
     * identifiers the server MAY return.
     */
    @JsonProperty(value = "MaximumItems")
    private Optional<Integer> maximum_items;

    /**
     * An Integer object that indicates the number of object identifiers to
     * skip that satisfy the identification criteria specified in the request.
     */
    @JsonProperty(value = "OffsetItems")
    private Optional<Integer> offset_items;

    /**
     * An Integer object (used as a bit mask) that indicates whether only
     * on-line objects, only archived objects, destroyed objects or any
     * combination of these, are to be searched. If omitted, then only on-line
     * objects SHALL be returned.
     */
    @JsonProperty(value = "StorageStatusMask")
    private Optional<Integer> storage_status_mask;

    /**
     * An Enumeration object that indicates the object group member type.
     */
    @JsonProperty(value = "ObjectGroupMember")
    private Optional<ObjectGroupMember> object_group_member;

    /**
     * Specifies an attribute and its value(s) that are REQUIRED to match those
     * in a candidate object (according to the matching rules defined above).
     */
    @JsonProperty(value = "Attributes")
    private Attributes attributes;

    public Locate() {
    }

    public Locate(Optional<Integer> maximum_items, Optional<Integer> offset_items,
            Optional<Integer> storage_status_mask, Optional<ObjectGroupMember> object_group_member,
            Attributes attributes) {
        this.maximum_items = maximum_items;
        this.offset_items = offset_items;
        this.storage_status_mask = storage_status_mask;
        this.object_group_member = object_group_member;
        this.attributes = attributes;
    }

    public Optional<Integer> getMaximum_items() {
        return this.maximum_items;
    }

    public void setMaximum_items(Optional<Integer> maximum_items) {
        this.maximum_items = maximum_items;
    }

    public Optional<Integer> getOffset_items() {
        return this.offset_items;
    }

    public void setOffset_items(Optional<Integer> offset_items) {
        this.offset_items = offset_items;
    }

    public Optional<Integer> getStorage_status_mask() {
        return this.storage_status_mask;
    }

    public void setStorage_status_mask(Optional<Integer> storage_status_mask) {
        this.storage_status_mask = storage_status_mask;
    }

    public Optional<ObjectGroupMember> getObject_group_member() {
        return this.object_group_member;
    }

    public void setObject_group_member(Optional<ObjectGroupMember> object_group_member) {
        this.object_group_member = object_group_member;
    }

    public Attributes getAttributes() {
        return this.attributes;
    }

    public void setAttributes(Attributes attributes) {
        this.attributes = attributes;
    }

    public Locate maximum_items(Optional<Integer> maximum_items) {
        setMaximum_items(maximum_items);
        return this;
    }

    public Locate offset_items(Optional<Integer> offset_items) {
        setOffset_items(offset_items);
        return this;
    }

    public Locate storage_status_mask(Optional<Integer> storage_status_mask) {
        setStorage_status_mask(storage_status_mask);
        return this;
    }

    public Locate object_group_member(Optional<ObjectGroupMember> object_group_member) {
        setObject_group_member(object_group_member);
        return this;
    }

    public Locate attributes(Attributes attributes) {
        setAttributes(attributes);
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (o == this)
            return true;
        if (!(o instanceof Locate)) {
            return false;
        }
        Locate locate = (Locate) o;
        return Objects.equals(maximum_items, locate.maximum_items) && Objects.equals(offset_items, locate.offset_items)
                && Objects.equals(storage_status_mask, locate.storage_status_mask)
                && Objects.equals(object_group_member, locate.object_group_member)
                && Objects.equals(attributes, locate.attributes);
    }

    @Override
    public int hashCode() {
        return Objects.hash(maximum_items, offset_items, storage_status_mask, object_group_member, attributes);
    }

    @Override
    public String toString() {
        return "{" +
                " maximum_items='" + getMaximum_items() + "'" +
                ", offset_items='" + getOffset_items() + "'" +
                ", storage_status_mask='" + getStorage_status_mask() + "'" +
                ", object_group_member='" + getObject_group_member() + "'" +
                ", attributes='" + getAttributes() + "'" +
                "}";
    }

}
