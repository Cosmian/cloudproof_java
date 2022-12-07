package com.cosmian.jna.findex.serde;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;

/**
 * Input stream supporting single-byte peek operations.
 */
public class PeekInputStream extends InputStream {

    /** underlying stream */
    private final InputStream in;

    /** peeked byte */
    private int peekb = -1;

    /** total bytes read from the stream */
    private long totalBytesRead = 0;

    /**
     * Creates new PeekInputStream on top of given underlying stream.
     */
    PeekInputStream(InputStream in) {
        this.in = in;
    }

    /**
     * Peeks at next byte value in stream. Similar to read(), except that it does not consume the read value.
     */
    int peek() throws IOException {
        if (peekb >= 0) {
            return peekb;
        }
        peekb = in.read();
        totalBytesRead += peekb >= 0 ? 1 : 0;
        return peekb;
    }

    public int read() throws IOException {
        if (peekb >= 0) {
            int v = peekb;
            peekb = -1;
            return v;
        } else {
            int nbytes = in.read();
            totalBytesRead += nbytes >= 0 ? 1 : 0;
            return nbytes;
        }
    }

    public int read(byte[] b,
                    int off,
                    int len)
        throws IOException {
        int nbytes;
        if (len == 0) {
            return 0;
        } else if (peekb < 0) {
            nbytes = in.read(b, off, len);
            totalBytesRead += nbytes >= 0 ? nbytes : 0;
            return nbytes;
        } else {
            b[off++] = (byte) peekb;
            len--;
            peekb = -1;
            nbytes = in.read(b, off, len);
            totalBytesRead += nbytes >= 0 ? nbytes : 0;
            return (nbytes >= 0) ? (nbytes + 1) : 1;
        }
    }

    void readFully(byte[] b,
                   int off,
                   int len)
        throws IOException {
        int n = 0;
        while (n < len) {
            int count = read(b, off + n, len - n);
            if (count < 0) {
                throw new EOFException();
            }
            n += count;
        }
    }

    public long skip(long n) throws IOException {
        if (n <= 0) {
            return 0;
        }
        int skipped = 0;
        if (peekb >= 0) {
            peekb = -1;
            skipped++;
            n--;
        }
        n = skipped + in.skip(n);
        totalBytesRead += n;
        return n;
    }

    public int available() throws IOException {
        return in.available() + ((peekb >= 0) ? 1 : 0);
    }

    public void close() throws IOException {
        in.close();
    }

    public long getBytesRead() {
        return totalBytesRead;
    }
}
