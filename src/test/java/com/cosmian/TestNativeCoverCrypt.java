package com.cosmian;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

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

import com.cosmian.jna.abe.CoverCrypt;
import com.cosmian.jna.abe.DecryptedHeader;
import com.cosmian.jna.abe.EncryptedHeader;
import com.cosmian.jna.abe.MasterKeys;
import com.cosmian.rest.abe.KmsClient;
import com.cosmian.rest.abe.access_policy.Attr;
import com.cosmian.rest.abe.policy.Policy;
import com.cosmian.rest.kmip.objects.PrivateKey;
import com.cosmian.rest.kmip.objects.PublicKey;

public class TestNativeCoverCrypt {
    static final CoverCrypt coverCrypt = new CoverCrypt();

    @BeforeAll
    public static void before_all() {
        TestUtils.initLogging();
    }

    @Test
    public void testError() throws Exception {
        assertEquals("", coverCrypt.get_last_error());
        String error = "An Error éà";
        coverCrypt.set_error(error);
        assertEquals("FFI error: " + error, coverCrypt.get_last_error());
        String base = "0123456789";
        String s = "";
        for (int i = 0; i < 110; i++) {
            s += base;
        }
        assertEquals(1100, s.length());
        coverCrypt.set_error(s);
        String err = coverCrypt.get_last_error(1023);
        assertEquals(1023, err.length());
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
        Attr[] attributes = new Attr[] {new Attr("Department", "FIN"), new Attr("Security Level", "Confidential")};
        Policy newPolicy = coverCrypt.rotateAttributes(attributes, policy);

        // Must refresh the master keys after an attributes rotation
        masterKeys = coverCrypt.generateMasterKeys(newPolicy);

        // Now generate the header which contains the ABE encryption of the randomly
        // generated AES key.
        EncryptedHeader encryptedHeader = coverCrypt.encryptHeader(
            newPolicy,
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
            newPolicy);

        // Decrypt the header to recover the symmetric AES key
        DecryptedHeader decryptedHeader = coverCrypt.decryptHeader(
            userDecryptionKeyRefreshed,
            encryptedHeader.getEncryptedHeaderBytes());

        assertArrayEquals(encryptedHeader.getSymmetricKey(), decryptedHeader.getSymmetricKey());
    }

    @Test
    public void testBenchKeysGeneration() throws Exception {

        System.out.println("");
        System.out.println("---------------------------------------");
        System.out.println(" Bench CoverCrypt keys generation");
        System.out.println("---------------------------------------");
        System.out.println("");

        Policy policy = policy();
        MasterKeys masterKeys = null;
        long start = System.nanoTime();
        // Single generation being very small (about 180µs), nb_occurrences should be at
        // least 1 million
        // for CI purpose, value is 10000
        int nb_occurrences = 10000;
        for (int i = 0; i < nb_occurrences; i++) {
            masterKeys = coverCrypt.generateMasterKeys(policy);
        }
        long time = (System.nanoTime() - start);
        System.out.println("CoverCrypt Master Key generation average time: " + time / nb_occurrences + "ns (or "
            + time / 1000 / nb_occurrences + "µs)");

        String accessPolicy = accessPolicyConfidential();
        start = System.nanoTime();
        for (int i = 0; i < nb_occurrences; i++) {
            coverCrypt.generateUserPrivateKey(masterKeys.getPrivateKey(), accessPolicy, policy);
        }
        time = (System.nanoTime() - start);
        System.out.println("CoverCrypt User Private Key generation average time: " + time / nb_occurrences + "ns (or "
            + time / 1000 / nb_occurrences + "µs)");
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
        byte[] ciphertext = coverCrypt.encrypt(policy, masterKeys.getPublicKey(), encryptionPolicy, data, uid);

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
        byte[][] res = coverCrypt.decrypt(userDecryptionKey, ciphertext, uid);
        byte[] data_ = res[0];
        byte[] additionalData_ = res[1];

        // Verify everything is correct
        assertTrue(Arrays.equals(data, data_));
        assertTrue(Arrays.equals(new byte[] {}, additionalData_));
    }

    @Test
    public void testHybridCryptoWithAdditionalData() throws Exception {

        System.out.println("");
        System.out.println("-----------------------------------------");
        System.out.println(" Hybrid Crypto test with additional data ");
        System.out.println("-----------------------------------------");
        System.out.println("");

        // The data we want to encrypt/decrypt
        byte[] data = "This s a test message".getBytes(StandardCharsets.UTF_8);
        byte[] additionalData = new byte[] {1, 2, 3, 4, 5, 6};

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
            additionalData, uid);

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
        byte[][] res = coverCrypt.decrypt(userDecryptionKey, ciphertext, uid);
        byte[] data_ = res[0];
        byte[] additionalData_ = res[1];

        // Verify everything is correct
        assertTrue(Arrays.equals(data, data_));
        assertTrue(Arrays.equals(additionalData, additionalData_));
    }

    private Policy policy() throws CosmianException {
        return new Policy(20)
            .addAxis("Security Level", new String[] {"Protected", "Confidential", "Top Secret"}, true)
            .addAxis("Department", new String[] {"FIN", "MKG", "HR"}, false);
    }

    private String accessPolicyConfidential() throws CosmianException {
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
        byte[] data = "This is a test message".getBytes(StandardCharsets.UTF_8);

        // A unique ID associated with this message. The unique id is used to
        // authenticate the message in the AES encryption scheme.
        // Typically this will be a hash of the content if it is unique, a unique
        // filename or a database unique key
        byte[] uid = MessageDigest.getInstance("SHA-256").digest(data);

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

        // The encryption policy attributes that will be used to encrypt the content.
        // Attributes must exist in the policy associated with the Public Key
        String encryptionPolicy = "Department::FIN && Security Level::Confidential";
        byte[] ciphertext = coverCrypt.encrypt(policy, publicKey.bytes(), encryptionPolicy, data, uid);

        //
        // Local Decryption
        //

        PrivateKey userKey = kmsClient.retrieveCoverCryptUserDecryptionKey(userKeyId);
        byte[][] decrypted = coverCrypt.decrypt(userKey.bytes(), ciphertext, uid);
        assertArrayEquals(data, decrypted[0]);
        assertArrayEquals(new byte[] {}, decrypted[1]);

        //
        // KMS Decryption
        //
        byte[] data_kms = kmsClient.coverCryptDecrypt(userKeyId, ciphertext, uid);
        assertArrayEquals(data, data_kms);
    }

    @Test
    public void testServerEncryptLocalDecrypt() throws Exception {

        System.out.println("");
        System.out.println("---------------------------------------");
        System.out.println(" Hybrid Crypto Test Server Encrypt + Local Decrypt");
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
        // String encryptionPolicy = "Department::FIN && Security Level::Confidential";
        // TODO Change to encryption policy when ready
        byte[] ciphertext =
            kmsClient.coverCryptEncrypt(publicMasterKeyId, data, new Attr[] {new Attr("Department", "FIN"),
                new Attr("Security Level", "Confidential")}); // , uid);

        //
        // Local Decryption
        //

        // TODO add authentication_data when fixed

        PrivateKey userKey = kmsClient.retrieveCoverCryptUserDecryptionKey(userKeyId);
        byte[][] decrypted = coverCrypt.decrypt(userKey.bytes(), ciphertext); // , uid);
        assertArrayEquals(data, decrypted[0]);
        assertArrayEquals(new byte[] {}, decrypted[1]);

        //
        // KMS Decryption
        //
        byte[] data_kms = kmsClient.coverCryptDecrypt(userKeyId, ciphertext); // , uid);
        assertArrayEquals(data, data_kms);
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
    public void testAccessPolicyToJSON() throws Exception {

        // encrypt
        String accessPolicy = "Department::MKG && ( Country::France || Country::Spain)";
        String json = coverCrypt.booleanAccessPolicyToJson(accessPolicy);
        assertEquals(
            "{\"And\":[{\"Attr\":\"Department::MKG\"},{\"Or\":[{\"Attr\":\"Country::France\"},{\"Attr\":\"Country::Spain\"}]}]}",
            json);
    }

}
