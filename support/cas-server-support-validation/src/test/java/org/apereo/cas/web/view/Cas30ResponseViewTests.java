package org.apereo.cas.web.view;

import org.apereo.cas.AbstractCentralAuthenticationServiceTests;
import org.apereo.cas.CasProtocolConstants;
import org.apereo.cas.CasViewConstants;
import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.authentication.DefaultAuthenticationAttributeReleasePolicy;
import org.apereo.cas.authentication.DefaultAuthenticationServiceSelectionPlan;
import org.apereo.cas.authentication.DefaultAuthenticationServiceSelectionStrategy;
import org.apereo.cas.authentication.ProtocolAttributeEncoder;
import org.apereo.cas.authentication.support.DefaultCasProtocolAttributeEncoder;
import org.apereo.cas.config.CasAuthenticationEventExecutionPlanTestConfiguration;
import org.apereo.cas.config.CasCoreAuthenticationConfiguration;
import org.apereo.cas.config.CasCoreAuthenticationHandlersConfiguration;
import org.apereo.cas.config.CasCoreAuthenticationMetadataConfiguration;
import org.apereo.cas.config.CasCoreAuthenticationPolicyConfiguration;
import org.apereo.cas.config.CasCoreAuthenticationPrincipalConfiguration;
import org.apereo.cas.config.CasCoreAuthenticationServiceSelectionStrategyConfiguration;
import org.apereo.cas.config.CasCoreAuthenticationSupportConfiguration;
import org.apereo.cas.config.CasCoreConfiguration;
import org.apereo.cas.config.CasCoreHttpConfiguration;
import org.apereo.cas.config.CasCoreMultifactorAuthenticationConfiguration;
import org.apereo.cas.config.CasCoreServicesAuthenticationConfiguration;
import org.apereo.cas.config.CasCoreServicesConfiguration;
import org.apereo.cas.config.CasCoreTicketCatalogConfiguration;
import org.apereo.cas.config.CasCoreTicketIdGeneratorsConfiguration;
import org.apereo.cas.config.CasCoreTicketsConfiguration;
import org.apereo.cas.config.CasCoreUtilConfiguration;
import org.apereo.cas.config.CasCoreWebConfiguration;
import org.apereo.cas.config.CasDefaultServiceTicketIdGeneratorsConfiguration;
import org.apereo.cas.config.CasPersonDirectoryTestConfiguration;
import org.apereo.cas.config.CasRegisteredServicesTestConfiguration;
import org.apereo.cas.config.support.CasWebApplicationServiceFactoryConfiguration;
import org.apereo.cas.logout.config.CasCoreLogoutConfiguration;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.services.web.view.AbstractCasView;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.EncodingUtils;
import org.apereo.cas.util.crypto.CipherExecutor;
import org.apereo.cas.util.crypto.PrivateKeyFactoryBean;
import org.apereo.cas.validation.DefaultServiceTicketValidationAuthorizersExecutionPlan;
import org.apereo.cas.validation.config.CasCoreValidationConfiguration;
import org.apereo.cas.web.AbstractServiceValidateController;
import org.apereo.cas.web.AbstractServiceValidateControllerTests;
import org.apereo.cas.web.ServiceValidateConfigurationContext;
import org.apereo.cas.web.ServiceValidationViewFactory;
import org.apereo.cas.web.config.CasCookieConfiguration;
import org.apereo.cas.web.config.CasProtocolViewsConfiguration;
import org.apereo.cas.web.config.CasValidationConfiguration;
import org.apereo.cas.web.flow.config.CasCoreWebflowConfiguration;
import org.apereo.cas.web.flow.config.CasMultifactorAuthenticationWebflowConfiguration;
import org.apereo.cas.web.flow.config.CasWebflowContextConfiguration;
import org.apereo.cas.web.v2.ServiceValidateController;
import org.apereo.cas.web.view.attributes.DefaultCas30ProtocolAttributesRenderer;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.tuple.Pair;
import org.apereo.services.persondir.IPersonAttributeDao;
import org.apereo.services.persondir.support.StubPersonAttributeDao;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.aop.AopAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockServletContext;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.web.context.support.GenericWebApplicationContext;
import org.springframework.web.servlet.View;
import org.springframework.web.servlet.support.RequestContext;

import javax.crypto.Cipher;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link Cas30ResponseView}.
 *
 * @author Misagh Moayyed
 * @since 4.0.0
 */
@DirtiesContext
@Slf4j
@SpringBootTest(properties = {"cas.clearpass.cacheCredential=true", "cas.clearpass.crypto.enabled=false"},
    classes = {
    Cas30ResponseViewTests.AttributeRepositoryTestConfiguration.class,
    AopAutoConfiguration.class,
    CasProtocolViewsConfiguration.class,
    RefreshAutoConfiguration.class,
    CasCoreServicesConfiguration.class,
    CasAuthenticationEventExecutionPlanTestConfiguration.class,
    AbstractCentralAuthenticationServiceTests.CasTestConfiguration.class,
    CasCoreServicesAuthenticationConfiguration.class,
    CasWebApplicationServiceFactoryConfiguration.class,
    CasDefaultServiceTicketIdGeneratorsConfiguration.class,
    CasCoreTicketIdGeneratorsConfiguration.class,
    CasCoreUtilConfiguration.class,
    CasCoreAuthenticationConfiguration.class,
    CasCoreServicesAuthenticationConfiguration.class,
    CasCoreAuthenticationPrincipalConfiguration.class,
    CasCoreAuthenticationPolicyConfiguration.class,
    CasCoreAuthenticationMetadataConfiguration.class,
    CasCoreAuthenticationSupportConfiguration.class,
    CasCoreAuthenticationHandlersConfiguration.class,
    CasCoreHttpConfiguration.class,
    CasCoreConfiguration.class,
    CasValidationConfiguration.class,
    CasRegisteredServicesTestConfiguration.class,
    CasCoreAuthenticationServiceSelectionStrategyConfiguration.class,
    CasCoreTicketsConfiguration.class,
    CasCoreTicketCatalogConfiguration.class,
    CasCoreWebConfiguration.class,
    CasWebflowContextConfiguration.class,
    CasCoreWebflowConfiguration.class,
    CasCoreLogoutConfiguration.class,
    CasCookieConfiguration.class,
    CasCoreAuthenticationConfiguration.class,
    CasCoreMultifactorAuthenticationConfiguration.class,
    CasMultifactorAuthenticationWebflowConfiguration.class,
    CasPersonDirectoryTestConfiguration.class,
    CasCoreValidationConfiguration.class
})
public class Cas30ResponseViewTests extends AbstractServiceValidateControllerTests {

    @Autowired
    @Qualifier("serviceValidationViewFactory")
    protected ServiceValidationViewFactory serviceValidationViewFactory;

    @Autowired
    @Qualifier("servicesManager")
    protected ServicesManager servicesManager;

    @SneakyThrows
    private static String decryptCredential(final String cred) {
        val factory = new PrivateKeyFactoryBean();
        factory.setAlgorithm("RSA");
        factory.setLocation(new ClassPathResource("keys/RSA4096Private.p8"));
        factory.setSingleton(false);
        val privateKey = factory.getObject();

        LOGGER.debug("Initializing cipher based on [{}]", privateKey.getAlgorithm());
        val cipher = Cipher.getInstance(privateKey.getAlgorithm());

        LOGGER.debug("Decoding value [{}]", cred);
        val cred64 = EncodingUtils.decodeBase64(cred);

        LOGGER.debug("Initializing decrypt-mode via private key [{}]", privateKey.getAlgorithm());
        cipher.init(Cipher.DECRYPT_MODE, privateKey);

        val cipherData = cipher.doFinal(cred64);
        return new String(cipherData, StandardCharsets.UTF_8);
    }

    @Override
    public AbstractServiceValidateController getServiceValidateControllerInstance() {
        val context = ServiceValidateConfigurationContext.builder()
            .validationSpecifications(CollectionUtils.wrapSet(getValidationSpecification()))
            .authenticationSystemSupport(getAuthenticationSystemSupport())
            .servicesManager(getServicesManager())
            .centralAuthenticationService(getCentralAuthenticationService().getObject())
            .argumentExtractor(getArgumentExtractor())
            .proxyHandler(getProxyHandler())
            .requestedContextValidator((assertion, request) -> Pair.of(Boolean.TRUE, Optional.empty()))
            .authnContextAttribute("authenticationContext")
            .validationAuthorizers(new DefaultServiceTicketValidationAuthorizersExecutionPlan())
            .renewEnabled(true)
            .validationViewFactory(serviceValidationViewFactory)
            .build();
        return new ServiceValidateController(context);
    }

    protected Map<?, ?> renderView() throws Exception {
        val modelAndView = this.getModelAndViewUponServiceValidationWithSecurePgtUrl(DEFAULT_SERVICE);
        LOGGER.debug("Retrieved model and view [{}]", modelAndView.getModel());

        val req = new MockHttpServletRequest(new MockServletContext());
        req.setAttribute(RequestContext.WEB_APPLICATION_CONTEXT_ATTRIBUTE, new GenericWebApplicationContext(req.getServletContext()));

        val encoder = new DefaultCasProtocolAttributeEncoder(this.servicesManager, CipherExecutor.noOpOfStringToString());
        val viewDelegated = new View() {
            @Override
            public String getContentType() {
                return MediaType.TEXT_HTML_VALUE;
            }

            @Override
            public void render(final Map<String, ?> map, final HttpServletRequest request, final HttpServletResponse response) {
                LOGGER.warn("Setting attribute [{}]", map.keySet());
                map.forEach(request::setAttribute);
            }
        };

        val view = getCasViewToRender(encoder, viewDelegated);
        val resp = new MockHttpServletResponse();
        view.render(modelAndView.getModel(), req, resp);
        return getRenderedViewModelMap(req);
    }

    protected Map getRenderedViewModelMap(final MockHttpServletRequest req) {
        return (Map) req.getAttribute(CasProtocolConstants.VALIDATION_CAS_MODEL_ATTRIBUTE_NAME_ATTRIBUTES);
    }

    protected AbstractCasView getCasViewToRender(final ProtocolAttributeEncoder encoder, final View viewDelegated) {
        return new Cas30ResponseView(true, encoder, servicesManager,
            viewDelegated, new DefaultAuthenticationAttributeReleasePolicy("attribute"),
            new DefaultAuthenticationServiceSelectionPlan(new DefaultAuthenticationServiceSelectionStrategy()),
            new DefaultCas30ProtocolAttributesRenderer());
    }

    @Test
    public void verifyViewAuthnAttributes() throws Exception {
        val attributes = renderView();
        assertTrue(attributes.containsKey(CasProtocolConstants.VALIDATION_CAS_MODEL_ATTRIBUTE_NAME_AUTHENTICATION_DATE));
        assertTrue(attributes.containsKey(CasProtocolConstants.VALIDATION_CAS_MODEL_ATTRIBUTE_NAME_FROM_NEW_LOGIN));
        assertTrue(attributes.containsKey(CasProtocolConstants.VALIDATION_REMEMBER_ME_ATTRIBUTE_NAME));
    }

    @Test
    public void verifyPasswordAsAuthenticationAttributeCanDecrypt() throws Exception {
        val attributes = renderView();
        assertTrue(attributes.containsKey(CasViewConstants.MODEL_ATTRIBUTE_NAME_PRINCIPAL_CREDENTIAL));

        val encodedPsw = (String) attributes.get(CasViewConstants.MODEL_ATTRIBUTE_NAME_PRINCIPAL_CREDENTIAL);
        val password = decryptCredential(encodedPsw);
        val creds = CoreAuthenticationTestUtils.getCredentialsWithSameUsernameAndPassword();
        assertEquals(password, creds.getPassword());
    }

    @Test
    public void verifyProxyGrantingTicketAsAuthenticationAttributeCanDecrypt() throws Exception {
        val attributes = renderView();
        LOGGER.trace("Attributes are [{}]", attributes.keySet());
        assertTrue(attributes.containsKey(CasViewConstants.MODEL_ATTRIBUTE_NAME_PROXY_GRANTING_TICKET));

        val encodedPgt = (String) attributes.get(CasViewConstants.MODEL_ATTRIBUTE_NAME_PROXY_GRANTING_TICKET);
        val pgt = decryptCredential(encodedPgt);
        assertNotNull(pgt);
    }

    @Test
    public void verifyViewBinaryAttributes() throws Exception {
        val attributes = renderView();
        assertTrue(attributes.containsKey("binaryAttribute"));
        val binaryAttr = attributes.get("binaryAttribute");
        assertEquals("binaryAttributeValue", EncodingUtils.decodeBase64ToString(binaryAttr.toString()));
    }

    @TestConfiguration
    public static class AttributeRepositoryTestConfiguration {
        @Bean
        public IPersonAttributeDao attributeRepository() {
            val attrs =
                CollectionUtils.wrap("uid", CollectionUtils.wrap("uid"),
                    "eduPersonAffiliation", CollectionUtils.wrap("developer"),
                    "groupMembership", CollectionUtils.wrap("adopters"),
                    "binaryAttribute", CollectionUtils.wrap("binaryAttributeValue".getBytes(StandardCharsets.UTF_8)));
            return new StubPersonAttributeDao((Map) attrs);
        }
    }
}
