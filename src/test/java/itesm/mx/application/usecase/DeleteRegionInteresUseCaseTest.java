package itesm.mx.application.usecase;

import itesm.mx.application.usecase.region.DeleteRegionInteresUseCase;
import itesm.mx.domain.repository.region.RegionInteresRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DeleteRegionInteresUseCaseTest {

    @Mock
    private RegionInteresRepository regionInteresRepository;

    @InjectMocks
    private DeleteRegionInteresUseCase useCase;

    @Test
    void execute_HappyPath_Deletes() {
        when(regionInteresRepository.deleteByIdAndUserId(3L, 7L)).thenReturn(true);
        useCase.execute(7L, 3L);
        verify(regionInteresRepository).deleteByIdAndUserId(3L, 7L);
    }

    @Test
    void execute_NotFound_ThrowsIllegalState() {
        when(regionInteresRepository.deleteByIdAndUserId(3L, 7L)).thenReturn(false);
        assertThrows(IllegalStateException.class, () -> useCase.execute(7L, 3L));
    }

    @Test
    void execute_InvalidUserId_ThrowsIllegalArgument() {
        assertThrows(IllegalArgumentException.class, () -> useCase.execute(0L, 3L));
    }

    @Test
    void execute_InvalidRegionId_ThrowsIllegalArgument() {
        assertThrows(IllegalArgumentException.class, () -> useCase.execute(7L, 0L));
    }
}
