package org.apereo.cas.services;

import org.springframework.context.ApplicationEventPublisher;

import java.util.Collection;
import java.util.List;

/**
 * This is {@link ImmutableInMemoryServiceRegistry}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
public class ImmutableInMemoryServiceRegistry extends InMemoryServiceRegistry implements ImmutableServiceRegistry {
    public ImmutableInMemoryServiceRegistry(final List<RegisteredService> registeredServices,
                                            final ApplicationEventPublisher eventPublisher,
                                            final Collection<ServiceRegistryListener> serviceRegistryListeners) {
        super(eventPublisher, registeredServices, serviceRegistryListeners);
    }

    @Override
    public RegisteredService save(final RegisteredService registeredService) {
        return registeredService;
    }
}
