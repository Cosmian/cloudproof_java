package com.cosmian.jna.covercrypt.structs;

import java.io.Serializable;

/**
 * An attribute in a policy group is characterized by the policy axis and its own name within that axis
 */
public class PolicyAxisAttribute implements Serializable {
    private final String name;

    private final boolean encryptionHint;

    public PolicyAxisAttribute(String name, boolean encryptionHint) {
        this.name = name;
        this.encryptionHint = encryptionHint;
    }

    public String getName() {
        return this.name;
    }

    public boolean getEncryptionHint() {
        return this.encryptionHint;
    }

    @Override
    public String toString() {
        String res = "{ \"name\": \"" + getName() + "\", \"encryption_hint\": \"";
        if (getEncryptionHint()) {
            res += "Hybridized";
        } else {
            res += "Classic";
        }
        ;
        return res + "\" }";
    }
}
