package com.cosmian;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;

import com.cosmian.jna.Ffi;
import com.cosmian.jna.abe.DecryptedHeader;
import com.cosmian.jna.abe.EncryptedHeader;
import com.cosmian.rest.abe.acccess_policy.Attr;
import com.cosmian.rest.abe.policy.Policy;
import com.cosmian.rest.kmip.KmipUtils;
import com.cosmian.rest.kmip.objects.PrivateKey;
import com.cosmian.rest.kmip.objects.PublicKey;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class TestLocalABE_AES {

	private static final Ffi FFI = new Ffi();

	@BeforeAll
	public static void before_all() {
		TestUtils.initLogging();
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
	public void testHybridCrypto() throws Exception {

		System.out.println("");
		System.out.println("---------------------------------------");
		System.out.println(" Hybrid Crypto Test");
		System.out.println("---------------------------------------");
		System.out.println("");

		String publicKeyJson = Resources.load_resource("ffi/public_master_key.json");
		PublicKey publicKey = PublicKey.fromJson(publicKeyJson);
		byte[] publicKeyBytes = KmipUtils.bytesFromKeyBlock(publicKey.getKeyBlock());

		Policy policy = Policy.fromVendorAttributes(publicKey.attributes());

		Attr[] attributes = new Attr[] { new Attr("Department", "FIN"), new Attr("Security Level", "Confidential") };
		byte[] uid = new byte[] { 1, 2, 3, 4, 5, 6, 7, 8, 9 };
		byte[] additionalData = new byte[] { 10, 11, 12, 13, 14 };
		EncryptedHeader encryptedHeader = FFI.encryptHeader(policy, publicKeyBytes, attributes, uid, additionalData);

		System.out.println("Symmetric Key length " + encryptedHeader.getSymmetricKey().length);
		System.out.println("Encrypted Header length " + encryptedHeader.getEncryptedHeaderBytes().length);

		byte[] data = new byte[] { 1, 2, 3, 4, 5, 6, 7, 8, 9 };
		byte[] encryptedBlock = FFI.encryptBlock(encryptedHeader.getSymmetricKey(), uid, 0, data);
		System.out.println("Clear Text Length " + data.length);
		System.out.println("Symmetric Crypto Overhead " + FFI.symmetricEncryptionOverhead());
		System.out.println("Encrypted Block Length " + encryptedBlock.length);

		// Decryption
		String userDecryptionKeyJson = Resources.load_resource("ffi/fin_confidential_user_key.json");
		PrivateKey userDecryptionKey = PrivateKey.fromJson(userDecryptionKeyJson);
		byte[] userDecryptionKeyBytes = KmipUtils.bytesFromKeyBlock(userDecryptionKey.getKeyBlock());

		DecryptedHeader header_ = FFI.decryptHeader(userDecryptionKeyBytes, encryptedHeader.getEncryptedHeaderBytes(),
				uid.length, additionalData.length);

		System.out.println("Decrypted Header: Symmetric Key Length " + header_.getSymmetricKey().length);
		System.out.println("Decrypted Header: UID Length " + header_.getUid().length);
		System.out.println("Decrypted Header: Additional Data Length " + header_.getAdditionalData());

		assertTrue(Arrays.equals(encryptedHeader.getSymmetricKey(), header_.getSymmetricKey()));
		assertTrue(Arrays.equals(uid, header_.getUid()));
		assertTrue(Arrays.equals(additionalData, header_.getAdditionalData()));

		byte[] data_ = FFI.decryptBlock(header_.getSymmetricKey(), header_.getUid(), 0, encryptedBlock);
		assertTrue(Arrays.equals(data, data_));

	}

	@Test
	public void testEmptyMetaData() throws Exception {

		System.out.println("");
		System.out.println("---------------------------------------");
		System.out.println(" Hybrid Crypto Test");
		System.out.println("---------------------------------------");
		System.out.println("");

		String publicKeyJson = Resources.load_resource("ffi/public_master_key.json");
		PublicKey publicKey = PublicKey.fromJson(publicKeyJson);
		byte[] publicKeyBytes = KmipUtils.bytesFromKeyBlock(publicKey.getKeyBlock());

		Policy policy = Policy.fromVendorAttributes(publicKey.attributes());

		Attr[] attributes = new Attr[] { new Attr("Department", "FIN"), new Attr("Security Level", "Confidential") };
		byte[] uid = new byte[] {};
		byte[] additionalData = new byte[] {};
		EncryptedHeader encryptedHeader = FFI.encryptHeader(policy, publicKeyBytes, attributes, uid, additionalData);

		System.out.println("Symmetric Key length " + encryptedHeader.getSymmetricKey().length);
		System.out.println("Encrypted Header length " + encryptedHeader.getEncryptedHeaderBytes().length);

		// Decryption
		String userDecryptionKeyJson = Resources.load_resource("ffi/fin_confidential_user_key.json");
		PrivateKey userDecryptionKey = PrivateKey.fromJson(userDecryptionKeyJson);
		byte[] userDecryptionKeyBytes = KmipUtils.bytesFromKeyBlock(userDecryptionKey.getKeyBlock());

		DecryptedHeader header_ = FFI.decryptHeader(userDecryptionKeyBytes, encryptedHeader.getEncryptedHeaderBytes(),
				uid.length, additionalData.length);

		System.out.println("Decrypted Header: Symmetric Key Length " + header_.getSymmetricKey().length);
		System.out.println("Decrypted Header: UID Length " + header_.getUid().length);
		System.out.println("Decrypted Header: Additional Data Length " + header_.getAdditionalData());

		assertTrue(Arrays.equals(encryptedHeader.getSymmetricKey(), header_.getSymmetricKey()));
		assertTrue(Arrays.equals(uid, header_.getUid()));
		assertTrue(Arrays.equals(additionalData, header_.getAdditionalData()));

	}

}
