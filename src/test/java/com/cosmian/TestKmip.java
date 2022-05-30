package com.cosmian;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.Optional;

import com.cosmian.rest.abe.policy.Policy;
import com.cosmian.rest.kmip.data_structures.KeyBlock;
import com.cosmian.rest.kmip.data_structures.KeyMaterial;
import com.cosmian.rest.kmip.data_structures.KeyValue;
import com.cosmian.rest.kmip.data_structures.PlainTextKeyValue;
import com.cosmian.rest.kmip.data_structures.TransparentSymmetricKey;
import com.cosmian.rest.kmip.json.KmipStruct;
import com.cosmian.rest.kmip.objects.SymmetricKey;
import com.cosmian.rest.kmip.operations.Import;
import com.cosmian.rest.kmip.operations.ImportResponse;
import com.cosmian.rest.kmip.operations.TestStruct;
import com.cosmian.rest.kmip.types.Attributes;
import com.cosmian.rest.kmip.types.CryptographicAlgorithm;
import com.cosmian.rest.kmip.types.KeyFormatType;
import com.cosmian.rest.kmip.types.KeyWrapType;
import com.cosmian.rest.kmip.types.Link;
import com.cosmian.rest.kmip.types.LinkType;
import com.cosmian.rest.kmip.types.LinkedObjectIdentifier;
import com.cosmian.rest.kmip.types.ObjectType;
import com.cosmian.rest.kmip.types.UniqueIdentifier;
import com.fasterxml.classmate.MemberResolver;
import com.fasterxml.classmate.ResolvedType;
import com.fasterxml.classmate.ResolvedTypeWithMembers;
import com.fasterxml.classmate.TypeResolver;
import com.fasterxml.classmate.members.ResolvedField;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class TestKmip {

    @BeforeAll
    public static void before_all() {
        TestUtils.initLogging();
    }

    private SymmetricKey symmetricKey() {
        PlainTextKeyValue pt_kv =
            new PlainTextKeyValue(new KeyMaterial(new TransparentSymmetricKey("bytes".getBytes())), Optional.empty());
        SymmetricKey symmetricKey = new SymmetricKey(new KeyBlock(KeyFormatType.TransparentSymmetricKey,
            Optional.empty(), new KeyValue(pt_kv), CryptographicAlgorithm.AES, 256, Optional.empty()

        ));
        return symmetricKey;
    }

    @Test
    public void test_serialization_deserialization() throws Exception {
        String unique_identifier = "unique_identifier";
        ObjectType object_type = ObjectType.Symmetric_Key;
        Optional<Boolean> replace_existing = Optional.of(Boolean.TRUE);
        Optional<KeyWrapType> key_wrap_type = Optional.of(KeyWrapType.As_Registered);
        Attributes attributes = new Attributes(object_type, Optional.of(CryptographicAlgorithm.AES));
        SymmetricKey symmetricKey = symmetricKey();
        Import import_request =
            new Import(unique_identifier, object_type, replace_existing, key_wrap_type, attributes, symmetricKey);

        ObjectMapper mapper = new ObjectMapper();

        String json = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(import_request);
        System.out.println(json);

        Import direct_deserialized = mapper.readValue(json, Import.class);
        assertEquals(unique_identifier, direct_deserialized.getUniqueIdentifier());
        assertEquals(object_type, direct_deserialized.getObjectType());
        assertEquals(replace_existing, direct_deserialized.getReplaceExisting());
        assertEquals(key_wrap_type, direct_deserialized.getKeyWrapType());
        assertEquals(symmetricKey, (SymmetricKey) direct_deserialized.getObject());

        KmipStruct indirect_deserialized = mapper.readValue(json, KmipStruct.class);
        assertEquals(Import.class, indirect_deserialized.getClass());
        Import deserialized = (Import) indirect_deserialized;
        assertEquals(unique_identifier, deserialized.getUniqueIdentifier());
        assertEquals(object_type, deserialized.getObjectType());
        assertEquals(replace_existing, deserialized.getReplaceExisting());
        assertEquals(key_wrap_type, deserialized.getKeyWrapType());
        assertEquals(symmetricKey, (SymmetricKey) deserialized.getObject());
    }

    @Test
    public void test_required() throws Exception {
        String unique_identifier = "unique_identifier";
        ObjectType object_type = ObjectType.Symmetric_Key;
        Optional<Boolean> replace_existing = Optional.of(Boolean.TRUE);
        Optional<KeyWrapType> key_wrap_type = Optional.of(KeyWrapType.As_Registered);
        Attributes attributes = new Attributes(object_type, Optional.of(CryptographicAlgorithm.AES));
        Import import_request =
            new Import(unique_identifier, object_type, replace_existing, key_wrap_type, attributes, null);
        ObjectMapper mapper = new ObjectMapper();
        try {
            mapper.writeValueAsString(import_request);
            throw new RuntimeException("should have thrown on NULL Object which is required");
        } catch (JsonMappingException e) {
            // fine
        }
    }

    @Test
    public void test_not_required() throws Exception {
        String unique_identifier = "unique_identifier";
        ObjectType object_type = ObjectType.Symmetric_Key;
        Optional<Boolean> replace_existing = Optional.empty();
        Optional<KeyWrapType> key_wrap_type = Optional.empty();
        Attributes attributes = new Attributes(object_type, Optional.of(CryptographicAlgorithm.AES));
        Import import_request =
            new Import(unique_identifier, object_type, replace_existing, key_wrap_type, attributes, symmetricKey());

        // ReplaceExisting KeyWrapType is not required
        ObjectMapper mapper = new ObjectMapper();
        String json = mapper.writeValueAsString(import_request);
        System.out.println(json);
        Import direct_deserialized = mapper.readValue(json, Import.class);
        assertTrue(!direct_deserialized.getKeyWrapType().isPresent());
    }

    @Test
    public void test_class_mate() throws Exception {
        TypeResolver type_resolver = new TypeResolver();

        ResolvedType resolved_test_class = type_resolver.resolve(TestStruct.class);
        MemberResolver member_resolver = new MemberResolver(type_resolver);
        ResolvedTypeWithMembers resolved_test_class_with_members =
            member_resolver.resolve(resolved_test_class, null, null);

        for (ResolvedField f : resolved_test_class_with_members.getMemberFields()) {
            if (f.getName().equals("opt_string")) {
                Class<?> clazz = f.getType().getTypeParameters().get(0).getErasedType();
                assertEquals(String.class, clazz);
            }
        }
    }

    @Test
    public void test_choice_optional_array() throws Exception {
        LinkedObjectIdentifier loi = new LinkedObjectIdentifier(UniqueIdentifier.ID_Placeholder);
        assertEquals(UniqueIdentifier.ID_Placeholder, loi.get());
        Link link_1 = new Link(LinkType.Public_Key_Link, new LinkedObjectIdentifier(UniqueIdentifier.ID_Placeholder));
        Link link_2 = new Link(LinkType.Certificate_Link, new LinkedObjectIdentifier("cert"));
        TestStruct test_struct = new TestStruct(Optional.of("blah"), new Link[] {link_1, link_2});

        ObjectMapper mapper = new ObjectMapper();
        String json = mapper.writeValueAsString(test_struct);
        System.out.println(json);
        TestStruct deserialized = mapper.readValue(json, TestStruct.class);
        assertEquals(test_struct.getUnique_identifier(), deserialized.getUnique_identifier());
        assertTrue(Arrays.equals(test_struct.getLink(), deserialized.getLink()));
    }

    @Test
    public void test_import_response_from_rust() throws Exception {
        String json =
            "{\"tag\":\"ImportResponse\",\"type\":\"Structure\",\"value\":[{\"tag\":\"UniqueIdentifier\",\"type\":\"TextString\",\"value\":\"blah\"}]}";
        ObjectMapper mapper = new ObjectMapper();
        ImportResponse ir = mapper.readValue(json, ImportResponse.class);
        assertEquals("blah", ir.getUniqueIdentifier());
    }

    @Test
    public void test_store_ser_de() throws Exception {
        Policy policy =
            new Policy(20).addAxis("Security Level", new String[] {"Protected", "Confidential", "Top Secret"}, true)
                .addAxis("Department", new String[] {"FIN", "MKG", "HR"}, false);
        ObjectMapper mapper = new ObjectMapper();
        String json = mapper.writeValueAsString(policy);
        // deserialize
        Policy policy_ = mapper.readValue(json, Policy.class);
        assertEquals(policy, policy_);
    }

}
