package com.cosmian.rest;

import java.util.Optional;
import java.util.logging.Logger;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;

import com.cosmian.CosmianException;
import com.cosmian.RestClient;
import com.cosmian.rest.gpsw.Abe;
import com.cosmian.rest.kmip.Kmip;

public class Cosmian {

    private static final Logger logger = Logger.getLogger(Cosmian.class.getName());

    private final RestClient rest_client;

    /**
     * Instantiate a new Cosmian Server REST Client
     *
     * @param server_url the REST Server URL e.g. http://localhost:9000
     * @param api_key the Cosmian API KEY
     * @param connection_timeout Sets a specified timeout value, in milliseconds, to be used when opening a
     *            communications link to the resource referenced by this URLConnection.
     * @param read_timeout Sets the read timeout to a specified timeout, in milliseconds.
     */
    Cosmian(String server_url, Optional<String> api_key, int connection_timeout, int read_timeout) {
        this.rest_client = new RestClient(server_url, api_key, connection_timeout, read_timeout);
    }

    /**
     * Instantiate a new Cosmian Server REST Client with DEFAULT_CONNECT_TIMEOUT and DEFAULT_READ_TIMEOUT
     *
     * @param server_url the REST Server URL e.g. http://localhost:9000
     * @param api_key the Cosmian API KEY
     * @see RestClient
     */
    public Cosmian(String server_url, Optional<String> api_key) {
        this.rest_client = new RestClient(server_url, api_key);
    }

    /**
     * Access to the REST client
     *
     * @return the {@link RestClient}
     */
    public RestClient rest() {
        return this.rest_client;
    }

    /**
     * Access to the KMIP endpoints
     *
     * @return a Kmip instance exposing the endpoints
     */
    public Kmip kmip() {
        return new Kmip(this.rest_client);
    }

    /**
     * Access to the ABE (Attribute Based Encryption) endpoints
     *
     * @return an Abe instance exposing the endpoints
     */
    public Abe abe() {
        return new Abe(this.rest_client);
    }

    /**
     * Hex Encode an array of bytes
     *
     * @param bytes the bytes to encode
     * @return the hex encoded String
     */
    public static String hex_encode(byte[] bytes) {
        return Hex.encodeHexString(bytes);
    }

    /**
     * Decode an hex encoded String to bytes
     *
     * @param hex_encoded_string the hex encoded String
     * @return the decoded bytes
     * @throws CosmianException if the hex String is invalid
     */
    public static byte[] hex_decode(String hex_encoded_string) throws CosmianException {
        try {
            // conversion to char array to make the library compatible with older versions of commons-codec
            // such as the ones found in spark-2.7
            return Hex.decodeHex(hex_encoded_string.toCharArray());
        } catch (DecoderException e) {
            String err = "Failed decoding the hex encoded String: " + e.getMessage();
            logger.severe(err);
            throw new CosmianException(err);
        }
    }

    /**
     * Concat 2 byte-arrays
     *
     * @param a first byte array
     * @param b second byte array
     * @return the merged byte array
     */
    public static byte[] concat(byte[] a, byte[] b) {
        byte[] result = new byte[a.length + b.length];
        System.arraycopy(a, 0, result, 0, a.length);
        System.arraycopy(b, 0, result, a.length, b.length);
        return result;
    }
}
