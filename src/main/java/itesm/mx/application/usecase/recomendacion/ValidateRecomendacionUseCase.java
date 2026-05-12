package itesm.mx.application.usecase.recomendacion;

import itesm.mx.application.dto.GetRecomendacionResponseDto;
import itesm.mx.application.mapper.recomendacion.RecomendacionDtoMapper;
import itesm.mx.domain.models.recomendacion.Recomendacion;
import itesm.mx.domain.repository.recomendacion.RecomendacionRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import java.time.LocalDateTime;
import java.util.Set;

@ApplicationScoped
public class ValidateRecomendacionUseCase {

    private static final Set<Long> VALID_VALIDATION_STATUSES = Set.of(1L, 3L); // 1=Accepted, 3=Rejected

    @Inject
    RecomendacionRepository recomendacionRepository;

    @Transactional
    public GetRecomendacionResponseDto execute(Long recomendacionId, Long statusId, Long adminUserId) {
        if (recomendacionId == null || recomendacionId <= 0) {
            throw new IllegalArgumentException("El ID de la recomendación no es válido");
        }
        if (statusId == null || !VALID_VALIDATION_STATUSES.contains(statusId)) {
            throw new IllegalArgumentException("El statusId debe ser 1 (Accepted) o 3 (Rejected)");
        }
        if (adminUserId == null || adminUserId <= 0) {
            throw new IllegalArgumentException("El ID del administrador no es válido");
        }

        recomendacionRepository.findRecomendacionById(recomendacionId)
                .orElseThrow(() -> new IllegalStateException("Recomendación no encontrada con id: " + recomendacionId));

        Recomendacion recomendacionToUpdate = new Recomendacion();
        recomendacionToUpdate.setRecomendacionId(recomendacionId);
        recomendacionToUpdate.setStatusId(statusId);
        recomendacionToUpdate.setValidatedByUserId(adminUserId);
        recomendacionToUpdate.setValidatedAt(LocalDateTime.now());

        Recomendacion updated = recomendacionRepository.update(recomendacionToUpdate);
        return RecomendacionDtoMapper.toResponseDto(updated);
    }
}
