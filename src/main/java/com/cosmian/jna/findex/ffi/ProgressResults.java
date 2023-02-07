package com.cosmian.jna.findex.ffi;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.cosmian.jna.findex.serde.Leb128Serializable;
import com.cosmian.jna.findex.structs.IndexedValue;
import com.cosmian.jna.findex.structs.Keyword;
import com.cosmian.utils.CloudproofException;
import com.cosmian.utils.Leb128;

public class ProgressResults implements Leb128Serializable {

    private Map<Keyword, Set<IndexedValue>> results;

    public ProgressResults() {
        this.results = new HashMap<>();
    }

    public Map<Keyword, Set<IndexedValue>> getResults() {
        return results;
    }

    @Override
    public void readObject(InputStream is) throws CloudproofException {
        try {
            int mapLen = (int) Leb128.readU64(is);
            for (int i = 0; i < mapLen; i++) {
                Keyword keyword = new Keyword(Leb128.readByteArray(is));
                int numResults = (int) Leb128.readU64(is);
                Set<IndexedValue> indexedValues = new HashSet<>();
                for (int j = 0; j < numResults; j++) {
                    System.out.println("reading: " + j + "/" + numResults);
                    indexedValues.add(new IndexedValue(Leb128.readByteArray(is)));
                }
                results.put(keyword, indexedValues);
            }
        } catch (IOException e) {
            throw new CloudproofException("failed deserializing the search results: " + e.getMessage(), e);
        }
    }

    @Override
    public void writeObject(OutputStream os) throws CloudproofException {
        throw new CloudproofException("Search Results are not serializable");
    }

}
