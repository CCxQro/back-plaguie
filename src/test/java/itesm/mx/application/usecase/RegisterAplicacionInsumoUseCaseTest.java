package itesm.mx.application.usecase;

import itesm.mx.application.dto.AplicacionInsumoResponseDto;
import itesm.mx.application.dto.RegisterAplicacionInsumoDto;
import itesm.mx.application.usecase.insumo.RegisterAplicacionInsumoUseCase;
import itesm.mx.domain.models.insumo.AplicacionInsumo;
import itesm.mx.domain.models.marketplace.Product;
import itesm.mx.domain.models.parcela.Parcela;
import itesm.mx.domain.models.user.Farmer;
import itesm.mx.domain.repository.insumo.AplicacionInsumoRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RegisterAplicacionInsumoUseCaseTest {

    @Mock
    AplicacionInsumoRepository aplicacionInsumoRepository;

    @InjectMocks
    RegisterAplicacionInsumoUseCase registerAplicacionInsumoUseCase;

    private RegisterAplicacionInsumoDto validDto() {
        RegisterAplicacionInsumoDto dto = new RegisterAplicacionInsumoDto();
        dto.fecha = LocalDate.of(2025, 5, 15);
        dto.skuIdVendedor = 1001L;
        dto.cantidad = 5.0;
        dto.parcelaId = 1L;
        return dto;
    }

    private AplicacionInsumo savedDomain(Long agricultorId) {
        Farmer agricultor = new Farmer();
        agricultor.setFarmerId(agricultorId);

        Product producto = new Product();
        producto.setSkuSellerId(1001L);
        producto.setName("Fertilizante");

        Parcela parcela = new Parcela();
        parcela.setParcelaId(1L);
        parcela.setNombreParcela("Parcela Norte");

        return new AplicacionInsumo(1L, LocalDate.of(2025, 5, 15), agricultor, producto, 5.0, parcela);
    }

    @Test
    void register_WhenStockIsSufficient_ReturnsSavedResponse() {
        Long agricultorId = 1L;
        RegisterAplicacionInsumoDto dto = validDto();

        when(aplicacionInsumoRepository.calcularStockDisponible(agricultorId, dto.skuIdVendedor))
                .thenReturn(10.0);
        when(aplicacionInsumoRepository.save(any())).thenReturn(savedDomain(agricultorId));

        AplicacionInsumoResponseDto result = registerAplicacionInsumoUseCase.execute(dto, agricultorId);

        assertNotNull(result);
        assertEquals(1L, result.aplicacionId);
        assertEquals(1001L, result.skuIdVendedor);
        assertEquals(5.0, result.cantidad);
        assertEquals(1L, result.parcelaId);
        assertEquals(agricultorId, result.agricultorId);
        verify(aplicacionInsumoRepository).save(any());
    }

    @Test
    void register_WhenCantidadExceedsStock_ThrowsIllegalArgumentException() {
        Long agricultorId = 1L;
        RegisterAplicacionInsumoDto dto = validDto();
        dto.cantidad = 5.0;

        when(aplicacionInsumoRepository.calcularStockDisponible(agricultorId, dto.skuIdVendedor))
                .thenReturn(2.0);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> registerAplicacionInsumoUseCase.execute(dto, agricultorId));

        assertTrue(ex.getMessage().contains("stock disponible"));
        verifyNoMoreInteractions(aplicacionInsumoRepository);
    }

    @Test
    void register_WhenDtoIsNull_ThrowsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class,
                () -> registerAplicacionInsumoUseCase.execute(null, 1L));
        verifyNoInteractions(aplicacionInsumoRepository);
    }
}
