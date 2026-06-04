package itesm.mx.application.usecase.parcela;

import itesm.mx.application.dto.GetLocationResponseDto;
import itesm.mx.application.dto.ParcelaResponseDto;
import itesm.mx.application.dto.RegisterLocationDto;
import itesm.mx.application.dto.RegisterParcelaDto;
import itesm.mx.application.usecase.location.location.RegisterLocationUseCase;
import itesm.mx.domain.models.parcela.EstadoParcela;
import itesm.mx.domain.models.parcela.Parcela;
import itesm.mx.domain.models.parcela.SistemaRiego;
import itesm.mx.domain.models.parcela.TipoCultivo;
import itesm.mx.domain.models.user.Farmer;
import itesm.mx.domain.repository.parcela.ParcelaCatalogRepository;
import itesm.mx.domain.repository.parcela.ParcelaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RegisterParcelaUseCaseTest {

    @Mock
    RegisterLocationUseCase registerLocationUseCase;

    @Mock
    ParcelaRepository parcelaRepository;

    @Mock
    ParcelaCatalogRepository parcelaCatalogRepository;

    @InjectMocks
    RegisterParcelaUseCase useCase;

    private RegisterParcelaDto buildDto() {
        RegisterParcelaDto dto = new RegisterParcelaDto();
        dto.nombreParcela = "Lote Norte";
        dto.tamanoHectareas = 5.0;
        dto.phSuelo = 6.5;
        dto.fechaSiembra = LocalDate.of(2024, 3, 1);
        dto.fechaCosecha = LocalDate.of(2024, 8, 1);
        dto.estadoParcelaId = 1L;
        dto.tipoCultivoId = 2L;
        dto.sistemaRiegoId = 3L;

        RegisterLocationDto loc = new RegisterLocationDto();
        loc.latitude = 20.67;
        loc.longitude = -103.35;
        loc.stateName = "Jalisco";
        loc.municipalityName = "Guadalajara";
        dto.ubicacion = loc;
        return dto;
    }

    private Parcela buildSavedParcela(RegisterParcelaDto dto) {
        Farmer farmer = new Farmer();
        farmer.setFarmerId(10L);

        EstadoParcela estado = new EstadoParcela(dto.estadoParcelaId, "Activo");
        TipoCultivo cultivo = new TipoCultivo(dto.tipoCultivoId, "Maíz", null, null);
        SistemaRiego riego = new SistemaRiego(dto.sistemaRiegoId, "Goteo");

        Parcela p = new Parcela();
        p.setParcelaId(100L);
        p.setNombreParcela(dto.nombreParcela);
        p.setTamanoHectareas(dto.tamanoHectareas);
        p.setFarmer(farmer);
        p.setEstadoParcela(estado);
        p.setTipoCultivo(cultivo);
        p.setSistemaRiego(riego);
        p.setIsActive(true);
        return p;
    }

    @Test
    void execute_HappyPath_ReturnsSavedDto() {
        RegisterParcelaDto dto = buildDto();

        when(parcelaCatalogRepository.estadoParcelaExists(1L)).thenReturn(true);
        when(parcelaCatalogRepository.tipoCultivoExists(2L)).thenReturn(true);
        when(parcelaCatalogRepository.sistemaRiegoExists(3L)).thenReturn(true);

        GetLocationResponseDto locationResponse = new GetLocationResponseDto(
                50L, 20.67, -103.35, null, "Jalisco", null, "Guadalajara", null, null, null, null);
        when(registerLocationUseCase.execute(any())).thenReturn(locationResponse);
        when(parcelaRepository.save(any())).thenReturn(buildSavedParcela(dto));

        ParcelaResponseDto result = useCase.execute(10L, dto);

        assertNotNull(result);
        assertEquals(100L, result.parcelaId);
        assertEquals("Lote Norte", result.nombre);
        assertEquals("Maíz", result.tipoCultivo);
        assertEquals("Activo", result.estadoParcela);
        assertTrue(result.isActive);
    }

    @Test
    void execute_InvalidEstadoParcela_ThrowsIllegalArgument() {
        RegisterParcelaDto dto = buildDto();

        when(parcelaCatalogRepository.estadoParcelaExists(1L)).thenReturn(false);

        assertThrows(IllegalArgumentException.class, () -> useCase.execute(10L, dto));
    }

    @Test
    void execute_InvalidTipoCultivo_ThrowsIllegalArgument() {
        RegisterParcelaDto dto = buildDto();

        when(parcelaCatalogRepository.estadoParcelaExists(1L)).thenReturn(true);
        when(parcelaCatalogRepository.tipoCultivoExists(2L)).thenReturn(false);

        assertThrows(IllegalArgumentException.class, () -> useCase.execute(10L, dto));
    }

    @Test
    void execute_InvalidSistemaRiego_ThrowsIllegalArgument() {
        RegisterParcelaDto dto = buildDto();

        when(parcelaCatalogRepository.estadoParcelaExists(1L)).thenReturn(true);
        when(parcelaCatalogRepository.tipoCultivoExists(2L)).thenReturn(true);
        when(parcelaCatalogRepository.sistemaRiegoExists(3L)).thenReturn(false);

        assertThrows(IllegalArgumentException.class, () -> useCase.execute(10L, dto));
    }
}
