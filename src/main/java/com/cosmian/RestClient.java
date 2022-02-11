package com.cosmian;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.logging.Logger;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;

public class RestClient {

    private static final Logger logger = Logger.getLogger(RestClient.class.getName());
    public static final int DEFAULT_CONNECT_TIMEOUT = 45000;
    public static final int DEFAULT_READ_TIMEOUT = 45000;

    private final String server_url;
    private final String api_key;
    private final int connection_timeout;
    private final int read_timeout;
    // cache the socket factory for kee-alive
    private final SSLSocketFactory ssl_socket_factory;

    /**
     * Instantiate a new REST Client
     *
     * @param server_url
     *                           the REST Server URL e.g. http://localhost:9000
     * @param connection_timeout
     *                           Sets a specified timeout value, in milliseconds, to
     *                           be used when opening a communications link to the
     *                           resource referenced by this URLConnection.
     * @param read_timeout
     *                           Sets the read timeout to a specified timeout, in
     *                           milliseconds.
     * @param api_key            The API Key to use to authenticate
     */
    public RestClient(String server_url, String api_key, int connection_timeout, int read_timeout) {
        if (server_url.endsWith("/")) {
            this.server_url = server_url.substring(0, server_url.length() - 1);
        } else {
            this.server_url = server_url;
        }
        this.api_key = api_key;
        this.connection_timeout = connection_timeout;
        this.read_timeout = read_timeout;
        SSLContext ssl_context;
        try {
            ssl_context = SSLContext.getInstance("TLS");
            ssl_context.init(null, null, null);
        } catch (NoSuchAlgorithmException e) {
            String err = "TLS is not available ! " + e.getMessage();
            logger.severe(err);
            throw new RuntimeException(err, e);
        } catch (KeyManagementException e) {
            String err = "Failed to initialize the SSL context ! " + e.getMessage();
            logger.severe(err);
            throw new RuntimeException(err, e);
        }
        this.ssl_socket_factory = ssl_context.getSocketFactory();
    }

    /**
     * Instantiate a new REST Client with DEFAULT_CONNECT_TIMEOUT and
     * DEFAULT_READ_TIMEOUT
     *
     * @param server_url the REST Server URL e.g. http://localhost:9000
     * @param api_key    API Key to use to authenticate
     */
    public RestClient(String server_url, String api_key) {
        this(server_url, api_key, DEFAULT_CONNECT_TIMEOUT, DEFAULT_READ_TIMEOUT);
    }

    private HttpURLConnection get_connection(String path) throws MalformedURLException, IOException {
        HttpURLConnection cnx = (HttpURLConnection) new URL(this.server_url + path).openConnection();
        if (cnx instanceof HttpsURLConnection) {
            ((HttpsURLConnection)cnx).setSSLSocketFactory(this.ssl_socket_factory);
        }
        cnx.setConnectTimeout(this.connection_timeout);
        cnx.setReadTimeout(this.read_timeout);
        cnx.setRequestProperty("Authorization", this.api_key);
        return cnx;
    }

    /**
     * Perform a GET request returning JSON
     *
     * @param path
     *             the REST service path and query parameters answering the GET
     *             request
     * @return the JSON response
     * @throws RestException on any REST error
     */
    public String json_get(String path) throws RestException {
        HttpURLConnection cnx = null;
        try {
            cnx = get_connection(path);
            cnx.setRequestMethod("GET");
            cnx.connect();
            return read_json_response(cnx);
        } catch (Throwable t) {
            throw handle_throwable("GET", t, cnx, this.server_url + path);
        }
    }

    /**
     * Perform a JSON POST request returning JSON
     *
     * @param path
     *                the REST service path and query parameters answering the POST
     *                request
     * @param payload
     *                the JSON payload passed as the body of the POST request
     * @return the JSON response
     * @throws RestException on any REST error
     */
    public String json_post(String path, String payload) throws RestException {
        HttpURLConnection cnx = null;
        try {
            cnx = get_connection(path);
            cnx.setRequestMethod("POST");
            cnx.setRequestProperty("Content-Type", "application/json; charset=utf-8");
            cnx.setDoOutput(true);
            cnx.connect();
            try (OutputStream os = cnx.getOutputStream()) {
                os.write(payload.getBytes(StandardCharsets.UTF_8));
                os.flush();
            }
            return read_json_response(cnx);
        } catch (Throwable t) {
            throw handle_throwable("POST", t, cnx, this.server_url + path);
        }

    }

    private static RestException handle_throwable(String method, Throwable t, HttpURLConnection cnx, String url) {
        if (t instanceof MalformedURLException) {
            return new RestException(method + " failed. Invalid URL: " + url, t);
        }
        if (!(t instanceof IOException)) {
            return new RestException(method + " failed: " + t.getClass() + ": " + t.getMessage(), t);
        }
        if (cnx == null) {
            return new RestException("Connection failed to: " + url, t);
        }
        try {
            // see
            // https://docs.oracle.com/javase/8/docs/technotes/guides/net/http-keepalive.html
            int code = cnx.getResponseCode();
            byte[] bytes;
            try (InputStream es = cnx.getErrorStream()) {
                bytes = read_all_bytes(es);
            }
            String body;
            try {
                body = new String(bytes, StandardCharsets.UTF_8);
            } catch (Exception _e) {
                body = "N/A";
            }
            return new RestException(method + " failed: " + code + ": " + body, t);
        } catch (IOException ex) {
            return new RestException(method + " failed. Unable to read response: " + t.getMessage(), ex);
        }
    }

    private static byte[] read_all_bytes(InputStream inputStream) throws IOException {
        final int BUFFER_LENGTH = 4096;
        byte[] buffer = new byte[BUFFER_LENGTH];
        int readLen;
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        while ((readLen = inputStream.read(buffer, 0, BUFFER_LENGTH)) != -1)
            outputStream.write(buffer, 0, readLen);
        return outputStream.toByteArray();
    }

    private static String read_json_response(URLConnection url_connection) throws IOException {
        byte[] bytes;
        try (InputStream is = url_connection.getInputStream()) {
            bytes = read_all_bytes(is);
        }
        return new String(bytes, StandardCharsets.UTF_8);
    }

}
