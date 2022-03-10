package com.cosmian.rest.kmip.json;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

@JsonDeserialize(using = KmipStructDeserializer.class)
@JsonSerialize(using = KmipStructSerializer.class)
public interface KmipStruct{

}
