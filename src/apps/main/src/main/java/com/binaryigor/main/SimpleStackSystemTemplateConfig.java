package com.binaryigor.main;

import com.binaryigor.email.factory.EmailFactory;
import com.binaryigor.email.server.EmailServer;
import com.binaryigor.email.server.PostmarkEmailServer;
import com.binaryigor.email.server.ToConsoleEmailServer;
import com.binaryigor.main._common.app.EmailModuleProvider;
import com.binaryigor.tools.PropertiesConverter;
import com.binaryigor.types.Transactions;
import com.binaryigor.types.event.AppEvents;
import com.binaryigor.types.event.AppEventsPublisher;
import com.binaryigor.types.event.InMemoryEvents;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import org.springframework.web.filter.ShallowEtagHeaderFilter;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.i18n.SessionLocaleResolver;

import java.time.Clock;
import java.util.Locale;
import java.util.function.Supplier;

@Configuration
@EnableConfigurationProperties(EmailProperties.class)
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
        source.setCacheSeconds(1);
        source.setUseCodeAsDefaultMessage(true);
        return source;
    }

    @Bean
    public EmailServer emailServer(EmailProperties emailProperties) {
        if (emailProperties.fakeServer()) {
            return new ToConsoleEmailServer();
        }
        var apiToken = PropertiesConverter.valueOrFromFile(emailProperties.postmarkApiToken());
        return new PostmarkEmailServer(apiToken);
    }

    @Bean
    public EmailFactory emailFactory(EmailProperties emailProperties) {
        return EmailModuleProvider.factory(emailProperties.templatesDir());
    }

    @Bean
    Clock clock() {
        return Clock.systemUTC();
    }

    //TODO: real transactions
    @Bean
    public Transactions transactions() {
        return new Transactions() {
            @Override
            public void execute(Runnable transaction) {
                transaction.run();
            }

            @Override
            public <T> T executeAndReturn(Supplier<T> transaction) {
                return transaction.get();
            }
        };
    }

    @Bean
    AppEvents appEvents() {
        return new InMemoryEvents();
    }

    @Bean
    AppEventsPublisher appEventsPublisher(AppEvents events) {
        return events.publisher();
    }

    //TODO: sth better, but maybe it's good enough!
    @Bean
    ShallowEtagHeaderFilter shallowEtagHeaderFilter() {
        return new ShallowEtagHeaderFilter();
    }
}
