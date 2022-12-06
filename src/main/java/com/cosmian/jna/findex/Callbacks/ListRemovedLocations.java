package com.cosmian.jna.findex.Callbacks;

import java.util.List;

import com.cosmian.CloudproofException;
import com.cosmian.jna.findex.FindexWrapper.ListRemovedLocationsCallback;
import com.cosmian.jna.findex.FindexWrapper.ListRemovedLocationsInterface;
import com.cosmian.jna.findex.Leb128Serializer;
import com.cosmian.jna.findex.Location;
import com.sun.jna.Pointer;
import com.sun.jna.ptr.IntByReference;

public class ListRemovedLocations implements ListRemovedLocationsCallback {

    private ListRemovedLocationsInterface list;

    public ListRemovedLocations(ListRemovedLocationsInterface list) {
        this.list = list;
    }

    @Override
    public int apply(Pointer output,
                     IntByReference outputSize,
                     Pointer items,
                     int itemsLength)
        throws CloudproofException {
        //
        // Read `items` until `itemsLength`
        //
        byte[] itemsBytes = new byte[itemsLength];
        items.read(0, itemsBytes, 0, itemsLength);

        List<Location> locations = Leb128Serializer.deserializeList(itemsBytes);

        List<Location> removedLocations = this.list.list(locations);

        if (removedLocations.size() > 0) {

            byte[] bytes = Leb128Serializer.serializeList(removedLocations);

            output.write(0, bytes, 0, bytes.length);
            outputSize.setValue(bytes.length);
        } else {
            outputSize.setValue(0);
        }

        return 0;
    }

}
