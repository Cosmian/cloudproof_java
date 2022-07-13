package com.cosmian.jna.findex.Callbacks;

import java.io.IOException;
import java.util.HashMap;

import com.cosmian.jna.FfiException;
import com.cosmian.jna.findex.FfiWrapper.UpsertChainCallback;
import com.cosmian.jna.findex.FfiWrapper.UpsertChainInterface;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.jna.Pointer;

public class UpsertChain implements UpsertChainCallback {

    private UpsertChainInterface upsert;

    public UpsertChain(UpsertChainInterface upsert) {
        this.upsert = upsert;
    }

    @Override
    public int apply(Pointer chains, int chainsLength) throws FfiException {
        //
        // Read `chains` until `chainsLength`
        //
        byte[] chainsBytes = new byte[chainsLength];
        chains.read(0, chainsBytes, 0, chainsLength);

        // For the JSON strings
        ObjectMapper mapper = new ObjectMapper();

        //
        // Deserialize vector Entry Table `uid`
        //
        HashMap<String, String> uidsAndValues = new HashMap<String, String>();
        try {
            uidsAndValues = mapper.readValue(chainsBytes, HashMap.class);
        } catch (IOException e) {
            throw new FfiException("Failed deserializing UpsertChain callback: " + e.toString());
        }

        //
        // Insert in database
        //
        this.upsert.upsert(uidsAndValues);

        return 0;
    }
}
