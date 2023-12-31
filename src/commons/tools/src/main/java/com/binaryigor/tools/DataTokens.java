package com.binaryigor.tools;

import com.binaryigor.json.JsonMapper;

import java.util.UUID;

public class DataTokens {

    private static final String PARTS_SEPARATOR = "~";
    private static final int PARTS = 2;

    public static String containing(Object data) {
        var hash = UUID.randomUUID().toString();

        String inTokenData;
        if (data.getClass().isAssignableFrom(String.class)) {
            inTokenData = data.toString();
        } else {
            inTokenData = JsonMapper.json(data);
        }

        var withHash = String.join(PARTS_SEPARATOR, inTokenData, hash);

        return Base64Url.asEncoded(withHash);
    }

    public static String extractedData(String token) {
        String data;
        try {
            var decoded = Base64Url.asDecoded(token);
            if (decoded.split(PARTS_SEPARATOR).length == PARTS) {
                data = decoded.split(PARTS_SEPARATOR)[0];
            } else {
                data = null;
            }
        } catch (Exception e) {
            data = null;
        }

        if (data == null) {
            throw new RuntimeException("Invalid data token");
        }
        return data;
    }

    public static <T> T extractedData(String decoded, Class<T> dataType) {
        var data = extractedData(decoded);
        return JsonMapper.object(data, dataType);
    }
}
