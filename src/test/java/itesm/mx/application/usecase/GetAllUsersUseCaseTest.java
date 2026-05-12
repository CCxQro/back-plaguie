package itesm.mx.application.usecase;

import itesm.mx.application.dto.UserPageResponseDto;
import itesm.mx.application.usecase.users.GetAllUsersUseCase;
import itesm.mx.domain.models.user.User;
import itesm.mx.domain.repository.user.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GetAllUsersUseCaseTest {

    @Mock
    UserRepository userRepository;

    @InjectMocks
    GetAllUsersUseCase getAllUsersUseCase;

    private User buildUser(Long id, String name, String email, Integer roleId, Boolean isActive) {
        return new User(id, "uuid-" + id, name, email, roleId, isActive);
    }

    @Test
    void execute_DelegatesToRepositoryWithCorrectArgs() {
        when(userRepository.findUsersFiltered(0, 10, null, null, null)).thenReturn(List.of());
        when(userRepository.countUsersFiltered(null, null, null)).thenReturn(0L);

        getAllUsersUseCase.execute(0, 10, null, null, null);

        verify(userRepository).findUsersFiltered(0, 10, null, null, null);
        verify(userRepository).countUsersFiltered(null, null, null);
    }

    @Test
    void execute_MapsUsersToDtoCorrectly() {
        User user = buildUser(1L, "Ana", "ana@test.mx", 2, true);
        when(userRepository.findUsersFiltered(0, 10, null, null, null)).thenReturn(List.of(user));
        when(userRepository.countUsersFiltered(null, null, null)).thenReturn(1L);

        UserPageResponseDto result = getAllUsersUseCase.execute(0, 10, null, null, null);

        assertEquals(1, result.content.size());
        assertEquals(1L, result.content.get(0).userId);
        assertEquals("Ana", result.content.get(0).name);
        assertEquals("ana@test.mx", result.content.get(0).email);
        assertEquals(2, result.content.get(0).roleId);
        assertTrue(result.content.get(0).isActive);
    }

    @Test
    void execute_CalculatesTotalPagesCorrectly() {
        when(userRepository.findUsersFiltered(0, 10, null, null, null)).thenReturn(List.of());
        when(userRepository.countUsersFiltered(null, null, null)).thenReturn(25L);

        UserPageResponseDto result = getAllUsersUseCase.execute(0, 10, null, null, null);

        assertEquals(25L, result.totalElements);
        assertEquals(3, result.totalPages);
        assertEquals(0, result.page);
        assertEquals(10, result.size);
    }

    @Test
    void execute_WhenTotalExactlyDivisible_CalculatesCorrectPages() {
        when(userRepository.findUsersFiltered(0, 5, null, null, null)).thenReturn(List.of());
        when(userRepository.countUsersFiltered(null, null, null)).thenReturn(10L);

        UserPageResponseDto result = getAllUsersUseCase.execute(0, 5, null, null, null);

        assertEquals(2, result.totalPages);
    }

    @Test
    void execute_ForwardsFiltersToRepository() {
        when(userRepository.findUsersFiltered(1, 5, "maria", 2, true)).thenReturn(List.of());
        when(userRepository.countUsersFiltered("maria", 2, true)).thenReturn(0L);

        getAllUsersUseCase.execute(1, 5, "maria", 2, true);

        verify(userRepository).findUsersFiltered(1, 5, "maria", 2, true);
        verify(userRepository).countUsersFiltered("maria", 2, true);
    }

    @Test
    void execute_WhenNoResults_ReturnsEmptyContentAndZeroTotals() {
        when(userRepository.findUsersFiltered(0, 10, "nonexistent", null, null)).thenReturn(List.of());
        when(userRepository.countUsersFiltered("nonexistent", null, null)).thenReturn(0L);

        UserPageResponseDto result = getAllUsersUseCase.execute(0, 10, "nonexistent", null, null);

        assertNotNull(result.content);
        assertTrue(result.content.isEmpty());
        assertEquals(0L, result.totalElements);
        assertEquals(0, result.totalPages);
    }

    @Test
    void execute_WhenPageIsNegative_ThrowsIllegalArgumentException() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> getAllUsersUseCase.execute(-1, 10, null, null, null));
        assertEquals("El número de página no puede ser negativo", ex.getMessage());
        verifyNoInteractions(userRepository);
    }

    @Test
    void execute_WhenSizeIsZero_ThrowsIllegalArgumentException() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> getAllUsersUseCase.execute(0, 0, null, null, null));
        assertEquals("El tamaño de página debe ser mayor a cero", ex.getMessage());
        verifyNoInteractions(userRepository);
    }

    @Test
    void execute_WhenSizeIsNegative_ThrowsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class,
                () -> getAllUsersUseCase.execute(0, -5, null, null, null));
        verifyNoInteractions(userRepository);
    }
}
