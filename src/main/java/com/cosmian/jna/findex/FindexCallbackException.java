package com.cosmian.jna.findex;

import java.util.ArrayList;
import java.util.List;

import com.cosmian.utils.CloudproofException;

public class FindexCallbackException {

    private final static int CALLBACK_ERROR_CODE_WHEN_THROWING = 42;

    public static List<FindexCallbackException> exceptions = new ArrayList<>();

    private long timestamp = System.currentTimeMillis();
    private CloudproofException e;

    public FindexCallbackException(CloudproofException e) {
        this.e = e;
    }

    public static int record(CloudproofException e) {
        exceptions.add(new FindexCallbackException(e));

        return CALLBACK_ERROR_CODE_WHEN_THROWING;
    }

    public static void rethrowOnErrorCode(int errorCode, long start, long end) throws CloudproofException {
        if (errorCode != CALLBACK_ERROR_CODE_WHEN_THROWING) {
            return;
        }

        for (FindexCallbackException e : exceptions) {
            if (e.timestamp > start && e.timestamp < end) {
                throw e.e;
            }
        }
    }
}
