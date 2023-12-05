package com.cosmian.jna.findex;

import java.util.List;

import com.cosmian.jna.findex.ffi.FindexNativeWrapper.DataFilterCallback;
import com.cosmian.jna.findex.serde.Leb128Reader;
import com.cosmian.jna.findex.serde.Leb128Writer;
import com.cosmian.jna.findex.structs.Location;
import com.cosmian.utils.CloudproofException;
import com.sun.jna.Pointer;
import com.sun.jna.ptr.IntByReference;

public interface DataFilter extends DataFilterCallback {

    /**
     * Filter the given locations.
     * <p>
     * This operation is used during compact operations to detect obsolete indexed values.
     *
     * @param locations a {@link List} of {@link Location}
     * @return the {@link List} of {@link Location} to keep in the index.
     * @throws CloudproofException if anything goes wrong
     */
    default List<Location> filter(List<Location> locations) throws CloudproofException {
        return locations;
    }

    @Override
    default int callback(Pointer output,
                         IntByReference outputSize,
                         Pointer items,
                         int itemsLength) {
        try {
            //
            // Read `items` until `itemsLength`
            //
            byte[] itemsBytes = new byte[itemsLength];
            items.read(0, itemsBytes, 0, itemsLength);

            // Locations values are sent, not the indexed value, hence the use of a BytesVector
            List<Location> locations = Leb128Reader.deserializeCollection(Location.class, itemsBytes);

            List<Location> remainingLocations = filter(locations);
            byte[] bytes = Leb128Writer.serializeCollection(remainingLocations);
            output.write(0, bytes, 0, bytes.length);
            if (remainingLocations.size() > 0) {
                outputSize.setValue(bytes.length);
            } else {
                outputSize.setValue(0);
            }
            return 0;
        } catch (CloudproofException e) {
            return FindexCallbackException.record(e);
        }
    }
}
