package itesm.mx.application.usecase;

import itesm.mx.application.usecase.marketplace.provider.DeleteProviderUseCase;
import itesm.mx.domain.repository.marketplace.ProviderRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DeleteProviderUseCaseTest {

    @Mock
    private ProviderRepository providerRepository;

    @InjectMocks
    private DeleteProviderUseCase deleteProviderUseCase;

    @Test
    void execute_WhenIdExists_ReturnsTrue() {
        when(providerRepository.delete(5L)).thenReturn(true);

        boolean result = deleteProviderUseCase.execute(5L);

        assertTrue(result);
        verify(providerRepository).delete(5L);
    }

    @Test
    void execute_WhenIdNotFound_ReturnsFalse() {
        when(providerRepository.delete(99L)).thenReturn(false);

        boolean result = deleteProviderUseCase.execute(99L);

        assertFalse(result);
        verify(providerRepository).delete(99L);
    }

    @Test
    void execute_WhenIdIsNull_ThrowsIllegalArgumentException() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> deleteProviderUseCase.execute(null));
        assertEquals("Provider id is required", ex.getMessage());
    }
}