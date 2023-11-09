package com.cosmian.jna.findex;

import java.util.ArrayList;
import java.util.List;

import com.cosmian.utils.CloudproofException;

public class FindexCallbackException {

    private final static int CALLBACK_ERROR_CODE_WHEN_THROWING = 42;
    public static int purgeExceptionsWhenCountIsGreaterThan = 50;
    public static int purgeExceptionsAfterMillis = 1000 * 60 * 5; // 5 minutes

    public static List<FindexCallbackException> exceptions = new ArrayList<>();

    private long timestamp = System.currentTimeMillis();
    private CloudproofException e;

    public FindexCallbackException(CloudproofException e) {
        this.e = e;
    }

    public static int record(CloudproofException e) {
        if (exceptions.size() > purgeExceptionsWhenCountIsGreaterThan) {
            exceptions.removeIf(exception -> exception.timestamp < System.currentTimeMillis() - purgeExceptionsAfterMillis);
        }

        exceptions.add(new FindexCallbackException(e));

        return CALLBACK_ERROR_CODE_WHEN_THROWING;
    }

    public static void rethrowOnErrorCode(int errorCode, long start, long end) throws CloudproofException {
	    if (errorCode == CALLBACK_ERROR_CODE_WHEN_THROWING) {
		    for (FindexCallbackException e : exceptions) {
			    if (start <= e.timestamp  && e.timestamp <= end) {
				    throw e.e;
			    }
		    }

		    throw new CloudproofException("Findex returned an error code " + errorCode
				    + " reserved for exceptions but no exception was recorded during the callbacks.");
	    }
    }
}
