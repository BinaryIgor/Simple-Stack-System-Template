package com.binaryigor.tools;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

public class Base64Url {

    public static String asEncoded(String data) {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(data.getBytes(StandardCharsets.UTF_8));
    }

    public static String asDecoded(String data) {
        return new String(Base64.getUrlDecoder().decode(data.getBytes()), StandardCharsets.UTF_8);
    }
}
