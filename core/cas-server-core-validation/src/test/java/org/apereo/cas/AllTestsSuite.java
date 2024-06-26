
package org.apereo.cas;

import org.apereo.cas.validation.Cas10ProtocolValidationSpecificationTests;
import org.apereo.cas.validation.Cas20ProtocolValidationSpecificationTests;
import org.apereo.cas.validation.Cas20WithoutProxyingValidationSpecificationTests;
import org.apereo.cas.validation.ImmutableAssertionTests;
import org.apereo.cas.validation.RegisteredServiceRequiredHandlersServiceTicketValidationAuthorizerTests;

import org.junit.platform.runner.JUnitPlatform;
import org.junit.platform.suite.api.SelectClasses;
import org.junit.runner.RunWith;

/**
 * This is {@link AllTestsSuite}.
 *
 * @author Auto-generated by Gradle Build
 * @since 6.0.0-RC3
 */
@SelectClasses({
    Cas20ProtocolValidationSpecificationTests.class,
    ImmutableAssertionTests.class,
    RegisteredServiceRequiredHandlersServiceTicketValidationAuthorizerTests.class,
    Cas10ProtocolValidationSpecificationTests.class,
    Cas20WithoutProxyingValidationSpecificationTests.class
})
@RunWith(JUnitPlatform.class)
public class AllTestsSuite {
}
