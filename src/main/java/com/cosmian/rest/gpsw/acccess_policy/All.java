package com.cosmian.rest.gpsw.acccess_policy;

public class All extends AccessPolicy {

    public All() {
    }

    @Override
    public boolean equals(Object o) {
        if (o == this)
            return true;
        if (!(o instanceof All)) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        return 1234554321;
    }

    @Override
    public String toString() {
        return "{" + "}";
    }

}
