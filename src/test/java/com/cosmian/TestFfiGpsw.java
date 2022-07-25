package com.cosmian;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.cosmian.jna.abe.DecryptedHeader;
import com.cosmian.jna.abe.EncryptedHeader;
import com.cosmian.jna.abe.Ffi;
import com.cosmian.jna.abe.FfiWrapper;
import com.cosmian.jna.abe.MasterKeys;
import com.cosmian.rest.abe.Abe;
import com.cosmian.rest.abe.Implementation;
import com.cosmian.rest.abe.Specifications;
import com.cosmian.rest.abe.access_policy.AccessPolicy;
import com.cosmian.rest.abe.access_policy.And;
import com.cosmian.rest.abe.access_policy.Attr;
import com.cosmian.rest.abe.policy.Policy;
import com.cosmian.rest.kmip.objects.PrivateKey;
import com.cosmian.rest.kmip.objects.PublicKey;
import com.sun.jna.Native;

public class TestFfiGpsw {

    static final Implementation abeImplementation = Implementation.GPSW;

    static final FfiWrapper INSTANCE = (FfiWrapper) Native.load("abe_gpsw", FfiWrapper.class);

    static final Ffi ffi = new Ffi(INSTANCE, new Specifications(abeImplementation));

    @BeforeAll
    public static void before_all() {
        TestUtils.initLogging();
    }

    @Test
    public void testError() throws Exception {
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

    public byte[] hash(byte[] data) throws NoSuchAlgorithmException {
        MessageDigest sha256 = MessageDigest.getInstance("SHA-256");
        byte[] passHash = sha256.digest(data);
        return passHash;
    }

    @Test
    public void testKeysGeneration() throws Exception {

        System.out.println("");
        System.out.println("---------------------------------------");
        System.out.println(" ABE keys generation");
        System.out.println("---------------------------------------");
        System.out.println("");

        // Declare the ABE Policy
        Policy policy = policy();

        // Generate the master keys
        MasterKeys masterKeys = ffi.generateMasterKeys(policy);

        // Generate an user decryption key
        AccessPolicy accessPolicy = accessPolicyConfidential();
        byte[] userDecryptionKey = ffi.generateUserPrivateKey(masterKeys.getPrivateKey(), accessPolicy, policy);

        // Rotate attributes
        Attr[] attributes = new Attr[] {new Attr("Department", "FIN"), new Attr("Security Level", "Confidential")};
        Policy newPolicy = ffi.rotateAttributes(attributes, policy);

        // Must refresh the master keys after an attributes rotation
        masterKeys = ffi.generateMasterKeys(newPolicy);

        // Now generate the header which contains the ABE encryption of the randomly
        // generated AES key.
        EncryptedHeader encryptedHeader =
            ffi.encryptHeader(newPolicy, masterKeys.getPublicKey(), attributes, Optional.empty(), Optional.empty());

        // Decrypt the header to recover the symmetric AES key
        // Should fail since user decryption key has not been refreshed
        try {
            ffi.decryptHeader(userDecryptionKey, encryptedHeader.getEncryptedHeaderBytes(), 0, 0);
        } catch (Exception ex) {
            System.out.println(
                "As expected, user cannot be decrypt ABE Header since his user decryption key has not been refreshed");
        }

        // Generate an user decryption key
        byte[] userDecryptionKeyRefreshed =
            ffi.generateUserPrivateKey(masterKeys.getPrivateKey(), accessPolicy, newPolicy);

        // Decrypt the header to recover the symmetric AES key
        ffi.decryptHeader(userDecryptionKeyRefreshed, encryptedHeader.getEncryptedHeaderBytes(), 0, 0);
    }

    @Test
    public void testBenchKeysGeneration() throws Exception {

        System.out.println("");
        System.out.println("---------------------------------------");
        System.out.println(" Bench ABE keys generation");
        System.out.println("---------------------------------------");
        System.out.println("");

        Policy policy = policy();
        MasterKeys masterKeys = null;
        long start = System.nanoTime();
        int nb_occurrences = 100;
        for (int i = 0; i < nb_occurrences; i++) {
            masterKeys = ffi.generateMasterKeys(policy);
        }
        long time = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - start);
        System.out.println("ABE Master Key generation average time: " + time / nb_occurrences + "s");

        AccessPolicy accessPolicy = accessPolicyConfidential();
        start = System.nanoTime();
        for (int i = 0; i < nb_occurrences; i++) {
            ffi.generateUserPrivateKey(masterKeys.getPrivateKey(), accessPolicy, policy);
        }
        time = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - start);
        System.out.println("ABE User Private Key generation average time: " + time / nb_occurrences + "ms");
    }

    @Test
    public void testHybridCryptoSimple() throws Exception {

        System.out.println("");
        System.out.println("---------------------------------------");
        System.out.println(" Hybrid Crypto Test Simple");
        System.out.println("---------------------------------------");
        System.out.println("");

        // The data we want to encrypt/decrypt
        byte[] data = "This s a test message".getBytes(StandardCharsets.UTF_8);

        // A unique ID associated with this message. The unique id is used to
        // authenticate the message in the AES encryption scheme.
        // Typically this will be a hash of the content if it is unique, a unique
        // filename or a database unique key
        byte[] uid = MessageDigest.getInstance("SHA-256").digest(data);

        // Load an existing public key
        String publicKeyJson = Resources.load_resource("ffi/abe/public_master_key.json");
        PublicKey publicKey = PublicKey.fromJson(publicKeyJson);

        // The policy attributes that will be used to encrypt the content. They must
        // exist in the policy associated with the Public Key
        Attr[] attributes = new Attr[] {new Attr("Department", "FIN"), new Attr("Security Level", "Confidential")};

        // Now generate the header which contains the ABE encryption of the randomly
        // generated AES key.
        // This example assumes that the Unique ID can be recovered at time of
        // decryption, and is thus not stored as part of the encrypted header.
        // If that is not the case check the other signature of #ffi.encryptedHeader()
        // to inject the unique id.
        EncryptedHeader encryptedHeader = ffi.encryptHeader(publicKey, attributes);

        // The data can now be encrypted with the generated key
        // The block number is also part of the authentication of the AES scheme
        byte[] encryptedBlock = ffi.encryptBlock(encryptedHeader.getSymmetricKey(), uid, 0, data);

        // Create a full message with header+encrypted data. The length of the header
        // is pre-pended.
        ByteBuffer headerSize =
            ByteBuffer.allocate(4).order(ByteOrder.BIG_ENDIAN).putInt(encryptedHeader.getEncryptedHeaderBytes().length);
        // Write the message
        ByteArrayOutputStream bao = new ByteArrayOutputStream();
        bao.write(headerSize.array());
        bao.write(encryptedHeader.getEncryptedHeaderBytes());
        bao.write(encryptedBlock);
        bao.flush();
        byte[] ciphertext = bao.toByteArray();

        //
        // Decryption
        //

        // Load an existing user decryption key
        String userDecryptionKeyJson = Resources.load_resource("ffi/abe/fin_confidential_user_key.json");
        PrivateKey userDecryptionKey = PrivateKey.fromJson(userDecryptionKeyJson);

        // Parse the message by first recovering the header length
        int headerSize_ = ByteBuffer.wrap(ciphertext).order(ByteOrder.BIG_ENDIAN).getInt(0);
        // Then recover the encrypted header and encrypted content
        byte[] encryptedHeader_ = Arrays.copyOfRange(ciphertext, 4, 4 + headerSize_);
        byte[] encryptedContent = Arrays.copyOfRange(ciphertext, 4 + headerSize_, ciphertext.length);

        // Decrypt he header to recover the symmetric AES key
        DecryptedHeader decryptedHeader = ffi.decryptHeader(userDecryptionKey, encryptedHeader_);

        // decrypt the content, passing the unique id and block number
        byte[] data_ = ffi.decryptBlock(decryptedHeader.getSymmetricKey(), uid, 0, encryptedContent);

        // Verify everything is correct
        assertTrue(Arrays.equals(data, data_));
    }

    @Test
    public void testHybridCryptoWithMetaData() throws Exception {

        System.out.println("");
        System.out.println("---------------------------------------");
        System.out.println(" Hybrid Crypto Test With Meta Data");
        System.out.println("---------------------------------------");
        System.out.println("");

        String publicKeyJson = Resources.load_resource("ffi/abe/public_master_key.json");
        PublicKey publicKey = PublicKey.fromJson(publicKeyJson);

        Attr[] attributes = new Attr[] {new Attr("Department", "FIN"), new Attr("Security Level", "Confidential")};
        byte[] uid = new byte[] {1, 2, 3, 4, 5, 6, 7, 8, 9};
        byte[] additionalData = new byte[] {10, 11, 12, 13, 14};
        EncryptedHeader encryptedHeader =
            ffi.encryptHeader(publicKey, attributes, Optional.of(uid), Optional.of(additionalData));

        System.out.println("Symmetric Key length " + encryptedHeader.getSymmetricKey().length);
        System.out.println("Encrypted Header length " + encryptedHeader.getEncryptedHeaderBytes().length);

        byte[] data = new byte[] {1, 2, 3, 4, 5, 6, 7, 8, 9};
        byte[] encryptedBlock = ffi.encryptBlock(encryptedHeader.getSymmetricKey(), uid, 0, data);
        System.out.println("Clear Text Length " + data.length);
        System.out.println("Symmetric Crypto Overhead " + ffi.symmetricEncryptionOverhead());
        System.out.println("Encrypted Block Length " + encryptedBlock.length);

        // Decryption
        String userDecryptionKeyJson = Resources.load_resource("ffi/abe/fin_confidential_user_key.json");
        PrivateKey userDecryptionKey = PrivateKey.fromJson(userDecryptionKeyJson);

        DecryptedHeader header_ = ffi.decryptHeader(userDecryptionKey, encryptedHeader.getEncryptedHeaderBytes(),
            uid.length, additionalData.length);

        System.out.println("Decrypted Header: Symmetric Key Length " + header_.getSymmetricKey().length);
        System.out.println("Decrypted Header: UID Length " + header_.getUid().length);
        System.out.println("Decrypted Header: Additional Data Length " + header_.getAdditionalData());

        assertTrue(Arrays.equals(encryptedHeader.getSymmetricKey(), header_.getSymmetricKey()));
        assertTrue(Arrays.equals(uid, header_.getUid()));
        assertTrue(Arrays.equals(additionalData, header_.getAdditionalData()));

        byte[] data_ = ffi.decryptBlock(header_.getSymmetricKey(), header_.getUid(), 0, encryptedBlock);
        assertTrue(Arrays.equals(data, data_));

    }

    @Test
    public void testEmptyMetaData() throws Exception {

        System.out.println("");
        System.out.println("---------------------------------------");
        System.out.println(" Simple header tests");
        System.out.println("---------------------------------------");
        System.out.println("");

        String publicKeyJson = Resources.load_resource("ffi/abe/public_master_key.json");
        PublicKey publicKey = PublicKey.fromJson(publicKeyJson);

        Attr[] attributes = new Attr[] {new Attr("Department", "FIN"), new Attr("Security Level", "Confidential")};
        EncryptedHeader encryptedHeader = ffi.encryptHeader(publicKey, attributes);

        System.out.println("Symmetric Key length " + encryptedHeader.getSymmetricKey().length);
        System.out.println("Encrypted Header length " + encryptedHeader.getEncryptedHeaderBytes().length);

        // Decryption
        String userDecryptionKeyJson = Resources.load_resource("ffi/abe/fin_confidential_user_key.json");
        PrivateKey userDecryptionKey = PrivateKey.fromJson(userDecryptionKeyJson);

        DecryptedHeader header_ = ffi.decryptHeader(userDecryptionKey, encryptedHeader.getEncryptedHeaderBytes());

        System.out.println("Decrypted Header: Symmetric Key Length " + header_.getSymmetricKey().length);
        System.out.println("Decrypted Header: UID Length " + header_.getUid().length);
        System.out.println("Decrypted Header: Additional Data Length " + header_.getAdditionalData());

        assertTrue(Arrays.equals(encryptedHeader.getSymmetricKey(), header_.getSymmetricKey()));
        assertTrue(Arrays.equals(new byte[] {}, header_.getUid()));
        assertTrue(Arrays.equals(new byte[] {}, header_.getAdditionalData()));

    }

    private Policy policy() throws CosmianException {
        return new Policy(20).addAxis("Security Level", new String[] {"Protected", "Confidential", "Top Secret"}, true)
            .addAxis("Department", new String[] {"FIN", "MKG", "HR"}, false);
    }

    private AccessPolicy accessPolicyConfidential() throws CosmianException {
        return new And(new Attr("Department", "FIN"), new Attr("Security Level", "Confidential"));
    }

    @Test
    public void testHybridCryptoLocalEncryptServerDecrypt() throws Exception {

        System.out.println("");
        System.out.println("---------------------------------------");
        System.out.println(" Hybrid Crypto Test Local Encrypt + Server Decrypt");
        System.out.println("---------------------------------------");
        System.out.println("");

        if (!TestUtils.serverAvailable(TestUtils.kmsServerUrl())) {
            System.out.println("No KMS Server: ignoring");
            return;
        }

        // The data we want to encrypt/decrypt
        byte[] data = "This is a test message".getBytes(StandardCharsets.UTF_8);

        // A unique ID associated with this message. The unique id is used to
        // authenticate the message in the AES encryption scheme.
        // Typically this will be a hash of the content if it is unique, a unique
        // filename or a database unique key
        byte[] uid = MessageDigest.getInstance("SHA-256").digest(data);

        Policy pg = policy();

        Abe abe = new Abe(new RestClient(TestUtils.kmsServerUrl(), TestUtils.apiKey()),
            new Specifications(Implementation.GPSW));

        String[] ids = abe.createMasterKeyPair(pg);

        String privateMasterKeyId = ids[0];
        String publicMasterKeyId = ids[1];
        PublicKey publicKey = abe.retrievePublicMasterKey(publicMasterKeyId);

        // User decryption key Confidential, FIN
        String userKeyId = abe.createUserDecryptionKey(accessPolicyConfidential(), privateMasterKeyId);

        //
        // Local Encryption
        //

        // The policy attributes that will be used to encrypt the content. They must
        // exist in the policy associated with the Public Key
        Attr[] attributes = new Attr[] {new Attr("Department", "FIN"), new Attr("Security Level", "Confidential")};

        // Now generate the header which contains the ABE encryption of the randomly
        // generated AES key.
        // This example assumes that the Unique ID can be recovered at time of
        // decryption, and is thus not stored as part of the encrypted header.
        // If that is not the case check the other signature of #ffi.encryptedHeader()
        // to inject the unique id.
        EncryptedHeader encryptedHeader = ffi.encryptHeader(publicKey, attributes);

        // The data can now be encrypted with the generated key
        // The block number is also part of the authentication of the AES scheme
        byte[] encryptedBlock = ffi.encryptBlock(encryptedHeader.getSymmetricKey(), uid, 0, data);

        // Create a full message with header+encrypted data. The length of the header
        // is pre-pended.
        ByteBuffer headerSize =
            ByteBuffer.allocate(4).order(ByteOrder.BIG_ENDIAN).putInt(encryptedHeader.getEncryptedHeaderBytes().length);
        // Write the message
        ByteArrayOutputStream bao = new ByteArrayOutputStream();
        bao.write(headerSize.array());
        bao.write(encryptedHeader.getEncryptedHeaderBytes());
        bao.write(encryptedBlock);
        bao.flush();
        byte[] ciphertext = bao.toByteArray();

        //
        // KMS Decryption
        //
        byte[] data_kms = abe.kmsDecrypt(userKeyId, ciphertext, Optional.of(uid));

        assertArrayEquals(data, data_kms);
    }

    @Test
    public void testHybridCryptoLocalEncryptServerDecryptNoUid() throws Exception {

        System.out.println("");
        System.out.println("---------------------------------------");
        System.out.println(" Hybrid Crypto Test Local Encrypt + Server Decrypt");
        System.out.println("---------------------------------------");
        System.out.println("");

        if (!TestUtils.serverAvailable(TestUtils.kmsServerUrl())) {
            System.out.println("No KMS Server: ignoring");
            return;
        }

        // The data we want to encrypt/decrypt
        byte[] data = "This is a test message".getBytes(StandardCharsets.UTF_8);

        Policy pg = policy();

        Abe abe = new Abe(new RestClient(TestUtils.kmsServerUrl(), TestUtils.apiKey()),
            new Specifications(Implementation.GPSW));

        String[] ids = abe.createMasterKeyPair(pg);

        String privateMasterKeyId = ids[0];
        String publicMasterKeyId = ids[1];
        PublicKey publicKey = abe.retrievePublicMasterKey(publicMasterKeyId);

        // User decryption key Confidential, FIN
        String userKeyId = abe.createUserDecryptionKey(accessPolicyConfidential(), privateMasterKeyId);

        //
        // Local Encryption
        //

        // The policy attributes that will be used to encrypt the content. They must
        // exist in the policy associated with the Public Key
        Attr[] attributes = new Attr[] {new Attr("Department", "FIN"), new Attr("Security Level", "Confidential")};

        // Now generate the header which contains the ABE encryption of the randomly
        // generated AES key.
        // This example assumes that the Unique ID can be recovered at time of
        // decryption, and is thus not stored as part of the encrypted header.
        // If that is not the case check the other signature of #ffi.encryptedHeader()
        // to inject the unique id.
        EncryptedHeader encryptedHeader = ffi.encryptHeader(publicKey, attributes);

        // The data can now be encrypted with the generated key
        // The block number is also part of the authentication of the AES scheme
        byte[] encryptedBlock = ffi.encryptBlock(encryptedHeader.getSymmetricKey(), data);

        // Create a full message with header+encrypted data. The length of the header
        // is pre-pended.
        ByteBuffer headerSize =
            ByteBuffer.allocate(4).order(ByteOrder.BIG_ENDIAN).putInt(encryptedHeader.getEncryptedHeaderBytes().length);
        // Write the message
        ByteArrayOutputStream bao = new ByteArrayOutputStream();
        bao.write(headerSize.array());
        bao.write(encryptedHeader.getEncryptedHeaderBytes());
        bao.write(encryptedBlock);
        bao.flush();
        byte[] ciphertext = bao.toByteArray();

        //
        // KMS Decryption
        //
        byte[] data_kms = abe.kmsDecrypt(userKeyId, ciphertext);

        assertArrayEquals(data, data_kms);
    }

    @Test
    public void testHybridCryptoKmsEncryptLocalDecrypt() throws Exception {

        System.out.println("");
        System.out.println("---------------------------------------");
        System.out.println(" Hybrid Crypto Test KMS Encrypt + Local Decrypt");
        System.out.println("---------------------------------------");
        System.out.println("");

        if (!TestUtils.serverAvailable(TestUtils.kmsServerUrl())) {
            System.out.println("No KMS Server: ignoring");
            return;
        }

        // The data we want to encrypt/decrypt
        byte[] data = "This is a test message".getBytes(StandardCharsets.UTF_8);

        // A unique ID associated with this message. The unique id is used to
        // authenticate the message in the AES encryption scheme.
        // Typically this will be a hash of the content if it is unique, a unique
        // filename or a database unique key
        byte[] uid = MessageDigest.getInstance("SHA-256").digest(data);

        Policy pg = policy();

        Abe abe = new Abe(new RestClient(TestUtils.kmsServerUrl(), TestUtils.apiKey()),
            new Specifications(Implementation.GPSW));

        String[] ids = abe.createMasterKeyPair(pg);

        String privateMasterKeyId = ids[0];
        String publicMasterKeyId = ids[1];

        // User decryption key Confidential, FIN
        String userKeyId = abe.createUserDecryptionKey(accessPolicyConfidential(), privateMasterKeyId);
        PrivateKey userKey = abe.retrieveUserDecryptionKey(userKeyId);

        // The policy attributes that will be used to encrypt the content. They must
        // exist in the policy associated with the Public Key
        Attr[] attributes = new Attr[] {new Attr("Department", "FIN"), new Attr("Security Level", "Confidential")};

        //
        // KMS Encryption
        //
        byte[] ciphertext = abe.kmsEncrypt(publicMasterKeyId, data, attributes, Optional.of(uid));
        System.out.println("CIPHER TEXT SIZE: " + ciphertext.length);

        //
        // Local Decryption
        //
        // Parse the message by first recovering the header length
        int headerSize = ByteBuffer.wrap(ciphertext).order(ByteOrder.BIG_ENDIAN).getInt(0);
        System.out.println("HEADER SIZE: " + headerSize);
        // Then recover the encrypted header and encrypted content
        byte[] encryptedHeader = Arrays.copyOfRange(ciphertext, 4, 4 + headerSize);

        byte[] encryptedContent = Arrays.copyOfRange(ciphertext, 4 + headerSize, ciphertext.length);

        // Decrypt he header to recover the symmetric AES key
        DecryptedHeader decryptedHeader = ffi.decryptHeader(userKey, encryptedHeader);

        // decrypt the content, passing the unique id and block number
        byte[] data_ = ffi.decryptBlock(decryptedHeader.getSymmetricKey(), uid, 0, encryptedContent);

        // Verify everything is correct
        assertTrue(Arrays.equals(data, data_));

    }

    @Test
    public void testHybridCryptoKmsEncryptLocalDecryptNoUid() throws Exception {

        System.out.println("");
        System.out.println("---------------------------------------");
        System.out.println(" Hybrid Crypto Test KMS Encrypt + Local Decrypt - No UID");
        System.out.println("---------------------------------------");
        System.out.println("");

        if (!TestUtils.serverAvailable(TestUtils.kmsServerUrl())) {
            System.out.println("No KMS Server: ignoring");
            return;
        }

        // The data we want to encrypt/decrypt
        byte[] data = "This is a test message".getBytes(StandardCharsets.UTF_8);

        Policy pg = policy();

        Abe abe = new Abe(new RestClient(TestUtils.kmsServerUrl(), TestUtils.apiKey()),
            new Specifications(Implementation.GPSW));

        String[] ids = abe.createMasterKeyPair(pg);

        String privateMasterKeyId = ids[0];
        String publicMasterKeyId = ids[1];

        // User decryption key Confidential, FIN
        String userKeyId = abe.createUserDecryptionKey(accessPolicyConfidential(), privateMasterKeyId);
        PrivateKey userKey = abe.retrieveUserDecryptionKey(userKeyId);

        // The policy attributes that will be used to encrypt the content. They must
        // exist in the policy associated with the Public Key
        Attr[] attributes = new Attr[] {new Attr("Department", "FIN"), new Attr("Security Level", "Confidential")};

        //
        // KMS Encryption
        //
        byte[] ciphertext = abe.kmsEncrypt(publicMasterKeyId, data, attributes);
        System.out.println("CIPHER TEXT SIZE: " + ciphertext.length);

        //
        // Local Decryption
        //
        // Parse the message by first recovering the header length
        int headerSize = ByteBuffer.wrap(ciphertext).order(ByteOrder.BIG_ENDIAN).getInt(0);
        System.out.println("HEADER SIZE: " + headerSize);
        // Then recover the encrypted header and encrypted content
        byte[] encryptedHeader = Arrays.copyOfRange(ciphertext, 4, 4 + headerSize);

        byte[] encryptedContent = Arrays.copyOfRange(ciphertext, 4 + headerSize, ciphertext.length);

        // Decrypt he header to recover the symmetric AES key
        DecryptedHeader decryptedHeader = ffi.decryptHeader(userKey, encryptedHeader);

        // decrypt the content, passing the unique id and block number
        byte[] data_ = ffi.decryptBlock(decryptedHeader.getSymmetricKey(), encryptedContent);

        // Verify everything is correct
        assertTrue(Arrays.equals(data, data_));

    }

    @Test
    public void testHybridEncryptionDecryptionUsingCacheLocal() throws Exception {

        // encrypt

        String publicKeyJson = Resources.load_resource("ffi/abe/public_master_key.json");
        PublicKey publicKey = PublicKey.fromJson(publicKeyJson);
        int encryptionCacheHandle = ffi.createEncryptionCache(publicKey);
        Attr[] attributes = new Attr[] {new Attr("Department", "FIN"), new Attr("Security Level", "Confidential")};
        byte[] uid = new byte[] {1, 2, 3, 4, 5};
        byte[] additional_data = new byte[] {6, 7, 8, 9, 10};

        EncryptedHeader encryptedHeader = ffi.encryptHeaderUsingCache(encryptionCacheHandle, attributes,
            Optional.of(uid), Optional.of(additional_data));

        ffi.destroyEncryptionCache(encryptionCacheHandle);

        // decrypt

        String userDecryptionKeyJson = Resources.load_resource("ffi/abe/fin_confidential_user_key.json");
        PrivateKey userDecryptionKey = PrivateKey.fromJson(userDecryptionKeyJson);

        int decryptionCacheHandle = ffi.createDecryptionCache(userDecryptionKey);

        DecryptedHeader decryptedHeader =
            ffi.decryptHeaderUsingCache(decryptionCacheHandle, encryptedHeader.getEncryptedHeaderBytes(), 10, 10);

        ffi.destroyDecryptionCache(decryptionCacheHandle);

        // assert

        assertArrayEquals(encryptedHeader.getSymmetricKey(), decryptedHeader.getSymmetricKey());
        assertArrayEquals(uid, decryptedHeader.getUid());
        assertArrayEquals(additional_data, decryptedHeader.getAdditionalData());
    }

    @Test
    public void testHybridEncryptionDecryptionUsingCacheLocalNoUid() throws Exception {

        // encrypt

        String publicKeyJson = Resources.load_resource("ffi/abe/public_master_key.json");
        PublicKey publicKey = PublicKey.fromJson(publicKeyJson);
        int encryptionCacheHandle = ffi.createEncryptionCache(publicKey);
        Attr[] attributes = new Attr[] {new Attr("Department", "FIN"), new Attr("Security Level", "Confidential")};
        byte[] uid = new byte[] {1, 2, 3, 4, 5};

        EncryptedHeader encryptedHeader =
            ffi.encryptHeaderUsingCache(encryptionCacheHandle, attributes, Optional.of(uid), Optional.empty());

        ffi.destroyEncryptionCache(encryptionCacheHandle);

        // decrypt

        String userDecryptionKeyJson = Resources.load_resource("ffi/abe/fin_confidential_user_key.json");
        PrivateKey userDecryptionKey = PrivateKey.fromJson(userDecryptionKeyJson);

        int decryptionCacheHandle = ffi.createDecryptionCache(userDecryptionKey);

        DecryptedHeader decryptedHeader =
            ffi.decryptHeaderUsingCache(decryptionCacheHandle, encryptedHeader.getEncryptedHeaderBytes(), 10, 10);

        ffi.destroyDecryptionCache(decryptionCacheHandle);

        // assert

        assertArrayEquals(encryptedHeader.getSymmetricKey(), decryptedHeader.getSymmetricKey());
        assertArrayEquals(uid, decryptedHeader.getUid());
        assertArrayEquals(new byte[] {}, decryptedHeader.getAdditionalData());
    }

    @Test
    public void testHybridEncryptionDecryptionUsingCacheLocalUidNoData() throws Exception {

        // encrypt

        String publicKeyJson = Resources.load_resource("ffi/abe/public_master_key.json");
        PublicKey publicKey = PublicKey.fromJson(publicKeyJson);
        int encryptionCacheHandle = ffi.createEncryptionCache(publicKey);
        Attr[] attributes = new Attr[] {new Attr("Department", "FIN"), new Attr("Security Level", "Confidential")};
        byte[] additional_data = new byte[] {6, 7, 8, 9, 10};

        EncryptedHeader encryptedHeader = ffi.encryptHeaderUsingCache(encryptionCacheHandle, attributes,
            Optional.empty(), Optional.of(additional_data));

        ffi.destroyEncryptionCache(encryptionCacheHandle);

        // decrypt

        String userDecryptionKeyJson = Resources.load_resource("ffi/abe/fin_confidential_user_key.json");
        PrivateKey userDecryptionKey = PrivateKey.fromJson(userDecryptionKeyJson);

        int decryptionCacheHandle = ffi.createDecryptionCache(userDecryptionKey);

        DecryptedHeader decryptedHeader =
            ffi.decryptHeaderUsingCache(decryptionCacheHandle, encryptedHeader.getEncryptedHeaderBytes(), 10, 10);

        ffi.destroyDecryptionCache(decryptionCacheHandle);

        // assert

        assertArrayEquals(encryptedHeader.getSymmetricKey(), decryptedHeader.getSymmetricKey());
        assertArrayEquals(new byte[] {}, decryptedHeader.getUid());
        assertArrayEquals(additional_data, decryptedHeader.getAdditionalData());
    }

    @Test
    public void testHybridEncryptionDecryptionUsingCacheLocalNoUidNoData() throws Exception {

        // encrypt

        String publicKeyJson = Resources.load_resource("ffi/abe/public_master_key.json");
        PublicKey publicKey = PublicKey.fromJson(publicKeyJson);
        int encryptionCacheHandle = ffi.createEncryptionCache(publicKey);
        Attr[] attributes = new Attr[] {new Attr("Department", "FIN"), new Attr("Security Level", "Confidential")};

        EncryptedHeader encryptedHeader = ffi.encryptHeaderUsingCache(encryptionCacheHandle, attributes);

        ffi.destroyEncryptionCache(encryptionCacheHandle);

        // decrypt

        String userDecryptionKeyJson = Resources.load_resource("ffi/abe/fin_confidential_user_key.json");
        PrivateKey userDecryptionKey = PrivateKey.fromJson(userDecryptionKeyJson);

        int decryptionCacheHandle = ffi.createDecryptionCache(userDecryptionKey);

        DecryptedHeader decryptedHeader =
            ffi.decryptHeaderUsingCache(decryptionCacheHandle, encryptedHeader.getEncryptedHeaderBytes(), 10, 10);

        ffi.destroyDecryptionCache(decryptionCacheHandle);

        // assert

        assertArrayEquals(encryptedHeader.getSymmetricKey(), decryptedHeader.getSymmetricKey());
        assertArrayEquals(new byte[] {}, decryptedHeader.getUid());
        assertArrayEquals(new byte[] {}, decryptedHeader.getAdditionalData());
    }

    @Test
    public void testHybridEncryptionDecryptionNoCacheLocal() throws Exception {
        String publicKeyJson = Resources.load_resource("ffi/abe/public_master_key.json");
        PublicKey publicKey = PublicKey.fromJson(publicKeyJson);

        Attr[] attributes = new Attr[] {new Attr("Department", "FIN"), new Attr("Security Level", "Confidential")};
        byte[] uid = new byte[] {1, 2, 3, 4, 5};
        byte[] additional_data = new byte[] {6, 7, 8, 9, 10};

        EncryptedHeader encryptedHeader =
            ffi.encryptHeader(publicKey, attributes, Optional.of(uid), Optional.of(additional_data));

        // decrypt

        String userDecryptionKeyJson = Resources.load_resource("ffi/abe/fin_confidential_user_key.json");
        PrivateKey userDecryptionKey = PrivateKey.fromJson(userDecryptionKeyJson);

        DecryptedHeader decryptedHeader =
            ffi.decryptHeader(userDecryptionKey, encryptedHeader.getEncryptedHeaderBytes(), 10, 10);

        assertArrayEquals(encryptedHeader.getSymmetricKey(), decryptedHeader.getSymmetricKey());
        assertArrayEquals(uid, decryptedHeader.getUid());
        assertArrayEquals(additional_data, decryptedHeader.getAdditionalData());
    }

    @Test
    public void testHybridEncryptionCacheSerialization() throws Exception {

        String publicKeyJson = Resources.load_resource("ffi/abe/public_master_key.json");
        PublicKey publicKey = PublicKey.fromJson(publicKeyJson);

        // serialize encryption cache
        int e_cache = ffi.createEncryptionCache(publicKey);

        Attr[] attributes = new Attr[] {new Attr("Department", "FIN"), new Attr("Security Level", "Confidential")};
        byte[] uid = new byte[] {1, 2, 3, 4, 5};
        byte[] additional_data = new byte[] {6, 7, 8, 9, 10};

        String userDecryptionKeyJson = Resources.load_resource("ffi/abe/fin_confidential_user_key.json");
        PrivateKey userDecryptionKey = PrivateKey.fromJson(userDecryptionKeyJson);

        // serialize decryption cache
        int d_cache = ffi.createDecryptionCache(userDecryptionKey);

        int threads = 4;
        ExecutorService executor = Executors.newFixedThreadPool(threads);
        for (int i = 0; i < threads; i++) {

            executor.submit(() -> {

                String threadName = Thread.currentThread().getName();
                // deserialize encryption cache
                EncryptedHeader encryptedHeader;
                DecryptedHeader decryptedHeader;
                try {

                    encryptedHeader = ffi.encryptHeaderUsingCache(e_cache, attributes, Optional.of(uid),
                        Optional.of(additional_data));
                    decryptedHeader =
                        ffi.decryptHeaderUsingCache(d_cache, encryptedHeader.getEncryptedHeaderBytes(), 10, 10);

                    assertArrayEquals(encryptedHeader.getSymmetricKey(), decryptedHeader.getSymmetricKey());
                    assertArrayEquals(uid, decryptedHeader.getUid());
                    assertArrayEquals(additional_data, decryptedHeader.getAdditionalData());

                    System.out.println("Thread name " + threadName + " OK");
                } catch (Throwable e) {
                    e.printStackTrace();
                }

            });
        }

        try {
            System.out.println("attempt to shutdown executor");
            executor.shutdown();
            executor.awaitTermination(5, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            System.err.println("tasks interrupted");
        } finally {
            if (!executor.isTerminated()) {
                System.err.println("cancel non-finished tasks");
            }
            executor.shutdownNow();
            System.out.println("shutdown finished");
        }

        ffi.destroyEncryptionCache(e_cache);
        ffi.destroyDecryptionCache(d_cache);

    }

}
