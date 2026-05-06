package itesm.mx.application.usecase;

import itesm.mx.application.usecase.marketplace.provider.RegisterProviderUseCase;
import itesm.mx.domain.models.marketplace.Provider;
import itesm.mx.domain.models.user.User;
import itesm.mx.domain.repository.marketplace.ProviderRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RegisterProviderUseCaseTest {

    @Mock
    private ProviderRepository providerRepository;

    @InjectMocks
    private RegisterProviderUseCase registerProviderUseCase;

    @Test
    void execute_WhenValid_SavesAndReturnsProvider() {
        User user = new User();
        user.setUserId(10L);
        Provider input = new Provider(null, user, "AgroSupplier");
        Provider saved = new Provider(42L, user, "AgroSupplier");

        when(providerRepository.save(any(Provider.class))).thenReturn(saved);

        Provider result = registerProviderUseCase.execute(input);

        assertNotNull(result);
        assertEquals(42L, result.getProviderId());
        assertEquals("AgroSupplier", result.getName());
        assertEquals(10L, result.getUser().getUserId());
        verify(providerRepository).save(any(Provider.class));
    }

    @Test
    void execute_WhenProviderIsNull_ThrowsIllegalArgumentException() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> registerProviderUseCase.execute(null));
        assertEquals("Provider is required", ex.getMessage());
    }

    @Test
    void execute_WhenNameIsBlank_ThrowsIllegalArgumentException() {
        User user = new User();
        user.setUserId(10L);
        Provider input = new Provider(null, user, "  ");
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> registerProviderUseCase.execute(input));
        assertEquals("Provider name is required", ex.getMessage());
    }

    @Test
    void execute_WhenUserIsNull_ThrowsIllegalArgumentException() {
        Provider input = new Provider(null, null, "AgroSupplier");
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> registerProviderUseCase.execute(input));
        assertEquals("User is required", ex.getMessage());
    }
}