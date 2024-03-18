package com.cosmian.rest.kmip;

import com.cosmian.rest.kmip.data_structures.ByteString;
import com.cosmian.rest.kmip.data_structures.KeyBlock;
import com.cosmian.rest.kmip.data_structures.KeyValue;
import com.cosmian.rest.kmip.data_structures.KeyWrappingData;
import com.cosmian.rest.kmip.data_structures.TransparentSymmetricKey;
import com.cosmian.utils.CloudproofException;

public class KmipUtils {

    /**
     * Extract the key bytes from a {@link KeyBlock} for those made of byte arrays
     *
     * @param keyBlock the {@link KeyBlock}
     * @return the bytes of the key
     * @throws CloudproofException if the {@link KeyBlock} is malformed the bytes cannot be found
     */
    public static byte[] bytesFromKeyBlock(KeyBlock keyBlock) throws CloudproofException {
        Object keyValueContent = keyBlock.getKeyValue();
        Object keyMaterialContent = ((KeyValue) keyValueContent).getKeyMaterial().get();
        byte[] bytes;
        if (keyMaterialContent instanceof byte[]) {
            bytes = (byte[]) keyMaterialContent;
        } else if (keyMaterialContent instanceof ByteString) {
            bytes = ((ByteString) keyMaterialContent).getByteString();
        } else if (keyMaterialContent instanceof TransparentSymmetricKey) {
            bytes = ((TransparentSymmetricKey) keyMaterialContent).getKey();
        } else {
            throw new CloudproofException(
                "KeyMaterial has type " + keyMaterialContent.getClass().getName() + " and is not made of byte[]");

        }
        return bytes;
    }

    /**
     * Extract the nonce/iv/counter bytes from a {@link KeyWrappingData} for those made of byte arrays
     */
    /**
     * Extract the nonce/iv/counter bytes from a {@link KeyWrappingData} for those made of byte arrays
     *
     * @param keyWrappingData the {@link KeyWrappingData}
     * @return the bytes of the Nonce
     * @throws CloudproofException if no Noce is available
     */
    public static byte[] nonceFromKeyWrappingData(KeyWrappingData keyWrappingData) throws CloudproofException {
        if (!keyWrappingData.getIv_counter_nonce().isPresent()) {
            throw new CloudproofException("No IV/counter/nonce found for key wrapping data");
        }

        return keyWrappingData.getIv_counter_nonce().get();
    }
}
