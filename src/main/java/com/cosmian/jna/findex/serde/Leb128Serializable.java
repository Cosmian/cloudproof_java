package com.cosmian.jna.findex.serde;

import java.io.InputStream;
import java.io.OutputStream;

import com.cosmian.CloudproofException;

/**
 * Serializable objects across the native interface must implement this interface
 */
public interface Leb128Serializable {

    public void readObject(InputStream is) throws CloudproofException;

    public void writeObject(OutputStream os) throws CloudproofException;
}
