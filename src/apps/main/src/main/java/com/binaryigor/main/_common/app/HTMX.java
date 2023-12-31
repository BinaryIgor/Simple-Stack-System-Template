package com.binaryigor.main._common.app;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.ui.Model;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.security.Signature;
import java.util.Optional;

public class HTMX {

    private static final String FULL_PAGE = "index";

    public static boolean isHTMXRequest(HttpServletRequest request) {
        return request.getHeader("hx-request") != null;
    }

    private static Optional<HttpServletRequest> currentRequest() {
        var ra = RequestContextHolder.getRequestAttributes();
        if (ra instanceof ServletRequestAttributes sra) {
            return Optional.of(sra.getRequest());
        }
        return Optional.empty();
    }

    public static void addClientReplaceUrlHeader(HttpServletResponse response, String url) {
        response.addHeader("hx-replace-url", url);
    }

    public static String fragmentOrFullPage(Model model,
                                            String templateName) {
        var currentRequest = currentRequest();
        if (currentRequest.isEmpty() || !isHTMXRequest(currentRequest.get())) {
            model.addAttribute("template", templateName);
            return FULL_PAGE;
        }
        return templateName;
    }

}
