package itesm.mx.application.usecase.region;

import itesm.mx.application.dto.CreateRegionInteresDto;
import itesm.mx.application.dto.GetRegionInteresResponseDto;
import itesm.mx.application.mapper.region.RegionInteresDtoMapper;
import itesm.mx.domain.models.location.State;
import itesm.mx.domain.models.region.RegionInteres;
import itesm.mx.domain.repository.location.StateRepository;
import itesm.mx.domain.repository.region.RegionInteresRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.time.LocalDateTime;

/** Adds a state as a region of interest for the authenticated seller (HU-26 CA-01). */
@ApplicationScoped
public class CreateRegionInteresUseCase {

    @Inject
    RegionInteresRepository regionInteresRepository;

    @Inject
    StateRepository stateRepository;

    public GetRegionInteresResponseDto execute(Long userId, CreateRegionInteresDto dto) {
        if (userId == null || userId <= 0) {
            throw new IllegalArgumentException("El ID de usuario no es válido");
        }
        if (dto == null || dto.stateId == null) {
            throw new IllegalArgumentException("El id del estado es requerido");
        }

        State state = stateRepository.findStateById(dto.stateId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "El estado indicado no existe: " + dto.stateId));

        if (regionInteresRepository.existsByUserIdAndStateId(userId, dto.stateId)) {
            throw new IllegalStateException("El estado ya está configurado como región de interés");
        }

        RegionInteres regionInteres = new RegionInteres();
        regionInteres.setUserId(userId);
        regionInteres.setStateId(state.getStateId());
        regionInteres.setCreatedAt(LocalDateTime.now());

        RegionInteres saved = regionInteresRepository.save(regionInteres);
        return RegionInteresDtoMapper.toResponseDto(saved);
    }
}
