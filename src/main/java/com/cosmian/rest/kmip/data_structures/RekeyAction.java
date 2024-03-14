package com.cosmian.rest.kmip.data_structures;

import com.cosmian.rest.kmip.types.VendorAttribute;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class RekeyAction {
    private byte[] serializedAction;

    public RekeyAction rekeyAccessPolicy(String accessPolicy) throws JsonProcessingException {
        this.serializedAction = toJsonBytes(new RekeyAccessPolicy(accessPolicy));
        return this;
    }

    public RekeyAction pruneAccessPolicy(String accessPolicy) throws JsonProcessingException {
        this.serializedAction = toJsonBytes(new PruneAccessPolicy(accessPolicy));
        return this;
    }

    public RekeyAction removeAttribute(String attribute) throws JsonProcessingException {
        this.serializedAction = toJsonBytes(new RemoveAttribute(attribute));
        return this;
    }

    public RekeyAction disableAttribute(String attribute) throws JsonProcessingException {
        this.serializedAction = toJsonBytes(new DisableAttribute(attribute));
        return this;
    }

    public RekeyAction addAttribute(String attribute, boolean isHybridized) throws JsonProcessingException {
        this.serializedAction = toJsonBytes(new AddAttribute(attribute, isHybridized));
        return this;
    }

    public RekeyAction renameAttribute(String attribute, String newName) throws JsonProcessingException {
        this.serializedAction = toJsonBytes(new RenameAttribute(attribute, newName));
        return this;
    }

    public VendorAttribute toVendorAttribute() {
        if (serializedAction == null) {
            throw new IllegalStateException("A RekeyAction must be properly initialized before calling toVendorAttribute.");
        }
        System.out.println(serializedAction);
        return new VendorAttribute(VendorAttribute.VENDOR_ID_COSMIAN,
                                    VendorAttribute.VENDOR_ATTR_COVER_CRYPT_REKEY_ACTION,
                                    serializedAction);
    }

    private byte[] toJsonBytes(Object obj) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.writeValueAsBytes(obj);
    }

    // Define classes for variants with Jackson annotations
    static class RekeyAccessPolicy {
        @JsonProperty("RekeyAccessPolicy")
        private String accessPolicy;

        RekeyAccessPolicy(String accessPolicy) {
            this.accessPolicy = accessPolicy;
        }
    }

    static class PruneAccessPolicy {
        @JsonProperty("PruneAccessPolicy")
        private String accessPolicy;

        PruneAccessPolicy(String accessPolicy) {
            this.accessPolicy = accessPolicy;
        }
    }

    static class RemoveAttribute {
        @JsonProperty("RemoveAttribute")
        private String[] attribute;

        RemoveAttribute(String attribute) {
            this.attribute = new String[]{attribute};
        }
    }

    static class DisableAttribute {
        @JsonProperty("DisableAttribute")
        private String[] attribute;

        DisableAttribute(String attribute) {
            this.attribute = new String[]{attribute};
        }
    }

    static class AddAttribute {
        @JsonProperty("AddAttribute")
        private String[][] attribute;

        AddAttribute(String attribute, boolean isHybridized) {
            this.attribute = new String[][]{{attribute, isHybridized ? "Hybridized" : "Classic"}};
        }
    }

    static class RenameAttribute {
        @JsonProperty("RenameAttribute")
        private String[][] attribute;

        RenameAttribute(String attribute, String newName) {
            this.attribute = new String[][]{{attribute, newName}};
        }
    }
}

