package com.cosmian.jna.findex.Callbacks;

import java.io.IOException;
import java.util.HashMap;

import com.cosmian.jna.FfiException;
import com.cosmian.jna.findex.FfiWrapper.UpsertEntryCallback;
import com.cosmian.jna.findex.FfiWrapper.UpsertEntryInterface;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.jna.Pointer;

public class UpsertEntry implements UpsertEntryCallback {

    private UpsertEntryInterface upsert;

    public UpsertEntry(UpsertEntryInterface upsert) {
        this.upsert = upsert;
    }

    @Override
    public int apply(Pointer entries, int entriesLength) throws FfiException {
        //
        // Read `entries` until `entriesLength`
        //
        byte[] entriesBytes = new byte[entriesLength];
        entries.read(0, entriesBytes, 0, entriesLength);

        // For the JSON strings
        ObjectMapper mapper = new ObjectMapper();

        //
        // Deserialize vector Entry Table `uid`
        //
        HashMap<String, String> uidsAndValues = new HashMap<String, String>();
        try {
            uidsAndValues = mapper.readValue(entriesBytes, HashMap.class);
        } catch (IOException e) {
            throw new FfiException("Failed deserializing UpsertChain callback: ", e);
        }

        //
        // Insert in database
        //
        this.upsert.upsert(uidsAndValues);

        return 0;
    }

}
