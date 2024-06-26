package org.apereo.cas.services.web.support;

import org.apereo.cas.authentication.AuthenticationServiceSelectionPlan;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.services.RegisteredServiceProperty.RegisteredServiceProperties;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.web.support.ArgumentExtractor;
import org.apereo.cas.web.support.filters.ResponseHeadersEnforcementFilter;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.BooleanUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Optional;

/**
 * This is {@link RegisteredServiceResponseHeadersEnforcementFilter}. A filter extension that looks at the properties of a
 * registered service to determine if headers should be injected into the response.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@RequiredArgsConstructor
@Slf4j
public class RegisteredServiceResponseHeadersEnforcementFilter extends ResponseHeadersEnforcementFilter {
    private final ServicesManager servicesManager;
    private final ArgumentExtractor argumentExtractor;
    private final AuthenticationServiceSelectionPlan authenticationRequestServiceSelectionStrategies;

    @Override
    protected void decideInsertContentSecurityPolicyHeader(final HttpServletResponse httpServletResponse, final HttpServletRequest httpServletRequest) {

        val shouldInject = shouldHttpHeaderBeInjectedIntoResponse(httpServletRequest, RegisteredServiceProperties.HTTP_HEADER_ENABLE_CONTENT_SECURITY_POLICY);

        if (shouldInject.isPresent()) {
            if (shouldInject.get()) {
                super.insertContentSecurityPolicyHeader(httpServletResponse, httpServletRequest);
            } else {
                LOGGER.trace("ContentSecurityPolicy header disabled by service definition");
            }
        } else {
            super.decideInsertContentSecurityPolicyHeader(httpServletResponse, httpServletRequest);
        }
    }

    @Override
    protected void decideInsertXSSProtectionHeader(final HttpServletResponse httpServletResponse, final HttpServletRequest httpServletRequest) {
        val shouldInject = shouldHttpHeaderBeInjectedIntoResponse(httpServletRequest, RegisteredServiceProperties.HTTP_HEADER_ENABLE_XSS_PROTECTION);
        if (shouldInject.isPresent()) {
            if (shouldInject.get()) {
                super.insertXSSProtectionHeader(httpServletResponse, httpServletRequest);
            } else {
                LOGGER.trace("XSSProtection header disabled by service definition");
            }
        } else {
            super.decideInsertXSSProtectionHeader(httpServletResponse, httpServletRequest);
        }
    }

    @Override
    protected void decideInsertXFrameOptionsHeader(final HttpServletResponse httpServletResponse, final HttpServletRequest httpServletRequest) {

        val shouldInject = shouldHttpHeaderBeInjectedIntoResponse(httpServletRequest, RegisteredServiceProperties.HTTP_HEADER_ENABLE_XFRAME_OPTIONS);

        if (shouldInject.isPresent()) {
            if (shouldInject.get()) {
                val xFrameOptions = getStringProperty(httpServletRequest, RegisteredServiceProperties.HTTP_HEADER_XFRAME_OPTIONS);
                super.insertXFrameOptionsHeader(httpServletResponse, httpServletRequest, xFrameOptions);    
            } else {
                LOGGER.trace("XFrameOptions header disabled by service definition");
            }
        } else {
            super.decideInsertXFrameOptionsHeader(httpServletResponse, httpServletRequest);
        }
    }

    @Override
    protected void decideInsertXContentTypeOptionsHeader(final HttpServletResponse httpServletResponse, final HttpServletRequest httpServletRequest) {
        val shouldInject = shouldHttpHeaderBeInjectedIntoResponse(httpServletRequest, RegisteredServiceProperties.HTTP_HEADER_ENABLE_XCONTENT_OPTIONS);
        if (shouldInject.isPresent()) {
            if (shouldInject.get()) {
                super.insertXContentTypeOptionsHeader(httpServletResponse, httpServletRequest);
            } else {
                LOGGER.trace("XContentOptions header disabled by service definition");
            }
        } else {
            super.decideInsertXContentTypeOptionsHeader(httpServletResponse, httpServletRequest);
        }
    }

    @Override
    protected void decideInsertCacheControlHeader(final HttpServletResponse httpServletResponse, final HttpServletRequest httpServletRequest) {
        val shouldInject = shouldHttpHeaderBeInjectedIntoResponse(httpServletRequest, RegisteredServiceProperties.HTTP_HEADER_ENABLE_CACHE_CONTROL);
        if (shouldInject.isPresent()) {
            if (shouldInject.get()) {
                super.insertCacheControlHeader(httpServletResponse, httpServletRequest);
            } else {
                LOGGER.trace("EnableCacheControl header disabled by service definition");
            }
        } else {
            super.decideInsertCacheControlHeader(httpServletResponse, httpServletRequest);
        }
    }

    @Override
    protected void decideInsertStrictTransportSecurityHeader(final HttpServletResponse httpServletResponse, final HttpServletRequest httpServletRequest) {
        val shouldInject = shouldHttpHeaderBeInjectedIntoResponse(httpServletRequest, RegisteredServiceProperties.HTTP_HEADER_ENABLE_STRICT_TRANSPORT_SECURITY);
        if (shouldInject.isPresent()) {
            if (shouldInject.get()) {
                super.insertStrictTransportSecurityHeader(httpServletResponse, httpServletRequest);
            } else {
                LOGGER.trace("StrictTransportSecurity header disabled by service definition");
            }
        } else {
            super.decideInsertStrictTransportSecurityHeader(httpServletResponse, httpServletRequest);
        }
    }

    private String getStringProperty(final HttpServletRequest request,
                                     final RegisteredServiceProperties property) {
        val result = getRegisteredServiceFromRequest(request);
        if (result.isPresent()) {
            val properties = result.get().getProperties();
            if (properties.containsKey(property.getPropertyName())) {
                val prop = properties.get(property.getPropertyName());
                return prop.getValue();
            }
        }
        return null;
    }
    
    /**
     * Check if the service is configured to include/not include the specified header.
     * 
     * @param request the http request
     * @param property the registered service property
     * @return Optional(true/false value of property); empty() if property not set
     */
    private Optional<Boolean> shouldHttpHeaderBeInjectedIntoResponse(final HttpServletRequest request,
                                                      final RegisteredServiceProperties property) {

        val propValue = getStringProperty(request, property);
        if (propValue != null) {
            return Optional.of(BooleanUtils.toBoolean(propValue));
        }
        return Optional.empty();
    }

    /**
     * Gets registered service from request.
     * Reading the request body by the argument extractor here may cause the underlying request stream
     * to close. If there are any underlying controllers or components that expect to read
     * or parse the request body, like those that handle ticket validation, they would fail given the
     * {@link HttpServletRequest#getReader()} is consumed by the argument extractor here and not available anymore.
     * Therefor, any of the inner components of the extractor might have to cache the request body
     * as an attribute, etc so they can re-process and re-extract as needed.
     *
     * @param request the request
     * @return the registered service from request
     */
    private Optional<RegisteredService> getRegisteredServiceFromRequest(final HttpServletRequest request) {
        val service = this.argumentExtractor.extractService(request);
        if (service != null) {
            val resolved = authenticationRequestServiceSelectionStrategies.resolveService(service);
            return Optional.ofNullable(this.servicesManager.findServiceBy(resolved));
        }
        return Optional.empty();
    }
}
