package com.binaryigor.main._common.app;

import com.binaryigor.main._common.core.exception.AppException;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Component;

import java.util.Locale;
import java.util.stream.Collectors;

@Component
public class ExceptionsTranslator {

    static final String UNKNOWN_ERROR_CODE = "Unknown";

    private final MessageSource messageSource;

    public ExceptionsTranslator(MessageSource messageSource) {
        this.messageSource = messageSource;
    }

    public String translated(Throwable exception) {
        var requestLocale = HttpRequestAttributes.get(HttpRequestAttributes.REQUEST_LOCALE_ATTRIBUTE, String.class)
                .map(Locale::of)
                .orElse(Locale.ENGLISH);

        if (exception instanceof AppException appException) {
            return appException.toErrors()
                    .stream()
                    .map(e -> messageSource.getMessage(e, new Object[0], requestLocale))
                    .collect(Collectors.joining("\n"));
        }

        return messageSource.getMessage(UNKNOWN_ERROR_CODE, new Object[0], requestLocale);
    }
}
