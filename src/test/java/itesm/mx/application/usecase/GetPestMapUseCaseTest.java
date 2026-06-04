package itesm.mx.application.usecase;

import itesm.mx.application.dto.GetPestMapPointDto;
import itesm.mx.application.usecase.reporte.GetPestMapUseCase;
import itesm.mx.domain.models.reporte.PestMapPoint;
import itesm.mx.domain.repository.reporte.PestMapRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetPestMapUseCaseTest {

    @Mock
    private PestMapRepository pestMapRepository;

    @InjectMocks
    private GetPestMapUseCase useCase;

    @Test
    void execute_MapsPointsToDto() {
        when(pestMapRepository.findValidatedMapPoints()).thenReturn(List.of(
                new PestMapPoint(3L, new BigDecimal("24.8053"), new BigDecimal("-107.3941"),
                        "Araña roja", "Tomate", "Especie A", "sinaloa", "culiacán",
                        new BigDecimal("15.00"), LocalDateTime.of(2026, 5, 10, 10, 30))
        ));

        List<GetPestMapPointDto> result = useCase.execute();

        assertEquals(1, result.size());
        GetPestMapPointDto dto = result.get(0);
        assertEquals(3L, dto.vigilanciaId);
        assertEquals(24.8053, dto.latitude);
        assertEquals(-107.3941, dto.longitude);
        assertEquals("Araña roja", dto.plagaNombre);
        assertEquals("sinaloa", dto.estadoNombre);
        assertEquals("culiacán", dto.municipioNombre);
        assertEquals("2026-05-10T10:30", dto.validatedAt);
    }

    @Test
    void execute_NoPoints_ReturnsEmpty() {
        when(pestMapRepository.findValidatedMapPoints()).thenReturn(List.of());
        assertTrue(useCase.execute().isEmpty());
    }
}
