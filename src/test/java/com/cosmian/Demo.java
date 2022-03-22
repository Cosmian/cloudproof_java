package com.cosmian;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;

import java.nio.charset.StandardCharsets;

import com.cosmian.rest.abe.Abe;
import com.cosmian.rest.abe.acccess_policy.AccessPolicy;
import com.cosmian.rest.abe.acccess_policy.And;
import com.cosmian.rest.abe.acccess_policy.Attr;
import com.cosmian.rest.abe.acccess_policy.Or;
import com.cosmian.rest.abe.policy.Policy;
import com.cosmian.rest.kmip.objects.PrivateKey;

import org.junit.jupiter.api.Test;

/**
 * This class contains demos of the Java API.
 * 
 * Demos are written as tests so that they can be easily launched from an IDE
 */
public class Demo {

        /**
         * <b>ABE</b>: Encryption using an Authorization Policy<br/>
         * <br/>
         * This demo demonstrates how data can be encrypted with policy attributes. An
         * user will only be able to decrypt
         * data when it holds a key with the proper attributes. <br/>
         * <br/>
         * This demo also demonstrates revocation of an attribute value and how to
         * implement forward secrecy.
         * 
         */
        @Test
        public void abe() throws Exception {

                if (!TestUtils.serverAvailable(TestUtils.kmsServerUrl())) {
                        System.out.println("Demo: No KMS Server: ignoring");
                        return;
                }

                // Change the Cosmian Server Server URL and API key as appropriate
                Abe abe = new Abe(new RestClient(TestUtils.kmsServerUrl(), TestUtils.apiKey()));

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
                                                new String[] { "Protected", "Low Secret", "Medium Secret",
                                                                "High Secret", "Top Secret", },
                                                true)
                                .addAxis("Department", new String[] { "R&D", "HR", "MKG", "FIN" }, false);

                // ## Master Authority
                // The Master Authority possesses the keys for the given Policy:
                // a Private Key which is used to generate user keys and a Public Key which is
                // used to encrypt files with proper level of security.
                // The call returns the KMS UIDs of the 2 keys
                String[] ids = abe.createMasterKeyPair(policy);
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
                byte[] low_secret_mkg_data = "low_secret_mkg_message".getBytes(StandardCharsets.UTF_8);
                Attr[] low_secret_mkg_attributes = new Attr[] { new Attr("Department", "MKG"),
                                new Attr("Security Level", "Low Secret") };
                byte[] low_secret_mkg_ct = abe.kmsEncrypt(publicMasterKeyUID, low_secret_mkg_data,
                                low_secret_mkg_attributes);

                // - a top secret marketing message
                byte[] top_secret_mkg_data = "top_secret_mkg_message".getBytes(StandardCharsets.UTF_8);
                Attr[] top_secret_mkg_attributes = new Attr[] { new Attr("Department", "MKG"),
                                new Attr("Security Level", "Top Secret") };
                byte[] top_secret_mkg_ct = abe.kmsEncrypt(publicMasterKeyUID, top_secret_mkg_data,
                                top_secret_mkg_attributes);

                // - and a low secret finance message
                byte[] low_secret_fin_data = "low_secret_fin_message".getBytes(StandardCharsets.UTF_8);
                Attr[] low_secret_fin_attributes = new Attr[] { new Attr("Department", "FIN"),
                                new Attr("Security Level", "Low Secret") };
                byte[] low_secret_fin_ct = abe.kmsEncrypt(publicMasterKeyUID, low_secret_fin_data,
                                low_secret_fin_attributes);

                // ## User Decryption Keys
                // User Decryption Keys are generated from the Master Private Key using Access
                // Policies.
                // Access Policies are monotonous boolean expressions combining attributes.

                // Let us create 3 users to illustrate the relationship between their Key
                // Policies and the message Attributes

                // ### The medium secret marketing user
                // This user can decrypt messages from the marketing department only with a
                // security level of Medium Secret or below
                AccessPolicy medium_secret_mkg_access = new And(new Attr("Department", "MKG"),
                                new Attr("Security Level", "Medium Secret"));
                String medium_secret_mkg_user_key_uid = abe.createUserDecryptionKey(medium_secret_mkg_access,
                                privateMasterKeyUID);

                // The medium secret marketing user can successfully decrypt a low security
                // marketing message
                assertArrayEquals(low_secret_mkg_data,
                                abe.kmsDecrypt(medium_secret_mkg_user_key_uid, low_secret_mkg_ct));
                // ... however it can neither decrypt a marketing message with higher security
                try {
                        abe.kmsDecrypt(medium_secret_mkg_user_key_uid, top_secret_mkg_ct);
                        throw new RuntimeException("Oh... something is wrong !");
                } catch (CosmianException e) {
                        // fine: the user is not be able to decrypt
                }
                // ... nor decrypt a message from another department even with a lower security
                try {
                        abe.kmsDecrypt(medium_secret_mkg_user_key_uid, low_secret_fin_ct);
                        throw new RuntimeException("Oh... something is wrong !");
                } catch (CosmianException e) {
                        // fine: the user is not be able to decrypt
                }

                // ### The top secret marketing financial user
                // This user can decrypt messages from the marketing department OR the financial
                // department that have a security level of Top Secret or below
                AccessPolicy top_secret_mkg_fin_access = new And(
                                new Or(new Attr("Department", "MKG"), new Attr("Department", "FIN")),
                                new Attr("Security Level", "Top Secret"));
                String top_secret_mkg_fin_user_key_uid = abe.createUserDecryptionKey(top_secret_mkg_fin_access,
                                privateMasterKeyUID);
                // As expected, the top secret marketing financial user can successfully decrypt
                // all messages
                assertArrayEquals(low_secret_mkg_data,
                                abe.kmsDecrypt(top_secret_mkg_fin_user_key_uid, low_secret_mkg_ct));
                assertArrayEquals(top_secret_mkg_data,
                                abe.kmsDecrypt(top_secret_mkg_fin_user_key_uid, top_secret_mkg_ct));
                assertArrayEquals(low_secret_fin_data,
                                abe.kmsDecrypt(top_secret_mkg_fin_user_key_uid, low_secret_fin_ct));

                // ## Revocation
                // At anytime the Master Authority can revoke an attribute.
                // When that happens future encryption of data for a given attribute cannot be
                // decrypted with keys which are not "refreshed" for that attribute. As long as
                // a key is active in the KMS (the key has not been revoked), it will be
                // automatically rekeyed when an attribute is revoked.

                // Before revoking the MKG attribute, let us make a local copy of the
                // medium_secret_mkg_user_key_uid
                PrivateKey original_medium_secret_mkg_user_key = abe
                                .retrieveUserDecryptionKey(medium_secret_mkg_user_key_uid);

                // Now revoke the MKG attribute
                abe.revokeAttributes(privateMasterKeyUID, new Attr[] { new Attr("Department", "MKG") });

                // ... and reimport the non rekeyed original medium secret marketing user key
                // under a new UID
                abe.importUserDecryptionKey("original_medium_secret_mkg_user_key_uid",
                                original_medium_secret_mkg_user_key,
                                true);

                // finally let us create a new medium secret marketing message
                byte[] medium_secret_mkg_data = "medium_secret_mkg_message".getBytes(StandardCharsets.UTF_8);
                Attr[] medium_secret_mkg_attributes = new Attr[] { new Attr("Department", "MKG"),
                                new Attr("Security Level", "Low Secret") };
                byte[] medium_secret_mkg_ct = abe.kmsEncrypt(publicMasterKeyUID, medium_secret_mkg_data,
                                medium_secret_mkg_attributes);

                // The automatically rekeyed medium secret marketing user key can still decrypt
                // the low secret marketing message
                assertArrayEquals(low_secret_mkg_data,
                                abe.kmsDecrypt(medium_secret_mkg_user_key_uid, low_secret_mkg_ct));
                // ... as well as the new medium secret marketing message
                assertArrayEquals(medium_secret_mkg_data,
                                abe.kmsDecrypt(medium_secret_mkg_user_key_uid, medium_secret_mkg_ct));

                // Likewise, the top secret marketing financial user can decrypt all messages
                // ... old
                assertArrayEquals(low_secret_mkg_data,
                                abe.kmsDecrypt(top_secret_mkg_fin_user_key_uid, low_secret_mkg_ct));
                assertArrayEquals(top_secret_mkg_data,
                                abe.kmsDecrypt(top_secret_mkg_fin_user_key_uid, top_secret_mkg_ct));
                assertArrayEquals(low_secret_fin_data,
                                abe.kmsDecrypt(top_secret_mkg_fin_user_key_uid, low_secret_fin_ct));
                // ..and new
                assertArrayEquals(medium_secret_mkg_data,
                                abe.kmsDecrypt(top_secret_mkg_fin_user_key_uid, medium_secret_mkg_ct));

                // However, the old, non rekeyed medium secret marketing user key
                // ...can still decrypt the old low secret marketing message
                assertArrayEquals(low_secret_mkg_data,
                                abe.kmsDecrypt("original_medium_secret_mkg_user_key_uid", low_secret_mkg_ct));
                // ... but NOT the new medium secret marketing message
                try {
                        abe.kmsDecrypt("original_medium_secret_mkg_user_key_uid", medium_secret_mkg_ct);
                        throw new RuntimeException("Oh... something is wrong !");
                } catch (CosmianException e) {
                        // fine: the user is not be able to decrypt
                }
        }

}
