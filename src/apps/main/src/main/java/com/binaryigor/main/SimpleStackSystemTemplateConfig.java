package com.binaryigor.main;

import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.i18n.SessionLocaleResolver;

import java.time.Clock;
import java.util.Locale;

@Configuration
public class SimpleStackSystemTemplateConfig {

    //TODO: target solution
    @Bean
    LocaleResolver localeResolver() {
        var slr = new SessionLocaleResolver();
        slr.setDefaultLocale(Locale.ENGLISH);
        return slr;
    }

    //TODO: more sophisticated config
    @Bean
    MessageSource messageSource() {
        var source = new ReloadableResourceBundleMessageSource();
        source.setBasenames("file:static/messages/messages",
                "file:static/messages/error-messages");
        source.setCacheSeconds(10);
        source.setUseCodeAsDefaultMessage(true);
        return source;
    }

    @Bean
    Clock clock() {
        return Clock.systemUTC();
    }
}
