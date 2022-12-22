package com.cosmian;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import org.junit.jupiter.api.Test;

import com.cosmian.jna.covercrypt.CoverCrypt;
import com.cosmian.jna.covercrypt.structs.MasterKeys;
import com.cosmian.rest.abe.KmsClient;
import com.cosmian.rest.abe.access_policy.Attr;
import com.cosmian.rest.abe.data.DecryptedData;
import com.cosmian.rest.abe.policy.Policy;
import com.cosmian.rest.kmip.objects.PrivateKey;
import com.cosmian.rest.kmip.objects.PublicKey;
import com.cosmian.utils.CloudproofException;

/**
 * This class contains demos of the Java API. Demos are written as tests so that they can be easily launched from an IDE
 */
public class TestDemo {

    // ## Policy
    // In this demo, we will create a Policy that combines two axes, a
    // `security level` and a `department`. A user will be able to decrypt
    // data only if it possesses a key with a sufficient security level
    // and the code for the department.
    //
    // ## Policy Axes
    // The Policy is defined by two Policy Axes, thus defining a two dimensional
    // matrix of authorizations. A user must possess keys with attributes
    // from these two axes to be able to decrypt files.
    //
    // ### Security Level Axis
    // The first Policy Axis is the 'Security Level' axis and is a
    // hierarchical axis made of 3 levels: Protected, Confidential, and Top Secret.
    // This axis is hierarchical: when a user is granted access to level `n`
    // is automatically granted access to all levels below `n`. The attributes must
    // be provided in ascending order.
    //
    // ### Department Security Axis
    // The second Policy Axis is the Department axis and is made of 3 values:
    // HR, MKG, FIN. This axis is not hierarchical: granting access to an
    // attribute of this axis to a user does not give access to any other
    // attribute. Each attribute must be granted individually.

    static Policy policy() throws CloudproofException {
        return new Policy()
            // Integer.MAX_VALUE is an arbitrary value that represents
            // the maximum number of attributes that can be
            // used in policy
            .addAxis("Security Level",
                new String[] {
                    "Protected",
                    "Confidential",
                    "Top Secret"
                },
                true) // <- hierarchical axis
            .addAxis("Department",
                new String[] {
                    "FIN",
                    "MKG",
                    "HR"
                },
                false); // <- non hierarchical axis
    }

    @Test
    public void testDocDemoKMS() throws CloudproofException {

        if (!TestUtils.serverAvailable(TestUtils.kmsServerUrl())) {
            System.out.println("Demo: No KMS Server available : ignoring");
            return;
        }

        // Access to the KMS server.
        // Change the Cosmian Server Server URL and API key as appropriate
        final KmsClient kmsClient = new KmsClient(TestUtils.kmsServerUrl(), TestUtils.apiKey());

        // Direct access to the native library (used on attributes rotation below)
        final CoverCrypt coverCrypt = new CoverCrypt();

        // Instantiate a policy; see comments in the policy() method for details
        Policy policy = policy();

        String[] ids = kmsClient.createCoverCryptMasterKeyPair(policy);
        String privateMasterKeyUniqueIdentifier = ids[0];
        String publicMasterKeyUniqueIdentifier = ids[1];

        // Master Keys can be exported from the KMS
        // export the private master key
        // PrivateKey privateMasterKey = kmsClient.retrieveCoverCryptPrivateMasterKey(privateMasterKeyUniqueIdentifier);
        // byte[] _privateMasterKeyBytes = privateMasterKey.bytes();
        // export the public key
        // PublicKey publicKey = kmsClient.retrieveCoverCryptPublicMasterKey(publicMasterKeyUniqueIdentifier);
        // byte[] _publicKeyBytes = publicKey.bytes();

        byte[] protectedMkgData = "protectedMkgMessage".getBytes(StandardCharsets.UTF_8);
        String protectedMkgEncryptionPolicy = "Department::MKG && Security Level::Protected";
        byte[] protectedMkgCT = kmsClient.coverCryptEncrypt(
            publicMasterKeyUniqueIdentifier,
            protectedMkgData,
            protectedMkgEncryptionPolicy);

        byte[] topSecretMkgData = "topSecretMkgMessage".getBytes(StandardCharsets.UTF_8);
        String topSecretMkgEncryptionPolicy = "Department::MKG && Security Level::Top Secret";
        byte[] topSecretMkgCT = kmsClient.coverCryptEncrypt(
            publicMasterKeyUniqueIdentifier,
            topSecretMkgData,
            topSecretMkgEncryptionPolicy);

        byte[] protectedFinData = "protectedFinMessage".getBytes(StandardCharsets.UTF_8);
        String protectedFinEncryptionPolicy = "Department::FIN && Security Level::Protected";
        byte[] protectedFinCT = kmsClient.coverCryptEncrypt(
            publicMasterKeyUniqueIdentifier,
            protectedFinData,
            protectedFinEncryptionPolicy);

        // the confidential marketing use
        String confidentialMkgUserKeyUid = kmsClient.createCoverCryptUserDecryptionKey(
            "Department::MKG && Security Level::Confidential",
            privateMasterKeyUniqueIdentifier);

        // the top secret marketing financial user
        String topSecretMkgFinUserKeyUid = kmsClient.createCoverCryptUserDecryptionKey(
            "(Department::MKG || Department::FIN) && Security Level::Top Secret",
            privateMasterKeyUniqueIdentifier);

        // as with the master keys, the user keys can be exported to be used with the
        // native library
        // PrivateKey confidentialMkgUserKey_ =
        // kmsClient.retrieveCoverCryptUserDecryptionKey(confidentialMkgUserKeyUid);
        // PrivateKey topSecretMkgFinUserKey = kmsClient.retrieveCoverCryptUserDecryptionKey(topSecretMkgFinUserKeyUid);

        // The confidential marketing user can successfully decrypt a low-security
        // marketing message
        DecryptedData protectedMkg = kmsClient.coverCryptDecrypt(confidentialMkgUserKeyUid, protectedMkgCT);
        assert Arrays.equals(protectedMkgData, protectedMkg.getPlaintext());

        // ... however, it can neither decrypt a marketing message with higher security:
        try {
            kmsClient.coverCryptDecrypt(confidentialMkgUserKeyUid, topSecretMkgCT);
            throw new RuntimeException("the message should not be decrypted!");
        } catch (CloudproofException e) {
            // ==> fine, the user is not able to decrypt
        }

        // ... nor decrypt a message from another department even with a lower security:
        try {
            kmsClient.coverCryptDecrypt(confidentialMkgUserKeyUid, protectedFinCT);
            throw new RuntimeException("the message should not be decrypted!");
        } catch (CloudproofException e) {
            // ==> fine, the user is not able to decrypt
        }

        // As expected, the top-secret marketing financial user can successfully decrypt
        // all messages
        DecryptedData protectedMkg_ = kmsClient.coverCryptDecrypt(topSecretMkgFinUserKeyUid, protectedMkgCT);
        assert Arrays.equals(protectedMkgData, protectedMkg_.getPlaintext());

        DecryptedData topSecretMkg_ = kmsClient.coverCryptDecrypt(topSecretMkgFinUserKeyUid, topSecretMkgCT);
        assert Arrays.equals(topSecretMkgData, topSecretMkg_.getPlaintext());

        DecryptedData protectedFin_ = kmsClient.coverCryptDecrypt(topSecretMkgFinUserKeyUid, protectedFinCT);
        assert Arrays.equals(protectedFinData, protectedFin_.getPlaintext());

        // -------------------------------------------
        // Attributes rotation
        // -------------------------------------------

        // Before rotating attributes, let us make a local copy of the current
        // `confidential marketing` user to show
        // what happens to non-refreshed keys after the attribute rotation.
        PrivateKey oldConfidentialMkgUserKey = kmsClient.retrieveCoverCryptUserDecryptionKey(confidentialMkgUserKeyUid);

        // Now rotate the MKG attribute - all active keys will be rekeyed
        kmsClient.rotateCoverCryptAttributes(privateMasterKeyUniqueIdentifier,
            new Attr[] {new Attr("Department", "MKG")});

        // Retrieve the rekeyed public key from the KMS
        PublicKey rekeyedPublicKey = kmsClient.retrieveCoverCryptPublicMasterKey(publicMasterKeyUniqueIdentifier);
        // Retrieve the updated policy
        Policy updatedPolicy = Policy.fromAttributes(rekeyedPublicKey.attributes());

        // Creating a new `confidential marketing` message
        byte[] confidentialMkgData = "confidentialMkgMessage".getBytes(StandardCharsets.UTF_8);
        String confidentialMkgEncryptionPolicy = "Department::MKG && Security Level::Confidential";
        byte[] confidentialMkgCT = coverCrypt.encrypt(
            updatedPolicy,
            rekeyedPublicKey.bytes(),
            confidentialMkgEncryptionPolicy,
            confidentialMkgData);

        // Decrypting the messages with the rekeyed key
        // The automatically rekeyed confidential marketing user key can still decrypt
        // the “old” protected marketing
        // message, as well as the new confidential marketing message.

        // Retrieving the rekeyed `confidential marketing user` decryption key
        PrivateKey rekeyedConfidentialMkgUserKey = kmsClient
            .retrieveCoverCryptUserDecryptionKey(confidentialMkgUserKeyUid);
        assert !Arrays.equals(oldConfidentialMkgUserKey.bytes(), rekeyedConfidentialMkgUserKey.bytes());

        // Decrypting the "old" `protected marketing` message
        DecryptedData protectedMkg__ = coverCrypt.decrypt(
            rekeyedConfidentialMkgUserKey.bytes(),
            protectedMkgCT);
        assert Arrays.equals(protectedMkgData, protectedMkg__.getPlaintext());

        // Decrypting the "new" `confidential marketing` message
        DecryptedData confidentialMkg__ = coverCrypt.decrypt(
            rekeyedConfidentialMkgUserKey.bytes(),
            confidentialMkgCT);
        assert Arrays.equals(confidentialMkgData, confidentialMkg__.getPlaintext());

        // Decrypting the messages with the NON rekeyed key
        // However, the old, non-rekeyed confidential marketing user key can still
        // decrypt the old protected marketing
        // message but not the new confidential marketing message:

        // Decrypting the "old" `protected marketing` message
        DecryptedData protectedMkg___ = coverCrypt.decrypt(
            oldConfidentialMkgUserKey.bytes(),
            protectedMkgCT);
        assert Arrays.equals(protectedMkgData, protectedMkg___.getPlaintext());

        // Decrypting the "new" `confidential marketing` message with the old key fails
        try {
            DecryptedData confidentialMkg___ = coverCrypt.decrypt(
                oldConfidentialMkgUserKey.bytes(),
                confidentialMkgCT);
            assert Arrays.equals(confidentialMkgData, confidentialMkg___.getPlaintext());
            throw new RuntimeException("the message should not be decrypted!");
        } catch (CloudproofException e) {
            // ==> fine, the user is not able to decrypt
        }

    }

    @Test
    public void testDemoNative() throws CloudproofException {

        // Direct access to the native library
        final CoverCrypt coverCrypt = new CoverCrypt();

        // Instantiate a policy; see comments in the policy() method for details
        Policy policy = policy();

        MasterKeys masterKeys = coverCrypt.generateMasterKeys(policy);
        byte[] privateMasterKeyBytes = masterKeys.getPrivateKey();
        byte[] publicKeyBytes = masterKeys.getPublicKey();

        byte[] protectedMkgData = "protectedMkgMessage".getBytes(StandardCharsets.UTF_8);
        String protectedMkgEncryptionPolicy = "Department::MKG && Security Level::Protected";
        byte[] protectedMkgCT = coverCrypt.encrypt(
            policy,
            publicKeyBytes,
            protectedMkgEncryptionPolicy,
            protectedMkgData);

        byte[] topSecretMkgData = "topSecretMkgMessage".getBytes(StandardCharsets.UTF_8);
        String topSecretMkgEncryptionPolicy = "Department::MKG && Security Level::Top Secret";
        byte[] topSecretMkgCT = coverCrypt.encrypt(
            policy,
            publicKeyBytes,
            topSecretMkgEncryptionPolicy,
            topSecretMkgData);

        byte[] protectedFinData = "protectedFinMessage".getBytes(StandardCharsets.UTF_8);
        String protectedFinEncryptionPolicy = "Department::FIN && Security Level::Protected";
        byte[] protectedFinCT = coverCrypt.encrypt(
            policy,
            publicKeyBytes,
            protectedFinEncryptionPolicy,
            protectedFinData);

        // the confidential marketing use
        byte[] confidentialMkgUserKey = coverCrypt.generateUserPrivateKey(
            privateMasterKeyBytes,
            "Department::MKG && Security Level::Confidential",
            policy);

        // the top secret marketing financial user
        byte[] topSecretMkgFinUserKeyUid = coverCrypt.generateUserPrivateKey(
            privateMasterKeyBytes,
            "(Department::MKG || Department::FIN) && Security Level::Top Secret",
            policy);

        // The confidential marketing user can successfully decrypt a low-security marketing message
        DecryptedData protectedMkg = coverCrypt.decrypt(confidentialMkgUserKey, protectedMkgCT);
        assert Arrays.equals(protectedMkgData, protectedMkg.getPlaintext());

        // ... however, it can neither decrypt a marketing message with higher security:
        try {
            coverCrypt.decrypt(confidentialMkgUserKey, topSecretMkgCT);
            throw new RuntimeException("the message should not be decrypted!");
        } catch (CloudproofException e) {
            // ==> fine, the user is not able to decrypt
        }

        // ... nor decrypt a message from another department even with a lower security:
        try {
            coverCrypt.decrypt(confidentialMkgUserKey, protectedFinCT);
            throw new RuntimeException("the message should not be decrypted!");
        } catch (CloudproofException e) {
            // ==> fine, the user is not able to decrypt
        }

        // As expected, the top-secret marketing financial user can successfully decrypt all messages
        DecryptedData protectedMkg_ = coverCrypt.decrypt(topSecretMkgFinUserKeyUid, protectedMkgCT);
        assert Arrays.equals(protectedMkgData, protectedMkg_.getPlaintext());

        DecryptedData topSecretMkg = coverCrypt.decrypt(topSecretMkgFinUserKeyUid, topSecretMkgCT);
        assert Arrays.equals(topSecretMkgData, topSecretMkg.getPlaintext());

        DecryptedData protectedFin = coverCrypt.decrypt(topSecretMkgFinUserKeyUid, protectedFinCT);
        assert Arrays.equals(protectedFinData, protectedFin.getPlaintext());

    }

}
