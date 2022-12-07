package com.cosmian.jna.findex;

import java.io.IOException;

import com.cosmian.Leb128;
import com.cosmian.jna.findex.Leb128Serializer.Leb128Serializable;

public class Tuple<LEFT extends Leb128Serializable, RIGHT extends Leb128Serializable> implements Leb128Serializable {
    private LEFT left;

    private RIGHT right;

    private boolean empty;

    public Tuple(LEFT left, RIGHT right) {
        this.left = left;
        this.right = right;
        this.empty = left.isEmpty() && right.isEmpty();
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
        out.writeObject(this.left);
        out.writeObject(this.right);
    }

    private void readObject(java.io.ObjectInputStream in)
            throws IOException, ClassNotFoundException {
        int length = (int) Leb128.readU64(in);
        if (length == 0) {
            this.empty = true;
        } else {
            @SuppressWarnings("unchecked")
            final LEFT left = (LEFT) in.readObject();
            @SuppressWarnings("unchecked")
            final RIGHT right = (RIGHT) in.readObject();
            this.left = left;
            this.right = right;
            this.empty = left.isEmpty() && right.isEmpty();
        }
    }

    @Override
    public boolean isEmpty() {
        return this.empty || (this.left.isEmpty() && this.right.isEmpty());
    }
}