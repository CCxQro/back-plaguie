package itesm.mx.application.usecase.insumo;

import itesm.mx.application.dto.AplicacionInsumoResponseDto;
import itesm.mx.application.dto.RegisterAplicacionInsumoDto;
import itesm.mx.application.mapper.insumo.AplicacionInsumoAppMapper;
import itesm.mx.domain.models.insumo.AplicacionInsumo;
import itesm.mx.domain.repository.insumo.AplicacionInsumoRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

@ApplicationScoped
public class RegisterAplicacionInsumoUseCase {

    @Inject
    AplicacionInsumoRepository aplicacionInsumoRepository;

    @Transactional
    public AplicacionInsumoResponseDto execute(RegisterAplicacionInsumoDto dto, Long agricultorId) {
        if (dto == null) {
            throw new IllegalArgumentException("El cuerpo de la solicitud es requerido");
        }
        if (dto.fecha == null) {
            throw new IllegalArgumentException("La fecha es requerida");
        }
        if (dto.skuIdVendedor == null) {
            throw new IllegalArgumentException("El identificador del producto es requerido");
        }
        if (dto.cantidad == null) {
            throw new IllegalArgumentException("La cantidad es requerida");
        }
        if (dto.parcelaId == null) {
            throw new IllegalArgumentException("El identificador de la parcela es requerido");
        }

        Double stockDisponible = aplicacionInsumoRepository.calcularStockDisponible(agricultorId, dto.skuIdVendedor);
        if (dto.cantidad > stockDisponible) {
            throw new IllegalArgumentException(
                    "La cantidad supera el stock disponible. Stock actual: " + stockDisponible);
        }

        AplicacionInsumo domain = AplicacionInsumoAppMapper.toDomain(dto, agricultorId);
        AplicacionInsumo saved = aplicacionInsumoRepository.save(domain);
        return AplicacionInsumoAppMapper.toResponseDto(saved);
    }
}
