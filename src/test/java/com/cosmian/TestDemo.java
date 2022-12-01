package com.cosmian;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import org.junit.jupiter.api.Test;

import com.cosmian.jna.abe.CoverCrypt;
import com.cosmian.rest.abe.KmsClient;
import com.cosmian.rest.abe.access_policy.AccessPolicy;
import com.cosmian.rest.abe.access_policy.And;
import com.cosmian.rest.abe.access_policy.Attr;
import com.cosmian.rest.abe.access_policy.Or;
import com.cosmian.rest.abe.data.DecryptedData;
import com.cosmian.rest.abe.policy.Policy;
import com.cosmian.rest.kmip.objects.PrivateKey;
import com.cosmian.rest.kmip.objects.PublicKey;

/**
 * This class contains demos of the Java API. Demos are written as tests so that
 * they can be easily launched from an IDE
 */
public class TestDemo {

    /**
     * <b>ABE</b>: Encryption using an Authorization Policy<br/>
     * <br/>
     * This demo demonstrates how data can be encrypted with policy attributes. An
     * user will only be able to decrypt
     * data when it holds a key with the proper attributes. <br/>
     * <br/>
     * This demo also demonstrates revocation of an attribute value and how to
     * implement forward secrecy.
     */
    @Test
    public void testCoverCrypt() throws Exception {

        if (!TestUtils.serverAvailable(TestUtils.kmsServerUrl())) {
            System.out.println("Demo: No KMS Server: ignoring");
            return;
        }

        // Change the Cosmian Server Server URL and API key as appropriate
        KmsClient kmsClient = new KmsClient(TestUtils.kmsServerUrl(), TestUtils.apiKey());

        // ## Policy
        // In this demo, we will create a Policy which combines two axes, a
        // 'security level' and a 'department'. An user will be able to decrypt
        // data only if it possesses a key with a sufficient security level
        // and the code for the department.
        //
        // The parameter fixes the maximum number of revocations of attributes (see
        // below) for this Policy. This number influences the number of
        // public keys which will be ultimately generated for this Security Group
        // and must be kept to a "reasonable" level to reduce security risks associated
        // with multiplying the number of keys.
        //
        // ## Policy Axes
        // The Policy is defined by two Policy Axes, thus defining a 2 dimensional
        // matrix of authorizations. An user must possess keys with attributes
        // from these two axes to be able to decrypt files.
        //
        // ### Security Level Axis
        // The first Policy Axis is the 'Security Level' axis and is a
        // hierarchical axis made of 5 levels: Protected, Low Secret , ...,
        // Top Secret. It is hierarchical: a user being granted access to level `n`
        // is automatically granted access to all levels below `n`. The attributes must
        // be provided in ascending order.
        //
        // ### Department Security Axis
        // The second Policy Axis is the Department axis and is made of 4 values: R&D,
        // HR, MKG, FIN. This axis is not hierarchical: granting access to an
        // attribute of this axis to a user does not give access to any other
        // attribute. Each attribute must be granted individually.
        Policy policy = new Policy(20)
                .addAxis("Security Level",
                        new String[] {
                                "Protected",
                                "Low Secret",
                                "Medium Secret",
                                "Top Secret",
                        },
                        true)
                .addAxis("Department", new String[] {
                        "R&D",
                        "HR",
                        "MKG",
                        "FIN"
                }, false);

        // ## Master Authority
        // The Master Authority possesses the keys for the given Policy:
        // a Private Key which is used to generate user keys and a Public Key which is
        // used to encrypt files with proper level of security.
        // The call returns the KMS UIDs of the 2 keys
        String[] ids = kmsClient.createCoverCryptMasterKeyPair(policy);
        String privateMasterKeyUID = ids[0];
        String publicMasterKeyUID = ids[1];

        // ## Encryption and Decryption
        // Data is encrypted using the Master Authority Public Key with two attributes:
        // one for the Security Level and one for the Department.
        //
        // Anyone who has access to the Public Key, can encrypt data with any
        // attribute combination. However, only users possessing user keys with
        // the right access policy can decrypt data.

        // Let us create 3 encrypted messages
        // - a low secret marketing message
        byte[] lowSecretMkgData = "lowSecretMkgMessage".getBytes(StandardCharsets.UTF_8);
        String lowSecretMkgDnc_policy = "Department::MKG && Security Level::Low Secret";
        byte[] lowSecretMkgDt = kmsClient.coverCryptEncrypt(publicMasterKeyUID, lowSecretMkgData,
                lowSecretMkgDnc_policy);

        // - a top secret marketing message
        byte[] top_secret_mkg_data = "top_secret_mkg_message".getBytes(StandardCharsets.UTF_8);
        String top_secret_mkg_enc_policy = "Department::MKG && Security Level::Top Secret";
        byte[] top_secret_mkg_ct = kmsClient.coverCryptEncrypt(publicMasterKeyUID, top_secret_mkg_data,
                top_secret_mkg_enc_policy);

        // - and a low secret finance message
        byte[] low_secret_fin_data = "low_secret_fin_message".getBytes(StandardCharsets.UTF_8);
        String low_secret_fin_enc_policy = "Department::FIN && Security Level::Low Secret";
        byte[] low_secret_fin_ct = kmsClient.coverCryptEncrypt(publicMasterKeyUID, low_secret_fin_data,
                low_secret_fin_enc_policy);

        // ## User Decryption Keys
        // User Decryption Keys are generated from the Master Private Key using Access
        // Policies.
        // Access Policies are monotonous boolean expressions combining attributes.

        // Let us create 2 users with their different access policies to illustrate
        // their effect.

        // ### The medium secret marketing user
        // This user can decrypt messages from the marketing department only with a
        // security level of Medium Secret or below
        AccessPolicy medium_secret_mkg_access = new And(
                new Attr("Department", "MKG"),
                new Attr("Security Level", "Medium Secret"));
        String medium_secret_mkg_user_key_uid = kmsClient.createCoverCryptUserDecryptionKey(medium_secret_mkg_access,
                privateMasterKeyUID);

        // The medium secret marketing user can successfully decrypt a low security
        // marketing message
        assertArrayEquals(lowSecretMkgData,
                kmsClient.coverCryptDecrypt(medium_secret_mkg_user_key_uid, lowSecretMkgDt).getPlaintext());
        // ... however it can neither decrypt a marketing message with higher security
        try {
            kmsClient.coverCryptDecrypt(
                    medium_secret_mkg_user_key_uid,
                    top_secret_mkg_ct);
            throw new RuntimeException("Oh... something is wrong !");
        } catch (CloudproofException e) {
            // fine: the user is not be able to decrypt
        }
        // ... nor decrypt a message from another department even with a lower security
        try {
            kmsClient.coverCryptDecrypt(
                    medium_secret_mkg_user_key_uid,
                    low_secret_fin_ct);
            throw new RuntimeException("Oh... something is wrong !");
        } catch (CloudproofException e) {
            // fine: the user is not be able to decrypt
        }

        // ### The top secret marketing financial user
        // This user can decrypt messages from the marketing department OR the financial
        // department that have a security level of Top Secret or below
        AccessPolicy top_secret_mkg_fin_access = new And(
                new Or(new Attr("Department", "MKG"), new Attr("Department", "FIN")),
                new Attr("Security Level", "Top Secret"));
        String top_secret_mkg_fin_user_key_uid = kmsClient.createCoverCryptUserDecryptionKey(top_secret_mkg_fin_access,
                privateMasterKeyUID);
        // As expected, the top secret marketing financial user can successfully decrypt
        // all messages
        assertArrayEquals(lowSecretMkgData,
                kmsClient.coverCryptDecrypt(
                        top_secret_mkg_fin_user_key_uid,
                        lowSecretMkgDt).getPlaintext());
        assertArrayEquals(top_secret_mkg_data,
                kmsClient.coverCryptDecrypt(
                        top_secret_mkg_fin_user_key_uid,
                        top_secret_mkg_ct).getPlaintext());
        assertArrayEquals(low_secret_fin_data,
                kmsClient.coverCryptDecrypt(
                        top_secret_mkg_fin_user_key_uid,
                        low_secret_fin_ct).getPlaintext());

        // ## Revocation
        // At anytime the Master Authority can revoke an attribute.
        // When that happens future encryption of data for a given attribute cannot be
        // decrypted with keys which are not "refreshed" for that attribute. As long as
        // a key is active in the KMS (the key has not been revoked), it will be
        // automatically rekeyed when an attribute is revoked.

        // Before revoking the MKG attribute, let us make a local copy of the
        // medium_secret_mkg_user_key_uid
        PrivateKey original_medium_secret_mkg_user_key = kmsClient
                .retrieveCoverCryptUserDecryptionKey(medium_secret_mkg_user_key_uid);

        // Now revoke the MKG attribute
        kmsClient.rotateCoverCryptAttributes(privateMasterKeyUID, new Attr[] { new Attr("Department", "MKG") });

        // ... and reimport the non rekeyed original medium secret marketing user key
        // under a new UID
        kmsClient.importCoverCryptUserDecryptionKey("original_medium_secret_mkg_user_key_uid",
                original_medium_secret_mkg_user_key,
                true);

        // finally let us create a new medium secret marketing message
        byte[] medium_secret_mkg_data = "medium_secret_mkg_message".getBytes(StandardCharsets.UTF_8);
        String medium_secret_mkg_enc_policy = "Department::MKG && Security Level::Low Secret";
        byte[] medium_secret_mkg_ct = kmsClient.coverCryptEncrypt(publicMasterKeyUID, medium_secret_mkg_data,
                medium_secret_mkg_enc_policy);

        // The automatically rekeyed medium secret marketing user key can still decrypt
        // the low secret marketing message
        assertArrayEquals(lowSecretMkgData,
                kmsClient.coverCryptDecrypt(medium_secret_mkg_user_key_uid, lowSecretMkgDt).getPlaintext());
        // ... as well as the new medium secret marketing message
        assertArrayEquals(medium_secret_mkg_data,
                kmsClient.coverCryptDecrypt(medium_secret_mkg_user_key_uid, medium_secret_mkg_ct).getPlaintext());

        // Likewise, the top secret marketing financial user can decrypt all messages
        // ... old
        assertArrayEquals(lowSecretMkgData,
                kmsClient.coverCryptDecrypt(
                        top_secret_mkg_fin_user_key_uid,
                        lowSecretMkgDt).getPlaintext());
        assertArrayEquals(top_secret_mkg_data,
                kmsClient.coverCryptDecrypt(
                        top_secret_mkg_fin_user_key_uid,
                        top_secret_mkg_ct).getPlaintext());
        assertArrayEquals(low_secret_fin_data,
                kmsClient.coverCryptDecrypt(
                        top_secret_mkg_fin_user_key_uid,
                        low_secret_fin_ct).getPlaintext());
        // ..and new
        assertArrayEquals(medium_secret_mkg_data,
                kmsClient.coverCryptDecrypt(
                        top_secret_mkg_fin_user_key_uid,
                        medium_secret_mkg_ct).getPlaintext());

        // However, the old, non rekeyed medium secret marketing user key
        // ...can still decrypt the old low secret marketing message
        assertArrayEquals(lowSecretMkgData,
                kmsClient.coverCryptDecrypt(
                        "original_medium_secret_mkg_user_key_uid",
                        lowSecretMkgDt).getPlaintext());
        // ... but NOT the new medium secret marketing message

        try {
            kmsClient.coverCryptDecrypt(
                    "original_medium_secret_mkg_user_key_uid",
                    medium_secret_mkg_ct);
            throw new RuntimeException("Oh... something is wrong !");
        } catch (CloudproofException e) {
            // fine: the user is not be able to decrypt
        }
    }

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
    public void testDocDemo() throws CloudproofException {

        // Change the Cosmian Server Server URL and API key as appropriate
        final KmsClient kmsClient = new KmsClient(TestUtils.kmsServerUrl(), TestUtils.apiKey());

        // direct access to the native library
        final CoverCrypt coverCrypt = new CoverCrypt();

        Policy policy = policy();

        String[] ids = kmsClient.createCoverCryptMasterKeyPair(policy);
        String privateMasterKeyUniqueIdentifier = ids[0];
        String publicMasterKeyUniqueIdentifier = ids[1];

        // export the private master key
        PrivateKey privateMasterKey = kmsClient.retrieveCoverCryptPrivateMasterKey(privateMasterKeyUniqueIdentifier);
        byte[] _privateMasterKeyBytes = privateMasterKey.bytes();

        // export the public key
        PublicKey publicKey = kmsClient.retrieveCoverCryptPublicMasterKey(publicMasterKeyUniqueIdentifier);
        byte[] _publicKeyBytes = publicKey.bytes();

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
        PrivateKey _confidentialMkgUserKey = kmsClient.retrieveCoverCryptUserDecryptionKey(confidentialMkgUserKeyUid);
        PrivateKey _topSecretMkgFinUserKey = kmsClient.retrieveCoverCryptUserDecryptionKey(topSecretMkgFinUserKeyUid);

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
                new Attr[] { new Attr("Department", "MKG") });

        // Retrieve the rekeyed public key from the KMS
        PublicKey rekeyedPublicKey = kmsClient.retrieveCoverCryptPublicMasterKey(publicMasterKeyUniqueIdentifier);

        System.out.println(Policy.fromAttributes(publicKey.attributes()));
        System.out.println(Policy.fromAttributes(rekeyedPublicKey.attributes()));
        assert !Arrays.equals(publicKey.bytes(), rekeyedPublicKey.bytes());
        assert !Policy.fromAttributes(publicKey.attributes()).equals(
                Policy.fromAttributes(rekeyedPublicKey.attributes()));

        // Creating a new `confidential marketing` message
        byte[] confidentialMkgData = "confidentialMkgMessage".getBytes(StandardCharsets.UTF_8);
        String confidentialMkgEncryptionPolicy = "Department::MKG && Security Level::Confidential";
        byte[] confidentialMkgCT = coverCrypt.encrypt(
                policy,
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
        // decrypt the old protected
        // marketing
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

}
