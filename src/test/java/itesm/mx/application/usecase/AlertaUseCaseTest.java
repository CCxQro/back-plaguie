package itesm.mx.application.usecase;

import itesm.mx.application.dto.CreateAlertaDto;
import itesm.mx.application.dto.GetAlertaResponseDto;
import itesm.mx.application.usecase.alerta.CreateAlertaUseCase;
import itesm.mx.application.usecase.alerta.GetAlertaByIdUseCase;
import itesm.mx.application.usecase.alerta.GetAllAlertasUseCase;
import itesm.mx.application.usecase.alerta.ValidateAlertaUseCase;
import itesm.mx.domain.models.alerta.Alerta;
import itesm.mx.domain.repository.alerta.AlertaRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AlertaUseCaseTest {

    @Mock
    AlertaRepository alertaRepository;

    @InjectMocks
    GetAllAlertasUseCase getAllAlertasUseCase;

    @InjectMocks
    GetAlertaByIdUseCase getAlertaByIdUseCase;

    @InjectMocks
    CreateAlertaUseCase createAlertaUseCase;

    @InjectMocks
    ValidateAlertaUseCase validateAlertaUseCase;

    // --- GetAll ---

    @Test
    void getAll_ReturnsListOfAlertas() {
        when(alertaRepository.findAllAlertas()).thenReturn(List.of(buildAlerta(1L), buildAlerta(2L)));

        List<GetAlertaResponseDto> result = getAllAlertasUseCase.execute();

        assertEquals(2, result.size());
        assertEquals(1L, result.get(0).alertaId);
        assertEquals(2L, result.get(1).alertaId);
    }

    // --- GetById ---

    @Test
    void getById_WhenExists_ReturnsAlerta() {
        when(alertaRepository.findAlertaById(1L)).thenReturn(Optional.of(buildAlerta(1L)));

        GetAlertaResponseDto result = getAlertaByIdUseCase.execute(1L);

        assertEquals(1L, result.alertaId);
        assertEquals("Alerta de prueba", result.titulo);
    }

    @Test
    void getById_WhenInvalidId_ThrowsIllegalArgumentException() {
        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> getAlertaByIdUseCase.execute(0L)
        );
        assertEquals("El ID de la alerta no es válido", ex.getMessage());
    }

    @Test
    void getById_WhenNotFound_ThrowsIllegalStateException() {
        when(alertaRepository.findAlertaById(999L)).thenReturn(Optional.empty());

        IllegalStateException ex = assertThrows(
                IllegalStateException.class,
                () -> getAlertaByIdUseCase.execute(999L)
        );
        assertEquals("Alerta no encontrada con id: 999", ex.getMessage());
    }

    // --- Create ---

    @Test
    void create_WhenValid_ReturnsCreatedAlerta() {
        CreateAlertaDto dto = buildCreateDto();
        when(alertaRepository.save(any(Alerta.class))).thenReturn(buildAlerta(10L));

        GetAlertaResponseDto result = createAlertaUseCase.execute(dto, 1L);

        assertEquals(10L, result.alertaId);
        assertEquals("Alerta de prueba", result.titulo);
        assertEquals(2L, result.statusId);
    }

    @Test
    void create_WhenNullDto_ThrowsIllegalArgumentException() {
        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> createAlertaUseCase.execute(null, 1L)
        );
        assertEquals("El cuerpo de la solicitud es requerido", ex.getMessage());
    }

    @Test
    void create_WhenInvalidSeveridad_ThrowsIllegalArgumentException() {
        CreateAlertaDto dto = buildCreateDto();
        dto.severidad = "invalida";

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> createAlertaUseCase.execute(dto, 1L)
        );
        assertEquals("La severidad debe ser: critico, advertencia o informacion", ex.getMessage());
    }

    // --- Validate ---

    @Test
    void validate_WhenAccepted_ReturnsUpdatedAlerta() {
        Alerta existing = buildAlerta(1L);
        Alerta updated = buildAlerta(1L);
        updated.setStatusId(1L);
        updated.setStatusName("Accepted");
        updated.setValidatedByUserId(1L);

        when(alertaRepository.findAlertaById(1L)).thenReturn(Optional.of(existing));
        when(alertaRepository.update(any(Alerta.class))).thenReturn(updated);

        GetAlertaResponseDto result = validateAlertaUseCase.execute(1L, 1L, 1L);

        assertEquals(1L, result.statusId);
        assertEquals("Accepted", result.statusName);
    }

    @Test
    void validate_WhenRejected_ReturnsUpdatedAlerta() {
        Alerta existing = buildAlerta(1L);
        Alerta updated = buildAlerta(1L);
        updated.setStatusId(3L);
        updated.setStatusName("Rejected");
        updated.setValidatedByUserId(1L);

        when(alertaRepository.findAlertaById(1L)).thenReturn(Optional.of(existing));
        when(alertaRepository.update(any(Alerta.class))).thenReturn(updated);

        GetAlertaResponseDto result = validateAlertaUseCase.execute(1L, 3L, 1L);

        assertEquals(3L, result.statusId);
        assertEquals("Rejected", result.statusName);
    }

    @Test
    void validate_WhenInvalidId_ThrowsIllegalArgumentException() {
        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> validateAlertaUseCase.execute(0L, 1L, 1L)
        );
        assertEquals("El ID de la alerta no es válido", ex.getMessage());
    }

    @Test
    void validate_WhenInvalidStatusId_ThrowsIllegalArgumentException() {
        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> validateAlertaUseCase.execute(1L, 2L, 1L)
        );
        assertEquals("El statusId debe ser 1 (Accepted) o 3 (Rejected)", ex.getMessage());
    }

    @Test
    void validate_WhenNotFound_ThrowsIllegalStateException() {
        when(alertaRepository.findAlertaById(999L)).thenReturn(Optional.empty());

        IllegalStateException ex = assertThrows(
                IllegalStateException.class,
                () -> validateAlertaUseCase.execute(999L, 1L, 1L)
        );
        assertEquals("Alerta no encontrada con id: 999", ex.getMessage());
    }

    // --- Helpers ---

    private CreateAlertaDto buildCreateDto() {
        CreateAlertaDto dto = new CreateAlertaDto();
        dto.titulo = "Alerta de prueba";
        dto.descripcion = "Descripción de prueba";
        dto.ubicacionId = 1L;
        dto.tipoPlaga = "Langosta";
        dto.hectareas = new BigDecimal("50.00");
        dto.severidad = "critico";
        return dto;
    }

    private Alerta buildAlerta(Long id) {
        Alerta alerta = new Alerta();
        alerta.setAlertaId(id);
        alerta.setTitulo("Alerta de prueba");
        alerta.setDescripcion("Descripción de prueba");
        alerta.setUbicacionId(1L);
        alerta.setTipoPlaga("Langosta");
        alerta.setHectareas(new BigDecimal("50.00"));
        alerta.setSeveridad("critico");
        alerta.setReportedByUserId(6L);
        alerta.setCreatedAt(LocalDateTime.of(2026, 1, 10, 8, 30));
        alerta.setStatusId(2L);
        alerta.setStatusName("Revision");
        return alerta;
    }
}
