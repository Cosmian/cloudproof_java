package com.cosmian.jna.findex;

import java.io.InputStream;
import java.io.OutputStream;

import com.cosmian.CloudproofException;
import com.cosmian.jna.findex.serde.Leb128Reader;
import com.cosmian.jna.findex.serde.Leb128Serializable;
import com.cosmian.jna.findex.serde.Leb128Writer;
import com.cosmian.jna.findex.serde.Tuple;

public class EntryTableValues extends Tuple<EntryTableValue, EntryTableValue> implements Leb128Serializable {

    public EntryTableValues() {
        super(new EntryTableValue(), new EntryTableValue());
    }

    public EntryTableValues(EntryTableValue previousValue, EntryTableValue newValue) {
        super(previousValue, newValue);
    }

    public EntryTableValues(byte[] previousValue, byte[] newValue) {
        super(new EntryTableValue(previousValue), new EntryTableValue(newValue));
    }

    public EntryTableValue getPrevious() {
        return this.left;
    }

    public EntryTableValue getNew() {
        return this.right;
    }

    @Override
    public void readObject(InputStream is) throws CloudproofException {
        Leb128Reader reader = new Leb128Reader(is);
        Tuple<EntryTableValue, EntryTableValue> tuple = reader.readTuple(EntryTableValue.class, EntryTableValue.class);
        this.left = tuple.getLeft();
        this.right = tuple.getRight();

    }

    @Override
    public void writeObject(OutputStream os) throws CloudproofException {
        Leb128Writer writer = new Leb128Writer(os);
        writer.writeTuple(this);
    }

}
