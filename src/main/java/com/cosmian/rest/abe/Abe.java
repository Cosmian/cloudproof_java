package com.cosmian.rest.abe;

import java.util.Optional;
import java.util.logging.Logger;

import com.cosmian.CosmianException;
import com.cosmian.RestClient;
import com.cosmian.rest.abe.acccess_policy.AccessPolicy;
import com.cosmian.rest.abe.acccess_policy.Attr;
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
 * ABE (Attribute Based Encryption) endpoints
 */
public class Abe {

    private static final Logger logger = Logger.getLogger(Abe.class.getName());
    private final Kmip kmip;

    public Abe(RestClient rest_client) {
        this.kmip = new Kmip(rest_client);
    }

    /**
     * Generate inside the KMS, a master private and public key pair for the {@link Policy}
     * 
     * @param policy
     *            the Key Policy
     * @return a tuple containing the master private key UID and the master public key UID
     * @throws CosmianException
     *             if the creation fails
     */
    public String[] createMasterKeyPair(Policy policy) throws CosmianException {
        try {
            Attributes commonAttributes =
                new Attributes(ObjectType.Private_Key, Optional.of(CryptographicAlgorithm.ABE));
            commonAttributes.setKeyFormatType(Optional.of(KeyFormatType.AbeMasterSecretKey));

            // convert the Policy to attributes and attach it to the common attributes
            VendorAttribute policy_attribute = policy.toVendorAttribute();
            commonAttributes.setVendorAttributes(Optional.of(new VendorAttribute[] {policy_attribute}));

            CreateKeyPair request = new CreateKeyPair(Optional.of(commonAttributes), Optional.empty());
            CreateKeyPairResponse response = this.kmip.createKeyPair(request);
            return new String[] {response.getPrivateKeyUniqueIdentifier(), response.getPublicKeyUniqueIdentifier()};
        } catch (CosmianException e) {
            throw e;
        } catch (Exception e) {
            String err = "ABE: Master Key generation failed: " + e.getMessage() + "  " + e.getClass();
            logger.severe(err);
            throw new CosmianException(err, e);
        }
    }

    /**
     * Retrieve the ABE Master Private Key from the KMS
     * 
     * @param privateMasterKeyUniqueIdentifier
     *            the key UID
     * @return the Private Key
     * @throws CosmianException
     *             if the retrieval fails
     */
    public PrivateKey retrievePrivateMasterKey(String privateMasterKeyUniqueIdentifier) throws CosmianException {
        try {
            Get request = new Get(privateMasterKeyUniqueIdentifier);
            request.setKeyFormatType(Optional.of(KeyFormatType.AbeMasterSecretKey));
            //
            GetResponse response = this.kmip.get(request);
            Object object = response.getObject();
            if (!(object instanceof PrivateKey)) {
                throw new CosmianException(
                    "No ABE Private Master Key at identifier " + privateMasterKeyUniqueIdentifier);
            }
            PrivateKey sk = (PrivateKey)object;
            if (!sk.getKeyBlock().getKeyFormatType().equals(KeyFormatType.AbeMasterSecretKey)) {
                throw new CosmianException(
                    "No ABE Private Master Key at identifier " + privateMasterKeyUniqueIdentifier);
            }
            return sk;
        } catch (CosmianException e) {
            throw e;
        } catch (Exception e) {
            String err = "ABE: Private Master Key could not be retrieved: " + e.getMessage() + "  " + e.getClass();
            logger.severe(err);
            throw new CosmianException(err, e);
        }
    }

    /**
     * Import a Private Master Key in the KMS
     * 
     * @param uniqueIdentifier
     *            the UID of the key
     * @param privateMasterKey
     *            the key
     * @param replaceExisting
     *            if a key exists under this UID, replace it
     * @return the UID of the imported key
     * @throws CosmianException
     *             if the import fails
     */
    public String importPrivateMasterKey(String uniqueIdentifier, PrivateKey privateMasterKey, boolean replaceExisting)
        throws CosmianException {
        try {
            Import request = new Import(uniqueIdentifier, ObjectType.Private_Key, Optional.of(replaceExisting),
                Optional.empty(), privateMasterKey.attributes(), privateMasterKey);
            ImportResponse response = this.kmip.importObject(request);
            return response.getUniqueIdentifier();
        } catch (CosmianException e) {
            throw e;
        } catch (Exception e) {
            String err = "ABE: Private Master Key could not be imported: " + e.getMessage() + "  " + e.getClass();
            logger.severe(err);
            throw new CosmianException(err, e);
        }
    }

    /**
     * Retrieve the ABE Master Public Key from the KMS
     * 
     * @param publicMasterKeyUniqueIdentifier
     *            the key UID
     * @return the Public Key
     * @throws CosmianException
     *             if the retrieval fails
     */
    public PublicKey retrievePublicMasterKey(String publicMasterKeyUniqueIdentifier) throws CosmianException {
        try {
            Get request = new Get(publicMasterKeyUniqueIdentifier);
            request.setKeyFormatType(Optional.of(KeyFormatType.AbeMasterPublicKey));
            //
            GetResponse response = this.kmip.get(request);
            Object object = response.getObject();
            if (!(object instanceof PublicKey)) {
                throw new CosmianException("No ABE Public Master Key at identifier " + publicMasterKeyUniqueIdentifier);
            }
            PublicKey sk = (PublicKey)object;
            if (!sk.getKeyBlock().getKeyFormatType().equals(KeyFormatType.AbeMasterPublicKey)) {
                throw new CosmianException("No ABE Public Master Key at identifier " + publicMasterKeyUniqueIdentifier);
            }
            return sk;
        } catch (CosmianException e) {
            throw e;
        } catch (Exception e) {
            String err = "ABE: Public Master Key could not be retrieved: " + e.getMessage() + "  " + e.getClass();
            logger.severe(err);
            throw new CosmianException(err, e);
        }
    }

    /**
     * Import a Public Master Key in the KMS
     * 
     * @param uniqueIdentifier
     *            the UID of the key
     * @param publicMasterKey
     *            the key
     * @param replaceExisting
     *            if a key exists under this UID, replace it
     * @return the UID of the imported key
     * @throws CosmianException
     *             if the import fails
     */
    public String importPublicMasterKey(String uniqueIdentifier, PublicKey publicMasterKey, boolean replaceExisting)
        throws CosmianException {
        try {
            Import request = new Import(uniqueIdentifier, ObjectType.Public_Key, Optional.of(replaceExisting),
                Optional.empty(), publicMasterKey.attributes(), publicMasterKey);
            ImportResponse response = this.kmip.importObject(request);
            return response.getUniqueIdentifier();
        } catch (CosmianException e) {
            throw e;
        } catch (Exception e) {
            String err = "ABE: Public Master Key could not be imported: " + e.getMessage() + "  " + e.getClass();
            logger.severe(err);
            throw new CosmianException(err, e);
        }
    }

    /**
     * Create a User Decryption Key for the given {@link AccessPolicy} in the KMS
     * 
     * @param accessPolicy
     *            the {@link AccessPolicy}
     * @param privateMasterKeyUniqueIdentifier
     *            the UID of the Master Private Key
     * @return the UID of the newly created key
     * @throws CosmianException
     *             if the creation fails
     */
    public String createUserDecryptionKey(AccessPolicy accessPolicy, String privateMasterKeyUniqueIdentifier)
        throws CosmianException {
        try {
            Attributes commonAttributes =
                new Attributes(ObjectType.Private_Key, Optional.of(CryptographicAlgorithm.ABE));
            commonAttributes.setKeyFormatType(Optional.of(KeyFormatType.AbeUserDecryptionKey));

            // convert the Access Policy to attributes and attach it to the common
            // attributes
            VendorAttribute accessPolicyAttribute = accessPolicy.toVendorAttribute();
            commonAttributes.setVendorAttributes(Optional.of(new VendorAttribute[] {accessPolicyAttribute}));
            // link to the master private key
            commonAttributes.setLink(new Link[] {
                new Link(LinkType.Parent_Link, new LinkedObjectIdentifier(privateMasterKeyUniqueIdentifier))});

            Create request = new Create(ObjectType.Private_Key, commonAttributes, Optional.empty());
            CreateResponse response = this.kmip.create(request);
            return response.getUniqueIdentifier();
        } catch (CosmianException e) {
            throw e;
        } catch (Exception e) {
            String err = "ABE: Master Key generation failed: " + e.getMessage() + " " + e.getClass();
            logger.severe(err);
            throw new CosmianException(err, e);
        }
    }

    /**
     * Retrieve a User Decryption Key from the KMS
     * 
     * @param userDecryptionKeyUniqueIdentifier
     *            the key UID
     * @return the User Decryption Key
     * @throws CosmianException
     *             if the retrieval fails
     */
    public PrivateKey retrieveUserDecryptionKey(String userDecryptionKeyUniqueIdentifier) throws CosmianException {
        try {
            Get request = new Get(userDecryptionKeyUniqueIdentifier);
            request.setKeyFormatType(Optional.of(KeyFormatType.AbeUserDecryptionKey));
            //
            GetResponse response = this.kmip.get(request);
            Object object = response.getObject();
            if (!(object instanceof PrivateKey)) {
                throw new CosmianException(
                    "No ABE User Decryption Key at identifier " + userDecryptionKeyUniqueIdentifier);
            }
            PrivateKey sk = (PrivateKey)object;
            if (!sk.getKeyBlock().getKeyFormatType().equals(KeyFormatType.AbeUserDecryptionKey)) {
                throw new CosmianException(
                    "No ABE User Decryption Key at identifier " + userDecryptionKeyUniqueIdentifier);
            }
            return sk;
        } catch (CosmianException e) {
            throw e;
        } catch (Exception e) {
            String err = "ABE: User Decryption Key could not be retrieved: " + e.getMessage() + "  " + e.getClass();
            logger.severe(err);
            throw new CosmianException(err, e);
        }
    }

    /**
     * Import a User Decryption Key in the KMS
     * 
     * @param uniqueIdentifier
     *            the UID of the key
     * @param userDecryptionKey
     *            the key
     * @param replaceExisting
     *            if a key exists under this UID, replace it
     * @return the UID of the imported key
     * @throws CosmianException
     *             if the import fails
     */
    public String importUserDecryptionKey(String uniqueIdentifier, PrivateKey userDecryptionKey,
        boolean replaceExisting) throws CosmianException {
        try {
            Import request = new Import(uniqueIdentifier, ObjectType.Private_Key, Optional.of(replaceExisting),
                Optional.empty(), userDecryptionKey.attributes(), userDecryptionKey);
            ImportResponse response = this.kmip.importObject(request);
            return response.getUniqueIdentifier();
        } catch (CosmianException e) {
            throw e;
        } catch (Exception e) {
            String err = "ABE: User Decryption Key could not be imported: " + e.getMessage() + "  " + e.getClass();
            logger.severe(err);
            throw new CosmianException(err, e);
        }
    }

    /**
     * Encrypt data in the KMS using the given Policy Attributes (@see {@link Attr}) and Public Master Key. The data is
     * encrypted using an hybrid encryption scheme ABE + AÉS 256 GCM. No Metadata is added to the header and no resource
     * uid is used in the AES AEAD scheme.
     * 
     * The generated cipher text is made of 3 parts - the length of the encrypted header as a u32 in big endian format
     * (4 bytes) - the header - the AES GCM encrypted content
     * 
     * @param publicMasterKeyUniqueIdentifier
     *            the UID of the Public Key
     * @param data
     *            the data to encrypt
     * @param attributes
     *            the Policy Attributes
     * @return the encrypted data
     * @throws CosmianException
     *             if the encryption fails
     */
    public byte[] kmsEncrypt(String publicMasterKeyUniqueIdentifier, byte[] data, Attr[] attributes)
        throws CosmianException {
        return kmsEncrypt(publicMasterKeyUniqueIdentifier, data, attributes, Optional.empty());
    }

    /**
     * Encrypt data in the KMS using the given Policy Attributes (@see {@link Attr}) and Public Master Key. The data is
     * encrypted using an hybrid encryption scheme ABE + AÉS 256 GCM.
     * 
     * The uid is used in the authentication of the AES GCM scheme. If supplied it will be saved encrypted as part of
     * the header
     * 
     * The generated cipher text is made of 3 parts - the length of the encrypted header as a u32 in big endian format
     * (4 bytes) - the header - the AES GCM encrypted content
     * 
     * @param publicMasterKeyUniqueIdentifier
     *            the UID of the Public Key
     * @param data
     *            the data to encrypt
     * @param attributes
     *            the Policy Attributes
     * @param uid
     *            the UID uses in the AEAD of the symmetric scheme
     * @return the encrypted data
     * @throws CosmianException
     *             if the encryption fails
     */
    public byte[] kmsEncrypt(String publicMasterKeyUniqueIdentifier, byte[] data, Attr[] attributes,
        Optional<byte[]> uid) throws CosmianException {
        try {
            DataToEncrypt dataToEncrypt = new DataToEncrypt(attributes, data);
            ObjectMapper mapper = new ObjectMapper();
            byte[] bytes = mapper.writeValueAsBytes(dataToEncrypt);
            Encrypt request = new Encrypt(publicMasterKeyUniqueIdentifier, bytes, Optional.empty(),
                uid.isPresent() ? Optional.of(uid.get()) : Optional.empty());
            EncryptResponse response = this.kmip.encrypt(request);
            if (response.getData().isPresent()) {
                return response.getData().get();
            }
            throw new CosmianException("No encrypted data in response !");
        } catch (CosmianException e) {
            throw e;
        } catch (Exception e) {
            String err = "ABE encryption failed: " + e.getMessage() + "  " + e.getClass();
            logger.severe(err);
            throw new CosmianException(err, e);
        }
    }

    /**
     * Decrypt the data in the KMS using the given User Decryption Key
     * 
     * The encryptedData should be made of 3 parts: - the length of the encrypted header as a u32 in big endian format
     * (4 bytes) - the header - the AES GCM encrypted content
     * 
     * @param userDecryptionKeyUniqueIdentifier
     *            the key UID
     * @param encryptedData
     *            the cipher text
     * @return the clear text data
     * @throws CosmianException
     *             if the decryption fails
     */
    public byte[] kmsDecrypt(String userDecryptionKeyUniqueIdentifier, byte[] encryptedData) throws CosmianException {
        return this.kmsDecrypt(userDecryptionKeyUniqueIdentifier, encryptedData, Optional.empty());
    }

    /**
     * Decrypt the data in the KMS using the given User Decryption Key
     * 
     * The encryptedData should be made of 3 parts: - the length of the encrypted header as a u32 in big endian format
     * (4 bytes) - the header - the AES GCM encrypted content
     * 
     * @param userDecryptionKeyUniqueIdentifier
     *            the key UID
     * @param encryptedData
     *            the cipher text
     * @param uid
     *            the resource uid to use in the authentication of the symmetric scheme
     * @return the clear text data
     * @throws CosmianException
     *             if the decryption fails
     */
    public byte[] kmsDecrypt(String userDecryptionKeyUniqueIdentifier, byte[] encryptedData, Optional<byte[]> uid)
        throws CosmianException {
        try {
            Decrypt request = new Decrypt(userDecryptionKeyUniqueIdentifier, encryptedData, uid);
            DecryptResponse response = this.kmip.decrypt(request);
            if (response.getData().isPresent()) {
                return response.getData().get();
            }
            throw new CosmianException("No decrypted data in response !");
        } catch (CosmianException e) {
            throw e;
        } catch (Exception e) {
            String err = "ABE decryption failed: " + e.getMessage() + "  " + e.getClass();
            logger.severe(err);
            throw new CosmianException(err, e);
        }
    }

    /**
     * Revoke the given ABE policy attributes.
     * 
     * This will rekey in the KMS:
     * <ul>
     * <li>the Master Public Key</li>
     * <li>all User Decryption Keys that contain one of these attributes in their policy and are not revoked.</li>
     * </ul>
     * 
     * Non Rekeyed User Decryption Keys cannot decrypt ata encrypted with the rekeyed Master Public Key and the given
     * attributes. <br>
     * Rekeyed User Decryption Keys however will be able to decrypt data encrypted by the previous Master Public Key and
     * the rekeyed one. <br>
     * Note: there is a limit on the number of revocations that can be performed which is set in the {@link Policy} when
     * Master Keys are created
     * 
     * @param privateMasterKeyUniqueIdentifier
     *            the UID of the private master key
     * @param abePolicyAttributes
     *            the array of {@link Attr}
     * @return the Master Public Key UID
     * @throws CosmianException
     *             if the revocation fails
     */
    public String revokeAttributes(String privateMasterKeyUniqueIdentifier, Attr[] abePolicyAttributes)
        throws CosmianException {
        try {
            Attributes attributes = new Attributes(ObjectType.Private_Key, Optional.of(CryptographicAlgorithm.ABE));
            attributes.keyFormatType(Optional.of(KeyFormatType.AbeMasterSecretKey));
            attributes
                .vendorAttributes(Optional.of(new VendorAttribute[] {Attr.toVendorAttribute(abePolicyAttributes)}));
            ReKeyKeyPair request =
                new ReKeyKeyPair(Optional.of(privateMasterKeyUniqueIdentifier), Optional.empty(), Optional.empty(),
                    Optional.of(attributes), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty());
            ReKeyKeyPairResponse response = this.kmip.reKeyKeyPair(request);
            return response.getPublicKeyUniqueIdentifier();
        } catch (CosmianException e) {
            throw e;
        } catch (Exception e) {
            String err = "Revocation of ABE policy attributes failed: " + e.getMessage() + "  " + e.getClass();
            logger.severe(err);
            throw new CosmianException(err, e);
        }
    }

    /**
     * Revoke a key in the KMS which makes it unavailable to use in the KMS to perform
     * {@link #kmsEncrypt(String, byte[], Attr[])} or {@link #kmsDecrypt(String, byte[])} operations. <br>
     * <br>
     * If this key is a User Decryption Key, it will not be rekeyed in case of attribute revocation. <br>
     * <br>
     * Note: this revokes the key **inside** the KMS: it does not prevent an user who has a local copy of a User
     * Decryption Key to perform decryption operations.
     * 
     * @param keyUniqueIdentifier
     *            the UID of the key to revoke
     * @return the UID of the revoked key
     * @throws CosmianException
     *             if the revocation fails
     */
    public String revokeKey(String keyUniqueIdentifier) throws CosmianException {
        try {
            Revoke request =
                new Revoke(Optional.of(keyUniqueIdentifier), new RevocationReason("Revoked"), Optional.empty());
            RevokeResponse response = this.kmip.revoke(request);
            return response.getUniqueIdentifier();
        } catch (CosmianException e) {
            throw e;
        } catch (Exception e) {
            String err = "ABE key revocation failed: " + e.getMessage() + "  " + e.getClass();
            logger.severe(err);
            throw new CosmianException(err, e);
        }
    }
}
