package com.binaryigor.main._commons.app;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.ui.Model;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

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
            //TODO: get from config!
            model.addAttribute("stylesPath", "/live-styles.css");
            model.addAttribute("template", templateName);
            return FULL_PAGE;
        }
        return templateName;
    }
}
