package com.cosmian.rest.abe.data;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

import com.cosmian.utils.CloudproofException;
import com.cosmian.utils.Leb128;

public class DataToEncrypt {

    private final String encryptionPolicy;

    private final byte[] plaintext;

    private final Optional<byte[]> headerMetaData;

    public DataToEncrypt(String encryptionPolicy, byte[] plaintext, Optional<byte[]> headerMetaData) {
        this.encryptionPolicy = encryptionPolicy;
        this.plaintext = plaintext;
        this.headerMetaData = headerMetaData;
    }

    public String getEncryptionPolicy() {
        return this.encryptionPolicy;
    }

    public byte[] getPlaintext() {
        return this.plaintext;
    }

    public Optional<byte[]> getHeaderMetaData() {
        return this.headerMetaData;
    }

    public byte[] toBytes() throws CloudproofException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try {
            Leb128.writeArray(bos, this.encryptionPolicy.getBytes(StandardCharsets.UTF_8));
            if (this.headerMetaData.isPresent()) {
                Leb128.writeArray(bos, this.headerMetaData.get());
            } else {
                Leb128.writeU64(bos, 0);
            }
            bos.write(this.plaintext);
        } catch (IOException e) {
            throw new CloudproofException("Failed serializing the data to encrypt to bytes: " + e.getMessage(), e);
        }
        return bos.toByteArray();
    }
}
