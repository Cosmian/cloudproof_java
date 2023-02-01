package com.cosmian;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Optional;
import java.util.stream.IntStream;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.cosmian.jna.covercrypt.CoverCrypt;
import com.cosmian.jna.covercrypt.structs.MasterKeys;
import com.cosmian.jna.covercrypt.structs.Policy;
import com.cosmian.jna.covercrypt.structs.PolicyAxis;
import com.cosmian.jna.covercrypt.structs.PolicyAxisAttribute;
import com.cosmian.rest.abe.data.DecryptedData;
import com.cosmian.utils.CloudproofException;

public class TestBenchesCoverCrypt {
    static final CoverCrypt coverCrypt = new CoverCrypt();

    @BeforeAll
    public static void before_all() {
        TestUtils.initLogging();
    }

    private Policy policy() throws CloudproofException {
        PolicyAxis security = new PolicyAxis("Security Level",
            new PolicyAxisAttribute[] {new PolicyAxisAttribute("Protected", false),
                new PolicyAxisAttribute("Confidential", false), new PolicyAxisAttribute("Top Secret", false)},
            true);
        PolicyAxis departments = new PolicyAxis("Department",
            new PolicyAxisAttribute[] {
                new PolicyAxisAttribute("FIN", false), new PolicyAxisAttribute("MKG", false),
                new PolicyAxisAttribute("HR", false), new PolicyAxisAttribute("R&D", false),
                new PolicyAxisAttribute("CYBER", false)},
            false);

        Policy policy = new Policy(30);

        policy.addAxis(security);
        policy.addAxis(departments);
        return policy;
    }

    private String accessPolicyConfidential() throws CloudproofException {
        return "Department::FIN && Security Level::Confidential";
    }

    private String[] accessPolicies() throws CloudproofException {
        return new String[] {
            "Department::FIN && Security Level::Protected",
            "(Department::FIN && Department::MKG) && Security Level::Protected",
            "(Department::FIN && Department::MKG && Department::HR) && Security Level::Protected",
            "(Department::R&D && Department::FIN && Department::MKG && Department::HR) && Security Level::Protected",
            "(Department::R&D && Department::FIN && Department::MKG && Department::HR && Department::CYBER) && Security Level::Protected"
        };
    }

    private byte[][] generateUserDecryptionKeys(byte[] msk,
                                                Policy policy)
        throws CloudproofException {
        return IntStream.range(0, accessPolicies().length)
            .mapToObj(i -> {
                try {
                    return CoverCrypt.generateUserPrivateKey(msk, accessPolicies()[i], policy);
                } catch (CloudproofException e) {
                    e.printStackTrace();
                    throw new RuntimeException("User decryption key generation");
                }
            })
            .toArray(byte[][]::new);
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
        // Single generation being very small (about 180µs), nb_occurrences should be
        // at least 1 million for CI purpose, value is 10000
        int nb_occurrences = 10000;
        for (int i = 0; i < nb_occurrences; i++) {
            masterKeys = CoverCrypt.generateMasterKeys(policy);
        }
        long time = (System.nanoTime() - start);
        System.out.println("CoverCrypt Master Key generation average time: " + time /
            nb_occurrences + "ns (or "
            + time / 1000 / nb_occurrences + "µs)");

        String accessPolicy = accessPolicyConfidential();
        start = System.nanoTime();
        for (int i = 0; i < nb_occurrences; i++) {
            CoverCrypt.generateUserPrivateKey(masterKeys.getPrivateKey(), accessPolicy,
                policy);
        }
        time = (System.nanoTime() - start);
        System.out.println("CoverCrypt User Private Key generation average time: " +
            time / nb_occurrences + "ns (or "
            + time / 1000 / nb_occurrences + "µs)");
    }

    byte[] encryptionTime(byte[] plaintext,
                          Policy policy,
                          MasterKeys masterKeys,
                          byte[] uid,
                          String accessPolicy)
        throws NoSuchAlgorithmException, CloudproofException {
        // The data we want to encrypt/decrypt
        byte[] ciphertext = new byte[0];
        int nb_occurrences = 10000;
        long start = System.nanoTime();
        for (int i = 0; i < nb_occurrences; i++) {
            ciphertext = CoverCrypt.encrypt(policy, masterKeys.getPublicKey(), accessPolicy, plaintext, Optional.of(uid), Optional.empty());
        }
        long time = (System.nanoTime() - start);
        System.out.print(
            "Encryption average time: " + time / nb_occurrences + "ns ("
                + time / 1000 / nb_occurrences + "µs). ");

        return ciphertext;
    }

    byte[] decryptionTime(byte[] userDecryptionKey,
                          byte[] ciphertext,
                          byte[] uid)
        throws CloudproofException {
        int nb_occurrences = 10000;
        long start = System.nanoTime();
        DecryptedData res = new DecryptedData(new byte[0], new byte[0]);
        for (int i = 0; i < nb_occurrences; i++) {
            res = CoverCrypt.decrypt(userDecryptionKey, ciphertext, Optional.of(uid));
        }
        long time = (System.nanoTime() - start);
        System.out.println("Decryption average time: " + time / nb_occurrences + "ns ("
            + time / 1000 / nb_occurrences + "µs)");
        return res.getPlaintext();
    }

    @Test
    public void testBenchesEncryptionDecryption() throws Exception {

        System.out.println("");
        System.out.println("---------------------------------------");
        System.out.println(" Benches CoverCrypt Encryption/Decryption");
        System.out.println("---------------------------------------");
        System.out.println("");

        // The data we want to encrypt/decrypt
        byte[] plaintext = "This is a test message".getBytes(StandardCharsets.UTF_8);

        // Declare the CoverCrypt Policy
        Policy policy = policy();

        // Generate the master keys
        MasterKeys masterKeys = CoverCrypt.generateMasterKeys(policy);

        // Generate the user decryption keys
        byte[][] userDecryptionKeys = generateUserDecryptionKeys(masterKeys.getPrivateKey(),
            policy);

        // A unique ID associated with this message. The unique id is used to
        // authenticate the message in the AES encryption scheme.
        // Typically this will be a hash of the content if it is unique, a unique
        // filename or a database unique key
        byte[] uid = MessageDigest.getInstance("SHA-256").digest(plaintext);

        String[] accessPolicies = accessPolicies();
        for (int partitionNumber = 0; partitionNumber < accessPolicies.length; partitionNumber++) {
            System.out.print("Number of partitions: " + String.valueOf(partitionNumber + 1) + ": ");
            // now hybrid encrypt the data using the uid as authentication in the symmetric
            // cipher
            byte[] ciphertext = encryptionTime(plaintext, policy, masterKeys, uid, accessPolicies[partitionNumber]);
            //
            // Decryption
            //
            byte[] cleartext = decryptionTime(userDecryptionKeys[partitionNumber], ciphertext, uid);

            // Verify everything is correct
            assertTrue(Arrays.equals(plaintext, cleartext));

        }

    }
}
