package itesm.mx.application.usecase;

import itesm.mx.application.dto.CreateRegionInteresDto;
import itesm.mx.application.dto.GetRegionInteresResponseDto;
import itesm.mx.application.usecase.region.CreateRegionInteresUseCase;
import itesm.mx.domain.models.location.State;
import itesm.mx.domain.models.region.RegionInteres;
import itesm.mx.domain.repository.location.StateRepository;
import itesm.mx.domain.repository.region.RegionInteresRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CreateRegionInteresUseCaseTest {

    @Mock
    private RegionInteresRepository regionInteresRepository;

    @Mock
    private StateRepository stateRepository;

    @InjectMocks
    private CreateRegionInteresUseCase useCase;

    @Test
    void execute_HappyPath_SavesAndReturnsDto() {
        when(stateRepository.findStateById(5L)).thenReturn(Optional.of(new State(5L, "Michoacán")));
        when(regionInteresRepository.existsByUserIdAndStateId(7L, 5L)).thenReturn(false);
        when(regionInteresRepository.save(any(RegionInteres.class)))
                .thenReturn(new RegionInteres(1L, 7L, 5L, "Michoacán", LocalDateTime.now()));

        GetRegionInteresResponseDto result = useCase.execute(7L, new CreateRegionInteresDto(5L));

        assertEquals(1L, result.regionInteresId);
        assertEquals(5L, result.stateId);
        assertEquals("Michoacán", result.stateName);
        verify(regionInteresRepository).save(any(RegionInteres.class));
    }

    @Test
    void execute_InvalidUserId_ThrowsIllegalArgument() {
        assertThrows(IllegalArgumentException.class,
                () -> useCase.execute(0L, new CreateRegionInteresDto(5L)));
        verify(regionInteresRepository, never()).save(any());
    }

    @Test
    void execute_MissingStateId_ThrowsIllegalArgument() {
        assertThrows(IllegalArgumentException.class,
                () -> useCase.execute(7L, new CreateRegionInteresDto(null)));
    }

    @Test
    void execute_StateNotFound_ThrowsIllegalArgument() {
        when(stateRepository.findStateById(99L)).thenReturn(Optional.empty());
        assertThrows(IllegalArgumentException.class,
                () -> useCase.execute(7L, new CreateRegionInteresDto(99L)));
        verify(regionInteresRepository, never()).save(any());
    }

    @Test
    void execute_DuplicateState_ThrowsIllegalState() {
        when(stateRepository.findStateById(5L)).thenReturn(Optional.of(new State(5L, "Michoacán")));
        when(regionInteresRepository.existsByUserIdAndStateId(7L, 5L)).thenReturn(true);

        assertThrows(IllegalStateException.class,
                () -> useCase.execute(7L, new CreateRegionInteresDto(5L)));
        verify(regionInteresRepository, never()).save(any());
    }
}
