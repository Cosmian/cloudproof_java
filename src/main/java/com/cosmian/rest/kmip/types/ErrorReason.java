package com.cosmian.rest.kmip.types;

import java.util.Map;

import com.cosmian.rest.kmip.json.KmipEnumUtils;

public enum ErrorReason {

    Item_Not_Found(0x0000_0001),
    Response_Too_Large(0x0000_0002),
    Authentication_Not_Successful(0x0000_0003),
    Invalid_Message(0x0000_0004),
    Operation_Not_Supported(0x0000_0005),
    Missing_Data(0x0000_0006),
    Invalid_Field(0x0000_0007),
    Feature_Not_Supported(0x0000_0008),
    Operation_Canceled_By_Requester(0x0000_0009),
    Cryptographic_Failure(0x0000_000A),
    Permission_Denied(0x0000_000C),
    Object_Archived(0x0000_000D),
    Application_Namespace_Not_Supported(0x0000_000F),
    Key_Format_Type_Not_Supported(0x0000_0010),
    Key_Compression_Type_Not_Supported(0x0000_0011),
    Encoding_Option_Error(0x0000_0012),
    Key_Value_Not_Present(0x0000_0013),
    Attestation_Required(0x0000_0014),
    Attestation_Failed(0x0000_0015),
    Sensitive(0x0000_0016),
    Not_Extractable(0x0000_0017),
    Object_Already_Exists(0x0000_0018),
    Invalid_Ticket(0x0000_0019),
    Usage_Limit_Exceeded(0x0000_001A),
    Numeric_Range(0x0000_001B),
    Invalid_Data_Type(0x0000_001C),
    Read_Only_Attribute(0x0000_001D),
    Multi_Valued_Attribute(0x0000_001E),
    Unsupported_Attribute(0x0000_001F),
    Attribute_Instance_Not_Found(0x0000_0020),
    Attribute_Not_Found(0x0000_0021),
    Attribute_Read_Only(0x0000_0022),
    Attribute_Single_Valued(0x0000_0023),
    Bad_Cryptographic_Parameters(0x0000_0024),
    Bad_Password(0x0000_0025),
    Codec_Error(0x0000_0026),
    Illegal_Object_Type(0x0000_0028),
    Incompatible_Cryptographic_Usage_Mask(0x0000_0029),
    Internal_Server_Error(0x0000_002A),
    Invalid_Asynchronous_Correlation_Value(0x0000_002B),
    Invalid_Attribute(0x0000_002C),
    Invalid_Attribute_Value(0x0000_002D),
    Invalid_Correlation_Value(0x0000_002E),
    Invalid_CSR(0x0000_002F),
    Invalid_Object_Type(0x0000_0030),
    Key_Wrap_Type_Not_Supported(0x0000_0032),
    Missing_Initialization_Vector(0x0000_0034),
    Non_Unique_Name_Attribute(0x0000_0035),
    Object_Destroyed(0x0000_0036),
    Object_Not_Found(0x0000_0037),
    Not_Authorised(0x0000_0039),
    Server_Limit_Exceeded(0x0000_003A),
    Unknown_Enumeration(0x0000_003B),
    Unknown_Message_Extension(0x0000_003C),
    Unknown_Tag(0x0000_003D),
    Unsupported_Cryptographic_Parameters(0x0000_003E),
    Unsupported_Protocol_Version(0x0000_003F),
    Wrapping_Object_Archived(0x0000_0040),
    Wrapping_Object_Destroyed(0x0000_0041),
    Wrapping_Object_Not_Found(0x0000_0042),
    Wrong_Key_Lifecycle_State(0x0000_0043),
    Protection_Storage_Unavailable(0x0000_0044),
    PKCS_11_Codec_Error(0x0000_0045),
    PKCS_11_Invalid_Function(0x0000_0046),
    PKCS_11_Invalid_Interface(0x0000_0047),
    Private_Protection_Storage_Unavailable(0x0000_0048),
    Public_Protection_Storage_Unavailable(0x0000_0049),
    General_Failure(0x0000_0100);

    private final int code;

    private ErrorReason(int code) {
        this.code = code;
    }

    public int getCode() {
        return this.code;
    }

    @Override
    public String toString() {
        return KmipEnumUtils.to_string(this);
    }

    static Map<String, ErrorReason> ENUM_MAP = KmipEnumUtils.to_map(ErrorReason.values());

    public static ErrorReason from(String name) throws IllegalArgumentException {
        ErrorReason o = ENUM_MAP.get(name);
        if (o == null) {
            throw new IllegalArgumentException("No ErrorReason with name: " + name);
        }
        return o;
    }

    public static ErrorReason from(int code) throws IllegalArgumentException {
        for (ErrorReason value : values())
            if (value.code == code)
                return value;
        throw new IllegalArgumentException("No ErrorReason with code: " + code);
    }
}
