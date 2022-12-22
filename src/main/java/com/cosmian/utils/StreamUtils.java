package com.cosmian.utils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class StreamUtils {

    public static byte[] readFully(InputStream is) throws IOException {
        byte[] buffer = new byte[4096];

        int read;
        try (ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
            while ((read = is.read(buffer)) != 0) {
                bos.write(buffer, 0, read);
            }
            return bos.toByteArray();
        }
    }
}
