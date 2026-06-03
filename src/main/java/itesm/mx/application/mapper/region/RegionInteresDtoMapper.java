package itesm.mx.application.mapper.region;

import itesm.mx.application.dto.GetRegionInteresResponseDto;
import itesm.mx.domain.models.region.RegionInteres;

import java.time.LocalDateTime;

public final class RegionInteresDtoMapper {

    private RegionInteresDtoMapper() {
    }

    public static GetRegionInteresResponseDto toResponseDto(RegionInteres domain) {
        LocalDateTime createdAt = domain.getCreatedAt();
        return new GetRegionInteresResponseDto(
                domain.getRegionInteresId(),
                domain.getStateId(),
                domain.getStateName(),
                createdAt != null ? createdAt.toString() : null
        );
    }
}
