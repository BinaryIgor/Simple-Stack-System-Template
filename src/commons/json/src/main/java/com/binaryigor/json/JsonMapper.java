package com.binaryigor.json;

import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class JsonMapper {

    //Remember that all getters are serialized using that approach (additional methods for records).
    public static final ObjectMapper MAPPER = com.fasterxml.jackson.databind.json.JsonMapper.builder()
            .enable(MapperFeature.IGNORE_DUPLICATE_MODULE_REGISTRATIONS)
            .enable(DeserializationFeature.FAIL_ON_MISSING_CREATOR_PROPERTIES)
            .disable(SerializationFeature.FAIL_ON_EMPTY_BEANS)
            .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
            .addModule(new DatesModule())
            .build();

    public static void addModule(Module module) {
        MAPPER.registerModule(module);
    }

    public static String json(Object value) {
        try {
            return MAPPER.writeValueAsString(value);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static <T> T object(String json, Class<T> clazz) {
        try {
            return MAPPER.readValue(json, clazz);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static <T> List<T> objects(String json, Class<T> clazz) {
        try {
            var type = MAPPER.getTypeFactory().constructCollectionType(List.class, clazz);
            return MAPPER.readValue(json, type);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static <T> List<List<T>> objectsObjects(String json, Class<T> clazz) {
        try {
            var typeFactory = MAPPER.getTypeFactory();
            var inType = typeFactory.constructCollectionType(List.class, clazz);
            var outType = typeFactory.constructCollectionType(List.class, inType);
            return MAPPER.readValue(json, outType);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static <T> Map<String, T> map(String json, Class<T> clazz) {
        return map(json, String.class, clazz);
    }

    public static <K, V> Map<K, V> map(String json, Class<K> kClass, Class<V> vClass) {
        try {
            var type = MAPPER.getTypeFactory().constructMapType(HashMap.class, kClass, vClass);
            return MAPPER.readValue(json, type);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static Map<String, Object> map(String json) {
        return map(json, Object.class);
    }

    public static <K, V> Map<K, List<V>> mapList(String json, Class<K> kClass, Class<V> vClass) {
        try {
            var typeFactory = MAPPER.getTypeFactory();

            var keyType = typeFactory.constructType(kClass);
            var valueType = typeFactory.constructCollectionType(ArrayList.class, vClass);

            var type = typeFactory.constructMapType(HashMap.class, keyType, valueType);

            return MAPPER.readValue(json, type);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static <OK, IK, V> Map<OK, Map<IK, V>> mapMap(String json,
                                                         Class<OK> okClass,
                                                         Class<IK> ikClass,
                                                         Class<V> vClass) {
        try {
            var typeFactory = MAPPER.getTypeFactory();

            var insideType = typeFactory.constructMapType(HashMap.class, ikClass, vClass);

            var outsideKeyType = typeFactory.constructType(okClass);
            var type = typeFactory.constructMapType(HashMap.class, outsideKeyType, insideType);

            return MAPPER.readValue(json, type);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
