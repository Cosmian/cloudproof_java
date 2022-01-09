package com.cosmian;

/**
 * An Exception thrown by the cosmian API calls
 */
public class CosmianException extends Exception {
    public CosmianException(String message, Throwable t) {
        super(message, t);
    }

    public CosmianException(String message) {
        super(message);
    }
}
