# Custom Providers

Learn how to create and deploy custom Keycloak providers.

## Overview

Keycloak uses a Service Provider Interface (SPI) architecture that allows you to extend its functionality through custom providers.

## Provider Types

### Event Listener Provider

Event listeners react to user and admin events.

```java
package com.ianlintner.keycloak.providers;

import org.keycloak.events.Event;
import org.keycloak.events.EventListenerProvider;
import org.keycloak.events.admin.AdminEvent;
import org.keycloak.models.KeycloakSession;

public class CustomEventListenerProvider implements EventListenerProvider {
    
    private final KeycloakSession session;

    public CustomEventListenerProvider(KeycloakSession session) {
        this.session = session;
    }

    @Override
    public void onEvent(Event event) {
        // Handle user events
        System.out.println("User event: " + event.getType());
    }

    @Override
    public void onEvent(AdminEvent event, boolean includeRepresentation) {
        // Handle admin events
        System.out.println("Admin event: " + event.getOperationType());
    }

    @Override
    public void close() {
        // Cleanup
    }
}
```

### Provider Factory

```java
package com.ianlintner.keycloak.providers;

import org.keycloak.Config;
import org.keycloak.events.EventListenerProvider;
import org.keycloak.events.EventListenerProviderFactory;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;

public class CustomEventListenerProviderFactory implements EventListenerProviderFactory {

    private static final String PROVIDER_ID = "custom-event-listener";

    @Override
    public EventListenerProvider create(KeycloakSession session) {
        return new CustomEventListenerProvider(session);
    }

    @Override
    public void init(Config.Scope config) {
        // Initialize with configuration
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
```

## Registering Providers

Create a service loader file:

**File**: `src/main/resources/META-INF/services/org.keycloak.events.EventListenerProviderFactory`

```
com.ianlintner.keycloak.providers.CustomEventListenerProviderFactory
```

## Available SPIs

| SPI | Purpose |
|-----|---------|
| EventListenerProvider | React to user and admin events |
| AuthenticatorProvider | Custom authentication flows |
| RequiredActionProvider | Actions required before login |
| ProtocolMapper | Map user attributes to tokens |
| UserStorageProvider | Custom user storage |
| IdentityProvider | Custom identity providers |

## Creating a Custom Authenticator

```java
public class CustomAuthenticator implements Authenticator {

    @Override
    public void authenticate(AuthenticationFlowContext context) {
        // Custom authentication logic
        context.success();
    }

    @Override
    public void action(AuthenticationFlowContext context) {
        // Handle form submission
    }

    @Override
    public boolean requiresUser() {
        return false;
    }

    @Override
    public boolean configuredFor(KeycloakSession session, 
                                 RealmModel realm, 
                                 UserModel user) {
        return true;
    }

    @Override
    public void setRequiredActions(KeycloakSession session, 
                                   RealmModel realm, 
                                   UserModel user) {
        // Set required actions
    }

    @Override
    public void close() {
        // Cleanup
    }
}
```

## Testing Custom Providers

```java
@Test
void testCustomProvider() {
    KeycloakSession session = mock(KeycloakSession.class);
    CustomEventListenerProvider provider = 
        new CustomEventListenerProvider(session);
    
    Event event = new Event();
    event.setType(EventType.LOGIN);
    
    assertDoesNotThrow(() -> provider.onEvent(event));
}
```

## Deployment

### Local Development

```bash
# Build the provider
mvn clean package

# Copy to Keycloak
cp target/*.jar /opt/keycloak/providers/

# Rebuild Keycloak
/opt/keycloak/bin/kc.sh build
```

### Docker

The Dockerfile automatically includes providers:

```dockerfile
COPY target/*.jar /opt/keycloak/providers/
RUN /opt/keycloak/bin/kc.sh build
```

### Kubernetes

Providers are baked into the Docker image, so just deploy:

```bash
kubectl apply -f k8s/keycloak-deployment.yaml
```

## Enabling Providers

### Via Admin Console

1. Go to Realm Settings
2. Click on Events tab
3. Add your provider to Event Listeners
4. Save

### Via Environment Variable

```bash
KC_SPI_EVENTS_LISTENER_CUSTOM_EVENT_LISTENER_ENABLED=true
```

## Best Practices

1. **Thread Safety**: Providers must be thread-safe
2. **Minimal State**: Keep provider state minimal
3. **Error Handling**: Handle exceptions gracefully
4. **Logging**: Use proper logging (not System.out)
5. **Testing**: Write comprehensive tests
6. **Documentation**: Document configuration options

## Example: Database Event Logger

```java
public class DatabaseEventLogger implements EventListenerProvider {
    
    private final KeycloakSession session;
    
    @Override
    public void onEvent(Event event) {
        // Log to database using JPA
        EntityManager em = session.getProvider(JpaConnectionProvider.class)
            .getEntityManager();
        
        EventLog log = new EventLog();
        log.setEventType(event.getType().toString());
        log.setUserId(event.getUserId());
        log.setTimestamp(new Date());
        
        em.persist(log);
    }
}
```

## Next Steps

- [Testing Guide](testing.md)
- [Deployment Guide](../deployment/kubernetes.md)
- [Keycloak SPI Documentation](https://www.keycloak.org/docs/latest/server_development/)
