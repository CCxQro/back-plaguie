package itesm.mx.application.usecase.parcela;

import itesm.mx.application.dto.ParcelaCatalogItemDto;
import itesm.mx.domain.models.parcela.EstadoParcela;
import itesm.mx.domain.models.parcela.SistemaRiego;
import itesm.mx.domain.models.parcela.TipoCultivo;
import itesm.mx.domain.repository.parcela.ParcelaCatalogRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetParcelaCatalogsUseCaseTest {

    @Mock
    ParcelaCatalogRepository parcelaCatalogRepository;

    @InjectMocks
    GetParcelaCatalogsUseCase useCase;

    // --- getEstadosParcela ---

    @Test
    void getEstadosParcela_HappyPath_ReturnsMappedItems() {
        when(parcelaCatalogRepository.findAllEstadosParcela()).thenReturn(List.of(
                new EstadoParcela(1L, "Activo"),
                new EstadoParcela(2L, "Cosechada"),
                new EstadoParcela(3L, "Inactiva")
        ));

        List<ParcelaCatalogItemDto> result = useCase.getEstadosParcela();

        assertEquals(3, result.size());
        assertEquals(1L, result.get(0).id);
        assertEquals("Activo", result.get(0).nombre);
        assertEquals(2L, result.get(1).id);
        assertEquals("Cosechada", result.get(1).nombre);
    }

    @Test
    void getEstadosParcela_EmptyCatalog_ReturnsEmptyList() {
        when(parcelaCatalogRepository.findAllEstadosParcela()).thenReturn(List.of());

        List<ParcelaCatalogItemDto> result = useCase.getEstadosParcela();

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    // --- getTiposCultivo ---

    @Test
    void getTiposCultivo_HappyPath_ReturnsMappedItems() {
        when(parcelaCatalogRepository.findAllTiposCultivo()).thenReturn(List.of(
                new TipoCultivo(1L, "Maíz", null, null),
                new TipoCultivo(2L, "Frijol", null, null)
        ));

        List<ParcelaCatalogItemDto> result = useCase.getTiposCultivo();

        assertEquals(2, result.size());
        assertEquals(1L, result.get(0).id);
        assertEquals("Maíz", result.get(0).nombre);
    }

    @Test
    void getTiposCultivo_EmptyCatalog_ReturnsEmptyList() {
        when(parcelaCatalogRepository.findAllTiposCultivo()).thenReturn(List.of());

        List<ParcelaCatalogItemDto> result = useCase.getTiposCultivo();

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    // --- getSistemasRiego ---

    @Test
    void getSistemasRiego_HappyPath_ReturnsMappedItems() {
        when(parcelaCatalogRepository.findAllSistemasRiego()).thenReturn(List.of(
                new SistemaRiego(1L, "Goteo"),
                new SistemaRiego(2L, "Aspersión"),
                new SistemaRiego(3L, "Gravedad")
        ));

        List<ParcelaCatalogItemDto> result = useCase.getSistemasRiego();

        assertEquals(3, result.size());
        assertEquals(1L, result.get(0).id);
        assertEquals("Goteo", result.get(0).nombre);
        assertEquals(3L, result.get(2).id);
        assertEquals("Gravedad", result.get(2).nombre);
    }

    @Test
    void getSistemasRiego_EmptyCatalog_ReturnsEmptyList() {
        when(parcelaCatalogRepository.findAllSistemasRiego()).thenReturn(List.of());

        List<ParcelaCatalogItemDto> result = useCase.getSistemasRiego();

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }
}
