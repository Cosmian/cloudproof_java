package com.cosmian.rest.kmip;

import com.cosmian.CosmianException;
import com.cosmian.rest.kmip.data_structures.KeyBlock;
import com.cosmian.rest.kmip.data_structures.PlainTextKeyValue;
import com.cosmian.rest.kmip.data_structures.TransparentSymmetricKey;

public class KmipUtils {

    /**
     * Extract the key bytes from a {@link KeyBlock} for those made of byte arrays
     */
    public static byte[] bytesFromKeyBlock(KeyBlock keyBlock) throws CosmianException {
        Object keyValueContent = keyBlock.getKeyValue().get();
        if (keyValueContent instanceof byte[]) {
            throw new CosmianException("Cannot handle wrapped type for now");
        } else if (!(keyValueContent instanceof PlainTextKeyValue)) {
            throw new CosmianException("Unknown KeyValue type: " + keyValueContent.getClass().getName());
        }
        Object keyMaterialContent = ((PlainTextKeyValue) keyValueContent).getKeyMaterial().get();
        byte[] bytes;
        if (keyMaterialContent instanceof byte[]) {
            bytes = (byte[]) keyMaterialContent;
        } else if (keyMaterialContent instanceof TransparentSymmetricKey) {
            bytes = ((TransparentSymmetricKey) keyMaterialContent).getKey();
        } else {
            throw new CosmianException(
                    "KeyMaterial has type " + keyMaterialContent.getClass().getName() + " and is not made of byte[]");

        }
        return bytes;
    }

}
