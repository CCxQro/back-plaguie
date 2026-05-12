package itesm.mx.application.usecase;

import itesm.mx.application.dto.CreateRecomendacionDto;
import itesm.mx.application.dto.GetRecomendacionResponseDto;
import itesm.mx.application.usecase.recomendacion.CreateRecomendacionUseCase;
import itesm.mx.application.usecase.recomendacion.GetRecomendacionByIdUseCase;
import itesm.mx.application.usecase.recomendacion.GetAllRecomendacionesUseCase;
import itesm.mx.application.usecase.recomendacion.ValidateRecomendacionUseCase;
import itesm.mx.domain.models.recomendacion.Recomendacion;
import itesm.mx.domain.repository.recomendacion.RecomendacionRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RecomendacionUseCaseTest {

    @Mock
    RecomendacionRepository recomendacionRepository;

    @InjectMocks
    GetAllRecomendacionesUseCase getAllRecomendacionesUseCase;

    @InjectMocks
    GetRecomendacionByIdUseCase getRecomendacionByIdUseCase;

    @InjectMocks
    CreateRecomendacionUseCase createRecomendacionUseCase;

    @InjectMocks
    ValidateRecomendacionUseCase validateRecomendacionUseCase;

    // --- GetAll ---

    @Test
    void getAll_ReturnsListOfRecomendaciones() {
        when(recomendacionRepository.findAllRecomendaciones()).thenReturn(List.of(buildRecomendacion(1L), buildRecomendacion(2L)));

        List<GetRecomendacionResponseDto> result = getAllRecomendacionesUseCase.execute();

        assertEquals(2, result.size());
        assertEquals(1L, result.get(0).recomendacionId);
        assertEquals(2L, result.get(1).recomendacionId);
    }

    // --- GetById ---

    @Test
    void getById_WhenExists_ReturnsRecomendacion() {
        when(recomendacionRepository.findRecomendacionById(1L)).thenReturn(Optional.of(buildRecomendacion(1L)));

        GetRecomendacionResponseDto result = getRecomendacionByIdUseCase.execute(1L);

        assertEquals(1L, result.recomendacionId);
        assertEquals("Recomendacion de prueba", result.titulo);
    }

    @Test
    void getById_WhenInvalidId_ThrowsIllegalArgumentException() {
        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> getRecomendacionByIdUseCase.execute(0L)
        );
        assertEquals("El ID de la recomendación no es válido", ex.getMessage());
    }

    @Test
    void getById_WhenNotFound_ThrowsIllegalStateException() {
        when(recomendacionRepository.findRecomendacionById(999L)).thenReturn(Optional.empty());

        IllegalStateException ex = assertThrows(
                IllegalStateException.class,
                () -> getRecomendacionByIdUseCase.execute(999L)
        );
        assertEquals("Recomendación no encontrada con id: 999", ex.getMessage());
    }

    // --- Create ---

    @Test
    void create_WhenValid_ReturnsCreatedRecomendacion() {
        CreateRecomendacionDto dto = buildCreateDto();
        when(recomendacionRepository.save(any(Recomendacion.class))).thenReturn(buildRecomendacion(10L));

        GetRecomendacionResponseDto result = createRecomendacionUseCase.execute(dto, 1L);

        assertEquals(10L, result.recomendacionId);
        assertEquals("Recomendacion de prueba", result.titulo);
        assertEquals(2L, result.statusId);
    }

    @Test
    void create_WhenNullDto_ThrowsIllegalArgumentException() {
        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> createRecomendacionUseCase.execute(null, 1L)
        );
        assertEquals("El cuerpo de la solicitud es requerido", ex.getMessage());
    }

    @Test
    void create_WhenMissingTitulo_ThrowsIllegalArgumentException() {
        CreateRecomendacionDto dto = buildCreateDto();
        dto.titulo = "   ";

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> createRecomendacionUseCase.execute(dto, 1L)
        );
        assertEquals("El título es requerido", ex.getMessage());
    }

    // --- Validate ---

    @Test
    void validate_WhenAccepted_ReturnsUpdatedRecomendacion() {
        Recomendacion existing = buildRecomendacion(1L);
        Recomendacion updated = buildRecomendacion(1L);
        updated.setStatusId(1L);
        updated.setStatusName("Accepted");
        updated.setValidatedByUserId(1L);

        when(recomendacionRepository.findRecomendacionById(1L)).thenReturn(Optional.of(existing));
        when(recomendacionRepository.update(any(Recomendacion.class))).thenReturn(updated);

        GetRecomendacionResponseDto result = validateRecomendacionUseCase.execute(1L, 1L, 1L);

        assertEquals(1L, result.statusId);
        assertEquals("Accepted", result.statusName);
    }

    @Test
    void validate_WhenRejected_ReturnsUpdatedRecomendacion() {
        Recomendacion existing = buildRecomendacion(1L);
        Recomendacion updated = buildRecomendacion(1L);
        updated.setStatusId(3L);
        updated.setStatusName("Rejected");
        updated.setValidatedByUserId(1L);

        when(recomendacionRepository.findRecomendacionById(1L)).thenReturn(Optional.of(existing));
        when(recomendacionRepository.update(any(Recomendacion.class))).thenReturn(updated);

        GetRecomendacionResponseDto result = validateRecomendacionUseCase.execute(1L, 3L, 1L);

        assertEquals(3L, result.statusId);
        assertEquals("Rejected", result.statusName);
    }

    @Test
    void validate_WhenInvalidId_ThrowsIllegalArgumentException() {
        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> validateRecomendacionUseCase.execute(0L, 1L, 1L)
        );
        assertEquals("El ID de la recomendación no es válido", ex.getMessage());
    }

    @Test
    void validate_WhenInvalidStatusId_ThrowsIllegalArgumentException() {
        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> validateRecomendacionUseCase.execute(1L, 2L, 1L)
        );
        assertEquals("El statusId debe ser 1 (Accepted) o 3 (Rejected)", ex.getMessage());
    }

    @Test
    void validate_WhenNotFound_ThrowsIllegalStateException() {
        when(recomendacionRepository.findRecomendacionById(999L)).thenReturn(Optional.empty());

        IllegalStateException ex = assertThrows(
                IllegalStateException.class,
                () -> validateRecomendacionUseCase.execute(999L, 1L, 1L)
        );
        assertEquals("Recomendación no encontrada con id: 999", ex.getMessage());
    }

    // --- Helpers ---

    private CreateRecomendacionDto buildCreateDto() {
        CreateRecomendacionDto dto = new CreateRecomendacionDto();
        dto.titulo = "Recomendacion de prueba";
        dto.descripcion = "Descripción de prueba";
        dto.tipoPlaga = "Langosta";
        dto.productosRecomendados = "Producto A, Producto B";
        return dto;
    }

    private Recomendacion buildRecomendacion(Long id) {
        Recomendacion recomendacion = new Recomendacion();
        recomendacion.setRecomendacionId(id);
        recomendacion.setTitulo("Recomendacion de prueba");
        recomendacion.setDescripcion("Descripción de prueba");
        recomendacion.setTipoPlaga("Langosta");
        recomendacion.setProductosRecomendados("Producto A, Producto B");
        recomendacion.setReportedByUserId(11L);
        recomendacion.setCreatedAt(LocalDateTime.of(2026, 1, 10, 8, 30));
        recomendacion.setStatusId(2L);
        recomendacion.setStatusName("Revision");
        return recomendacion;
    }
}
