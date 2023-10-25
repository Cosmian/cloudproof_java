package com.cosmian;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Optional;
import java.util.stream.IntStream;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.cosmian.jna.covercrypt.CoverCrypt;
import com.cosmian.jna.covercrypt.structs.DecryptedHeader;
import com.cosmian.jna.covercrypt.structs.EncryptedHeader;
import com.cosmian.jna.covercrypt.structs.MasterKeys;
import com.cosmian.jna.covercrypt.structs.Policy;
import com.cosmian.jna.covercrypt.structs.PolicyAxis;
import com.cosmian.jna.covercrypt.structs.PolicyAxisAttribute;
import com.cosmian.utils.CloudproofException;

public class TestBenchesCoverCrypt {
    static final CoverCrypt coverCrypt = new CoverCrypt();

    @BeforeAll
    public static void before_all() {
        TestUtils.initLogging();
    }

    private Policy policy() throws CloudproofException {
        PolicyAxis hybridization = new PolicyAxis("Hybridization",
            new PolicyAxisAttribute[] {new PolicyAxisAttribute("Hybridized", true),
                new PolicyAxisAttribute("Classic", false)},
            true);
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

        Policy policy = new Policy();

        policy.addAxis(hybridization);
        policy.addAxis(security);
        policy.addAxis(departments);
        return policy;
    }

    private String accessPolicyConfidential() throws CloudproofException {
        return "Department::FIN && Security Level::Confidential";
    }

    private String[] hybridizedAccessPolicies() throws CloudproofException {
        return new String[] {
            "Hybridization::Hybridized && Department::FIN && Security Level::Protected",
            "Hybridization::Hybridized && (Department::FIN || Department::MKG) && Security Level::Protected",
            "Hybridization::Hybridized && (Department::FIN || Department::MKG || Department::HR) && Security Level::Protected",
            "Hybridization::Hybridized && (Department::R&D || Department::FIN || Department::MKG || Department::HR) && Security Level::Protected",
            "Hybridization::Hybridized && (Department::R&D || Department::FIN || Department::MKG || Department::HR || Department::CYBER) && Security Level::Protected"
        };
    }

    private String[] classicAccessPolicies() throws CloudproofException {
        return new String[] {
            "Hybridization::Classic && Department::FIN && Security Level::Protected",
            "Hybridization::Classic && (Department::FIN || Department::MKG) && Security Level::Protected",
            "Hybridization::Classic && (Department::FIN || Department::MKG || Department::HR) && Security Level::Protected",
            "Hybridization::Classic && (Department::R&D || Department::FIN || Department::MKG || Department::HR) && Security Level::Protected",
            "Hybridization::Classic && (Department::R&D || Department::FIN || Department::MKG || Department::HR || Department::CYBER) && Security Level::Protected"
        };
    }

    private byte[][] generateUserDecryptionKeys(byte[] msk,
                                                Policy policy)
        throws CloudproofException {
        return IntStream.range(0, classicAccessPolicies().length)
            .mapToObj(i -> {
                try {
                    return CoverCrypt.generateUserPrivateKey(msk, classicAccessPolicies()[i], policy);
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

        System.out.println("CoverCrypt Policy size: " + policy.getBytes().length + " bytes");
        System.out.println("CoverCrypt Public Key size: " + masterKeys.getPublicKey().length + " bytes");

        System.out.println("CoverCrypt Master Key generation average time: " + time /
            nb_occurrences + "ns (or "
            + time / 1000 / nb_occurrences + "µs)");

        String accessPolicy = accessPolicyConfidential();
        byte[] userPrivateKey = new byte[0];
        start = System.nanoTime();
        for (int i = 0; i < nb_occurrences; i++) {
            userPrivateKey = CoverCrypt.generateUserPrivateKey(masterKeys.getPrivateKey(), accessPolicy,
                policy);
        }
        time = (System.nanoTime() - start);

        System.out.println("CoverCrypt User Private Key size: " + userPrivateKey.length + " bytes");

        System.out.println("CoverCrypt User Private Key generation average time: " +
            time / nb_occurrences + "ns (or "
            + time / 1000 / nb_occurrences + "µs)");
    }

    void benchHeaderEncryptionDecryptionWithCache(Policy policy,
                                                  MasterKeys masterKeys,
                                                  byte[] userDecryptionKey,
                                                  String accessPolicy)
        throws NoSuchAlgorithmException, CloudproofException {

        int nb_occurrences = 10000;
        int encryptionCacheHandle = CoverCrypt.createEncryptionCache(policy, masterKeys.getPublicKey());
        int decryptionCacheHandle = CoverCrypt.createDecryptionCache(userDecryptionKey);

        long encryption_time = 0;
        long decryption_time = 0;
        int encryptedHeaderLength = 0;

        for (int i = 0; i < nb_occurrences; i++) {
            // Encryption
            long start = System.nanoTime();
            EncryptedHeader encryptedHeader =
                CoverCrypt.encryptHeaderUsingCache(encryptionCacheHandle, accessPolicy);
            long stop = System.nanoTime();
            encryption_time += stop - start;

            encryptedHeaderLength = encryptedHeader.getEncryptedHeaderBytes().length;

            // Decryption
            start = System.nanoTime();
            DecryptedHeader decryptedHeader = CoverCrypt.decryptHeaderUsingCache(decryptionCacheHandle,
                encryptedHeader.getEncryptedHeaderBytes(), Optional.empty());
            stop = System.nanoTime();
            decryption_time += stop - start;

            assertTrue(Arrays.equals(encryptedHeader.getSymmetricKey(), decryptedHeader.getSymmetricKey()));
        }

        System.out.print("Encrypted Header size: " + encryptedHeaderLength + ". ");
        System.out.print("Encryption average time: " + encryption_time / nb_occurrences + "ns ("
            + encryption_time / 1000 / nb_occurrences + "µs). ");
        System.out.println("Decryption average time: " + decryption_time / nb_occurrences + "ns ("
            + decryption_time / 1000 / nb_occurrences + "µs)");
    }

    @Test
    public void testBenchesEncryptionDecryptionWithCache() throws Exception {

        System.out.println("");
        System.out.println("-----------------------------------------------------");
        System.out.println(" Benches CoverCrypt Encryption/Decryption With Cache ");
        System.out.println("-----------------------------------------------------");
        System.out.println("");

        Policy policy = policy();
        MasterKeys masterKeys = CoverCrypt.generateMasterKeys(policy);
        byte[][] userDecryptionKeys = generateUserDecryptionKeys(masterKeys.getPrivateKey(),
            policy);

        System.out.println("");
        System.out.println("Classic encryption");
        System.out.println("==================");
        System.out.println("");

        String[] accessPolicies = classicAccessPolicies();
        for (int partitionNumber = 0; partitionNumber < accessPolicies.length; partitionNumber++) {
            System.out.print("Number of partitions: " + String.valueOf(partitionNumber + 1) + ": ");
            benchHeaderEncryptionDecryptionWithCache(policy, masterKeys, userDecryptionKeys[partitionNumber],
                accessPolicies[partitionNumber]);
        }

        System.out.println("");
        System.out.println("Hybridized encryption");
        System.out.println("======================");
        System.out.println("");

        accessPolicies = hybridizedAccessPolicies();
        for (int partitionNumber = 0; partitionNumber < accessPolicies.length; partitionNumber++) {
            System.out.print("Number of partitions: " + String.valueOf(partitionNumber + 1) + ": ");
            benchHeaderEncryptionDecryptionWithCache(policy, masterKeys, userDecryptionKeys[partitionNumber],
                accessPolicies[partitionNumber]);
        }
    }
}
