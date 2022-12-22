package com.cosmian.utils;

/**
 * An Exception thrown by the REST Client
 */
public class RestException extends Exception {
    public RestException(String message, Throwable t) {
        super(message, t);
    }

    public RestException(String message) {
        super(message);
    }
}
