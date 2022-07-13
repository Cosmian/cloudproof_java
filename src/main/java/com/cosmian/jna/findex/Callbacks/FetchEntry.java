package com.cosmian.jna.findex.Callbacks;

import java.io.IOException;
import java.util.HashMap;

import com.cosmian.jna.FfiException;
import com.cosmian.jna.findex.FfiWrapper.FetchEntryCallback;
import com.cosmian.jna.findex.FfiWrapper.FetchEntryInterface;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.jna.Pointer;
import com.sun.jna.ptr.IntByReference;

public class FetchEntry implements FetchEntryCallback {
    private FetchEntryInterface fetch;

    public FetchEntry(FetchEntryInterface fetch) {
        this.fetch = fetch;
    }

    @Override
    public int apply(Pointer output, IntByReference outputSize, Pointer uidsPointer, int uidsLength)
        throws FfiException {
        //
        // Read `uidsPointer` until `uidsLength`
        //
        byte[] uids = new byte[uidsLength];
        uidsPointer.read(0, uids, 0, uidsLength);

        // For the JSON strings
        ObjectMapper mapper = new ObjectMapper();

        //
        // Deserialize vector Entry Table `uid`
        //
        String[] entryTableUids = null;
        try {
            entryTableUids = mapper.readValue(uids, String[].class);
        } catch (IOException e) {
            throw new FfiException("Failed deserializing uids in FetchEntry callback: " + e.toString());
        }

        //
        // Select uid and value in EntryTable
        //
        HashMap<String, String> uidsAndValues = this.fetch.fetch(entryTableUids);

        //
        // Set outputs
        //
        if (uidsAndValues.size() > 0) {
            String uidsAndValuesJson;
            try {
                uidsAndValuesJson = mapper.writeValueAsString(uidsAndValues);
            } catch (JsonProcessingException e) {
                throw new FfiException("Failed serializing FetchEntry results callback: " + e.toString());
            }

            byte[] uidsAndValuesBytes = uidsAndValuesJson.getBytes();
            output.write(0, uidsAndValuesBytes, 0, uidsAndValuesBytes.length);
            outputSize.setValue(uidsAndValuesBytes.length);
        } else {
            outputSize.setValue(0);
        }

        return 0;
    }

}
