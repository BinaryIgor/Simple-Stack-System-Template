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
import org.jooq.DSLContext;
import org.jooq.SQLDialect;
import org.jooq.impl.DSL;
import org.jooq.impl.DefaultConfiguration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.i18n.SessionLocaleResolver;

import javax.sql.DataSource;
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
    MessageSource messageSource(@Value("${messages.path-prefixes}") String pathPrefixes) {
        var source = new ReloadableResourceBundleMessageSource();
        source.setBasenames(pathPrefixes.split(","));
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

//    @Bean
//    public DataSource dataSource(@Value("${spring.datasource.url}") String jdbcUrl,
//                                 @Value("${spring.datasource.username}") String username,
//                                 @Value("${spring.datasource.password}") String password,
//                                 @Value("${spring.datasource.pool-size}") int poolSize) {
//        var config = new HikariConfig();
//        config.setJdbcUrl(jdbcUrl);
//        config.setUsername(username);
//        config.setPassword(PropertiesConverter.valueOrFromFile(password));
//        config.setMinimumIdle(poolSize);
//        config.setMaximumPoolSize(poolSize);
//
//        return new HikariDataSource(config);
//    }


    @Bean
    DataSource dataSource(DataSourceProperties props) {
        return props
                .initializeDataSourceBuilder()
                .build();
    }

    //TODO
    @Bean
    public Transactions transactions(PlatformTransactionManager platformTransactionManager) {
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
//    @Bean
//    ShallowEtagHeaderFilter shallowEtagHeaderFilter() {
//        return new ShallowEtagHeaderFilter();
//    }

    @Bean
    DSLContext dslContext(DataSource dataSource) {
        return DSL.using(new DefaultConfiguration()
                .set(dataSource)
                .set(SQLDialect.POSTGRES));
    }
}
