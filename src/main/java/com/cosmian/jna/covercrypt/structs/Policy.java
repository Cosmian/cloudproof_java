package com.cosmian.jna.covercrypt.structs;

import java.util.Arrays;
import java.util.Objects;

import com.cosmian.rest.kmip.types.Attributes;
import com.cosmian.rest.kmip.types.VendorAttribute;
import com.cosmian.utils.CloudproofException;
import com.sun.jna.ptr.IntByReference;

/**
 * A policy group is a set of fixed policy axis, defining an inner attribute element for each policy axis attribute a
 * fixed number of revocation / addition of attributes is allowed
 */
public class Policy extends Ffi {
    private byte[] _bytes;

    /**
     * Instantiate a `Policy` with the given bytes.
     *
     * @param bytes the array of bytes to be used for this policy
     */
    public Policy(byte[] bytes) {
        this._bytes = bytes;
    }

    /**
     * Instantiate an empty policy allowing up to 2^32 attribute modifications.
     *
     * @throws CloudproofException if the policy buffer cannot be retrieved
     */
    public Policy() throws CloudproofException {
        byte[] policyBuffer = new byte[8192];
        IntByReference policyBufferSize = new IntByReference(policyBuffer.length);
        unwrap(instance.h_policy(policyBuffer, policyBufferSize, Integer.MAX_VALUE));
        _bytes = Arrays.copyOfRange(policyBuffer, 0, policyBufferSize.getValue());
    }

    /**
     * Instantiate an empty policy allowing the given max number of revocations of attributes
     *
     * @param maxAttributeCreations the maximum number of attribute creations allowed
     * @throws CloudproofException if the policy buffer cannot be retrieved
     */
    public Policy(int maxAttributeCreations) throws CloudproofException {
        byte[] policyBuffer = new byte[8192];
        IntByReference policyBufferSize = new IntByReference(policyBuffer.length);
        unwrap(instance.h_policy(policyBuffer, policyBufferSize, maxAttributeCreations));
        _bytes = Arrays.copyOfRange(policyBuffer, 0, policyBufferSize.getValue());
    }

    /**
     * Constructs a Policy object with a specified limit on the number of attribute creations and a set of policy axes,
     * retrieves its policy buffer, and initializes its byte array.
     *
     * @param maxAttributeCreations the maximum number of attribute creations allowed
     * @param axes the set of policy axes to be added to the policy
     * @throws CloudproofException if the policy buffer cannot be retrieved
     */
    public Policy(int maxAttributeCreations, PolicyAxis[] axes) throws CloudproofException {
        byte[] buffer = new byte[8192];
        IntByReference bufferSize = new IntByReference(buffer.length);
        unwrap(instance.h_policy(buffer, bufferSize, maxAttributeCreations));
        _bytes = Arrays.copyOfRange(buffer, 0, bufferSize.getValue());
        bufferSize.setValue(buffer.length);

        for (int i = 0; i < axes.length; i++) {
            unwrap(instance.h_add_policy_axis(buffer, bufferSize, _bytes, _bytes.length,
                axes[i].toString() + "\0"));
            _bytes = Arrays.copyOfRange(buffer, 0, bufferSize.getValue());
            bufferSize.setValue(buffer.length);
        }
    }

    /**
     * Add the given Axis to this policy in place.
     *
     * @param axis acis to add to the policy
     * @throws CloudproofException if the addition fails
     */
    public void addAxis(PolicyAxis axis) throws CloudproofException {
        byte[] updatedPolicy = new byte[8192];
        IntByReference updatePolicySize = new IntByReference(updatedPolicy.length);
        unwrap(instance.h_add_policy_axis(updatedPolicy, updatePolicySize, this._bytes, this._bytes.length,
            axis.toString() + "\0"));
        _bytes = Arrays.copyOfRange(updatedPolicy, 0, updatePolicySize.getValue());
    }

    /**
     * Return the policy bytes.
     *
     * @return the array of bytes from this object
     */
    public byte[] getBytes() {
        return _bytes;
    }

    /**
     * Convert the policy to a KMIP Vendor attribute that can be set on a KMIP Object
     *
     * @return the {@link VendorAttribute}
     * @throws CloudproofException if the JSON cannot be serialized
     */
    public VendorAttribute toVendorAttribute() throws CloudproofException {
        return new VendorAttribute(
            VendorAttribute.VENDOR_ID_COSMIAN,
            VendorAttribute.VENDOR_ATTR_COVER_CRYPT_POLICY,
            this._bytes);
    }

    /**
     * Extract the policy from the Key Attributes
     *
     * @param attributes the key attributes
     * @return the {Policy}
     * @throws CloudproofException if there is no policy in the attributes
     */
    public static Policy fromAttributes(Attributes attributes) throws CloudproofException {
        VendorAttribute[] vas;
        if (attributes.getVendorAttributes().isPresent()) {
            vas = attributes.getVendorAttributes().get();
        } else {
            throw new CloudproofException("No policy available in the attributes: no vendor attributes");
        }
        for (VendorAttribute va : vas) {
            if (va.getVendor_identification().equals(VendorAttribute.VENDOR_ID_COSMIAN)) {
                if (va.getAttribute_name().equals(VendorAttribute.VENDOR_ATTR_ABE_POLICY)
                    || va.getAttribute_name().equals(
                        VendorAttribute.VENDOR_ATTR_COVER_CRYPT_POLICY)) {
                    return new Policy(va.getAttribute_value());
                }
            }
        }
        throw new CloudproofException("No policy available in the vendor attributes");
    }

    public void rotateAttributes(String[] attributes) throws CloudproofException {
        byte[] updatedPolicyBuffer = new byte[8192];
        IntByReference updatedPolicyBufferSize = new IntByReference(updatedPolicyBuffer.length);
        for (String attr : attributes) {
            unwrap(instance.h_rotate_attribute(updatedPolicyBuffer, updatedPolicyBufferSize, _bytes,
                _bytes.length, attr));
            _bytes = Arrays.copyOfRange(updatedPolicyBuffer, 0, updatedPolicyBufferSize.getValue());
            updatedPolicyBufferSize.setValue(updatedPolicyBuffer.length);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (o == this)
            return true;
        if (!(o instanceof Policy)) {
            return false;
        }
        Policy policyGroup = (Policy) o;
        return _bytes == policyGroup._bytes;
    }

    @Override
    public int hashCode() {
        return Objects.hash(_bytes);
    }
}
