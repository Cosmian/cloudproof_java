package com.cosmian;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.cosmian.rest.abe.VendorAttributes;
import com.cosmian.rest.abe.acccess_policy.AccessPolicy;
import com.cosmian.rest.abe.acccess_policy.And;
import com.cosmian.rest.abe.acccess_policy.Attr;
import com.cosmian.rest.abe.acccess_policy.Or;
import com.cosmian.rest.abe.policy.Policy;
import com.cosmian.rest.jna.Ffi;
import com.cosmian.rest.jna.abe.EncryptedHeader;
import com.cosmian.rest.kmip.KmipUtils;
import com.cosmian.rest.kmip.objects.PublicKey;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import com.sun.jna.Pointer;

public class TestFfi {

	// private static final Logger logger =
	// Logger.getLogger(TestFfi.class.getName());
	private static final Ffi FFI = new Ffi();

	private Policy policy() throws CosmianException {
		return new Policy(20)
				.addAxis("Security Level", new String[] { "Protected", "Confidential", "Top Secret" }, true)
				.addAxis("Department", new String[] { "FIN", "MKG", "HR" }, false);
	}

	private AccessPolicy accessPolicyProtected() throws CosmianException {
		return new And(new Or(new Attr("Department", "FIN"), new Attr("Department", "MKG")),
				new Attr("Security Level", "Protected"));
	}

	private AccessPolicy accessPolicyConfidential() throws CosmianException {
		return new And(new Attr("Department", "FIN"), new Attr("Security Level", "Confidential"));
	}

	private Attr[] finConfidentialAttributes() throws CosmianException {
		return new Attr[] { new Attr("Department", "FIN"), new Attr("Security Level", "Confidential") };
	}

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

	@Test
	public void testEncryptedHeader() throws Exception {
		// Policy policy = policy();

		String publicKeyJson = Resources.load_resource("ffi/public_key.json");
		PublicKey publicKey = PublicKey.fromJson(publicKeyJson);
		Policy policy = VendorAttributes.policyFromVendorAttributes(publicKey.attributes());
		System.out.println(policy.toString());

		byte[] publicKeyBytes = KmipUtils.bytesFromKeyBlock(publicKey.getKeyBlock());
		Attr[] attributes = finConfidentialAttributes();
		byte[] uid = new byte[] { 1, 2, 3, 4, 5, 6, 7, 8, 9 };
		byte[] additionalData = new byte[] { 10, 11, 12, 13, 14 };
		EncryptedHeader header = FFI.encryptHeader(policy, publicKeyBytes, attributes, uid, additionalData);

		System.out.println(header.getSymmetricKey().length);
		System.out.println(header.getHeaderBytes().length);

		byte[] data = new byte[] { 1, 2, 3, 4, 5, 6, 7, 8, 9 };
		byte[] encrypted = FFI.encryptBlock(header.getSymmetricKey(), uid, 0, data);
		System.out.println(data.length);
		System.out.println(FFI.symmetricEncryptionOverhead());
		System.out.println(encrypted.length);

	}

	@Test
	@Disabled
	public void testHybridCipher() throws Exception {
		Pointer cipherPointer = Pointer.NULL;
		try {
			Policy policy = policy();

			String publicKeyJson = Resources.load_resource("ffi/public_key.json");
			PublicKey publicKey = PublicKey.fromJson(publicKeyJson);

			byte[] publicKeyBytes = KmipUtils.bytesFromKeyBlock(publicKey.getKeyBlock());
			Attr[] attributes = finConfidentialAttributes();
			byte[] uid = new byte[] { 1, 2, 3, 4, 5, 6, 7, 8, 9 };
			byte[] additionalData = new byte[] { 10, 11, 12, 13, 14 };
			cipherPointer = FFI.newHybridCipher(policy, publicKeyBytes, attributes, uid,
					additionalData);
		} finally {
			if (cipherPointer != Pointer.NULL) {
				FFI.destroyHybridCipher(cipherPointer);
				cipherPointer = Pointer.NULL;
			}
		}
	}
}
