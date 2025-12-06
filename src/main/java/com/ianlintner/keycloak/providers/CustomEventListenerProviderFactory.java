package com.ianlintner.keycloak.providers;

import org.keycloak.Config;
import org.keycloak.events.EventListenerProvider;
import org.keycloak.events.EventListenerProviderFactory;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;

/**
 * Factory for creating CustomEventListenerProvider instances
 */
public class CustomEventListenerProviderFactory implements EventListenerProviderFactory {

    private static final String PROVIDER_ID = "custom-event-listener";

    @Override
    public EventListenerProvider create(KeycloakSession session) {
        return new CustomEventListenerProvider(session);
    }

    @Override
    public void init(Config.Scope config) {
        // Initialize with configuration
        System.out.println("Initializing Custom Event Listener Provider");
    }

    @Override
    public void postInit(KeycloakSessionFactory factory) {
        // Post initialization
    }

    @Override
    public void close() {
        // Cleanup
    }

    @Override
    public String getId() {
        return PROVIDER_ID;
    }
}
