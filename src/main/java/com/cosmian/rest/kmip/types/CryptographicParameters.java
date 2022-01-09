package com.cosmian.rest.kmip.types;

import java.util.Optional;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * The Cryptographic Parameters attribute is a structure that contains a set of OPTIONAL fields that describe certain
 * cryptographic parameters to be used when performing cryptographic operations using the object. Specific fields MAY
 * pertain only to certain types of Managed Objects. The Cryptographic Parameters attribute of a Certificate object
 * identifies the cryptographic parameters of the public key contained within the Certificate.
 *
 * The Cryptographic Algorithm is also used to specify the parameters for cryptographic operations. For operations
 * involving digital signatures, either the Digital Signature Algorithm can be specified or the Cryptographic Algorithm
 * and Hashing Algorithm combination can be specified. Random IV can be used to request that the KMIP server generate an
 * appropriate IV for a cryptographic operation that uses an IV. The generated Random IV is returned in the response to
 * the cryptographic operation.
 *
 * IV Length is the length of the Initialization Vector in bits. This parameter SHALL be provided when the specified
 * Block Cipher Mode supports variable IV lengths such as CTR or GCM. Tag Length is the length of the authenticator tag
 * in bytes. This parameter SHALL be provided when the Block Cipher Mode is GCM.
 *
 * The IV used with counter modes of operation (e.g., CTR and GCM) cannot repeat for a given cryptographic key. To
 * prevent an IV/key reuse, the IV is often constructed of three parts: a fixed field, an invocation field, and a
 * counter as described in [SP800-38A] and [SP800-38D]. The Fixed Field Length is the length of the fixed field portion
 * of the IV in bits. The Invocation Field Length is the length of the invocation field portion of the IV in bits. The
 * Counter Length is the length of the counter portion of the IV in bits.
 *
 * Initial Counter Value is the starting counter value for CTR mode (for [RFC3686] it is 1).
 */
public class CryptographicParameters {

    @JsonProperty("BlockCipherMode")
    private Optional<BlockCipherMode> block_cipher_mode = Optional.empty();

    @JsonProperty("PaddingMethod")
    private Optional<PaddingMethod> padding_method = Optional.empty();

    @JsonProperty("HashingAlgorithm")
    private Optional<HashingAlgorithm> hashing_algorithm = Optional.empty();

    @JsonProperty("KeyRoleType")
    private Optional<KeyRoleType> key_role_type = Optional.empty();

    @JsonProperty("DigitalSignatureAlgorithm")
    private Optional<DigitalSignatureAlgorithm> digital_signature_algorithm = Optional.empty();

    @JsonProperty("CryptographicAlgorithm")
    private Optional<CryptographicAlgorithm> cryptographic_algorithm = Optional.empty();

    @JsonProperty("RandomIv")
    private Optional<Boolean> random_iv = Optional.empty();

    @JsonProperty("IvLength")
    private Optional<Integer> iv_length = Optional.empty();

    @JsonProperty("TagLength")
    private Optional<Integer> tag_length = Optional.empty();

    @JsonProperty("FixedFieldLength")
    private Optional<Integer> fixed_field_length = Optional.empty();

    @JsonProperty("InvocationFieldLength")
    private Optional<Integer> invocation_field_length = Optional.empty();

    @JsonProperty("CounterLength")
    private Optional<Integer> counter_length = Optional.empty();

    @JsonProperty("InitialCounterValue")
    private Optional<Integer> initial_counter_value = Optional.empty();
    /// if omitted, defaults to the block size of the Mask Generator Hashing Algorithm
    /// Cosmian extension: In AES: used as the number of additional data at the end of the
    /// submitted data that become part of the MAC calculation. These additional data are removed
    /// from the encrypted data

    @JsonProperty("SaltLength")
    private Optional<Integer> salt_length = Optional.empty();
    /// if omitted defaults to MGF1

    @JsonProperty("MaskGenerator")
    private Optional<MaskGenerator> mask_generator = Optional.empty();
    /// if omitted defaults to SHA-1

    @JsonProperty("MaskGeneratorHashingAlgorithm")
    private Optional<HashingAlgorithm> mask_generator_hashing_algorithm = Optional.empty();

    @JsonProperty("PSource")
    private Optional<byte[]> p_source = Optional.empty();

    @JsonProperty("TrailerField")
    private Optional<Integer> trailer_field = Optional.empty();

    public static CryptographicParameters empty() {
        return new CryptographicParameters();
    }

}
