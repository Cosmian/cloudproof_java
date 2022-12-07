package com.cosmian.jna.findex.serde;

import java.io.InputStream;
import java.io.OutputStream;

import com.cosmian.CloudproofException;

/**
 * A tuple holds a pair of {@link com.cosmian.jna.findex.serde.Leb128CollectionsSerializer.Leb128Serializable} values
 * and is itself {@link com.cosmian.jna.findex.serde.Leb128CollectionsSerializer.Leb128Serializable}
 */
public class Tuple<LEFT extends Leb128Serializable, RIGHT extends Leb128Serializable> implements Leb128Serializable {
    private LEFT left;

    private RIGHT right;

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

    public void writeObject(OutputStream out) throws CloudproofException {
        this.left.writeObject(out);
        this.right.writeObject(out);
    }

    @Override
    public void readObject(InputStream in) throws CloudproofException {
        Leb128Reader reader = new Leb128Reader(in);
        reader.readObject(this.left);
        reader.readObject(this.right);
    }

}
