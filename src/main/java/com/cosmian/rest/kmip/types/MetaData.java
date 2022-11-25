package com.cosmian.rest.kmip.types;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;

import com.cosmian.CloudproofException;

/**
 * The symmetric encryption optional meta data: - the uid uniquely identifies the resource and is part of the AEAD of
 * the encryption scheme - the additional data is not part of the AEAD The meta data in symmetrically encrypted in the
 * hybrid header is provided
 */
public class MetaData {

    private final Optional<byte[]> uid;

    private final Optional<byte[]> additionalData;

    /**
     * Parse meta data from bytes
     * 
     * @param bytes the meta data obtained using the {@link #toBytes()} method
     * @return the parsed {@link MetaData}
     * @throws CloudproofException if the data cannot be parsed
     */
    public static MetaData fromBytes(byte[] bytes) throws CloudproofException {
        if (bytes.length == 0) {
            return new MetaData();
        }
        if (bytes.length < 4) {
            throw new CloudproofException("Invalid meta data length");
        }
        // Parse the metadata by first recovering the header length
        int headerSize_ = ByteBuffer.wrap(bytes).order(ByteOrder.BIG_ENDIAN).getInt(0);
        // Then recover the uid and additional data
        byte[] uid = Arrays.copyOfRange(bytes, 4, 4 + headerSize_);
        byte[] additionalData = Arrays.copyOfRange(bytes, 4 + headerSize_, bytes.length);
        return new MetaData(uid.length == 0 ? Optional.empty() : Optional.of(uid),
            additionalData.length == 0 ? Optional.empty() : Optional.of(additionalData));
    }

    public MetaData() {
        this.uid = Optional.empty();
        this.additionalData = Optional.empty();
    }

    public MetaData(Optional<byte[]> uid, Optional<byte[]> additionalData) {
        this.uid = uid;
        this.additionalData = additionalData;
    }

    public Optional<byte[]> getUid() {
        return this.uid;
    }

    public Optional<byte[]> getAdditionalData() {
        return this.additionalData;
    }

    /**
     * Converts the meta data to a byte array which can parsed back using the {@link #fromBytes(byte[])} method The
     * first 4 bytes is the u32 size of the uid in big endian format.
     * 
     * @return the meta data as a byte array
     * @throws CloudproofException if the {@link MetaData} cannot be serialized
     */
    public byte[] toBytes() throws CloudproofException {
        // The length of the uid is pre-pended.
        ByteBuffer uidSize = ByteBuffer.allocate(4).order(ByteOrder.BIG_ENDIAN)
            .putInt(this.getUid().isPresent() ? this.uid.get().length : 0);
        // Write the message
        ByteArrayOutputStream bao = new ByteArrayOutputStream();
        try {
            bao.write(uidSize.array());
            if (this.uid.isPresent() && this.uid.get().length > 0) {
                bao.write(this.uid.get());
            }
            if (this.additionalData.isPresent() && this.additionalData.get().length > 0) {
                bao.write(this.additionalData.get());
            }
            bao.flush();
        } catch (IOException e) {
            throw new CloudproofException("failed serializing the meta data: " + e.getMessage(), e);
        }
        return bao.toByteArray();
    }

    @Override
    public boolean equals(Object o) {
        if (o == this)
            return true;
        if (!(o instanceof MetaData)) {
            return false;
        }
        MetaData metaData = (MetaData) o;

        boolean uidEquals =
            (uid.isPresent() && metaData.uid.isPresent() && Arrays.equals(uid.get(), metaData.uid.get()))
                || (!uid.isPresent() && !metaData.uid.isPresent());
        boolean additionalDataEquals = (additionalData.isPresent() && metaData.additionalData.isPresent()
            && Arrays.equals(additionalData.get(), metaData.additionalData.get()))
            || (!additionalData.isPresent() && !metaData.additionalData.isPresent());

        return uidEquals && additionalDataEquals;
    }

    @Override
    public int hashCode() {
        return Objects.hash(uid, additionalData);
    }

    @Override
    public String toString() {
        return "{" + " uid='" + getUid() + "'" + ", additionalData='" + getAdditionalData() + "'" + "}";
    }

}
