package com.cosmian.jna.findex;

import java.io.IOException;

import com.cosmian.jna.findex.Leb128Serializer.Leb128Serializable;

/**
 * A tuple holds a pair of
 * {@link com.cosmian.jna.findex.Leb128Serializer.Leb128Serializable} values and
 * is itself {@link com.cosmian.jna.findex.Leb128Serializer.Leb128Serializable}
 */
public class Tuple<LEFT extends Leb128Serializable, RIGHT extends Leb128Serializable> implements Leb128Serializable {
    private LEFT left;

    private RIGHT right;

    private boolean empty;

    public Tuple(LEFT left, RIGHT right) {
        this.left = left;
        this.right = right;
        this.empty = false;
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

    private void writeObject(java.io.ObjectOutputStream out)
            throws IOException {
        if (this.empty) {
            out.write(0);
        } else {
            out.writeObject(this.left);
            out.writeObject(this.right);
        }
    }

    private void readObject(java.io.ObjectInputStream in)
            throws IOException, ClassNotFoundException {
        @SuppressWarnings("unchecked")
        final LEFT left = (LEFT) in.readObject();
        if (!left.isEmpty()) {
            @SuppressWarnings("unchecked")
            final RIGHT right = (RIGHT) in.readObject();
            this.empty = false;
            this.left = left;
            this.right = right;
        } else {
            this.empty = true;
            // cleanup potential previous values
            this.left = null;
            this.right = null;
        }
    }

    @Override
    public boolean isEmpty() {
        return this.empty || (this.left.isEmpty() && this.right.isEmpty());
    }
}