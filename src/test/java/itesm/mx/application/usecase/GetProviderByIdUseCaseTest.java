package itesm.mx.application.usecase;

import itesm.mx.application.usecase.marketplace.provider.GetProviderByIdUseCase;
import itesm.mx.domain.models.marketplace.Provider;
import itesm.mx.domain.models.user.User;
import itesm.mx.domain.repository.marketplace.ProviderRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GetProviderByIdUseCaseTest {

    @Mock
    private ProviderRepository providerRepository;

    @InjectMocks
    private GetProviderByIdUseCase getProviderByIdUseCase;

    @Test
    void execute_WhenFound_ReturnsProvider() {
        User user = new User();
        user.setUserId(1L);
        Provider provider = new Provider(7L, user, "Acme");
        when(providerRepository.findByProviderId(7L)).thenReturn(Optional.of(provider));

        Optional<Provider> result = getProviderByIdUseCase.execute(7L);

        assertTrue(result.isPresent());
        assertEquals(7L, result.get().getProviderId());
        assertEquals("Acme", result.get().getName());
    }

    @Test
    void execute_WhenNotFound_ReturnsEmpty() {
        when(providerRepository.findByProviderId(99L)).thenReturn(Optional.empty());

        Optional<Provider> result = getProviderByIdUseCase.execute(99L);

        assertTrue(result.isEmpty());
    }

    @Test
    void execute_WhenIdIsNull_ThrowsIllegalArgumentException() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> getProviderByIdUseCase.execute(null));
        assertEquals("Provider id is required", ex.getMessage());
    }
}