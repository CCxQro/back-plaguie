package itesm.mx.application.usecase.parcela;

import itesm.mx.application.dto.ParcelaDetailDto;
import itesm.mx.application.dto.ParcelaResponseDto;
import itesm.mx.domain.models.parcela.EstadoParcela;
import itesm.mx.domain.models.parcela.Parcela;
import itesm.mx.domain.models.parcela.ParcelaSuggestion;
import itesm.mx.domain.models.parcela.TipoCultivo;
import itesm.mx.domain.models.user.Farmer;
import itesm.mx.domain.repository.parcela.ParcelaRepository;
import itesm.mx.domain.repository.parcela.ParcelaSuggestionRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
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

        Parcela p = new Parcela();
        p.setParcelaId(parcelaId);
        p.setNombreParcela("Lote Norte");
        p.setFarmer(farmer);
        p.setTamanoHectareas(5.0);
        p.setFechaSiembra(LocalDate.now().minusMonths(2));
        p.setFechaCosecha(LocalDate.now().plusMonths(1));
        p.setEstadoParcela(estado);
        p.setTipoCultivo(cultivo);
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
    }
}
