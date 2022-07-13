package com.cosmian.rest.gpsw.acccess_policy;

import java.util.Objects;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;

@JsonSerialize(using = OrSerializer.class)
public class Or extends AccessPolicy {

    private AccessPolicy left;

    private AccessPolicy right;

    public Or() {
    }

    public Or(AccessPolicy left, AccessPolicy right) {
        this.left = left;
        this.right = right;
    }

    public AccessPolicy getLeft() {
        return this.left;
    }

    public void setLeft(AccessPolicy left) {
        this.left = left;
    }

    public AccessPolicy getRight() {
        return this.right;
    }

    public void setRight(AccessPolicy right) {
        this.right = right;
    }

    public Or left(AccessPolicy left) {
        setLeft(left);
        return this;
    }

    public Or right(AccessPolicy right) {
        setRight(right);
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (o == this)
            return true;
        if (!(o instanceof Or)) {
            return false;
        }
        Or and = (Or) o;
        return Objects.equals(left, and.left) && Objects.equals(right, and.right);
    }

    @Override
    public int hashCode() {
        return Objects.hash(left, right);
    }

    @Override
    public String toString() {
        return "{" + " left='" + getLeft() + "'" + ", right='" + getRight() + "'" + "}";
    }

}
