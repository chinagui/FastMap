package com.navinfo.dataservice.commons.util;

import java.io.IOException;
import java.util.Map;

import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;

public abstract class JacksonUtils {
	private static ObjectMapper m = null;

	private JacksonUtils() {

	}

	private static ObjectMapper getObjectMapper() {
		if (m == null) {
			m = new ObjectMapper();
		}
		return m;
	}

	@SuppressWarnings("unchecked")
	public static Map toMap(String json) throws JsonParseException, JsonMappingException, IOException {
		Map userData = getObjectMapper().readValue(json, Map.class);
		return userData;
	}
	
	public static  <T> T toObject(String json,Class<T> clazz) throws IOException,JsonParseException, JsonMappingException
    {
        T o = getObjectMapper().readValue(json, clazz);
        return o;
    }
	
	public static String toJson(Object o) throws java.io.IOException, org.codehaus.jackson.JsonGenerationException, org.codehaus.jackson.map.JsonMappingException
    {
        return getObjectMapper().writeValueAsString(o);
    }
	
	@SuppressWarnings("unchecked")
    public static Map toMapThrowException(String json) throws Exception {
        Map userData = getObjectMapper().readValue(json, Map.class);
        return userData;
    }
	
	public static  <T> T toObjectThrowException(String json,Class<T> clazz) throws Exception 
    {
        T o = getObjectMapper().readValue(json, clazz);
        return o;
    }
    

}
