package itesm.mx.application.usecase.reporte;

import itesm.mx.application.dto.GetPestMapPointDto;
import itesm.mx.application.mapper.reporte.PestMapDtoMapper;
import itesm.mx.domain.repository.reporte.PestMapRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.List;

/**
 * Returns the validated surveillance observations for the interactive pest map
 * (HU-27). Filtering (pest, date, region) and zone aggregation are applied
 * client-side over these points.
 */
@ApplicationScoped
public class GetPestMapUseCase {

    @Inject
    PestMapRepository pestMapRepository;

    public List<GetPestMapPointDto> execute() {
        return pestMapRepository.findValidatedMapPoints()
                .stream()
                .map(PestMapDtoMapper::toResponseDto)
                .toList();
    }
}
