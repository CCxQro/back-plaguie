package itesm.mx.application.usecase.parcela;

import itesm.mx.application.dto.ParcelaResponseDto;
import itesm.mx.application.mapper.parcela.ParcelaAppMapper;
import itesm.mx.domain.repository.parcela.ParcelaRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.List;

@ApplicationScoped
public class GetParcelasByFarmerUseCase {

    @Inject
    ParcelaRepository parcelaRepository;

    public List<ParcelaResponseDto> execute(Long farmerId) {
        return parcelaRepository.findByFarmerId(farmerId)
                .stream()
                .map(ParcelaAppMapper::toResponseDto)
                .toList();
    }
}
