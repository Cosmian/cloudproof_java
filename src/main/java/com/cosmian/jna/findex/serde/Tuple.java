package com.cosmian.jna.findex.serde;

import java.util.Map;
import java.util.Objects;

/**
 * A tuple holds a pair of {@link Leb128Serializable}. A tuple is also a {@link java.util.Map.Entry} and can be used in
 * methods taking it as argument (LEFT is the KEY, RIGHT is the VALUE).
 */
public class Tuple<LEFT extends Leb128Serializable, RIGHT extends Leb128Serializable>
    implements Map.Entry<LEFT, RIGHT> {
    protected LEFT left;

    protected RIGHT right;

    public Tuple(LEFT left, RIGHT right) {
        this.left = left;
        this.right = right;
    }

    public LEFT getLeft() {
        return left;
    }

    public RIGHT getRight() {
        return right;
    }

    @Override
    public String toString() {
        return "(" + this.left.toString() + ", " + this.right.toString() + ")";
    }

    @Override
    public int hashCode() {
        return right.hashCode() ^ left.hashCode();
    }

    @Override
    public boolean equals(Object o) {
        if (o == this)
            return true;
        if (!(o instanceof Tuple)) {
            return false;
        }
        Tuple<?, ?> tuple = (Tuple<?, ?>) o;
        return Objects.equals(left, tuple.left) && Objects.equals(right, tuple.right);
    }

    @Override
    public LEFT getKey() {
        return this.left;
    }

    @Override
    public RIGHT getValue() {
        return this.right;
    }

    @Override
    public RIGHT setValue(RIGHT value) {
        this.right = value;
        return right;
    }

}
