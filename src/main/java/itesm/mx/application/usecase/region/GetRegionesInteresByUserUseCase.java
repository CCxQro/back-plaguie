package itesm.mx.application.usecase.region;

import itesm.mx.application.dto.GetRegionInteresResponseDto;
import itesm.mx.application.mapper.region.RegionInteresDtoMapper;
import itesm.mx.domain.repository.region.RegionInteresRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.List;

/** Lists the regions of interest configured by a seller (HU-26 CA-01). */
@ApplicationScoped
public class GetRegionesInteresByUserUseCase {

    @Inject
    RegionInteresRepository regionInteresRepository;

    public List<GetRegionInteresResponseDto> execute(Long userId) {
        if (userId == null || userId <= 0) {
            throw new IllegalArgumentException("El ID de usuario no es válido");
        }
        return regionInteresRepository.findByUserId(userId)
                .stream()
                .map(RegionInteresDtoMapper::toResponseDto)
                .toList();
    }
}
