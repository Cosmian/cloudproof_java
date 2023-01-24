package com.cosmian;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.cosmian.cover_crypt.NonRegressionVector;
import com.cosmian.jna.covercrypt.CoverCrypt;
import com.cosmian.jna.covercrypt.structs.DecryptedHeader;
import com.cosmian.jna.covercrypt.structs.EncryptedHeader;
import com.cosmian.jna.covercrypt.structs.MasterKeys;
import com.cosmian.jna.covercrypt.structs.Policy;
import com.cosmian.jna.covercrypt.structs.PolicyAxis;
import com.cosmian.jna.covercrypt.structs.PolicyAxisAttribute;
import com.cosmian.rest.abe.KmsClient;
import com.cosmian.rest.abe.data.DecryptedData;
import com.cosmian.rest.kmip.objects.PrivateKey;
import com.cosmian.rest.kmip.objects.PublicKey;
import com.cosmian.utils.CloudproofException;

public class TestNativeCoverCrypt {
    static final CoverCrypt coverCrypt = new CoverCrypt();

    @BeforeAll
    public static void before_all() {
        TestUtils.initLogging();
    }

    @Test
    public void testError() throws Exception {
        assertEquals("", coverCrypt.get_last_error());
        String error = "An Error";
        coverCrypt.set_error(error);
        assertEquals("FFI error: " + error, coverCrypt.get_last_error());
        String base = "0123456789";
        String s = "";
        for (int i = 0; i < 110; i++) {
            s += base;
        }
        assertEquals(1100, s.length());
        coverCrypt.set_error(s);
        try {
            coverCrypt.get_last_error(1024);
        } catch (Exception ex) {
            ex.getMessage().contains("Failed retrieving the last error");
        }
    }

    public byte[] hash(byte[] data) throws NoSuchAlgorithmException {
        MessageDigest sha256 = MessageDigest.getInstance("SHA-256");
        byte[] passHash = sha256.digest(data);
        return passHash;
    }

    @Test
    public void testHeaderSimple() throws Exception {

        System.out.println("");
        System.out.println("---------------------------------------");
        System.out.println(" Simple                                ");
        System.out.println("---------------------------------------");
        System.out.println("");

        // Declare the CoverCrypt Policy
        Policy policy = policy();

        // Generate the master keys
        MasterKeys masterKeys = coverCrypt.generateMasterKeys(policy);

        // Generate an user decryption key
        String accessPolicy = accessPolicyConfidential();
        byte[] userDecryptionKey = coverCrypt.generateUserPrivateKey(
            masterKeys.getPrivateKey(),
            accessPolicy,
            policy);

        // Rotate attributes
        String encryptionPolicy = "Department::FIN && Security Level::Confidential";

        // Now generate the header which contains the ABE encryption of the randomly
        // generated AES key.
        EncryptedHeader encryptedHeader = coverCrypt.encryptHeader(
            policy,
            masterKeys.getPublicKey(),
            encryptionPolicy,
            Optional.empty(),
            Optional.empty());

        System.out.println("USER KEY SIZE: " + userDecryptionKey.length);
        System.out.println("HEADER BYTES SIZE: " + encryptedHeader.getEncryptedHeaderBytes().length);
        System.out.println("SYMMETRIC KEY SIZE: " + encryptedHeader.getSymmetricKey().length);

        // Decrypt the header to recover the symmetric AES key
        DecryptedHeader decryptedHeader = coverCrypt.decryptHeader(
            userDecryptionKey,
            encryptedHeader.getEncryptedHeaderBytes());

        assertArrayEquals(encryptedHeader.getSymmetricKey(), decryptedHeader.getSymmetricKey());
    }

    @Test
    public void testKeysGeneration() throws Exception {

        System.out.println("");
        System.out.println("---------------------------------------");
        System.out.println(" CoverCrypt keys generation");
        System.out.println("---------------------------------------");
        System.out.println("");

        // Declare the CoverCrypt Policy
        Policy policy = policy();

        // Generate the master keys
        MasterKeys masterKeys = coverCrypt.generateMasterKeys(policy);

        // Generate an user decryption key
        String accessPolicy = accessPolicyConfidential();
        byte[] userDecryptionKey = coverCrypt.generateUserPrivateKey(masterKeys.getPrivateKey(), accessPolicy, policy);

        // Rotate attributes
        String encryptionPolicy = "Department::FIN && Security Level::Confidential";
        String[] attributes = new String[] {"Department::FIN", "Security Level::Confidential"};
        policy.rotateAttributes(attributes);

        // Must refresh the master keys after an attributes rotation
        masterKeys = coverCrypt.generateMasterKeys(policy);

        // Now generate the header which contains the ABE encryption of the randomly
        // generated AES key.
        EncryptedHeader encryptedHeader = coverCrypt.encryptHeader(
            policy,
            masterKeys.getPublicKey(),
            encryptionPolicy);

        // Decrypt the header to recover the symmetric AES key
        // Should fail since user decryption key has not been refreshed
        try {
            coverCrypt.decryptHeader(userDecryptionKey, encryptedHeader.getEncryptedHeaderBytes());
        } catch (Exception ex) {
            System.out.println(
                "As expected, user cannot be decrypt CoverCrypt Header since his user decryption key has not been refreshed: "
                    + ex.getMessage());
        }

        // Generate an user decryption key
        byte[] userDecryptionKeyRefreshed = coverCrypt.generateUserPrivateKey(masterKeys.getPrivateKey(), accessPolicy,
            policy);

        // Decrypt the header to recover the symmetric AES key
        DecryptedHeader decryptedHeader = coverCrypt.decryptHeader(
            userDecryptionKeyRefreshed,
            encryptedHeader.getEncryptedHeaderBytes());

        assertArrayEquals(encryptedHeader.getSymmetricKey(), decryptedHeader.getSymmetricKey());
    }

    @Test
    public void testHybridCryptoSimple() throws Exception {

        System.out.println("");
        System.out.println("---------------------------------------");
        System.out.println(" Hybrid Crypto Test Simple");
        System.out.println("---------------------------------------");
        System.out.println("");

        // The data we want to encrypt/decrypt
        byte[] plaintext = "This s a test message".getBytes(StandardCharsets.UTF_8);

        // Declare the CoverCrypt Policy
        Policy policy = policy();

        // Generate the master keys
        MasterKeys masterKeys = coverCrypt.generateMasterKeys(policy);

        // A unique ID associated with this message. The unique id is used to
        // authenticate the message in the AES encryption scheme.
        // Typically this will be a hash of the content if it is unique, a unique
        // filename or a database unique key
        byte[] uid = MessageDigest.getInstance("SHA-256").digest(plaintext);

        // The policy attributes that will be used to encrypt the content. They must
        // exist in the policy associated with the Public Key
        String encryptionPolicy = "Department::FIN && Security Level::Confidential";

        // now hybrid encrypt the data using the uid as authentication in the symmetric
        // cipher
        byte[] ciphertext = coverCrypt.encrypt(policy, masterKeys.getPublicKey(), encryptionPolicy, plaintext, uid);

        //
        // Decryption
        //

        // Generate an user decryption key
        String accessPolicy = accessPolicyConfidential();
        byte[] userDecryptionKey = coverCrypt.generateUserPrivateKey(
            masterKeys.getPrivateKey(),
            accessPolicy,
            policy);

        // decrypt the ciphertext using the uid as authentication in the symmetric
        // cipher
        DecryptedData res = coverCrypt.decrypt(userDecryptionKey, ciphertext, uid);

        // Verify everything is correct
        assertTrue(Arrays.equals(plaintext, res.getPlaintext()));
        assertTrue(Arrays.equals(new byte[] {}, res.getHeaderMetaData()));
    }

    @Test
    public void testHybridCryptoWithHeaderMetadata() throws Exception {

        System.out.println("");
        System.out.println("-----------------------------------------");
        System.out.println(" Hybrid Crypto test with header metadata ");
        System.out.println("-----------------------------------------");
        System.out.println("");

        // The data we want to encrypt/decrypt
        byte[] data = "This s a test message".getBytes(StandardCharsets.UTF_8);
        byte[] headerMetadata = new byte[] {1, 2, 3, 4, 5, 6};

        // Declare the CoverCrypt Policy
        Policy policy = policy();

        // Generate the master keys
        MasterKeys masterKeys = coverCrypt.generateMasterKeys(policy);

        // A unique ID associated with this message. The unique id is used to
        // authenticate the message in the AES encryption scheme.
        // Typically this will be a hash of the content if it is unique, a unique
        // filename or a database unique key
        byte[] uid = MessageDigest.getInstance("SHA-256").digest(data);

        // The policy attributes that will be used to encrypt the content. They must
        // exist in the policy associated with the Public Key
        String encryptionPolicy = "Department::FIN && Security Level::Confidential";

        // now hybrid encrypt the data using the uid as authentication in the symmetric
        // cipher
        byte[] ciphertext = coverCrypt.encrypt(policy, masterKeys.getPublicKey(), encryptionPolicy, data,
            uid, headerMetadata);

        //
        // Decryption
        //

        // Generate an user decryption key
        String accessPolicy = accessPolicyConfidential();
        byte[] userDecryptionKey = coverCrypt.generateUserPrivateKey(
            masterKeys.getPrivateKey(),
            accessPolicy,
            policy);

        // decrypt the ciphertext using the uid as authentication in the symmetric
        // cipher
        DecryptedData res = coverCrypt.decrypt(userDecryptionKey, ciphertext, uid);

        // Verify everything is correct
        assertTrue(Arrays.equals(data, res.getPlaintext()));
        assertTrue(Arrays.equals(headerMetadata, res.getHeaderMetaData()));
    }

    public static Policy policy() throws CloudproofException {
        return new Policy(20, new PolicyAxis[] {
            new PolicyAxis("Security Level",
                new PolicyAxisAttribute[] {new PolicyAxisAttribute("Protected", false),
                    new PolicyAxisAttribute("Confidential", false), new PolicyAxisAttribute("Top Secret", true)},
                true),
            new PolicyAxis("Department", new PolicyAxisAttribute[] {new PolicyAxisAttribute("FIN", false),
                new PolicyAxisAttribute("MKG", false), new PolicyAxisAttribute("HR", false)}, false)});
    }

    private String accessPolicyConfidential() throws CloudproofException {
        return "Department::FIN && Security Level::Confidential";
    }

    @Test
    public void testLocalEncryptServerDecrypt() throws Exception {

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
        byte[] plaintext = "This is a test message".getBytes(StandardCharsets.UTF_8);

        // A unique ID associated with this message. The unique id is used to
        // authenticate the message in the AES encryption scheme.
        // Typically this will be a hash of the content if it is unique, a unique
        // filename or a database unique key
        byte[] uid = MessageDigest.getInstance("SHA-256").digest(plaintext);

        Policy policy = policy();

        KmsClient kmsClient = new KmsClient(TestUtils.kmsServerUrl(), TestUtils.apiKey());

        String[] ids = kmsClient.createCoverCryptMasterKeyPair(policy);

        String privateMasterKeyId = ids[0];
        String publicMasterKeyId = ids[1];
        PublicKey publicKey = kmsClient.retrieveCoverCryptPublicMasterKey(publicMasterKeyId);

        // User decryption key Confidential, FIN
        String userKeyId = kmsClient.createCoverCryptUserDecryptionKey(accessPolicyConfidential(),
            privateMasterKeyId);

        //
        // Local Encryption
        //

        System.out.println("Local Encryption");

        // The encryption policy attributes that will be used to encrypt the content.
        // Attributes must exist in the policy associated with the Public Key
        String encryptionPolicy = "Department::FIN && Security Level::Confidential";
        byte[] ciphertext = coverCrypt.encrypt(policy, publicKey.bytes(), encryptionPolicy, plaintext, uid);

        //
        // Local Decryption
        //

        System.out.println("Local Decryption");

        PrivateKey userKey = kmsClient.retrieveCoverCryptUserDecryptionKey(userKeyId);
        DecryptedData res = coverCrypt.decrypt(userKey.bytes(), ciphertext, uid);
        assertArrayEquals(plaintext, res.getPlaintext());
        assertArrayEquals(new byte[] {}, res.getHeaderMetaData());

        //
        // KMS Decryption
        //

        System.out.println("KMS Decryption");

        byte[] data_kms = kmsClient.coverCryptDecrypt(userKeyId, ciphertext, uid).getPlaintext();
        assertArrayEquals(plaintext, data_kms);
    }

    @Test
    public void testServerEncryptLocalDecrypt() throws Exception {

        System.out.println("");
        System.out.println("------------------------------------------------------");
        System.out.println(" Hybrid Crypto Test Server Encrypt + Local Decrypt    ");
        System.out.println("------------------------------------------------------");
        System.out.println("");

        if (!TestUtils.serverAvailable(TestUtils.kmsServerUrl())) {
            System.out.println("No KMS Server: ignoring");
            return;
        }

        // The data we want to encrypt/decrypt
        byte[] plaintext = new byte[] {1, 2, 3, 4, 5, 6};

        // A unique ID associated with this message. The unique id is used to
        // authenticate the message in the AES encryption scheme.
        // Typically this will be a hash of the content if it is unique, a unique
        // filename or a database unique key
        byte[] uid = MessageDigest.getInstance("SHA-256").digest(plaintext);

        Policy policy = policy();

        KmsClient kmsClient = new KmsClient(TestUtils.kmsServerUrl(), TestUtils.apiKey());

        String[] ids = kmsClient.createCoverCryptMasterKeyPair(policy);

        String privateMasterKeyId = ids[0];
        String publicMasterKeyId = ids[1];

        // User decryption key Confidential, FIN
        String userKeyId = kmsClient.createCoverCryptUserDecryptionKey(accessPolicyConfidential(), privateMasterKeyId);

        //
        // Server Encryption
        //

        // The encryption policy attributes that will be used to encrypt the content.
        // Attributes must exist in the policy associated with the Public Key
        String encryptionPolicy = "Department::FIN && Security Level::Confidential";
        byte[] ciphertext = kmsClient.coverCryptEncrypt(publicMasterKeyId, plaintext, encryptionPolicy, uid);

        //
        // KMS Decryption
        //
        DecryptedData data_kms = kmsClient.coverCryptDecrypt(userKeyId, ciphertext, uid);
        assertArrayEquals(plaintext, data_kms.getPlaintext());
        assertArrayEquals(new byte[] {}, data_kms.getHeaderMetaData());

        //
        // Local Decryption
        //

        PrivateKey userKey = kmsClient.retrieveCoverCryptUserDecryptionKey(userKeyId);
        DecryptedData res = coverCrypt.decrypt(userKey.bytes(), ciphertext, uid);
        assertArrayEquals(plaintext, res.getPlaintext());
        assertArrayEquals(new byte[] {}, res.getHeaderMetaData());

    }

    @Test
    public void testHybridEncryptionDecryptionUsingCacheLocal() throws Exception {

        // Declare the CoverCrypt Policy
        Policy policy = policy();

        // Generate the master keys
        MasterKeys masterKeys = coverCrypt.generateMasterKeys(policy);

        // create encryption cache
        int encryptionCacheHandle = coverCrypt.createEncryptionCache(policy, masterKeys.getPublicKey());

        // encrypt
        String encryptionPolicy = "Department::FIN && Security Level::Confidential";
        byte[] uid = new byte[] {1, 2, 3, 4, 5};
        byte[] additional_data = new byte[] {6, 7, 8, 9, 10};

        EncryptedHeader encryptedHeader = coverCrypt.encryptHeaderUsingCache(encryptionCacheHandle, encryptionPolicy,
            Optional.of(uid), Optional.of(additional_data));

        coverCrypt.destroyEncryptionCache(encryptionCacheHandle);

        // decrypt

        // Generate an user decryption key
        String accessPolicy = accessPolicyConfidential();
        byte[] userDecryptionKey = coverCrypt.generateUserPrivateKey(
            masterKeys.getPrivateKey(),
            accessPolicy,
            policy);

        int decryptionCacheHandle = coverCrypt.createDecryptionCache(userDecryptionKey);

        DecryptedHeader decryptedHeader = coverCrypt.decryptHeaderUsingCache(decryptionCacheHandle,
            encryptedHeader.getEncryptedHeaderBytes(), 10,
            Optional.of(uid));

        coverCrypt.destroyDecryptionCache(decryptionCacheHandle);

        // assert

        assertArrayEquals(encryptedHeader.getSymmetricKey(), decryptedHeader.getSymmetricKey());
        assertArrayEquals(uid, decryptedHeader.getUid());
        assertArrayEquals(additional_data, decryptedHeader.getAdditionalData());
    }

    @Test
    public void testHybridEncryptionDecryptionUsingCacheLocalNoUid() throws Exception {

        // Declare the CoverCrypt Policy
        Policy policy = policy();

        // Generate the master keys
        MasterKeys masterKeys = coverCrypt.generateMasterKeys(policy);

        // create encryption cache
        int encryptionCacheHandle = coverCrypt.createEncryptionCache(policy, masterKeys.getPublicKey());

        // encrypt
        String encryptionPolicy = "Department::FIN && Security Level::Confidential";
        byte[] uid = new byte[] {1, 2, 3, 4, 5};

        EncryptedHeader encryptedHeader = coverCrypt.encryptHeaderUsingCache(encryptionCacheHandle, encryptionPolicy,
            Optional.of(uid), Optional.empty());

        coverCrypt.destroyEncryptionCache(encryptionCacheHandle);

        // decrypt

        // Generate an user decryption key
        String accessPolicy = accessPolicyConfidential();
        byte[] userDecryptionKey = coverCrypt.generateUserPrivateKey(
            masterKeys.getPrivateKey(),
            accessPolicy,
            policy);

        int decryptionCacheHandle = coverCrypt.createDecryptionCache(userDecryptionKey);

        DecryptedHeader decryptedHeader = coverCrypt.decryptHeaderUsingCache(decryptionCacheHandle,
            encryptedHeader.getEncryptedHeaderBytes(), 10,
            Optional.of(uid));

        coverCrypt.destroyDecryptionCache(decryptionCacheHandle);

        // assert

        assertArrayEquals(encryptedHeader.getSymmetricKey(), decryptedHeader.getSymmetricKey());
        assertArrayEquals(uid, decryptedHeader.getUid());
        assertArrayEquals(new byte[] {}, decryptedHeader.getAdditionalData());
    }

    @Test
    public void testHybridEncryptionDecryptionUsingCacheLocalUidNoData() throws Exception {

        // Declare the CoverCrypt Policy
        Policy policy = policy();

        // Generate the master keys
        MasterKeys masterKeys = coverCrypt.generateMasterKeys(policy);

        // create encryption cache
        int encryptionCacheHandle = coverCrypt.createEncryptionCache(policy, masterKeys.getPublicKey());

        // encrypt
        String encryptionPolicy = "Department::FIN && Security Level::Confidential";
        byte[] additional_data = new byte[] {6, 7, 8, 9, 10};

        EncryptedHeader encryptedHeader = coverCrypt.encryptHeaderUsingCache(encryptionCacheHandle, encryptionPolicy,
            Optional.empty(), Optional.of(additional_data));

        coverCrypt.destroyEncryptionCache(encryptionCacheHandle);

        // decrypt

        // Generate an user decryption key
        String accessPolicy = accessPolicyConfidential();
        byte[] userDecryptionKey = coverCrypt.generateUserPrivateKey(
            masterKeys.getPrivateKey(),
            accessPolicy,
            policy);

        int decryptionCacheHandle = coverCrypt.createDecryptionCache(userDecryptionKey);

        DecryptedHeader decryptedHeader = coverCrypt.decryptHeaderUsingCache(decryptionCacheHandle,
            encryptedHeader.getEncryptedHeaderBytes(), 10,
            Optional.empty());

        coverCrypt.destroyDecryptionCache(decryptionCacheHandle);

        // assert

        assertArrayEquals(encryptedHeader.getSymmetricKey(), decryptedHeader.getSymmetricKey());
        assertArrayEquals(new byte[] {}, decryptedHeader.getUid());
        assertArrayEquals(additional_data, decryptedHeader.getAdditionalData());
    }

    @Test
    public void testHybridEncryptionDecryptionUsingCacheLocalNoUidNoData() throws Exception {

        // Declare the CoverCrypt Policy
        Policy policy = policy();

        // Generate the master keys
        MasterKeys masterKeys = coverCrypt.generateMasterKeys(policy);

        // create encryption cache
        int encryptionCacheHandle = coverCrypt.createEncryptionCache(policy, masterKeys.getPublicKey());

        // encrypt
        String encryptionPolicy = "Department::FIN && Security Level::Confidential";

        EncryptedHeader encryptedHeader = coverCrypt.encryptHeaderUsingCache(encryptionCacheHandle, encryptionPolicy);

        coverCrypt.destroyEncryptionCache(encryptionCacheHandle);

        // decrypt

        // Generate an user decryption key
        String accessPolicy = accessPolicyConfidential();
        byte[] userDecryptionKey = coverCrypt.generateUserPrivateKey(
            masterKeys.getPrivateKey(),
            accessPolicy,
            policy);

        int decryptionCacheHandle = coverCrypt.createDecryptionCache(userDecryptionKey);

        DecryptedHeader decryptedHeader = coverCrypt.decryptHeaderUsingCache(decryptionCacheHandle,
            encryptedHeader.getEncryptedHeaderBytes(), 10,
            Optional.empty());

        coverCrypt.destroyDecryptionCache(decryptionCacheHandle);

        // assert

        assertArrayEquals(encryptedHeader.getSymmetricKey(), decryptedHeader.getSymmetricKey());
        assertArrayEquals(new byte[] {}, decryptedHeader.getUid());
        assertArrayEquals(new byte[] {}, decryptedHeader.getAdditionalData());
    }

    @Test
    public void testHybridEncryptionCacheSerialization() throws Exception {

        // Declare the CoverCrypt Policy
        Policy policy = policy();

        // Generate the master keys
        MasterKeys masterKeys = coverCrypt.generateMasterKeys(policy);

        // create encryption cache
        int encryptionCache = coverCrypt.createEncryptionCache(policy, masterKeys.getPublicKey());

        // encrypt
        String encryptionPolicy = "Department::FIN && Security Level::Confidential";
        byte[] uid = new byte[] {1, 2, 3, 4, 5};
        byte[] additional_data = new byte[] {6, 7, 8, 9, 10};

        // Generate an user decryption key
        String accessPolicy = accessPolicyConfidential();
        byte[] userDecryptionKey = coverCrypt.generateUserPrivateKey(
            masterKeys.getPrivateKey(),
            accessPolicy,
            policy);

        // serialize decryption cache
        int decryptionCache = coverCrypt.createDecryptionCache(userDecryptionKey);

        int threads = 4;
        ExecutorService executor = Executors.newFixedThreadPool(threads);
        for (int i = 0; i < threads; i++) {

            executor.submit(() -> {

                String threadName = Thread.currentThread().getName();
                // deserialize encryption cache
                EncryptedHeader encryptedHeader;
                DecryptedHeader decryptedHeader;
                try {

                    encryptedHeader = coverCrypt.encryptHeaderUsingCache(encryptionCache, encryptionPolicy,
                        Optional.of(uid),
                        Optional.of(additional_data));
                    decryptedHeader = coverCrypt.decryptHeaderUsingCache(decryptionCache,
                        encryptedHeader.getEncryptedHeaderBytes(), 10,
                        Optional.of(uid));

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

        coverCrypt.destroyEncryptionCache(encryptionCache);
        coverCrypt.destroyDecryptionCache(decryptionCache);

    }

    @Test
    public void test_non_regression_vectors_generation() throws Exception {
        NonRegressionVector nrv = NonRegressionVector.generate();
        Files.write(Paths.get("./target/non_regression_vector.json"), nrv.toJson().getBytes(StandardCharsets.UTF_8));
    }

    @Test
    public void test_non_regression_vectors() throws Exception {
        String testFolder = "src/test/resources/cover_crypt/non_regression/";
        for (String file : TestUtils.listFiles(testFolder)) {
            System.out.println("Non-regression test file: " + file);
            NonRegressionVector.verify(testFolder + file);
        }
    }

}
