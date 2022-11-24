package com.cosmian.jna.findex.Callbacks;

import java.util.List;
import java.util.stream.Collectors;

import com.cosmian.jna.CloudproofException;
import com.cosmian.jna.findex.FfiWrapper.ListRemovedLocationsCallback;
import com.cosmian.jna.findex.FfiWrapper.ListRemovedLocationsInterface;
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
    public int apply(Pointer output, IntByReference outputSize, Pointer items, int itemsLength)
            throws CloudproofException {
        //
        // Read `items` until `itemsLength`
        //
        byte[] itemsBytes = new byte[itemsLength];
        items.read(0, itemsBytes, 0, itemsLength);

        List<byte[]> locationsBytes = Leb128Serializer.deserializeList(itemsBytes);
        List<Location> locations = locationsBytes.stream().map((byte[] bytes) -> new Location(bytes))
                .collect(Collectors.toList());

        List<Location> removedLocations = this.list.list(locations);

        if (removedLocations.size() > 0) {
            List<byte[]> removedLocationsAsBytes = removedLocations.stream()
                    .map((Location location) -> location.getBytes()).collect(Collectors.toList());

            byte[] bytes = Leb128Serializer.serializeList(removedLocationsAsBytes);

            output.write(0, bytes, 0, bytes.length);
            outputSize.setValue(bytes.length);
        } else {
            outputSize.setValue(0);
        }

        return 0;
    }

}
