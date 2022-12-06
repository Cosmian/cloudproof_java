package com.cosmian.jna.findex.Callbacks;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import com.cosmian.CloudproofException;
import com.cosmian.Leb128;
import com.cosmian.jna.findex.FindexWrapper.EntryTableValue;
import com.cosmian.jna.findex.FindexWrapper.UpsertEntryCallback;
import com.cosmian.jna.findex.FindexWrapper.UpsertEntryInterface;
import com.cosmian.jna.findex.Uid;
import com.sun.jna.Pointer;
import com.sun.jna.ptr.IntByReference;

public class UpsertEntry implements UpsertEntryCallback {

    private UpsertEntryInterface upsert;

    public UpsertEntry(UpsertEntryInterface upsert) {
        this.upsert = upsert;
    }

    @Override
    public int apply(Pointer entries,
                     int entriesLength,
                     Pointer outputs,
                     IntByReference outputsLength)
        throws CloudproofException {
        //
        // Read `entries` until `itemsLength`
        //
        byte[] entriesBytes = new byte[entriesLength];
        entries.read(0, entriesBytes, 0, entriesLength);

        HashMap<Uid, EntryTableValue> map;
        try (ByteArrayInputStream bis = new ByteArrayInputStream(entriesBytes)) {
            int numEntries = (int) Leb128.readU64(bis);
            map = new HashMap<>(numEntries);
            for (int i = 0; i < numEntries; i++) {
                Uid uid = new Uid(Leb128.readByteArray(bis));
                byte[] previousValue = Leb128.readByteArray(bis);
                byte[] newValue = Leb128.readByteArray(bis);
                map.put(uid, new EntryTableValue(previousValue, newValue));
            }
        } catch (IOException e) {
            String err = "UpsertEntry: failed deserializing the inputs: " + e.getMessage();
            throw new CloudproofException(err, e);
        }

        Map<Uid, byte[]> failedEntries = upsert.upsert(map);

        // serialize the failedEntries
        try (ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
            Leb128.writeU64(bos, failedEntries.size());
            for (Entry<Uid, byte[]> entry : failedEntries.entrySet()) {
                Leb128.writeArray(bos, entry.getKey().getBytes());
                Leb128.writeArray(bos, entry.getValue());
            }
        } catch (IOException e) {
            String err = "UpsertEntry: failed serializing the outputs: " + e.getMessage();
            throw new CloudproofException(err, e);
        }

        return 0;
    }

}
