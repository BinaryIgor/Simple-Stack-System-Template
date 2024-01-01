package com.binaryigor.test;

import com.binaryigor.json.JsonMapper;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class TestDataLoader {

    public static <T> T object(String classpathResource, Class<T> clazz) {
        return JsonMapper.object(classpathResourceContent(classpathResource), clazz);
    }

    public static <T> List<T> objects(String classpathResource, Class<T> clazz) {
        return JsonMapper.objects(classpathResourceContent(classpathResource), clazz);
    }

    public static <T> List<List<T>> objectsObjects(String classpathResource, Class<T> clazz) {
        return JsonMapper.objectsObjects(classpathResourceContent(classpathResource), clazz);
    }

    public static <T> Map<String, T> map(String classpathResource, Class<T> clazz) {
        return JsonMapper.map(classpathResourceContent(classpathResource), clazz);
    }

    public static <K, V> Map<K, V> map(String classpathResource, Class<K> kClass, Class<V> vClass) {
        return JsonMapper.map(classpathResourceContent(classpathResource), kClass, vClass);
    }

    public static <K, V> Map<K, List<V>> mapList(String classpathResource, Class<K> kClass, Class<V> vClass) {
        return JsonMapper.mapList(classpathResourceContent(classpathResource), kClass, vClass);
    }


    public static String content(InputStream stream) {
        try (var r = new BufferedReader(new InputStreamReader(stream))) {
            return r.lines().collect(Collectors.joining("\n"));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static InputStream classpathStream(String resource) {
        var resName = resourceName(resource);
        var stream = TestDataLoader.class.getResourceAsStream(resName);
        if (stream == null) {
            throw new RuntimeException(String.format("Null stream for %s resource", resource));
        }
        return stream;
    }

    private static String resourceName(String resource) {
        return resource.startsWith("/") ? resource : "/" + resource;
    }

    public static File classpathFile(String resource) {
        var url = TestDataLoader.class.getResource(resourceName(resource));
        return new File(url.getFile());
    }

    public static String classpathResourceContent(String resource) {
        return content(classpathStream(resource));
    }
}
