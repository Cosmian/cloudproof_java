package com.cosmian.jna.findex.ffi;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashSet;
import java.util.Set;
import com.cosmian.jna.findex.serde.Leb128Serializable;
import com.cosmian.jna.findex.structs.Keyword;
import com.cosmian.utils.CloudproofException;
import com.cosmian.utils.Leb128;

public class UpsertResults implements Leb128Serializable {

    private Set<Keyword> results;

    public UpsertResults() {
        this.results = new HashSet<>();
    }

    public Set<Keyword> getResults() {
        return results;
    }

    public boolean isEmpty() {
        return results.isEmpty();
    }

    public int numberOfKeywords() {
        return results.size();
    }

    @Override
    public void readObject(InputStream is) throws CloudproofException {
        try {

            int setLen = (int) Leb128.readU64(is);
            for (int i = 0; i < setLen; i++) {
                Keyword keyword = new Keyword(Leb128.readByteArray(is));
                results.add(keyword);
            }
        } catch (IOException e) {
            throw new CloudproofException("failed deserializing the upsert results: " + e.getMessage(), e);
        }

    }

    @Override
    public void writeObject(OutputStream os) throws CloudproofException {
        throw new CloudproofException("Upsert results are not serializable");
    }
}
