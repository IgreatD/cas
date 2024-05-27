package org.apereo.cas.config;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.model.support.jpa.JpaConfigDataHolder;
import org.apereo.cas.configuration.support.JpaBeans;
import org.apereo.cas.gauth.credential.GoogleAuthenticatorAccount;
import org.apereo.cas.gauth.credential.JpaGoogleAuthenticatorTokenCredentialRepository;
import org.apereo.cas.gauth.token.GoogleAuthenticatorJpaTokenRepository;
import org.apereo.cas.gauth.token.GoogleAuthenticatorToken;
import org.apereo.cas.otp.repository.credentials.OneTimeTokenCredentialRepository;
import org.apereo.cas.otp.repository.token.OneTimeTokenRepository;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.crypto.CipherExecutor;

import com.warrenstrange.googleauth.IGoogleAuthenticator;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;
import java.util.List;

/**
 * This is {@link GoogleAuthenticatorJpaConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Configuration("googleAuthentiacatorJpaConfiguration")
@EnableConfigurationProperties(CasConfigurationProperties.class)
@EnableTransactionManagement(proxyTargetClass = true)
@EnableScheduling
public class GoogleAuthenticatorJpaConfiguration {


    @Autowired
    private CasConfigurationProperties casProperties;

    @RefreshScope
    @Bean
    public HibernateJpaVendorAdapter jpaGoogleAuthenticatorVendorAdapter() {
        return JpaBeans.newHibernateJpaVendorAdapter(casProperties.getJdbc());
    }

    @Bean
    public DataSource dataSourceGoogleAuthenticator() {
        return JpaBeans.newDataSource(casProperties.getAuthn().getMfa().getGauth().getJpa());
    }

    @Bean
    public List<String> jpaPackagesToScanGoogleAuthenticator() {
        return CollectionUtils.wrapList(GoogleAuthenticatorAccount.class.getPackage().getName(),
            GoogleAuthenticatorToken.class.getPackage().getName());
    }

    @Lazy
    @Bean
    public LocalContainerEntityManagerFactoryBean googleAuthenticatorEntityManagerFactory() {

        return JpaBeans.newHibernateEntityManagerFactoryBean(
            new JpaConfigDataHolder(
                jpaGoogleAuthenticatorVendorAdapter(),
                "jpaGoogleAuthenticatorContext",
                jpaPackagesToScanGoogleAuthenticator(),
                dataSourceGoogleAuthenticator()),
            casProperties.getAuthn().getMfa().getGauth().getJpa());
    }

    @Autowired
    @Bean
    public PlatformTransactionManager transactionManagerGoogleAuthenticator(
        @Qualifier("googleAuthenticatorEntityManagerFactory") final EntityManagerFactory emf) {
        val mgmr = new JpaTransactionManager();
        mgmr.setEntityManagerFactory(emf);
        return mgmr;
    }

    @Autowired
    @Bean
    @ConditionalOnMissingBean(name = "googleAuthenticatorAccountRegistry")
    public OneTimeTokenCredentialRepository googleAuthenticatorAccountRegistry(@Qualifier("googleAuthenticatorInstance") final IGoogleAuthenticator googleAuthenticatorInstance,
                                                                               @Qualifier("googleAuthenticatorAccountCipherExecutor")
                                                                               final CipherExecutor googleAuthenticatorAccountCipherExecutor) {
        return new JpaGoogleAuthenticatorTokenCredentialRepository(googleAuthenticatorAccountCipherExecutor, googleAuthenticatorInstance);
    }

    @ConditionalOnMissingBean(name = "oneTimeTokenAuthenticatorTokenRepository")
    @Bean
    public OneTimeTokenRepository oneTimeTokenAuthenticatorTokenRepository() {
        return new GoogleAuthenticatorJpaTokenRepository(
            casProperties.getAuthn().getMfa().getGauth().getTimeStepSize()
        );
    }

}
