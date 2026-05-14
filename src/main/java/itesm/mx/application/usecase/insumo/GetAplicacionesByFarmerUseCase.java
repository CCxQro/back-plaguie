package itesm.mx.application.usecase.insumo;

import itesm.mx.application.dto.AplicacionInsumoResponseDto;
import itesm.mx.application.mapper.insumo.AplicacionInsumoAppMapper;
import itesm.mx.domain.repository.insumo.AplicacionInsumoRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.List;

@ApplicationScoped
public class GetAplicacionesByFarmerUseCase {

    @Inject
    AplicacionInsumoRepository aplicacionInsumoRepository;

    public List<AplicacionInsumoResponseDto> execute(Long agricultorId) {
        if (agricultorId == null || agricultorId <= 0) {
            throw new IllegalArgumentException("El identificador del agricultor debe ser positivo");
        }
        return aplicacionInsumoRepository.findByFarmerId(agricultorId)
                .stream()
                .map(AplicacionInsumoAppMapper::toResponseDto)
                .toList();
    }
}
