package com.cosmian.rest.kmip.types;

import java.util.Map;

import com.cosmian.rest.kmip.json.KmipEnumUtils;
import com.fasterxml.jackson.annotation.JsonProperty;

public enum Tag {
    ActivationDate(0x42_0001), ApplicationData(0x42_0002), ApplicationNamespace(0x42_0003),
    ApplicationSpecific_Information(0x42_0004), ArchiveDate(0x42_0005), AsynchronousCorrelation_Value(0x42_0006),
    AsynchronousIndicator(0x42_0007), Attribute(0x42_0008), AttributeName(0x42_000A), AttributeValue(0x42_000B),
    Authentication(0x42_000C), BatchCount(0x42_000D), BatchErrorContinuationOption(0x42_000E), BatchItem(0x42_000F),
    BatchOrderOption(0x42_0010), BlockCipherMode(0x42_0011), CancellationResult(0x42_0012), Certificate(0x42_0013),
    CertificateRequest(0x42_0018), CertificateRequestType(0x42_0019), CertificateType(0x42_001D),
    CertificateValue(0x42_001E), CompromiseDate(0x42_0020), CompromiseOccurrenceDate(0x42_0021),
    ContactInformation(0x42_0022), Credential(0x42_0023), CredentialType(0x42_0024), CredentialValue(0x42_0025),
    CriticalityIndicator(0x42_0026), CRTCoefficient(0x42_0027), @JsonProperty(value = "Tag")
    ATag(0x42_0028), CryptographicDomainParameters(0x42_0029), CryptographicLength(0x42_002A),
    CryptographicParameters(0x42_002B), CryptographicUsageMask(0x42_002C), D(0x42_002E), DeactivationDate(0x42_002F),
    DerivationData(0x42_0030), DerivationMethod(0x42_0031), DerivationParameters(0x42_0032), DestroyDate(0x42_0033),
    Digest(0x42_0034), DigestValue(0x42_0035), EncryptionKeyInformation(0x42_0036), G(0x42_0037),
    HashingAlgorithm(0x42_0038), InitialDate(0x42_0039), InitializationVector(0x42_003A), IterationCount(0x42_003C),
    IVCounterNonce(0x42_003D), J(0x42_003E), Key(0x42_003F), KeyBlock(0x42_0040), KeyCompressionType(0x42_0041),
    KeyFormatType(0x42_0042), KeyMaterial(0x42_0043), KeyPartIdentifier(0x42_0044), KeyValue(0x42_0045),
    KeyWrappingData(0x42_0046), KeyWrappingSpecification(0x42_0047), LastChangeDate(0x42_0048), LeaseTime(0x42_0049),
    Link(0x42_004A), LinkType(0x42_004B), LinkedObjectIdentifier(0x42_004C), MACSignature(0x42_004D),
    MACSignatureKey_Information(0x42_004E), MaximumItems(0x42_004F), MaximumResponseSize(0x42_0050),
    MessageExtension(0x42_0051), Modulus(0x42_0052), Name(0x42_0053), NameType(0x42_0054), NameValue(0x42_0055),
    ObjectGroup(0x42_0056), ObjectType(0x42_0057), Offset(0x42_0058), OpaqueDataType(0x42_0059),
    OpaqueDataValue(0x42_005A), OpaqueObject(0x42_005B), Operation(0x42_005C), P(0x42_005E), PaddingMethod(0x42_005F),
    PrimeExponentP(0x42_0060), PrimeExponentQ(0x42_0061), PrimeFieldSize(0x42_0062), PrivateExponent(0x42_0063),
    PrivateKey(0x42_0064), PrivateKeyUniqueIdentifier(0x42_0066), ProcessStartDate(0x42_0067),
    ProtectStopDate(0x42_0068), ProtocolVersion(0x42_0069), ProtocolVersionMajor(0x42_006A),
    ProtocolVersionMinor(0x42_006B), PublicExponent(0x42_006C), PublicKey(0x42_006D),
    PublicKeyUniqueIdentifier(0x42_006F), PutFunction(0x42_0070), Q(0x42_0071), QString(0x42_0072), Qlength(0x42_0073),
    QueryFunction(0x42_0074), RecommendedCurve(0x42_0075), ReplacedUniqueIdentifier(0x42_0076),
    RequestHeader(0x42_0077), RequestMessage(0x42_0078), RequestPayload(0x42_0079), ResponseHeader(0x42_007A),
    ResponseMessage(0x42_007B), ResponsePayload(0x42_007C), ResultMessage(0x42_007D), ResultReason(0x42_007E),
    ResultStatus(0x42_007F), RevocationMessage(0x42_0080), RevocationReason(0x42_0081), RevocationReasonCode(0x42_0082),
    KeyRoleType(0x42_0083), Salt(0x42_0084), SecretData(0x42_0085), SecretDataType(0x42_0086),
    ServerInformation(0x42_0088), SplitKey(0x42_0089), SplitKeyMethod(0x42_008A), SplitKeyParts(0x42_008B),
    SplitKeyThreshold(0x42_008C), State(0x42_008D), StorageStatusMask(0x42_008E), SymmetricKey(0x42_008F),
    TimeStamp(0x42_0092), UniqueBatchItemID(0x42_0093), UniqueIdentifier(0x42_0094), UsageLimits(0x42_0095),
    UsageLimitsCount(0x42_0096), UsageLimitsTotal(0x42_0097), UsageLimitsUnit(0x42_0098), Username(0x42_0099),
    ValidityDate(0x42_009A), ValidityIndicator(0x42_009B), VendorExtension(0x42_009C), VendorIdentification(0x42_009D),
    WrappingMethod(0x42_009E), X(0x42_009F), Y(0x42_00A0), Password(0x42_00A1), DeviceIdentifier(0x42_00A2),
    EncodingOption(0x42_00A3), ExtensionInformation(0x42_00A4), ExtensionName(0x42_00A5), ExtensionTag(0x42_00A6),
    ExtensionType(0x42_00A7), Fresh(0x42_00A8), MachineIdentifier(0x42_00A9), MediaIdentifier(0x42_00AA),
    NetworkIdentifier(0x42_00AB), ObjectGroupMember(0x42_00AC), CertificateLength(0x42_00AD),
    DigitalSignatureAlgorithm(0x42_00AE), CertificateSerialNumber(0x42_00AF), DeviceSerialNumber(0x42_00B0),
    IssuerAlternativeName(0x42_00B1), IssuerDistinguishedName(0x42_00B2), SubjectAlternativeName(0x42_00B3),
    SubjectDistinguishedName(0x42_00B4), X509CertificateIdentifier(0x42_00B5), X509CertificateIssuer(0x42_00B6),
    X509CertificateSubject(0x42_00B7), KeyValueLocation(0x42_00B8), KeyValueLocationValue(0x42_00B9),
    KeyValueLocationType(0x42_00BA), KeyValuePresent(0x42_00BB), OriginalCreationDate(0x42_00BC), PGPKey(0x42_00BD),
    PGPKeyVersion(0x42_00BE), AlternativeName(0x42_00BF), AlternativeNameValue(0x42_00C0),
    AlternativeNameType(0x42_00C1), Data(0x42_00C2), SignatureData(0x42_00C3), DataLength(0x42_00C4),
    RandomIV(0x42_00C5), MACData(0x42_00C6), AttestationType(0x42_00C7), Nonce(0x42_00C8), NonceID(0x42_00C9),
    NonceValue(0x42_00CA), AttestationMeasurement(0x42_00CB), AttestationAssertion(0x42_00CC), IVLength(0x42_00CD),
    TagLength(0x42_00CE), FixedFieldLength(0x42_00CF), CounterLength(0x42_00D0), InitialCounterValue(0x42_00D1),
    InvocationFieldLength(0x42_00D2), AttestationCapableIndicator(0x42_00D3), OffsetItems(0x42_00D4),
    LocatedItems(0x42_00D5), CorrelationValue(0x42_00D6), InitIndicator(0x42_00D7), FinalIndicator(0x42_00D8),
    RNGParameters(0x42_00D9), RNGAlgorithm(0x42_00DA), DRBGAlgorithm(0x42_00DB), FIPS186Variation(0x42_00DC),
    PredictionResistance(0x42_00DD), RandomNumberGenerator(0x42_00DE), ValidationInformation(0x42_00DF),
    ValidationAuthorityType(0x42_00E0), ValidationAuthorityCountry(0x42_00E1), ValidationAuthorityURI(0x42_00E2),
    ValidationVersionMajor(0x42_00E3), ValidationVersionMinor(0x42_00E4), ValidationType(0x42_00E5),
    ValidationLevel(0x42_00E6), ValidationCertificateIdentifier(0x42_00E7), ValidationCertificateURI(0x42_00E8),
    ValidationVendorURI(0x42_00E9), ValidationProfile(0x42_00EA), ProfileInformation(0x42_00EB), ProfileName(0x42_00EC),
    ServerURI(0x42_00ED), ServerPort(0x42_00EE), StreamingCapability(0x42_00EF), AsynchronousCapability(0x42_00F0),
    AttestationCapability(0x42_00F1), UnwrapMode(0x42_00F2), DestroyAction(0x42_00F3), ShreddingAlgorithm(0x42_00F4),
    RNGMode(0x42_00F5), ClientRegistrationMethod(0x42_00F6), CapabilityInformation(0x42_00F7), KeyWrapType(0x42_00F8),
    BatchUndoCapability(0x42_00F9), BatchContinueCapability(0x42_00FA), PKCS12FriendlyName(0x42_00FB),
    Description(0x42_00FC), Comment(0x42_00FD), AuthenticatedEncryptionAdditionalData(0x42_00FE),
    AuthenticatedEncryptionTag(0x42_00FF), SaltLength(0x42_0100), MaskGenerator(0x42_0101),
    MaskGeneratorHashingAlgorithm(0x42_0102), PSource(0x42_0103), TrailerField(0x42_0104),
    ClientCorrelationValue(0x42_0105), ServerCorrelationValue(0x42_0106), DigestedData(0x42_0107),
    CertificateSubjectCN(0x42_0108), CertificateSubjectO(0x42_0109), CertificateSubjectOU(0x42_010A),
    CertificateSubjectEmail(0x42_010B), CertificateSubjectC(0x42_010C), CertificateSubjectST(0x42_010D),
    CertificateSubjectL(0x42_010E), CertificateSubjectUID(0x42_010F), CertificateSubjectSerialNumber(0x42_0110),
    CertificateSubjectTitle(0x42_0111), CertificateSubjectDC(0x42_0112), CertificateSubjectDNQualifier(0x42_0113),
    CertificateIssuerCN(0x42_0114), CertificateIssuerO(0x42_0115), CertificateIssuerOU(0x42_0116),
    CertificateIssuerEmail(0x42_0117), CertificateIssuerC(0x42_0118), CertificateIssuerST(0x42_0119),
    CertificateIssuerL(0x42_011A), CertificateIssuerUID(0x42_011B), CertificateIssuerSerialNumber(0x42_011C),
    CertificateIssuerTitle(0x42_011D), CertificateIssuerDC(0x42_011E), CertificateIssuerDNQualifier(0x42_011F),
    Sensitive(0x42_0120), AlwaysSensitive(0x42_0121), Extractable(0x42_0122), NeverExtractable(0x42_0123),
    ReplaceExisting(0x42_0124), Attributes(0x42_0125), CommonAttributes(0x42_0126), PrivateKeyAttributes(0x42_0127),
    PublicKeyAttributes(0x42_0128), ExtensionEnumeration(0x42_0129), ExtensionAttribute(0x42_012A),
    ExtensionParentStructureTag(0x42_012B), ExtensionDescription(0x42_012C), ServerName(0x42_012D),
    ServerSerialNumber(0x42_012E), ServerVersion(0x42_012F), ServerLoad(0x42_0130), ProductName(0x42_0131),
    BuildLevel(0x42_0132), BuildDate(0x42_0133), ClusterInfo(0x42_0134), AlternateFailoverEndpoints(0x42_0135),
    ShortUniqueIdentifier(0x42_0136), Reserved(0x42_0137), @JsonProperty(value = "Tag")
    AlternateTag(0x42_0138), CertificateRequestUniqueIdentifier(0x42_0139), NISTKeyType(0x42_013A),
    AttributeReference(0x42_013B), CurrentAttribute(0x42_013C), NewAttribute(0x42_013D),
    CertificateRequestValue(0x42_0140), LogMessage(0x42_0141), ProfileVersion(0x42_0142),
    ProfileVersionMajor(0x42_0143), ProfileVersionMinor(0x42_0144), ProtectionLevel(0x42_0145),
    ProtectionPeriod(0x42_0146), QuantumSafe(0x42_0147), QuantumSafeCapability(0x42_0148), Ticket(0x42_0149),
    TicketType(0x42_014A), TicketValue(0x42_014B), RequestCount(0x42_014C), Rights(0x42_014D), Objects(0x42_014E),
    Operations(0x42_014F), Right(0x42_0150), EndpointRole(0x42_0151), DefaultsInformation(0x42_0152),
    ObjectDefaults(0x42_0153), Ephemeral(0x42_0154), ServerHashedPassword(0x42_0155), OneTimePassword(0x42_0156),
    HashedPassword(0x42_0157), AdjustmentType(0x42_0158), PKCS11Interface(0x42_0159), PKCS11Function(0x42_015A),
    PKCS11InputParameters(0x42_015B), PKCS11OutputParameters(0x42_015C), PKCS11ReturnCode(0x42_015D),
    ProtectionStorageMask(0x42_015E), ProtectionStorageMasks(0x42_015F), InteropFunction(0x42_0160),
    InteropIdentifier(0x42_0161), AdjustmentValue(0x42_0162), CommonProtectionStorageMasks(0x42_0163),
    PrivateProtectionStorageMasks(0x42_0164), PublicProtectionStorageMasks(0x42_0165);
    // Extensions 540000 – 54FFFF

    private final int code;

    private Tag(int code) {
        this.code = code;
    }

    public int getCode() {
        return this.code;
    }

    @Override
    public String toString() {
        return KmipEnumUtils.to_string(this);
    }

    static Map<String, Tag> ENUM_MAP = KmipEnumUtils.to_map(Tag.values());

    public static Tag from(String name) throws IllegalArgumentException {
        Tag o = ENUM_MAP.get(name);
        if (o == null) {
            throw new IllegalArgumentException("No Tag with name: " + name);
        }
        return o;
    }

    public static Tag from(int code) throws IllegalArgumentException {
        for (Tag value : values())
            if (value.code == code)
                return value;
        throw new IllegalArgumentException("No ObjectType with code: " + code);
    }
}
