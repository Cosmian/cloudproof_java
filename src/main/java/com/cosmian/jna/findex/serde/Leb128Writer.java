package com.cosmian.jna.findex.serde;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

import com.cosmian.CloudproofException;

public class Leb128Writer {

    final OutputStream os;

    public Leb128Writer(OutputStream os) {
        this.os = os;
    }

    public <T extends Leb128Serializable> void writeObject(T obj) throws CloudproofException {
        obj.writeObject(os);
    }

    private void writeCollectionEnd() throws CloudproofException {
        try {
            os.write(0);
            os.flush();
        } catch (IOException e) {
            throw new CloudproofException("Leb128 writer: failed writing the collection end mark: " + e.getMessage(),
                e);
        }
    }

    public <T extends Leb128Serializable> void writeList(List<T> elements) throws CloudproofException {
        for (T value : elements) {
            this.writeObject(value);
        }
        // mark the end
        this.writeCollectionEnd();
    }

    public static <T extends Leb128Serializable> byte[] serializeList(List<T> elements) throws CloudproofException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        new Leb128Writer(bos).writeList(elements);
        return bos.toByteArray();
    }

}
