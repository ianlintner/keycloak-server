# Testing

This guide covers testing strategies for the Keycloak Server.

## Running Tests

### All Tests

```bash
mvn test
```

### Specific Test Class

```bash
mvn test -Dtest=CustomEventListenerProviderTest
```

### Specific Test Method

```bash
mvn test -Dtest=CustomEventListenerProviderTest#testOnEvent
```

### With Coverage

```bash
mvn verify
```

## Test Structure

```
src/test/java/
└── com/
    └── ianlintner/
        └── keycloak/
            └── providers/
                └── CustomEventListenerProviderTest.java
```

## Unit Testing

### Testing Providers

```java
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

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
        event.setUserId("test-user");

        assertDoesNotThrow(() -> provider.onEvent(event));
    }
}
```

### Testing with Mockito

```java
import static org.mockito.Mockito.*;

@Test
void testWithMocks() {
    KeycloakSession session = mock(KeycloakSession.class);
    RealmModel realm = mock(RealmModel.class);
    
    when(session.getContext()).thenReturn(context);
    when(context.getRealm()).thenReturn(realm);
    
    // Test your provider
    CustomProvider provider = new CustomProvider(session);
    provider.doSomething();
    
    verify(session, times(1)).getContext();
}
```

## Integration Testing

### Docker Test Containers

```java
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers
class IntegrationTest {

    @Container
    private static PostgreSQLContainer<?> postgres = 
        new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("keycloak")
            .withUsername("keycloak")
            .withPassword("keycloak");

    @Test
    void testDatabaseIntegration() {
        // Test with real database
    }
}
```

### Testing with Keycloak Test Framework

```java
import org.keycloak.testsuite.AbstractKeycloakTest;

class KeycloakIntegrationTest extends AbstractKeycloakTest {

    @Test
    void testCustomProvider() {
        // Test with Keycloak running
    }
}
```

## Test Coverage

### Generate Coverage Report

```bash
mvn verify
```

### View Coverage Report

Open `target/site/jacoco/index.html` in your browser.

### Coverage Goals

- Line Coverage: > 80%
- Branch Coverage: > 70%
- Method Coverage: > 90%

## Testing Best Practices

### 1. Follow AAA Pattern

```java
@Test
void testExample() {
    // Arrange
    Event event = new Event();
    event.setType(EventType.LOGIN);
    
    // Act
    provider.onEvent(event);
    
    // Assert
    verify(logger).info(contains("LOGIN"));
}
```

### 2. Use Descriptive Names

```java
@Test
void shouldLogLoginEventWhenUserLogsIn() {
    // Test implementation
}
```

### 3. Test Edge Cases

```java
@Test
void shouldHandleNullEvent() {
    assertThrows(NullPointerException.class, 
        () -> provider.onEvent(null));
}

@Test
void shouldHandleEmptyUserId() {
    Event event = new Event();
    event.setUserId("");
    
    assertDoesNotThrow(() -> provider.onEvent(event));
}
```

### 4. Use Test Fixtures

```java
class TestFixtures {
    static Event createLoginEvent(String userId) {
        Event event = new Event();
        event.setType(EventType.LOGIN);
        event.setUserId(userId);
        return event;
    }
}
```

## Continuous Testing

### Watch Mode

```bash
# Use Maven wrapper for watch mode
./mvnw fizzed-watcher:run
```

### IDE Integration

- **IntelliJ IDEA**: Run with coverage (Ctrl+Shift+F10)
- **Eclipse**: Right-click → Coverage As → JUnit Test
- **VS Code**: Use Test Explorer

## Performance Testing

### JMH Benchmarks

```java
import org.openjdk.jmh.annotations.*;

@State(Scope.Thread)
public class ProviderBenchmark {

    @Benchmark
    public void testProviderPerformance() {
        provider.onEvent(event);
    }
}
```

Run benchmarks:
```bash
mvn clean install
java -jar target/benchmarks.jar
```

## Test Data

### Test Resources

Place test data in `src/test/resources/`:

```
src/test/resources/
├── test-realm.json
├── test-users.json
└── application-test.properties
```

### Loading Test Data

```java
@Test
void testWithRealm() {
    InputStream is = getClass()
        .getResourceAsStream("/test-realm.json");
    // Use test data
}
```

## Debugging Tests

### Enable Debug Logging

```bash
mvn test -Dlog.level=DEBUG
```

### Remote Debug

```bash
mvn test -Dmaven.surefire.debug
```

Then attach debugger to port 5005.

## CI Testing

Tests run automatically in GitHub Actions:

```yaml
- name: Run tests
  run: mvn test

- name: Generate coverage
  run: mvn verify
```

## Next Steps

- [Building Guide](building.md)
- [Custom Providers](custom-providers.md)
- [CI/CD Setup](../deployment/production.md)
