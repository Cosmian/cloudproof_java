package com.cosmian.jna.findex.ffi;

import java.util.Map;
import java.util.Set;

import com.cosmian.jna.findex.serde.Leb128Serializable;
import com.cosmian.jna.findex.serde.Leb128Writer;
import com.cosmian.utils.CloudproofException;
import com.sun.jna.Pointer;
import com.sun.jna.ptr.IntByReference;

public class FFiUtils {

    /**
     * Serialize a map to a memory location specified by the Pointer; set its actual size in the pointed int.
     * 
     * @param <K> the map key type. Must be {@link Leb128Serializable}
     * @param <V> the map value type. Must be {@link Leb128Serializable}
     * @param map the map to serialize and export
     * @param output the output Pointer
     * @param outputSize the output byte size
     * @return 0 on success, 1 if the pre-allocated memory is too small. The outputSized contains the required size to
     *         hold the map.
     * @throws CloudproofException if the pointer cannot be constructed
     */
    public static <K extends Leb128Serializable, V extends Leb128Serializable> int mapToOutputPointer(Map<K, V> map,
                                                                                                      Pointer output,
                                                                                                      IntByReference outputSize)
        throws CloudproofException {
        byte[] uidsAndValuesBytes = Leb128Writer.serializeMap(map);
        if (outputSize.getValue() < uidsAndValuesBytes.length) {
            outputSize.setValue(uidsAndValuesBytes.length);
            return 1;
        }
        outputSize.setValue(uidsAndValuesBytes.length);
        output.write(0, uidsAndValuesBytes, 0, uidsAndValuesBytes.length);
        return 0;
    }

    public static <V extends Leb128Serializable> int setToOutputPointer(Set<V> set,
                                                                        Pointer output,
                                                                        IntByReference outputSize)
        throws CloudproofException {
        byte[] uidsAndValuesBytes = Leb128Writer.serializeCollection(set);
        if (outputSize.getValue() < uidsAndValuesBytes.length) {
            outputSize.setValue(uidsAndValuesBytes.length);
            return 1;
        }
        outputSize.setValue(uidsAndValuesBytes.length);
        output.write(0, uidsAndValuesBytes, 0, uidsAndValuesBytes.length);
        return 0;
    }
}
