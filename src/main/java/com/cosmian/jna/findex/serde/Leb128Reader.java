package com.cosmian.jna.findex.serde;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import com.cosmian.CloudproofException;

public class Leb128Reader {

    final PeekInputStream in;

    public Leb128Reader(byte[] bytes) {
        this.in = new PeekInputStream(new ByteArrayInputStream(bytes));
    }

    public Leb128Reader(InputStream in) {
        this.in = new PeekInputStream(in);
    }

    public <T extends Leb128Serializable> T readObject(T newInstance) throws CloudproofException {
        newInstance.readObject(this.in);
        return newInstance;
    }

    public boolean reachedCollectionEnd() throws CloudproofException {
        try {
            return this.in.peek() == 0;
        } catch (IOException e) {
            throw new CloudproofException("Leb128 reader: failed reading the collection end mark: " + e.getMessage(),
                e);
        }
    }

    public <T extends Leb128Serializable> List<T> readList(Class<? extends Leb128Serializable> clazzOfT)
        throws CloudproofException {
        List<T> result = new ArrayList<T>();
        while (!this.reachedCollectionEnd()) {
            T element;
            try {
                @SuppressWarnings("unchecked")
                final T el = (T) clazzOfT.newInstance();
                element = el;
            } catch (InstantiationException | IllegalAccessException e) {
                throw new CloudproofException(
                    "Leb128 reader: failed instantiating a " + clazzOfT.getSimpleName() + ": " + e.getMessage(), e);
            }
            element.readObject(this.in);
            result.add(element);
        }
        return result;
    }

    public static <T extends Leb128Serializable> List<T> deserializeList(Class<? extends Leb128Serializable> clazzOfT,
                                                                         byte[] bytes)
        throws CloudproofException {
        return new Leb128Reader(new ByteArrayInputStream(bytes)).readList(clazzOfT);
    }

}
