package com.cosmian.rest.abe;

import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.logging.Logger;

import com.cosmian.CloudproofException;
import com.cosmian.RestClient;
import com.cosmian.jna.abe.CoverCrypt;
import com.cosmian.rest.abe.access_policy.AccessPolicy;
import com.cosmian.rest.abe.access_policy.Attr;
import com.cosmian.rest.abe.data.DataToEncrypt;
import com.cosmian.rest.abe.policy.Policy;
import com.cosmian.rest.kmip.Kmip;
import com.cosmian.rest.kmip.objects.PrivateKey;
import com.cosmian.rest.kmip.objects.PublicKey;
import com.cosmian.rest.kmip.operations.Create;
import com.cosmian.rest.kmip.operations.CreateKeyPair;
import com.cosmian.rest.kmip.operations.CreateKeyPairResponse;
import com.cosmian.rest.kmip.operations.CreateResponse;
import com.cosmian.rest.kmip.operations.Decrypt;
import com.cosmian.rest.kmip.operations.DecryptResponse;
import com.cosmian.rest.kmip.operations.Destroy;
import com.cosmian.rest.kmip.operations.DestroyResponse;
import com.cosmian.rest.kmip.operations.Encrypt;
import com.cosmian.rest.kmip.operations.EncryptResponse;
import com.cosmian.rest.kmip.operations.Get;
import com.cosmian.rest.kmip.operations.GetResponse;
import com.cosmian.rest.kmip.operations.Import;
import com.cosmian.rest.kmip.operations.ImportResponse;
import com.cosmian.rest.kmip.operations.ReKeyKeyPair;
import com.cosmian.rest.kmip.operations.ReKeyKeyPairResponse;
import com.cosmian.rest.kmip.operations.Revoke;
import com.cosmian.rest.kmip.operations.RevokeResponse;
import com.cosmian.rest.kmip.types.Attributes;
import com.cosmian.rest.kmip.types.CryptographicAlgorithm;
import com.cosmian.rest.kmip.types.KeyFormatType;
import com.cosmian.rest.kmip.types.Link;
import com.cosmian.rest.kmip.types.LinkType;
import com.cosmian.rest.kmip.types.LinkedObjectIdentifier;
import com.cosmian.rest.kmip.types.ObjectType;
import com.cosmian.rest.kmip.types.RevocationReason;
import com.cosmian.rest.kmip.types.VendorAttribute;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Attribute Based Encryption endpoints
 */
public class KmsClient {

    private static final Logger logger = Logger.getLogger(KmsClient.class.getName());

    private final Kmip kmip;

    /**
     * Instantiate a new KmipClient with DEFAULT_CONNECT_TIMEOUT and DEFAULT_READ_TIMEOUT
     *
     * @param server_url the REST Server URL e.g. http://localhost:9000
     * @param api_key he optional API Key to use to authenticate
     */
    public KmsClient(String server_url, Optional<String> api_key) {
        this(new RestClient(server_url, api_key));
    }

    /**
     * Instantiate a new Kmip client using a {@link RestClient}
     * 
     * @param rest_client the {@link RestClient}
     */
    public KmsClient(RestClient rest_client) {
        this.kmip = new Kmip(rest_client);
    }

    /**
     * Generate inside the KMS, a master private and public key pair for the {@link Policy}
     *
     * @param policy the Key Policy
     * @return a tuple containing the master private key UID and the master public key UID
     * @throws CloudproofException if the creation fails
     */
    public String[] createCoverCryptMasterKeyPair(Policy policy) throws CloudproofException {
        try {
            Attributes commonAttributes = new Attributes(ObjectType.Private_Key,
                Optional.of(CryptographicAlgorithm.CoverCrypt));
            commonAttributes.setKeyFormatType(Optional.of(KeyFormatType.CoverCryptSecretKey));

            // convert the Policy to attributes and attach it to the common attributes
            VendorAttribute policy_attribute = policy.toVendorAttribute();

            commonAttributes.setVendorAttributes(Optional.of(new VendorAttribute[] {policy_attribute}));

            CreateKeyPair request = new CreateKeyPair(Optional.of(commonAttributes), Optional.empty());
            CreateKeyPairResponse response = this.kmip.createKeyPair(request);
            return new String[] {response.getPrivateKeyUniqueIdentifier(), response.getPublicKeyUniqueIdentifier()};
        } catch (CloudproofException e) {
            throw e;
        } catch (Exception e) {
            String err = "CoverCrypt" + ": Master Key generation failed: " + e.getMessage() + "  "
                + e.getClass();
            logger.severe(err);
            throw new CloudproofException(err, e);
        }
    }

    /**
     * Retrieve the Master Private Key from the KMS
     *
     * @param privateMasterKeyUniqueIdentifier the key UID
     * @return the Private Key
     * @throws CloudproofException if the retrieval fails
     */
    public PrivateKey retrieveCoverCryptPrivateMasterKey(String privateMasterKeyUniqueIdentifier)
        throws CloudproofException {
        try {
            Get request = new Get(privateMasterKeyUniqueIdentifier);
            request.setKeyFormatType(Optional.of(KeyFormatType.CoverCryptSecretKey));
            //
            GetResponse response = this.kmip.get(request);
            Object object = response.getObject();
            if (!(object instanceof PrivateKey)) {
                throw new CloudproofException("No " + "CoverCrypt" + " Private Master Key at identifier "
                    + privateMasterKeyUniqueIdentifier);
            }
            PrivateKey sk = (PrivateKey) object;
            if (!sk.getKeyBlock().getKeyFormatType().equals(KeyFormatType.CoverCryptSecretKey)) {
                throw new CloudproofException("No " + "CoverCrypt" + " Private Master Key at identifier "
                    + privateMasterKeyUniqueIdentifier);
            }
            return sk;
        } catch (CloudproofException e) {
            throw e;
        } catch (Exception e) {
            String err = "CoverCrypt" + ": Private Master Key could not be retrieved: " + e.getMessage()
                + "  " + e.getClass();
            logger.severe(err);
            throw new CloudproofException(err, e);
        }
    }

    /**
     * Import a Private Master Key in the KMS
     *
     * @param uniqueIdentifier the UID of the key
     * @param privateMasterKey the key
     * @param replaceExisting if a key exists under this UID, replace it
     * @return the UID of the imported key
     * @throws CloudproofException if the import fails
     */
    public String importCoverCryptPrivateMasterKey(String uniqueIdentifier, PrivateKey privateMasterKey,
        boolean replaceExisting)
        throws CloudproofException {
        try {
            Import request = new Import(uniqueIdentifier, ObjectType.Private_Key, Optional.of(replaceExisting),
                Optional.empty(), privateMasterKey.attributes(), privateMasterKey);
            ImportResponse response = this.kmip.importObject(request);
            return response.getUniqueIdentifier();
        } catch (CloudproofException e) {
            throw e;
        } catch (Exception e) {
            String err = "CoverCrypt" + ": Private Master Key could not be imported: " + e.getMessage()
                + "  " + e.getClass();
            logger.severe(err);
            throw new CloudproofException(err, e);
        }
    }

    /**
     * Retrieve the Master Public Key from the KMS
     *
     * @param publicMasterKeyUniqueIdentifier the key UID
     * @return the Public Key
     * @throws CloudproofException if the retrieval fails
     */
    public PublicKey retrieveCoverCryptPublicMasterKey(String publicMasterKeyUniqueIdentifier)
        throws CloudproofException {
        try {
            Get request = new Get(publicMasterKeyUniqueIdentifier);
            request.setKeyFormatType(Optional.of(KeyFormatType.CoverCryptPublicKey));
            //
            GetResponse response = this.kmip.get(request);
            Object object = response.getObject();
            if (!(object instanceof PublicKey)) {
                throw new CloudproofException("No " + "CoverCrypt" + " Public Master Key at identifier "
                    + publicMasterKeyUniqueIdentifier);
            }
            PublicKey sk = (PublicKey) object;
            if (!sk.getKeyBlock().getKeyFormatType().equals(KeyFormatType.CoverCryptPublicKey)) {
                throw new CloudproofException("No " + "CoverCrypt" + " Public Master Key at identifier "
                    + publicMasterKeyUniqueIdentifier);
            }
            return sk;
        } catch (CloudproofException e) {
            throw e;
        } catch (Exception e) {
            String err = "CoverCrypt" + ": Public Master Key could not be retrieved: " + e.getMessage()
                + "  " + e.getClass();
            logger.severe(err);
            throw new CloudproofException(err, e);
        }
    }

    /**
     * Import a Public Master Key in the KMS
     *
     * @param uniqueIdentifier the UID of the key
     * @param publicMasterKey the key
     * @param replaceExisting if a key exists under this UID, replace it
     * @return the UID of the imported key
     * @throws CloudproofException if the import fails
     */
    public String importCoverCryptPublicMasterKey(String uniqueIdentifier, PublicKey publicMasterKey,
        boolean replaceExisting)
        throws CloudproofException {
        try {
            Import request = new Import(uniqueIdentifier, ObjectType.Public_Key, Optional.of(replaceExisting),
                Optional.empty(), publicMasterKey.attributes(), publicMasterKey);
            ImportResponse response = this.kmip.importObject(request);
            return response.getUniqueIdentifier();
        } catch (CloudproofException e) {
            throw e;
        } catch (Exception e) {
            String err = "CoverCrypt" + ": Public Master Key could not be imported: " + e.getMessage()
                + "  " + e.getClass();
            logger.severe(err);
            throw new CloudproofException(err, e);
        }
    }

    /**
     * Create a User Decryption Key for the given {@link AccessPolicy} expressed as a boolean expression
     *
     * @param accessPolicy the {@link AccessPolicy} as a string
     * @param privateMasterKeyUniqueIdentifier the UID of the Master Private Key
     * @return the UID of the newly created key
     * @throws CloudproofException if the creation fails
     */
    public String createCoverCryptUserDecryptionKey(String accessPolicy, String privateMasterKeyUniqueIdentifier)
        throws CloudproofException {

        // not qt the class level, so the rest of the methods can be used without a
        // native library
        final CoverCrypt ffi = new CoverCrypt();
        String json;
        try {
            json = ffi.booleanAccessPolicyToJson(accessPolicy);
        } catch (CloudproofException e) {
            throw new CloudproofException("Failed converting the boolean access policy to a JSON: " + e.getMessage(),
                e);
        }
        VendorAttribute accessPolicyAttribute = new VendorAttribute(
            VendorAttribute.VENDOR_ID_COSMIAN,
            VendorAttribute.VENDOR_ATTR_COVER_CRYPT_ACCESS_POLICY,
            json.getBytes(StandardCharsets.UTF_8));

        return createCoverCryptUserDecryptionKey(accessPolicyAttribute, privateMasterKeyUniqueIdentifier);
    }

    /**
     * Create a User Decryption Key for the given {@link AccessPolicy} in the KMS
     *
     * @param accessPolicy the {@link AccessPolicy}
     * @param privateMasterKeyUniqueIdentifier the UID of the Master Private Key
     * @return the UID of the newly created key
     * @throws CloudproofException if the creation fails
     */
    public String createCoverCryptUserDecryptionKey(AccessPolicy accessPolicy, String privateMasterKeyUniqueIdentifier)
        throws CloudproofException {
        // convert the Access Policy to attributes and attach it to the common
        // attributes
        VendorAttribute accessPolicyAttribute = accessPolicy
            .toVendorAttribute();

        return createCoverCryptUserDecryptionKey(accessPolicyAttribute, privateMasterKeyUniqueIdentifier);
    }

    /**
     * Create a User Decryption Key for the given {@link AccessPolicy} in the KMS
     *
     * @param accessPolicyAttribute the {@link AccessPolicy} as a {@link VendorAttribute}
     * @param privateMasterKeyUniqueIdentifier the UID of the Master Private Key
     * @return the UID of the newly created key
     * @throws CloudproofException if the creation fails
     */
    String createCoverCryptUserDecryptionKey(VendorAttribute accessPolicyAttribute,
        String privateMasterKeyUniqueIdentifier)
        throws CloudproofException {
        try {
            Attributes commonAttributes = new Attributes(
                ObjectType.Private_Key,
                Optional.of(CryptographicAlgorithm.CoverCrypt));
            commonAttributes.setKeyFormatType(Optional.of(KeyFormatType.CoverCryptSecretKey));

            // convert the Access Policy to attributes and attach it to the common
            // attributes
            commonAttributes.setVendorAttributes(Optional.of(new VendorAttribute[] {accessPolicyAttribute}));
            // link to the master private key
            commonAttributes.setLink(Optional.of(new Link[] {
                new Link(
                    LinkType.Parent_Link,
                    new LinkedObjectIdentifier(privateMasterKeyUniqueIdentifier))
            }));

            Create request = new Create(ObjectType.Private_Key, commonAttributes, Optional.empty());
            CreateResponse response = this.kmip.create(request);
            return response.getUniqueIdentifier();
        } catch (CloudproofException e) {
            throw e;
        } catch (Exception e) {
            String err = "CoverCrypt" + ": Master Key generation failed: " + e.getMessage() + " "
                + e.getClass();
            logger.severe(err);
            throw new CloudproofException(err, e);
        }
    }

    /**
     * Retrieve a User Decryption Key from the KMS
     *
     * @param userDecryptionKeyUniqueIdentifier the key UID
     * @return the User Decryption Key
     * @throws CloudproofException if the retrieval fails
     */
    public PrivateKey retrieveCoverCryptUserDecryptionKey(String userDecryptionKeyUniqueIdentifier)
        throws CloudproofException {
        try {
            Get request = new Get(userDecryptionKeyUniqueIdentifier);
            request.setKeyFormatType(Optional.of(KeyFormatType.CoverCryptSecretKey));
            //
            GetResponse response = this.kmip.get(request);
            Object object = response.getObject();
            if (!(object instanceof PrivateKey)) {
                throw new CloudproofException("No " + "CoverCrypt" + " User Decryption Key at identifier "
                    + userDecryptionKeyUniqueIdentifier);
            }
            PrivateKey sk = (PrivateKey) object;
            if (!sk.getKeyBlock().getKeyFormatType().equals(KeyFormatType.CoverCryptSecretKey)) {
                throw new CloudproofException("No " + "CoverCrypt" + " User Decryption Key at identifier "
                    + userDecryptionKeyUniqueIdentifier);
            }
            return sk;
        } catch (CloudproofException e) {
            throw e;
        } catch (Exception e) {
            String err = "CoverCrypt" + ": User Decryption Key could not be retrieved: " + e.getMessage()
                + "  " + e.getClass();
            logger.severe(err);
            throw new CloudproofException(err, e);
        }
    }

    /**
     * Import a User Decryption Key in the KMS
     *
     * @param uniqueIdentifier the UID of the key
     * @param userDecryptionKey the key
     * @param replaceExisting if a key exists under this UID, replace it
     * @return the UID of the imported key
     * @throws CloudproofException if the import fails
     */
    public String importCoverCryptUserDecryptionKey(String uniqueIdentifier, PrivateKey userDecryptionKey,
        boolean replaceExisting) throws CloudproofException {
        try {
            Import request = new Import(uniqueIdentifier, ObjectType.Private_Key, Optional.of(replaceExisting),
                Optional.empty(), userDecryptionKey.attributes(), userDecryptionKey);
            ImportResponse response = this.kmip.importObject(request);
            return response.getUniqueIdentifier();
        } catch (CloudproofException e) {
            throw e;
        } catch (Exception e) {
            String err = "CoverCrypt" + ": User Decryption Key could not be imported: " + e.getMessage()
                + "  " + e.getClass();
            logger.severe(err);
            throw new CloudproofException(err, e);
        }
    }

    /**
     * Encrypt data in the KMS using the given Policy Attributes (@see {@link Attr}) and Public Master Key. The data is
     * encrypted using an hybrid encryption scheme + AÉS 256 GCM. No Metadata is added to the header and no resource uid
     * is used in the AES AEAD scheme. The generated cipher text is made of 3 parts - the length of the encrypted header
     * as a u32 in big endian format (4 bytes) - the header - the AES GCM encrypted content
     *
     * @param publicMasterKeyUniqueIdentifier the UID of the Public Key
     * @param data the data to encrypt
     * @param attributes the Policy Attributes
     * @return the encrypted data
     * @throws CloudproofException if the encryption fails
     */
    public byte[] coverCryptEncrypt(String publicMasterKeyUniqueIdentifier, byte[] data, Attr[] attributes)
        throws CloudproofException {
        return coverCryptEncrypt(publicMasterKeyUniqueIdentifier, data, attributes, Optional.empty());
    }

    /**
     * Encrypt data in the KMS using the given Policy Attributes (@see {@link Attr}) and Public Master Key. The data is
     * encrypted using an hybrid encryption scheme + AÉS 256 GCM. The uid is used in the authentication of the AES GCM
     * scheme. The generated cipher text is made of 3 parts - the length of the encrypted header as a u32 in big endian
     * format (4 bytes) - the header - the AES GCM encrypted content
     *
     * @param publicMasterKeyUniqueIdentifier the UID of the Public Key
     * @param data the data to encrypt
     * @param attributes the Policy Attributes
     * @param authenticationData the authentication data used in the AEAD of the symmetric scheme
     * @return the encrypted data
     * @throws CloudproofException if the encryption fails
     */
    public byte[] coverCryptEncrypt(String publicMasterKeyUniqueIdentifier, byte[] data, Attr[] attributes,
        byte[] authenticationData) throws CloudproofException {
        return coverCryptEncrypt(publicMasterKeyUniqueIdentifier, authenticationData, attributes,
            Optional.of(authenticationData));
    }

    /**
     * Encrypt data in the KMS using the given Policy Attributes (@see {@link Attr}) and Public Master Key. The data is
     * encrypted using an hybrid encryption scheme + AÉS 256 GCM. The uid is used in the authentication of the AES GCM
     * scheme. The generated cipher text is made of 3 parts - the length of the encrypted header as a u32 in big endian
     * format (4 bytes) - the header - the AES GCM encrypted content
     *
     * @param publicMasterKeyUniqueIdentifier the UID of the Public Key
     * @param data the data to encrypt
     * @param attributes the Policy Attributes
     * @param authenticationData the authentication data used in the AEAD of the symmetric scheme
     * @return the encrypted data
     * @throws CloudproofException if the encryption fails
     */
    byte[] coverCryptEncrypt(String publicMasterKeyUniqueIdentifier, byte[] data, Attr[] attributes,
        Optional<byte[]> authenticationData) throws CloudproofException {
        try {
            DataToEncrypt dataToEncrypt = new DataToEncrypt(attributes, data);
            ObjectMapper mapper = new ObjectMapper();
            byte[] bytes = mapper.writeValueAsBytes(dataToEncrypt);
            Encrypt request = new Encrypt(publicMasterKeyUniqueIdentifier, bytes, Optional.empty(),
                authenticationData.isPresent() ? Optional.of(authenticationData.get()) : Optional.empty());
            EncryptResponse response = this.kmip.encrypt(request);
            if (response.getData().isPresent()) {
                return response.getData().get();
            }
            throw new CloudproofException("No encrypted data in response !");
        } catch (CloudproofException e) {
            throw e;
        } catch (Exception e) {
            String err = "CoverCrypt" + " encryption failed: " + e.getMessage() + "  " + e.getClass();
            logger.severe(err);
            throw new CloudproofException(err, e);
        }
    }

    /**
     * Decrypt the data in the KMS using the given User Decryption Key The encryptedData should be made of 3 parts: -
     * the length of the encrypted header as a u32 in big endian format (4 bytes) - the header - the AES GCM encrypted
     * content
     *
     * @param userDecryptionKeyUniqueIdentifier the key UID
     * @param encryptedData the cipher text
     * @return the clear text data
     * @throws CloudproofException if the decryption fails
     */
    public byte[] coverCryptDecrypt(String userDecryptionKeyUniqueIdentifier, byte[] encryptedData)
        throws CloudproofException {
        return this.coverCryptDecrypt(userDecryptionKeyUniqueIdentifier, encryptedData, Optional.empty());
    }

    /**
     * Decrypt the data in the KMS using the given User Decryption Key The encryptedData should be made of 3 parts: -
     * the length of the encrypted header as a u32 in big endian format (4 bytes) - the header - the AES GCM encrypted
     * content
     *
     * @param userDecryptionKeyUniqueIdentifier the key UID
     * @param encryptedData the cipher text
     * @param authenticationData the data to use in the authentication of the symmetric scheme
     * @return the clear text data
     * @throws CloudproofException if the decryption fails
     */
    public byte[] coverCryptDecrypt(String userDecryptionKeyUniqueIdentifier, byte[] encryptedData,
        byte[] authenticationData)
        throws CloudproofException {
        return coverCryptDecrypt(userDecryptionKeyUniqueIdentifier, encryptedData, Optional.of(authenticationData));
    }

    /**
     * Decrypt the data in the KMS using the given User Decryption Key The encryptedData should be made of 3 parts: -
     * the length of the encrypted header as a u32 in big endian format (4 bytes) - the header - the AES GCM encrypted
     * content
     *
     * @param userDecryptionKeyUniqueIdentifier the key UID
     * @param encryptedData the cipher text
     * @param authenticationData the data to use in the authentication of the symmetric scheme
     * @return the clear text data
     * @throws CloudproofException if the decryption fails
     */
    byte[] coverCryptDecrypt(String userDecryptionKeyUniqueIdentifier, byte[] encryptedData,
        Optional<byte[]> authenticationData)
        throws CloudproofException {
        try {
            Decrypt request = new Decrypt(userDecryptionKeyUniqueIdentifier, encryptedData, authenticationData);
            DecryptResponse response = this.kmip.decrypt(request);
            if (response.getData().isPresent()) {
                return response.getData().get();
            }
            throw new CloudproofException("No decrypted data in response !");
        } catch (CloudproofException e) {
            throw e;
        } catch (Exception e) {
            String err = "CoverCrypt" + " decryption failed: " + e.getMessage() + "  " + e.getClass();
            logger.severe(err);
            throw new CloudproofException(err, e);
        }
    }

    /**
     * Rotate the given policy attributes. This will rekey in the KMS:
     * <ul>
     * <li>the Master Keys</li>
     * <li>all User Decryption Keys that contain one of these attributes in their policy and are not rotated.</li>
     * </ul>
     * Non Rekeyed User Decryption Keys cannot decrypt ata encrypted with the rekeyed Master Public Key and the given
     * attributes. <br>
     * Rekeyed User Decryption Keys however will be able to decrypt data encrypted by the previous Master Public Key and
     * the rekeyed one. <br>
     * Note: there is a limit on the number of revocations that can be performed which is set in the {@link Policy} when
     * Master Keys are created
     *
     * @param privateMasterKeyUniqueIdentifier the UID of the private master key
     * @param policyAttributes the array of {@link Attr}
     * @return the Master Public Key UID
     * @throws CloudproofException if the revocation fails
     */
    public String rotateCoverCryptAttributes(String privateMasterKeyUniqueIdentifier, Attr[] policyAttributes)
        throws CloudproofException {
        try {
            Attributes attributes = new Attributes(ObjectType.Private_Key,
                Optional.of(CryptographicAlgorithm.CoverCrypt));
            attributes.keyFormatType(Optional.of(KeyFormatType.CoverCryptSecretKey));
            attributes.vendorAttributes(Optional.of(new VendorAttribute[] {
                Attr.toVendorAttribute(policyAttributes, VendorAttribute.VENDOR_ATTR_COVER_CRYPT_ATTR)}));
            ReKeyKeyPair request = new ReKeyKeyPair(Optional.of(privateMasterKeyUniqueIdentifier), Optional.empty(),
                Optional.empty(),
                Optional.of(attributes), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty());
            ReKeyKeyPairResponse response = this.kmip.reKeyKeyPair(request);
            return response.getPublicKeyUniqueIdentifier();
        } catch (CloudproofException e) {
            throw e;
        } catch (Exception e) {
            String err = "Revocation of CoverCrypt policy attributes failed: "
                + e.getMessage() + "  " + e.getClass();
            logger.severe(err);
            throw new CloudproofException(err, e);
        }
    }

    /**
     * Revoke a key in the KMS which makes it unavailable to use in the KMS to perform
     * {@link #coverCryptEncrypt(String, byte[], Attr[])} or {@link #coverCryptDecrypt(String, byte[])} operations. <br>
     * <br>
     * If this key is a User Decryption Key, it will not be rekeyed in case of attribute revocation. <br>
     * <br>
     * Note: this revokes the key **inside** the KMS: it does not prevent an user who has a local copy of a User
     * Decryption Key to perform decryption operations.
     *
     * @param keyUniqueIdentifier the UID of the key to revoke
     * @return the UID of the revoked key
     * @throws CloudproofException if the revocation fails
     */
    public String revokeKey(String keyUniqueIdentifier) throws CloudproofException {
        try {
            Revoke request = new Revoke(Optional.of(keyUniqueIdentifier), new RevocationReason("Revoked"),
                Optional.empty());
            RevokeResponse response = this.kmip.revoke(request);
            return response.getUniqueIdentifier();
        } catch (CloudproofException e) {
            throw e;
        } catch (Exception e) {
            String err = "CoverCrypt" + " key revocation failed: " + e.getMessage() + "  " + e.getClass();
            logger.severe(err);
            throw new CloudproofException(err, e);
        }
    }

    /**
     * Destroy a key in the KMS which makes it unavailable to use in the KMS to perform
     * {@link #coverCryptEncrypt(String, byte[], Attr[])} or {@link #coverCryptDecrypt(String, byte[])} operations. <br>
     * <br>
     * Note: this destroy the key **inside** the KMS: it does not prevent an user who has a local copy of a User
     * Decryption Key to perform decryption operations.
     *
     * @param uniqueIdentifier the UID of the key to revoke
     * @return the UID of the destroyed key
     * @throws CloudproofException if the destruction fails
     */
    public String destroyKey(String uniqueIdentifier) throws CloudproofException {
        try {
            Destroy request = new Destroy(Optional.of(uniqueIdentifier));
            DestroyResponse response = kmip.destroy(request);
            return response.getUniqueIdentifier();
        } catch (CloudproofException e) {
            throw e;
        } catch (Exception e) {
            String err = "CoverCrypt" + " destroy key failed: " + e.getMessage() + "  " + e.getClass();
            logger.severe(err);
            throw new CloudproofException(err, e);
        }
    }
}
