package com.cosmian;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.Optional;
import java.util.logging.Logger;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.cosmian.jna.covercrypt.CoverCrypt;
import com.cosmian.jna.covercrypt.structs.Policy;
import com.cosmian.rest.abe.KmsClient;
import com.cosmian.rest.abe.data.DecryptedData;
import com.cosmian.rest.kmip.data_structures.KeyValue;
import com.cosmian.rest.kmip.objects.PrivateKey;
import com.cosmian.rest.kmip.objects.PublicKey;
import com.cosmian.rest.kmip.types.CryptographicAlgorithm;
import com.cosmian.rest.kmip.types.KeyFormatType;
import com.cosmian.rest.kmip.types.ObjectType;
import com.cosmian.rest.kmip.types.VendorAttribute;
import com.cosmian.utils.CloudproofException;

public class TestKmsCoverCrypt {

    private static final Logger logger = Logger.getLogger(TestKmsCoverCrypt.class.getName());

    @BeforeAll
    public static void before_all() {
        TestUtils.initLogging();
    }

    private String accessPolicyProtected() throws CloudproofException {
        return "(Department::FIN || Department::MKG) && Security Level::Protected";
    }

    private String accessPolicyConfidential() throws CloudproofException {
        return "Department::FIN && Security Level::Confidential";
    }

    @Test
    public void testKeysImportExport() throws Exception {

        if (!TestUtils.serverAvailable(TestUtils.kmsServerUrl())) {
            throw new RuntimeException("No KMS Server available");
        }

        Policy pg = TestNativeCoverCrypt.policy();

        KmsClient kmsClient = new KmsClient(TestUtils.kmsServerUrl(), TestUtils.apiKey());

        String[] ids = kmsClient.createCoverCryptMasterKeyPair(pg);
        logger.info("Created Master Key: Private Key ID: " + ids[0] + ", Public Key ID: " + ids[1]);

        String privateMasterKeyUniqueIdentifier = ids[0];
        PrivateKey privateMasterKey = kmsClient
            .retrieveCoverCryptPrivateMasterKey(privateMasterKeyUniqueIdentifier);
        assertEquals(KeyFormatType.CoverCryptSecretKey, privateMasterKey.getKeyBlock().getKeyFormatType());
        assertEquals(CryptographicAlgorithm.CoverCrypt,
            privateMasterKey.getKeyBlock().getCryptographicAlgorithm());

        String publicMasterKeyUniqueIdentifier = ids[1];
        PublicKey publicMasterKey = kmsClient
            .retrieveCoverCryptPublicMasterKey(publicMasterKeyUniqueIdentifier);
        assertEquals(KeyFormatType.CoverCryptPublicKey, publicMasterKey.getKeyBlock().getKeyFormatType());
        assertEquals(CryptographicAlgorithm.CoverCrypt,
            publicMasterKey.getKeyBlock().getCryptographicAlgorithm());

        // try reimporting the public key with the same ID
        try {
            kmsClient.importCoverCryptPublicMasterKey(publicMasterKeyUniqueIdentifier, publicMasterKey,
                false);
        } catch (CloudproofException e) {
            // expected cannot re-insert with the same id if replaceExisting is false
        } catch (Exception e) {
            throw e;
        }
        // allow overwrite
        String publicMasterKeyUniqueIdentifier_ = kmsClient.importCoverCryptPublicMasterKey(
            publicMasterKeyUniqueIdentifier,
            publicMasterKey, true);
        logger.info("Imported Public Key with id: " + publicMasterKeyUniqueIdentifier_);
        // retrieve it again
        PublicKey publicMasterKey_ = kmsClient
            .retrieveCoverCryptPublicMasterKey(publicMasterKeyUniqueIdentifier);
        assertEquals(ObjectType.Public_Key, publicMasterKey_.getObjectType());
        assertEquals(KeyFormatType.CoverCryptPublicKey, publicMasterKey_.getKeyBlock().getKeyFormatType());
        assertEquals(CryptographicAlgorithm.CoverCrypt,
            publicMasterKey_.getKeyBlock().getCryptographicAlgorithm());

        // User decryption key
        String userDecryptionKeyUniqueIdentifier = kmsClient.createCoverCryptUserDecryptionKey(
            accessPolicyProtected(),
            privateMasterKeyUniqueIdentifier);
        logger.info("Created User Decryption Key with id: " + userDecryptionKeyUniqueIdentifier);
        // ... retrieve it
        PrivateKey userDecryptionKey = kmsClient
            .retrieveCoverCryptUserDecryptionKey(userDecryptionKeyUniqueIdentifier);
        assertEquals(KeyFormatType.CoverCryptSecretKey, userDecryptionKey.getKeyBlock().getKeyFormatType());
        assertEquals(CryptographicAlgorithm.CoverCrypt,
            userDecryptionKey.getKeyBlock().getCryptographicAlgorithm());
        KeyValue keyValue = (KeyValue) userDecryptionKey.getKeyBlock().getKeyValue();
        VendorAttribute[] vendorAttributes = keyValue.getAttributes().get().getVendorAttributes().get();
        // TODO better check on Vendor Attributes
        logger.info(() -> Arrays.asList(vendorAttributes).toString());

    }

    @Test
    public void testKmsEncryptDecrypt() throws Exception {

        if (!TestUtils.serverAvailable(TestUtils.kmsServerUrl())) {
            throw new RuntimeException("No KMS Server available");
        }

        Policy pg = TestNativeCoverCrypt.policy();

        KmsClient kmsClient = new KmsClient(TestUtils.kmsServerUrl(), TestUtils.apiKey());

        String[] ids = kmsClient.createCoverCryptMasterKeyPair(pg);
        logger.info("Created Master Key: Private Key ID: " + ids[0] + ", Public Key ID: " + ids[1]);

        String privateMasterKeyID = ids[0];
        PrivateKey privateMasterKey = kmsClient.retrieveCoverCryptPrivateMasterKey(privateMasterKeyID);
        assertEquals(KeyFormatType.CoverCryptSecretKey, privateMasterKey.getKeyBlock().getKeyFormatType());
        assertEquals(CryptographicAlgorithm.CoverCrypt,
            privateMasterKey.getKeyBlock().getCryptographicAlgorithm());
        TestUtils.writeResource("cover_crypt/private_master_key.json",
            privateMasterKey.toJson().getBytes(StandardCharsets.UTF_8));

        String publicMasterKeyUniqueIdentifier = ids[1];
        PublicKey publicMasterKey = kmsClient
            .retrieveCoverCryptPublicMasterKey(publicMasterKeyUniqueIdentifier);
        assertEquals(KeyFormatType.CoverCryptPublicKey, publicMasterKey.getKeyBlock().getKeyFormatType());
        assertEquals(CryptographicAlgorithm.CoverCrypt,
            publicMasterKey.getKeyBlock().getCryptographicAlgorithm());
        TestUtils.writeResource("cover_crypt/public_master_key.json",
            publicMasterKey.toJson().getBytes(StandardCharsets.UTF_8));

        // encryption
        String protected_fin_data = "protected_fin_attributes";
        String protected_fin_enc_policy = "Department::FIN && Security Level::Protected";
        byte[] protected_fin_ct = kmsClient.coverCryptEncrypt(publicMasterKeyUniqueIdentifier,
            protected_fin_data.getBytes(StandardCharsets.UTF_8), protected_fin_enc_policy);

        String confidential_fin_data = "confidential_fin_attributes";
        String confidential_fin_enc_policy = "Department::FIN && Security Level::Confidential";
        byte[] confidential_fin_ct = kmsClient.coverCryptEncrypt(publicMasterKeyUniqueIdentifier,
            confidential_fin_data.getBytes(StandardCharsets.UTF_8), confidential_fin_enc_policy);

        // User decryption key Protected, FIN, MKG
        String fin_mkg_protected_user_key = kmsClient.createCoverCryptUserDecryptionKey(accessPolicyProtected(),
            privateMasterKeyID);
        PrivateKey userKey_1 = kmsClient.retrieveCoverCryptUserDecryptionKey(fin_mkg_protected_user_key);
        TestUtils.writeResource("cover_crypt/fin_mkg_protected_user_key.json",
            userKey_1.toJson().getBytes(StandardCharsets.UTF_8));

        // User decryption key Confidential, FIN
        String fin_confidential_user_key = kmsClient.createCoverCryptUserDecryptionKey(
            accessPolicyConfidential(),
            privateMasterKeyID);
        PrivateKey userKey_2 = kmsClient.retrieveCoverCryptUserDecryptionKey(fin_confidential_user_key);
        TestUtils.writeResource("cover_crypt/fin_confidential_user_key.json",
            userKey_2.toJson().getBytes(StandardCharsets.UTF_8));

        // User decryption key Protected should be able to decrypt protected_fin_ct
        String plaintext_1_1 = new String(
            kmsClient.coverCryptDecrypt(fin_mkg_protected_user_key, protected_fin_ct)
                .getPlaintext(),
            StandardCharsets.UTF_8);
        assertEquals(protected_fin_data, plaintext_1_1);
        // User decryption key Confidential should be able to decrypt protected_fin_ct
        String plaintext_1_2 = new String(
            kmsClient.coverCryptDecrypt(fin_confidential_user_key, protected_fin_ct)
                .getPlaintext(),
            StandardCharsets.UTF_8);
        assertEquals(protected_fin_data, plaintext_1_2);

        // User decryption key Protected should not be able to decrypt
        // confidential_fin_ct
        try {
            new String(kmsClient.coverCryptDecrypt(fin_mkg_protected_user_key, confidential_fin_ct)
                .getPlaintext(), StandardCharsets.UTF_8);
            throw new RuntimeException(
                "User with key Confidential should not be able to decrypt data Confidential");
        } catch (CloudproofException e) {
            // fine: should not be able to decrypt
        }

        // User decryption key Confidential should not be able to decrypt
        // confidential_fin_ct
        String plaintext_2_2 = new String(
            kmsClient.coverCryptDecrypt(fin_confidential_user_key, confidential_fin_ct)
                .getPlaintext(),
            StandardCharsets.UTF_8);
        assertEquals(confidential_fin_data, plaintext_2_2);
    }

    @Test
    public void testKmsEncryptDecryptMetaData() throws Exception {

        if (!TestUtils.serverAvailable(TestUtils.kmsServerUrl())) {
            throw new RuntimeException("No KMS Server available");
        }

        Policy pg = TestNativeCoverCrypt.policy();

        KmsClient kmsClient = new KmsClient(TestUtils.kmsServerUrl(), TestUtils.apiKey());

        String[] ids = kmsClient.createCoverCryptMasterKeyPair(pg);
        logger.info("Created Master Key: Private Key ID: " + ids[0] + ", Public Key ID: " + ids[1]);
        String privateMasterKeyID = ids[0];
        String publicMasterKeyUniqueIdentifier = ids[1];

        // encryption
        byte[] protected_fin_data = "protected_fin_attributes".getBytes(StandardCharsets.UTF_8);
        String protected_fin_enc_policy = "Department::FIN && Security Level::Protected";
        byte[] authenticationData = "authentication".getBytes(StandardCharsets.UTF_8);
        byte[] headerMetaData = "headerMeta".getBytes(StandardCharsets.UTF_8);
        byte[] protected_fin_ct = kmsClient.coverCryptEncrypt(publicMasterKeyUniqueIdentifier,
            protected_fin_data, protected_fin_enc_policy, authenticationData, headerMetaData);

        // User decryption key Protected, FIN, MKG
        String fin_mkg_protected_user_key = kmsClient.createCoverCryptUserDecryptionKey(accessPolicyProtected(),
            privateMasterKeyID);
        PrivateKey userKey_1 = kmsClient.retrieveCoverCryptUserDecryptionKey(fin_mkg_protected_user_key);
        TestUtils.writeResource("cover_crypt/fin_mkg_protected_user_key.json",
            userKey_1.toJson().getBytes(StandardCharsets.UTF_8));

        // User decryption key Protected should be able to decrypt protected_fin_ct
        DecryptedData decryptedData = kmsClient.coverCryptDecrypt(fin_mkg_protected_user_key, protected_fin_ct,
            authenticationData);
        byte[] plaintext_ = decryptedData.getPlaintext();
        byte[] headerMetadata_ = decryptedData.getHeaderMetaData();
        assertArrayEquals(protected_fin_data, plaintext_);
        assertArrayEquals(headerMetaData, headerMetadata_);
    }

    @Test
    public void testLocalEncryptServerDecrypt() throws Exception {

        System.out.println("");
        System.out.println("---------------------------------------");
        System.out.println(" Hybrid Crypto Test Local Encrypt + Server Decrypt");
        System.out.println("---------------------------------------");
        System.out.println("");

        if (!TestUtils.serverAvailable(TestUtils.kmsServerUrl())) {
            throw new RuntimeException("No KMS Server available");
        }

        // The data we want to encrypt/decrypt
        byte[] plaintext = "This is a test message".getBytes(StandardCharsets.UTF_8);

        // A unique ID associated with this message. The unique id is used to
        // authenticate the message in the AES encryption scheme.
        // Typically this will be a hash of the content if it is unique, a unique
        // filename or a database unique key
        byte[] uid = MessageDigest.getInstance("SHA-256").digest(plaintext);

        Policy policy = TestNativeCoverCrypt.policy();

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
        byte[] ciphertext = CoverCrypt.encrypt(policy, publicKey.bytes(), encryptionPolicy, plaintext, Optional.of(uid),
            Optional.empty());

        //
        // Local Decryption
        //

        System.out.println("Local Decryption");

        PrivateKey userKey = kmsClient.retrieveCoverCryptUserDecryptionKey(userKeyId);
        DecryptedData res = CoverCrypt.decrypt(userKey.bytes(), ciphertext, Optional.of(uid));
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
            throw new RuntimeException("No KMS Server available");
        }

        // The data we want to encrypt/decrypt
        byte[] plaintext = new byte[] {1, 2, 3, 4, 5, 6};

        // A unique ID associated with this message. The unique id is used to
        // authenticate the message in the AES encryption scheme.
        // Typically this will be a hash of the content if it is unique, a unique
        // filename or a database unique key
        byte[] uid = MessageDigest.getInstance("SHA-256").digest(plaintext);

        Policy policy = TestNativeCoverCrypt.policy();

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
        DecryptedData res = CoverCrypt.decrypt(userKey.bytes(), ciphertext, Optional.of(uid));
        assertArrayEquals(plaintext, res.getPlaintext());
        assertArrayEquals(new byte[] {}, res.getHeaderMetaData());

    }
}
