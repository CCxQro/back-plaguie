package itesm.mx.application.usecase.region;

import itesm.mx.application.dto.GetAlertaResponseDto;
import itesm.mx.application.mapper.alerta.AlertaDtoMapper;
import itesm.mx.domain.models.region.RegionInteres;
import itesm.mx.domain.repository.alerta.AlertaRepository;
import itesm.mx.domain.repository.region.RegionInteresRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.List;

/**
 * Returns the early alerts (validated pest alerts) located in the states the
 * seller has configured as regions of interest (HU-26 CA-02).
 *
 * If the seller has no regions configured, returns an empty list (the UI guides
 * them to configure at least one region).
 */
@ApplicationScoped
public class GetAlertasByRegionesInteresUseCase {

    @Inject
    RegionInteresRepository regionInteresRepository;

    @Inject
    AlertaRepository alertaRepository;

    public List<GetAlertaResponseDto> execute(Long userId) {
        if (userId == null || userId <= 0) {
            throw new IllegalArgumentException("El ID de usuario no es válido");
        }

        List<Long> stateIds = regionInteresRepository.findByUserId(userId)
                .stream()
                .map(RegionInteres::getStateId)
                .distinct()
                .toList();

        if (stateIds.isEmpty()) {
            return List.of();
        }

        return alertaRepository.findValidatedByStateIds(stateIds)
                .stream()
                .map(AlertaDtoMapper::toResponseDto)
                .toList();
    }
}
