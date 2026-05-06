package itesm.mx.application.usecase;

import itesm.mx.application.usecase.marketplace.provider.GetAllProvidersUseCase;
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
class GetAllProvidersUseCaseTest {

    @Mock
    private ProviderRepository providerRepository;

    @InjectMocks
    private GetAllProvidersUseCase getAllProvidersUseCase;

    @Test
    void execute_ReturnsAllProvidersFromRepository() {
        User user = new User();
        user.setUserId(1L);
        List<Provider> providers = List.of(
                new Provider(1L, user, "A"),
                new Provider(2L, user, "B")
        );
        when(providerRepository.findAllProviders()).thenReturn(providers);

        List<Provider> result = getAllProvidersUseCase.execute();

        assertEquals(2, result.size());
        assertEquals("A", result.get(0).getName());
        assertEquals("B", result.get(1).getName());
        verify(providerRepository).findAllProviders();
    }
}