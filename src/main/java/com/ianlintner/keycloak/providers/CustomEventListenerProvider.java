package com.ianlintner.keycloak.providers;

import org.keycloak.events.Event;
import org.keycloak.events.EventListenerProvider;
import org.keycloak.events.admin.AdminEvent;
import org.keycloak.models.KeycloakSession;

/**
 * Custom event listener provider for Keycloak
 * This is an example provider that can be extended for custom behavior
 */
public class CustomEventListenerProvider implements EventListenerProvider {

    private final KeycloakSession session;

    public CustomEventListenerProvider(KeycloakSession session) {
        this.session = session;
    }

    @Override
    public void onEvent(Event event) {
        // Log user events
        System.out.println("Custom Event: " + event.getType() + " for user: " + event.getUserId());
    }

    @Override
    public void onEvent(AdminEvent event, boolean includeRepresentation) {
        // Log admin events
        System.out.println("Custom Admin Event: " + event.getOperationType() + " on resource: " + event.getResourcePath());
    }

    @Override
    public void close() {
        // Cleanup if needed
    }
}
