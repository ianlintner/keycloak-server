package com.ianlintner.keycloak.providers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.keycloak.events.Event;
import org.keycloak.events.EventType;
import org.keycloak.events.admin.AdminEvent;
import org.keycloak.events.admin.OperationType;
import org.keycloak.models.KeycloakSession;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test for CustomEventListenerProvider
 */
class CustomEventListenerProviderTest {

    @Mock
    private KeycloakSession session;

    private CustomEventListenerProvider provider;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        provider = new CustomEventListenerProvider(session);
    }

    @Test
    void testOnEvent() {
        Event event = new Event();
        event.setType(EventType.LOGIN);
        event.setUserId("test-user-id");

        assertDoesNotThrow(() -> provider.onEvent(event));
    }

    @Test
    void testOnAdminEvent() {
        AdminEvent adminEvent = new AdminEvent();
        adminEvent.setOperationType(OperationType.CREATE);
        adminEvent.setResourcePath("test/resource");

        assertDoesNotThrow(() -> provider.onEvent(adminEvent, true));
    }

    @Test
    void testClose() {
        assertDoesNotThrow(() -> provider.close());
    }
}
