package itesm.mx.application.usecase.parcela;

import itesm.mx.application.dto.ParcelaDetailDto;
import itesm.mx.application.dto.ParcelaResponseDto;
import itesm.mx.domain.models.location.Location;
import itesm.mx.domain.models.parcela.EstadoParcela;
import itesm.mx.domain.models.parcela.Parcela;
import itesm.mx.domain.models.parcela.ParcelaSuggestion;
import itesm.mx.domain.models.parcela.SistemaRiego;
import itesm.mx.domain.models.parcela.TipoCultivo;
import itesm.mx.domain.models.user.Farmer;
import itesm.mx.domain.repository.parcela.ParcelaRepository;
import itesm.mx.domain.repository.parcela.ParcelaSuggestionRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetParcelaDetailUseCaseTest {

    private static final GeometryFactory GEOMETRY_FACTORY = new GeometryFactory();

    @Mock
    ParcelaRepository parcelaRepository;

    @Mock
    ParcelaSuggestionRepository parcelaSuggestionRepository;

    @InjectMocks
    GetParcelaDetailUseCase useCase;

    private Parcela makeParcela(Long parcelaId, Long farmerId) {
        Farmer farmer = new Farmer();
        farmer.setFarmerId(farmerId);

        EstadoParcela estado = new EstadoParcela();
        estado.setEstadoParcelaId(1L);
        estado.setNombre("Activo");

        TipoCultivo cultivo = new TipoCultivo();
        cultivo.setTipoCultivoId(1L);
        cultivo.setNombre("Maíz");

        SistemaRiego riego = new SistemaRiego();
        riego.setSistemaRiegoId(1L);
        riego.setNombre("Goteo");

        Location location = new Location();
        location.setLocationId(1L);
        location.setCoordinates(GEOMETRY_FACTORY.createPoint(new Coordinate(-103.35, 20.67)));

        Parcela p = new Parcela();
        p.setParcelaId(parcelaId);
        p.setNombreParcela("Lote Norte");
        p.setFarmer(farmer);
        p.setTamanoHectareas(5.0);
        p.setPhSuelo(6.8);
        p.setFechaSiembra(LocalDate.of(2024, 3, 1));
        p.setFechaCosecha(LocalDate.of(2024, 8, 1));
        p.setEstadoParcela(estado);
        p.setTipoCultivo(cultivo);
        p.setSistemaRiego(riego);
        p.setLocation(location);
        p.setIsActive(true);
        return p;
    }

    @Test
    void execute_WithNoSuggestions_Returns100HealthAndEmptyList() {
        Parcela parcela = makeParcela(1L, 10L);
        when(parcelaRepository.findParcelaById(1L)).thenReturn(Optional.of(parcela));
        when(parcelaSuggestionRepository.findByParcelaId(1L)).thenReturn(Collections.emptyList());

        ParcelaDetailDto dto = useCase.execute(1L, null);

        assertNotNull(dto);
        assertEquals(100.0, dto.healthPercentage);
        assertTrue(dto.suggestions.isEmpty());
        assertNotNull(dto.parcela);
        assertEquals("Lote Norte", dto.parcela.nombre);
    }

    @Test
    void execute_WithSuggestions_ReturnsReducedHealth() {
        Parcela parcela = makeParcela(2L, 10L);
        when(parcelaRepository.findParcelaById(2L)).thenReturn(Optional.of(parcela));

        ParcelaSuggestion s1 = new ParcelaSuggestion();
        s1.setMessage("Revisión preventiva 1");
        ParcelaSuggestion s2 = new ParcelaSuggestion();
        s2.setMessage("Revisión preventiva 2");
        when(parcelaSuggestionRepository.findByParcelaId(2L)).thenReturn(List.of(s1, s2));

        ParcelaDetailDto dto = useCase.execute(2L, null);

        assertEquals(80.0, dto.healthPercentage);
        assertEquals(2, dto.suggestions.size());
    }

    @Test
    void execute_NullParcelaId_ThrowsIllegalArgument() {
        assertThrows(IllegalArgumentException.class, () -> useCase.execute(null, null));
    }

    @Test
    void execute_ParcelaNotFound_ThrowsIllegalArgument() {
        when(parcelaRepository.findParcelaById(99L)).thenReturn(Optional.empty());
        assertThrows(IllegalArgumentException.class, () -> useCase.execute(99L, null));
    }

    @Test
    void execute_FarmerIdMismatch_ThrowsIllegalArgument() {
        Parcela parcela = makeParcela(3L, 10L);
        when(parcelaRepository.findParcelaById(3L)).thenReturn(Optional.of(parcela));

        // farmerId 99 is not owner (owner is 10)
        assertThrows(IllegalArgumentException.class, () -> useCase.execute(3L, 99L));
    }

    @Test
    void execute_HealthNeverBelowZero_WithManySuggestions() {
        Parcela parcela = makeParcela(4L, 10L);
        when(parcelaRepository.findParcelaById(4L)).thenReturn(Optional.of(parcela));

        // 15 suggestions would give -50, should clamp to 0
        List<ParcelaSuggestion> many = new java.util.ArrayList<>();
        for (int i = 0; i < 15; i++) {
            ParcelaSuggestion s = new ParcelaSuggestion();
            s.setMessage("msg " + i);
            many.add(s);
        }
        when(parcelaSuggestionRepository.findByParcelaId(4L)).thenReturn(many);

        ParcelaDetailDto dto = useCase.execute(4L, null);

        assertEquals(0.0, dto.healthPercentage);
        assertEquals(0.0, dto.saludPorcentaje);
    }

    @Test
    void execute_WithEnrichedFields_PopulatesCoordinatesAndAgronomicData() {
        Parcela parcela = makeParcela(5L, 10L);
        when(parcelaRepository.findParcelaById(5L)).thenReturn(Optional.of(parcela));
        when(parcelaSuggestionRepository.findByParcelaId(5L)).thenReturn(Collections.emptyList());

        ParcelaDetailDto dto = useCase.execute(5L, null);

        assertNotNull(dto);
        // Coordenadas
        assertEquals(20.67, dto.latitud, 0.001);
        assertEquals(-103.35, dto.longitud, 0.001);
        // Datos agronómicos
        assertEquals("Goteo", dto.sistemaRiego);
        assertEquals(6.8, dto.phSuelo);
        assertEquals("2024-03-01", dto.fechaSiembra);
        assertEquals("2024-08-01", dto.fechaCosecha);
        // Historial siempre vacío hasta que exista la tabla
        assertNotNull(dto.historial);
        assertTrue(dto.historial.isEmpty());
        // saludPorcentaje y sugerencias
        assertEquals(100.0, dto.saludPorcentaje);
        assertNotNull(dto.sugerencias);
        assertTrue(dto.sugerencias.isEmpty());
    }
}
