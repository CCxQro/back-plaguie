package itesm.mx.application.usecase;

import itesm.mx.application.dto.CreateVigilanciaFitosanitariaDto;
import itesm.mx.application.dto.GetVigilanciaFitosanitariaResponseDto;
import itesm.mx.application.dto.UpdateVigilanciaFitosanitariaDto;
import itesm.mx.application.usecase.vigilancia.CreateVigilanciaFitosanitariaUseCase;
import itesm.mx.application.usecase.vigilancia.DeleteVigilanciaFitosanitariaUseCase;
import itesm.mx.application.usecase.vigilancia.GetVigilanciaFitosanitariaByIdUseCase;
import itesm.mx.application.usecase.vigilancia.UpdateVigilanciaFitosanitariaUseCase;
import itesm.mx.domain.models.location.Location;
import itesm.mx.domain.models.vigilancia.ClaveIdentificacionPlaga;
import itesm.mx.domain.models.vigilancia.Especie;
import itesm.mx.domain.models.vigilancia.Hospedante;
import itesm.mx.domain.models.vigilancia.Plaga;
import itesm.mx.domain.models.vigilancia.SistemaMonitoreo;
import itesm.mx.domain.models.vigilancia.VigilanciaFitosanitaria;
import itesm.mx.domain.models.vigilancia.Variedad;
import itesm.mx.domain.repository.vigilancia.VigilanciaFitosanitariaRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class VigilanciaFitosanitariaUseCaseTest {

    @Mock
    VigilanciaFitosanitariaRepository vigilanciaFitosanitariaRepository;

    @InjectMocks
    CreateVigilanciaFitosanitariaUseCase createVigilanciaFitosanitariaUseCase;

    @InjectMocks
    UpdateVigilanciaFitosanitariaUseCase updateVigilanciaFitosanitariaUseCase;

    @InjectMocks
    DeleteVigilanciaFitosanitariaUseCase deleteVigilanciaFitosanitariaUseCase;

    @InjectMocks
    GetVigilanciaFitosanitariaByIdUseCase getVigilanciaFitosanitariaByIdUseCase;

    @Test
    void create_WhenRequestIsValid_ReturnsCreatedResponse() {
        CreateVigilanciaFitosanitariaDto request = buildCreateDto();

        VigilanciaFitosanitaria created = buildDomain(22L, new BigDecimal("15.25"));
        when(vigilanciaFitosanitariaRepository.save(any(VigilanciaFitosanitaria.class))).thenReturn(created);

        GetVigilanciaFitosanitariaResponseDto response = createVigilanciaFitosanitariaUseCase.execute(request);

        assertEquals(22L, response.vigilanciaFitosanitariaId);
        assertEquals(new BigDecimal("15.25"), response.ahosp);
        verify(vigilanciaFitosanitariaRepository).save(any(VigilanciaFitosanitaria.class));
    }

    @Test
    void create_WhenLatitudeIsInvalid_ThrowsIllegalArgumentException() {
        CreateVigilanciaFitosanitariaDto request = buildCreateDto();
        request.latitude = new BigDecimal("95.00000000");

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> createVigilanciaFitosanitariaUseCase.execute(request)
        );

        assertEquals("La latitud no es valida", exception.getMessage());
    }

    @Test
    void update_WhenPartialRequestIsValid_ReturnsUpdatedResponse() {
        UpdateVigilanciaFitosanitariaDto request = new UpdateVigilanciaFitosanitariaDto();
        request.ahosp = new BigDecimal("18.00");

        VigilanciaFitosanitaria existing = buildDomain(22L, new BigDecimal("15.25"));
        VigilanciaFitosanitaria updated = buildDomain(22L, new BigDecimal("18.00"));

        when(vigilanciaFitosanitariaRepository.findVigilanciaFitosanitariaById(22L)).thenReturn(Optional.of(existing));
        when(vigilanciaFitosanitariaRepository.update(any(VigilanciaFitosanitaria.class))).thenReturn(updated);

        GetVigilanciaFitosanitariaResponseDto response = updateVigilanciaFitosanitariaUseCase.execute(22L, request);

        assertEquals(22L, response.vigilanciaFitosanitariaId);
        assertEquals(new BigDecimal("18.00"), response.ahosp);
        verify(vigilanciaFitosanitariaRepository).update(any(VigilanciaFitosanitaria.class));
    }

    @Test
    void update_WhenDtoHasNoFields_ThrowsIllegalArgumentException() {
        UpdateVigilanciaFitosanitariaDto request = new UpdateVigilanciaFitosanitariaDto();

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> updateVigilanciaFitosanitariaUseCase.execute(22L, request)
        );

        assertEquals("Debe proporcionar al menos un campo para actualizar", exception.getMessage());
    }

    @Test
    void delete_WhenRecordExists_DelegatesToRepository() {
        VigilanciaFitosanitaria existing = buildDomain(22L, new BigDecimal("15.25"));
        when(vigilanciaFitosanitariaRepository.findVigilanciaFitosanitariaById(22L)).thenReturn(Optional.of(existing));

        deleteVigilanciaFitosanitariaUseCase.execute(22L);

        verify(vigilanciaFitosanitariaRepository).delete(22L);
    }

    @Test
    void getById_WhenRecordExists_ReturnsResponse() {
        VigilanciaFitosanitaria existing = buildDomain(22L, new BigDecimal("15.25"));
        when(vigilanciaFitosanitariaRepository.findVigilanciaFitosanitariaById(22L)).thenReturn(Optional.of(existing));

        GetVigilanciaFitosanitariaResponseDto response = getVigilanciaFitosanitariaByIdUseCase.execute(22L);

        assertEquals(22L, response.vigilanciaFitosanitariaId);
        assertEquals(new BigDecimal("15.25"), response.ahosp);
    }

    @Test
    void getById_WhenIdIsInvalid_ThrowsIllegalArgumentException() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> getVigilanciaFitosanitariaByIdUseCase.execute(0L)
        );

        assertEquals("El ID de la vigilancia fitosanitaria no es válido", exception.getMessage());
    }

    private CreateVigilanciaFitosanitariaDto buildCreateDto() {
        CreateVigilanciaFitosanitariaDto dto = new CreateVigilanciaFitosanitariaDto();
        dto.systemMonitoringId = 2L;
        dto.identificationKeyId = 3L;
        dto.latitude = new BigDecimal("20.67000000");
        dto.longitude = new BigDecimal("-103.35000000");
        dto.locationId = 4L;
        dto.plagueId = 5L;
        dto.hostId = 6L;
        dto.varietyId = 7L;
        dto.speciesId = 8L;
        dto.ahosp = new BigDecimal("12.50");
        return dto;
    }

    private VigilanciaFitosanitaria buildDomain(Long id, BigDecimal ahosp) {
        VigilanciaFitosanitaria vigilancia = new VigilanciaFitosanitaria();
        vigilancia.setVigilanciaFitosanitariaId(id);
        vigilancia.setSistemaMonitoreo(new SistemaMonitoreo(2L, "monitoreo"));
        vigilancia.setClaveIdentificacionPlaga(new ClaveIdentificacionPlaga(3L, "cid"));
        vigilancia.setLatitude(new BigDecimal("20.67000000"));
        vigilancia.setLongitude(new BigDecimal("-103.35000000"));
        vigilancia.setUbicacion(new Location(4L, null, null, null, null, null));
        vigilancia.setPlaga(new Plaga(5L, "plaga"));
        vigilancia.setHospedante(new Hospedante(6L, "hospedante"));
        vigilancia.setVariedad(new Variedad(7L, "variedad"));
        vigilancia.setEspecie(new Especie(8L, "especie"));
        vigilancia.setAhosp(ahosp);
        return vigilancia;
    }
}