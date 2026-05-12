package itesm.mx.application.usecase;

import itesm.mx.application.usecase.marketplace.provider.GetProvidersByUserUseCase;
import itesm.mx.domain.models.marketplace.Provider;
import itesm.mx.domain.models.user.User;
import itesm.mx.domain.repository.marketplace.ProviderRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GetProvidersByUserUseCaseTest {

    @Mock
    private ProviderRepository providerRepository;

    @InjectMocks
    private GetProvidersByUserUseCase getProvidersByUserUseCase;

    @Test
    void execute_WhenValidUserId_ReturnsList() {
        User user = new User();
        user.setUserId(5L);
        List<Provider> providers = List.of(new Provider(1L, user, "A"));
        when(providerRepository.findAllByUserId(5L)).thenReturn(providers);

        List<Provider> result = getProvidersByUserUseCase.execute(5L);

        assertEquals(1, result.size());
        assertEquals("A", result.get(0).getName());
        verify(providerRepository).findAllByUserId(5L);
    }

    @Test
    void execute_WhenUserIdIsNull_ThrowsIllegalArgumentException() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> getProvidersByUserUseCase.execute(null));
        assertEquals("User id is required", ex.getMessage());
    }
}