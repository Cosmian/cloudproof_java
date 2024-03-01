package com.cosmian;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Optional;

import org.junit.jupiter.api.Test;

import com.cosmian.jna.covercrypt.CoverCrypt;
import com.cosmian.jna.covercrypt.structs.MasterKeys;
import com.cosmian.jna.covercrypt.structs.Policy;
import com.cosmian.rest.abe.KmsClient;
import com.cosmian.rest.abe.data.DecryptedData;
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

    @Test
    public void testDocDemoKMS() throws CloudproofException {

        if (!TestUtils.serverAvailable(TestUtils.kmsServerUrl())) {
            throw new RuntimeException("Demo: No KMS Server available");
        }

        // Access to the KMS server.
        // Change the Cosmian Server Server URL and API key as appropriate
        final KmsClient kmsClient = new KmsClient(TestUtils.kmsServerUrl(), TestUtils.apiKey());

        // Instantiate a policy; see comments in the policy() method for details
        Policy policy = TestNativeCoverCrypt.policy();

        String[] ids = kmsClient.createCoverCryptMasterKeyPair(policy);
        String privateMasterKeyUniqueIdentifier = ids[0];
        String publicMasterKeyUniqueIdentifier = ids[1];

        // Master Keys can be exported from the KMS
        // export the private master key
        PrivateKey privateMasterKey =
            kmsClient.retrieveCoverCryptPrivateMasterKey(privateMasterKeyUniqueIdentifier);
        /*byte[] privateMasterKeyBytes =*/ privateMasterKey.bytes();
        // export the public key
        PublicKey publicKey =
            kmsClient.retrieveCoverCryptPublicMasterKey(publicMasterKeyUniqueIdentifier);
        /*byte[] _publicKeyBytes =*/ publicKey.bytes();

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
        /*PrivateKey confidentialMkgUserKey_ =*/
            kmsClient.retrieveCoverCryptUserDecryptionKey(confidentialMkgUserKeyUid);
        /*PrivateKey topSecretMkgFinUserKey =*/
            kmsClient.retrieveCoverCryptUserDecryptionKey(topSecretMkgFinUserKeyUid);

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
        // Rekey access policy
        // -------------------------------------------

        // Before rekeying, let us make a local copy of the current
        // `confidential marketing` user to show
        // what happens to non-refreshed keys after the operation.
        PrivateKey oldConfidentialMkgUserKey = kmsClient
            .retrieveCoverCryptUserDecryptionKey(confidentialMkgUserKeyUid);

        // Now rekey all active keys with access to MKG
        kmsClient.rekeyCoverCryptAccessPolicy(privateMasterKeyUniqueIdentifier,
            "Department::MKG");

        // Retrieve the rekeyed public key from the KMS
        PublicKey rekeyedPublicKey = kmsClient
            .retrieveCoverCryptPublicMasterKey(publicMasterKeyUniqueIdentifier);
        // Retrieve the updated policy
        Policy updatedPolicy = Policy.fromAttributes(rekeyedPublicKey.attributes());

        // Creating a new `confidential marketing` message
        byte[] confidentialMkgData = "confidentialMkgMessage".getBytes(StandardCharsets.UTF_8);
        String confidentialMkgEncryptionPolicy = "Department::MKG && Security Level::Confidential";
        byte[] confidentialMkgCT = CoverCrypt.encrypt(updatedPolicy, rekeyedPublicKey.bytes(),
            confidentialMkgEncryptionPolicy, confidentialMkgData, Optional.empty(), Optional.empty());

        // Decrypting the messages with the rekeyed key
        // The automatically rekeyed confidential marketing user key can still decrypt
        // the "old" protected marketing
        // message, as well as the new confidential marketing message.

        // Retrieving the rekeyed `confidential marketing user` decryption key
        PrivateKey rekeyedConfidentialMkgUserKey = kmsClient
            .retrieveCoverCryptUserDecryptionKey(confidentialMkgUserKeyUid);
        assert !Arrays.equals(oldConfidentialMkgUserKey.bytes(), rekeyedConfidentialMkgUserKey.bytes());

        // Decrypting the "old" `protected marketing` message
        DecryptedData protectedMkg__ =
            CoverCrypt.decrypt(rekeyedConfidentialMkgUserKey.bytes(), protectedMkgCT, Optional.empty());
        assert Arrays.equals(protectedMkgData, protectedMkg__.getPlaintext());

        // Decrypting the "new" `confidential marketing` message
        DecryptedData confidentialMkg__ =
            CoverCrypt.decrypt(rekeyedConfidentialMkgUserKey.bytes(), confidentialMkgCT, Optional.empty());
        assert Arrays.equals(confidentialMkgData, confidentialMkg__.getPlaintext());

        // Decrypting the messages with the NON rekeyed key
        // However, the old, non-rekeyed confidential marketing user key can still
        // decrypt the old protected marketing
        // message but not the new confidential marketing message:

        // Decrypting the "old" `protected marketing` message
        DecryptedData protectedMkg___ =
            CoverCrypt.decrypt(oldConfidentialMkgUserKey.bytes(), protectedMkgCT, Optional.empty());
        assert Arrays.equals(protectedMkgData, protectedMkg___.getPlaintext());

        // Decrypting the "new" `confidential marketing` message with the old key fails
        try {
            DecryptedData confidentialMkg___ =
                CoverCrypt.decrypt(oldConfidentialMkgUserKey.bytes(), confidentialMkgCT, Optional.empty());
            assert Arrays.equals(confidentialMkgData, confidentialMkg___.getPlaintext());
            throw new RuntimeException("the message should not be decrypted!");
        } catch (CloudproofException e) {
            // ==> fine, the user is not able to decrypt
        }

        // -------------------------------------------
        // Prune access policy
        // -------------------------------------------

        kmsClient.pruneCoverCryptAccessPolicy(privateMasterKeyUniqueIdentifier, "Department::MKG");

        // Decrypting previous marketing ciphers will no longer be possible.
        try {
            kmsClient.coverCryptDecrypt(confidentialMkgUserKeyUid, protectedMkgCT);
        } catch (CloudproofException e) {
            // ==> fine, the user is not able to decrypt
        }

        // Pruned keys will only be able to decrypt ciphers generated after the last rekey operation.
        confidentialMkg__ =
            kmsClient.coverCryptDecrypt(confidentialMkgUserKeyUid, confidentialMkgCT);
        assert Arrays.equals(confidentialMkgData, confidentialMkg__.getPlaintext());

        // -------------------------------------------
        // Rename attributes
        // -------------------------------------------

        kmsClient.renameCoverCryptAttribute(privateMasterKeyUniqueIdentifier,
            "Department::MKG", "Marketing");

        // Encrypt data with the renamed attribute
        byte[] topSecretMarketingData = "topSecretMarketingMessage".getBytes(StandardCharsets.UTF_8);
        byte[] topSecretMarketingCT = kmsClient.coverCryptEncrypt(publicMasterKeyUniqueIdentifier, topSecretMarketingData,
            "Department::Marketing && Security Level::Top Secret");

        // New "Marketing" message is readable by existing "MKG" user keys
        DecryptedData topSecretMarketing = kmsClient.coverCryptDecrypt(topSecretMkgFinUserKeyUid, topSecretMarketingCT);
        assert Arrays.equals(topSecretMarketingData, topSecretMarketing.getPlaintext());

        // -------------------------------------------
        // Add attributes
        // -------------------------------------------

        kmsClient.addCoverCryptAttribute(privateMasterKeyUniqueIdentifier, "Department::R&D", false);

        // Encrypt a new message for the newly created attribute
        byte[] protectedRdData = "protectedRdMessage".getBytes(StandardCharsets.UTF_8);
        byte[] protectedRdCT = kmsClient.coverCryptEncrypt(publicMasterKeyUniqueIdentifier, protectedRdData,
            "Department::R&D && Security Level::Protected");

        // Create a new user key
        String confidentialRdFinUserKeyUid = kmsClient.createCoverCryptUserDecryptionKey(
            "(Department::R&D || Department::FIN) && Security Level::Confidential",
            privateMasterKeyUniqueIdentifier
        );

        // The new user can decrypt the R&D message
        DecryptedData protectedRd_ = kmsClient.coverCryptDecrypt(confidentialRdFinUserKeyUid, protectedRdCT);
        assert Arrays.equals(protectedRdData, protectedRd_.getPlaintext());

        // -------------------------------------------
        // Disable attributes
        // -------------------------------------------

        kmsClient.disableCoverCryptAttribute(privateMasterKeyUniqueIdentifier, "Department::R&D");

        // Disabled attributes can no longer be used to encrypt data
        // New data encryption for `Department::R&D` will fail
        try {
            kmsClient.coverCryptEncrypt(
                publicMasterKeyUniqueIdentifier,
                protectedRdData,
                "Department::R&D && Security Level::Protected"
            );
        } catch (CloudproofException e) {
            // ==> fine, the user is not able to encrypt
        }

        // Decryption of R&D ciphertext is still possible
        DecryptedData protectedRd__ = kmsClient.coverCryptDecrypt(confidentialRdFinUserKeyUid, protectedRdCT);
        assert Arrays.equals(protectedRdData, protectedRd__.getPlaintext());

        // -------------------------------------------
        // Remove attributes
        // -------------------------------------------

        kmsClient.removeCoverCryptAttribute(privateMasterKeyUniqueIdentifier, "Department::R&D");

        // Removed attributes can no longer be used to encrypt or decrypt
        try {
            kmsClient.coverCryptDecrypt(confidentialRdFinUserKeyUid, protectedRdCT);
        } catch (CloudproofException e) {
            // ==> fine, the user is not able to decrypt
        }
    }

    @Test
    public void testDemoNative() throws CloudproofException {
        // Instantiate a policy; see comments in the policy() method for details
        Policy policy = TestNativeCoverCrypt.policy();

        MasterKeys masterKeys = CoverCrypt.generateMasterKeys(policy);
        byte[] privateMasterKeyBytes = masterKeys.getPrivateKey();
        byte[] publicKeyBytes = masterKeys.getPublicKey();

        byte[] protectedMkgData = "protectedMkgMessage".getBytes(StandardCharsets.UTF_8);
        String protectedMkgEncryptionPolicy = "Department::MKG && Security Level::Protected";
        byte[] protectedMkgCT = CoverCrypt.encrypt(policy, publicKeyBytes, protectedMkgEncryptionPolicy,
            protectedMkgData, Optional.empty(), Optional.empty());

        byte[] topSecretMkgData = "topSecretMkgMessage".getBytes(StandardCharsets.UTF_8);
        String topSecretMkgEncryptionPolicy = "Department::MKG && Security Level::Top Secret";
        byte[] topSecretMkgCT = CoverCrypt.encrypt(policy, publicKeyBytes, topSecretMkgEncryptionPolicy,
            topSecretMkgData, Optional.empty(), Optional.empty());

        byte[] protectedFinData = "protectedFinMessage".getBytes(StandardCharsets.UTF_8);
        String protectedFinEncryptionPolicy = "Department::FIN && Security Level::Protected";
        byte[] protectedFinCT = CoverCrypt.encrypt(policy, publicKeyBytes, protectedFinEncryptionPolicy,
            protectedFinData, Optional.empty(), Optional.empty());

        // the confidential marketing use
        byte[] confidentialMkgUserKey = CoverCrypt.generateUserPrivateKey(
            privateMasterKeyBytes,
            "Department::MKG && Security Level::Confidential",
            policy);

        // the top secret marketing financial user
        byte[] topSecretMkgFinUserKeyUid = CoverCrypt.generateUserPrivateKey(
            privateMasterKeyBytes,
            "(Department::MKG || Department::FIN) && Security Level::Top Secret",
            policy);

        // The confidential marketing user can successfully decrypt a low-security
        // marketing message
        DecryptedData protectedMkg = CoverCrypt.decrypt(confidentialMkgUserKey, protectedMkgCT, Optional.empty());
        assert Arrays.equals(protectedMkgData, protectedMkg.getPlaintext());

        // ... however, it can neither decrypt a marketing message with higher security:
        try {
            CoverCrypt.decrypt(confidentialMkgUserKey, topSecretMkgCT, Optional.empty());
            throw new RuntimeException("the message should not be decrypted!");
        } catch (CloudproofException e) {
            // ==> fine, the user is not able to decrypt
        }

        // ... nor decrypt a message from another department even with a lower security:
        try {
            CoverCrypt.decrypt(confidentialMkgUserKey, protectedFinCT, Optional.empty());
            throw new RuntimeException("the message should not be decrypted!");
        } catch (CloudproofException e) {
            // ==> fine, the user is not able to decrypt
        }

        // As expected, the top-secret marketing financial user can successfully decrypt
        // all messages
        DecryptedData protectedMkg_ = CoverCrypt.decrypt(topSecretMkgFinUserKeyUid, protectedMkgCT, Optional.empty());
        assert Arrays.equals(protectedMkgData, protectedMkg_.getPlaintext());

        DecryptedData topSecretMkg = CoverCrypt.decrypt(topSecretMkgFinUserKeyUid, topSecretMkgCT, Optional.empty());
        assert Arrays.equals(topSecretMkgData, topSecretMkg.getPlaintext());

        DecryptedData protectedFin = CoverCrypt.decrypt(topSecretMkgFinUserKeyUid, protectedFinCT, Optional.empty());
        assert Arrays.equals(protectedFinData, protectedFin.getPlaintext());

    }

}
