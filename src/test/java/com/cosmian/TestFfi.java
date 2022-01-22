package com.cosmian;

import static org.junit.jupiter.api.Assertions.assertEquals;

// import java.util.logging.Logger;

import com.cosmian.rest.jna.Ffi;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class TestFfi {

	// private static final Logger logger =
	// Logger.getLogger(TestFfi.class.getName());
	private static final Ffi FFI = new Ffi();

	@BeforeAll
	public static void before_all() {
		TestUtils.initLogging();
	}

	@Test
	public void testSquare() throws Exception {
		assertEquals(256, FFI.square(16));
	}

	@Test
	public void testError() throws Exception {
		Ffi ffi = new Ffi();
		assertEquals("", ffi.get_last_error());
		String error = "An Error éà";
		ffi.set_error(error);
		assertEquals("FFI error: " + error, ffi.get_last_error());
		String base = "0123456789";
		String s = "";
		for (int i = 0; i < 110; i++) {
			s += base;
		}
		assertEquals(1100, s.length());
		ffi.set_error(s);
		String err = ffi.get_last_error(1023);
		assertEquals(1023, err.length());
	}
}
