package com.cosmian;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Optional;
import java.util.logging.Logger;

import org.apache.commons.codec.binary.Hex;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.cosmian.rest.abe.Abe;
import com.cosmian.rest.abe.Implementation;
import com.cosmian.rest.abe.Specifications;
import com.cosmian.rest.abe.access_policy.AccessPolicy;
import com.cosmian.rest.abe.access_policy.And;
import com.cosmian.rest.abe.access_policy.Attr;
import com.cosmian.rest.abe.access_policy.Or;
import com.cosmian.rest.abe.policy.Policy;
import com.cosmian.rest.kmip.data_structures.KeyValue;
import com.cosmian.rest.kmip.objects.PrivateKey;
import com.cosmian.rest.kmip.objects.PublicKey;
import com.cosmian.rest.kmip.types.Attributes;
import com.cosmian.rest.kmip.types.CryptographicAlgorithm;
import com.cosmian.rest.kmip.types.KeyFormatType;
import com.cosmian.rest.kmip.types.ObjectType;
import com.cosmian.rest.kmip.types.VendorAttribute;
import com.fasterxml.jackson.databind.ObjectMapper;

public class TestGpsw {

    private static final Logger logger = Logger.getLogger(TestGpsw.class.getName());

    @BeforeAll
    public static void before_all() {
        TestUtils.initLogging();
    }

    private Policy policy() throws CosmianException {
        return new Policy(20).addAxis("Security Level", new String[] {"Protected", "Confidential", "Top Secret"}, true)
            .addAxis("Department", new String[] {"FIN", "MKG", "HR"}, false);
    }

    private AccessPolicy accessPolicyProtected() throws CosmianException {
        return new And(new Or(new Attr("Department", "FIN"), new Attr("Department", "MKG")),
            new Attr("Security Level", "Protected"));
    }

    private AccessPolicy accessPolicyConfidential() throws CosmianException {
        return new And(new Attr("Department", "FIN"), new Attr("Security Level", "Confidential"));
    }

    @Test
    public void test_policy() throws Exception {
        Policy pg = policy();

        ObjectMapper mapper = new ObjectMapper();
        String str = mapper.writeValueAsString(pg);
        logger.info(str);
        // make sure the correct serializer is used
        JSONObject json = new JSONObject(str);
        assertTrue(json.has("last_attribute_value"));
        assertTrue(json.has("max_attribute_creations"));
        assertTrue(json.has("axes"));
    }

    @Test
    public void test_attributes_serde() throws Exception {
        Attributes attributes = new Attributes(ObjectType.Private_Key, Optional.of(CryptographicAlgorithm.ABE));
        attributes.keyFormatType(Optional.of(KeyFormatType.AbeMasterSecretKey));
        attributes.vendorAttributes(Optional.of(new VendorAttribute[] {
            Attr.toVendorAttribute(new Attr[] {new Attr("Department", "MKG")}, VendorAttribute.VENDOR_ATTR_ABE_ATTR)}));
        ObjectMapper mapper = new ObjectMapper();
        String str = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(attributes);
        logger.info(str);
        String hexValue = "5B224465706172746D656E743A3A4D4B47225D";
        String v = new String(Hex.decodeHex(hexValue), StandardCharsets.UTF_8);
        logger.info(v);
        // Attributes rec = mapper.readValue(str, Attributes.class);
        // logger.info(rec.getVendorAttributes().get()[0].toString());
    }

    @Test
    public void test_keys_import_export() throws Exception {

        if (!TestUtils.serverAvailable(TestUtils.kmsServerUrl())) {
            System.out.println("No KMS Server: ignoring");
            return;
        }

        Policy pg = policy();

        Abe abe = new Abe(new RestClient(TestUtils.kmsServerUrl(), TestUtils.apiKey()),
            new Specifications(Implementation.GPSW));

        String[] ids = abe.createMasterKeyPair(pg);
        logger.info("Created Master Key: Private Key ID: " + ids[0] + ", Public Key ID: " + ids[1]);

        String privateMasterKeyUniqueIdentifier = ids[0];
        PrivateKey privateMasterKey = abe.retrievePrivateMasterKey(privateMasterKeyUniqueIdentifier);
        assertEquals(KeyFormatType.AbeMasterSecretKey, privateMasterKey.getKeyBlock().getKeyFormatType());
        assertEquals(CryptographicAlgorithm.ABE, privateMasterKey.getKeyBlock().getCryptographicAlgorithm());

        String publicMasterKeyUniqueIdentifier = ids[1];
        PublicKey publicMasterKey = abe.retrievePublicMasterKey(publicMasterKeyUniqueIdentifier);
        assertEquals(KeyFormatType.AbeMasterPublicKey, publicMasterKey.getKeyBlock().getKeyFormatType());
        assertEquals(CryptographicAlgorithm.ABE, publicMasterKey.getKeyBlock().getCryptographicAlgorithm());

        // try reimporting the public key with the same ID
        try {
            abe.importPublicMasterKey(publicMasterKeyUniqueIdentifier, publicMasterKey, false);
        } catch (CosmianException e) {
            // expected cannot re-insert with the same id if replaceExisting is false
        } catch (Exception e) {
            throw e;
        }
        // allow overwrite
        String publicMasterKeyUniqueIdentifier_ =
            abe.importPublicMasterKey(publicMasterKeyUniqueIdentifier, publicMasterKey, true);
        logger.info("Imported Public Key with id: " + publicMasterKeyUniqueIdentifier_);
        // retrieve it again
        PublicKey publicMasterKey_ = abe.retrievePublicMasterKey(publicMasterKeyUniqueIdentifier);
        assertEquals(ObjectType.Public_Key, publicMasterKey_.getObjectType());
        assertEquals(KeyFormatType.AbeMasterPublicKey, publicMasterKey_.getKeyBlock().getKeyFormatType());
        assertEquals(CryptographicAlgorithm.ABE, publicMasterKey_.getKeyBlock().getCryptographicAlgorithm());

        // User decryption key
        String userDecryptionKeyUniqueIdentifier =
            abe.createUserDecryptionKey(accessPolicyProtected(), privateMasterKeyUniqueIdentifier);
        logger.info("Created User Decryption Key with id: " + userDecryptionKeyUniqueIdentifier);
        // ... retrieve it
        PrivateKey userDecryptionKey = abe.retrieveUserDecryptionKey(userDecryptionKeyUniqueIdentifier);
        assertEquals(KeyFormatType.AbeUserDecryptionKey, userDecryptionKey.getKeyBlock().getKeyFormatType());
        assertEquals(CryptographicAlgorithm.ABE, userDecryptionKey.getKeyBlock().getCryptographicAlgorithm());
        KeyValue keyValue = (KeyValue) userDecryptionKey.getKeyBlock().getKeyValue();
        VendorAttribute[] vendorAttributes = keyValue.getAttributes().get().getVendorAttributes().get();
        // TODO better check on Vendor Attributes
        logger.info(() -> Arrays.asList(vendorAttributes).toString());

    }

    @Test
    public void test_user_decryption_keys() throws Exception {

        if (!TestUtils.serverAvailable(TestUtils.kmsServerUrl())) {
            System.out.println("No KMS Server: ignoring");
            return;
        }

        Policy pg = policy();

        Abe abe = new Abe(new RestClient(TestUtils.kmsServerUrl(), TestUtils.apiKey()),
            new Specifications(Implementation.GPSW));

        String[] ids = abe.createMasterKeyPair(pg);
        logger.info("Created Master Key: Private Key ID: " + ids[0] + ", Public Key ID: " + ids[1]);

        String privateMasterKeyID = ids[0];
        PrivateKey privateMasterKey = abe.retrievePrivateMasterKey(privateMasterKeyID);
        assertEquals(KeyFormatType.AbeMasterSecretKey, privateMasterKey.getKeyBlock().getKeyFormatType());
        assertEquals(CryptographicAlgorithm.ABE, privateMasterKey.getKeyBlock().getCryptographicAlgorithm());
        Resources.write_resource("abe/private_master_key.json",
            privateMasterKey.toJson().getBytes(StandardCharsets.UTF_8));

        String publicMasterKeyUniqueIdentifier = ids[1];
        PublicKey publicMasterKey = abe.retrievePublicMasterKey(publicMasterKeyUniqueIdentifier);
        assertEquals(KeyFormatType.AbeMasterPublicKey, publicMasterKey.getKeyBlock().getKeyFormatType());
        assertEquals(CryptographicAlgorithm.ABE, publicMasterKey.getKeyBlock().getCryptographicAlgorithm());
        Resources.write_resource("abe/public_master_key.json",
            publicMasterKey.toJson().getBytes(StandardCharsets.UTF_8));

        // encryption
        String protected_fin_data = "protected_fin_attributes";
        Attr[] protected_fin_attributes =
            new Attr[] {new Attr("Department", "FIN"), new Attr("Security Level", "Protected")};
        byte[] protected_fin_ct = abe.kmsEncrypt(publicMasterKeyUniqueIdentifier,
            protected_fin_data.getBytes(StandardCharsets.UTF_8), protected_fin_attributes);

        String confidential_fin_data = "confidential_fin_attributes";
        Attr[] confidential_fin_attributes =
            new Attr[] {new Attr("Department", "FIN"), new Attr("Security Level", "Confidential")};
        byte[] confidential_fin_ct = abe.kmsEncrypt(publicMasterKeyUniqueIdentifier,
            confidential_fin_data.getBytes(StandardCharsets.UTF_8), confidential_fin_attributes);

        // User decryption key Protected, FIN, MKG
        String fin_mkg_protected_user_key = abe.createUserDecryptionKey(accessPolicyProtected(), privateMasterKeyID);
        PrivateKey userKey_1 = abe.retrieveUserDecryptionKey(fin_mkg_protected_user_key);
        Resources.write_resource("abe/fin_mkg_protected_user_key.json",
            userKey_1.toJson().getBytes(StandardCharsets.UTF_8));

        // User decryption key Confidential, FIN
        String fin_confidential_user_key = abe.createUserDecryptionKey(accessPolicyConfidential(), privateMasterKeyID);
        PrivateKey userKey_2 = abe.retrieveUserDecryptionKey(fin_confidential_user_key);
        Resources.write_resource("abe/fin_confidential_user_key.json",
            userKey_2.toJson().getBytes(StandardCharsets.UTF_8));

        // User decryption key Protected should be able to decrypt protected_fin_ct
        String clearText_1_1 =
            new String(abe.kmsDecrypt(fin_mkg_protected_user_key, protected_fin_ct), StandardCharsets.UTF_8);
        assertEquals(protected_fin_data, clearText_1_1);
        // User decryption key Confidential should be able to decrypt protected_fin_ct
        String clearText_1_2 =
            new String(abe.kmsDecrypt(fin_confidential_user_key, protected_fin_ct), StandardCharsets.UTF_8);
        assertEquals(protected_fin_data, clearText_1_2);

        // User decryption key Protected should not be able to decrypt
        // confidential_fin_ct
        try {
            new String(abe.kmsDecrypt(fin_mkg_protected_user_key, confidential_fin_ct), StandardCharsets.UTF_8);
            throw new RuntimeException("User with key Confidential should not be able to decrypt data Confidential");
        } catch (CosmianException e) {
            // fine: should not be able to decrypt
        }

        // User decryption key Confidential should not be able to decrypt
        // confidential_fin_ct
        String clearText_2_2 =
            new String(abe.kmsDecrypt(fin_confidential_user_key, confidential_fin_ct), StandardCharsets.UTF_8);
        assertEquals(confidential_fin_data, clearText_2_2);
    }

    @Test
    public void testAccessPolicy() throws Exception {

        String expected =
            "{\"And\":[{\"Or\":[{\"Attr\":\"Department::FIN\"},{\"Attr\":\"Department::MKG\"}]},{\"Attr\":\"Levels::Sec_level_1\"}]}";

        AccessPolicy accessPolicy = new And(new Or(new Attr("Department", "FIN"), new Attr("Department", "MKG")),
            new Attr("Levels", "Sec_level_1"));

        ObjectMapper mapper = new ObjectMapper();
        String actual = mapper.writeValueAsString(accessPolicy);
        assertEquals(expected, actual);
    }

    @Test
    public void testObjectsSerDe() throws Exception {
        String private_key_json = Resources.load_resource("abe/private_key.json");
        PrivateKey sk1 = PrivateKey.fromJson(private_key_json);
        PrivateKey sk2 = PrivateKey.fromJson(sk1.toJson());
        assertEquals(sk1.toJson(), sk2.toJson());

        String public_key_json = Resources.load_resource("abe/public_key.json");
        PublicKey pk1 = PublicKey.fromJson(public_key_json);
        PublicKey pk2 = PublicKey.fromJson(pk1.toJson());
        assertEquals(pk1.toJson(), pk2.toJson());
    }
}
