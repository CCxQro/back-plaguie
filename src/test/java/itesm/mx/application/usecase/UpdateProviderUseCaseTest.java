package itesm.mx.application.usecase;

import itesm.mx.application.usecase.marketplace.provider.UpdateProviderUseCase;
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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UpdateProviderUseCaseTest {

    @Mock
    private ProviderRepository providerRepository;

    @InjectMocks
    private UpdateProviderUseCase updateProviderUseCase;

    @Test
    void execute_WhenValid_UpdatesAndReturnsProvider() {
        User user = new User();
        user.setUserId(10L);
        Provider input = new Provider(null, user, "Renamed");
        Provider updated = new Provider(7L, user, "Renamed");

        when(providerRepository.update(eq(7L), any(Provider.class))).thenReturn(updated);

        Provider result = updateProviderUseCase.execute(7L, input);

        assertNotNull(result);
        assertEquals(7L, result.getProviderId());
        assertEquals("Renamed", result.getName());
        verify(providerRepository).update(eq(7L), any(Provider.class));
    }

    @Test
    void execute_WhenIdIsNull_ThrowsIllegalArgumentException() {
        User user = new User();
        user.setUserId(10L);
        Provider input = new Provider(null, user, "X");
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> updateProviderUseCase.execute(null, input));
        assertEquals("Provider id is required", ex.getMessage());
    }

    @Test
    void execute_WhenProviderIsNull_ThrowsIllegalArgumentException() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> updateProviderUseCase.execute(1L, null));
        assertEquals("Provider is required", ex.getMessage());
    }

    @Test
    void execute_WhenNameIsBlank_ThrowsIllegalArgumentException() {
        User user = new User();
        user.setUserId(10L);
        Provider input = new Provider(null, user, "");
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> updateProviderUseCase.execute(1L, input));
        assertEquals("Provider name is required", ex.getMessage());
    }

    @Test
    void execute_WhenUserIsNull_ThrowsIllegalArgumentException() {
        Provider input = new Provider(null, null, "X");
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> updateProviderUseCase.execute(1L, input));
        assertEquals("User is required", ex.getMessage());
    }
}