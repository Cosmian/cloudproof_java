package com.cosmian.jna.findex.Callbacks;

import java.io.IOException;
import java.nio.charset.Charset;

import com.cosmian.jna.FfiException;
import com.cosmian.jna.findex.FfiWrapper.FetchChainCallback;
import com.cosmian.jna.findex.FfiWrapper.FetchChainInterface;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.jna.Pointer;
import com.sun.jna.ptr.IntByReference;

public class FetchChain implements FetchChainCallback {

    private FetchChainInterface fetch;

    public FetchChain(FetchChainInterface fetch) {
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
        // Deserialize vector Chain Table `uid`
        //
        String[] chainTableUids = null;
        try {
            chainTableUids = mapper.readValue(uids, String[].class);
        } catch (IOException e) {
            throw new FfiException("Failed deserializing uids in FetchChain callback: " + e.toString());
        }

        //
        // Select uid and value in EntryTable
        //
        String[] values = this.fetch.fetch(chainTableUids);

        //
        // Set outputs
        //
        if (values.length > 0) {
            String valuesJson;
            try {
                valuesJson = mapper.writeValueAsString(values);
            } catch (JsonProcessingException e) {
                throw new FfiException("Failed serializing FetchChain results callback: " + e.toString());
            }

            byte[] valuesBytes = valuesJson.getBytes(Charset.defaultCharset());
            output.write(0, valuesBytes, 0, valuesBytes.length);
            outputSize.setValue(valuesBytes.length);
        } else {
            outputSize.setValue(0);
        }

        return 0;
    }

}
