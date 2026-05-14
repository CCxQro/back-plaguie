package itesm.mx.application.usecase.alerta;

import itesm.mx.application.dto.GetAlertaResponseDto;
import itesm.mx.application.mapper.alerta.AlertaDtoMapper;
import itesm.mx.domain.models.alerta.Alerta;
import itesm.mx.domain.repository.alerta.AlertaRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import java.time.LocalDateTime;
import java.util.Set;

@ApplicationScoped
public class ValidateAlertaUseCase {

    private static final Set<Long> VALID_VALIDATION_STATUSES = Set.of(1L, 3L); // 1=Accepted, 3=Rejected

    @Inject
    AlertaRepository alertaRepository;

    @Transactional
    public GetAlertaResponseDto execute(Long alertaId, Long statusId, Long adminUserId) {
        if (alertaId == null || alertaId <= 0) {
            throw new IllegalArgumentException("El ID de la alerta no es válido");
        }
        if (statusId == null || !VALID_VALIDATION_STATUSES.contains(statusId)) {
            throw new IllegalArgumentException("El statusId debe ser 1 (Accepted) o 3 (Rejected)");
        }
        if (adminUserId == null || adminUserId <= 0) {
            throw new IllegalArgumentException("El ID del administrador no es válido");
        }

        alertaRepository.findAlertaById(alertaId)
                .orElseThrow(() -> new IllegalStateException("Alerta no encontrada con id: " + alertaId));

        Alerta alertaToUpdate = new Alerta();
        alertaToUpdate.setAlertaId(alertaId);
        alertaToUpdate.setStatusId(statusId);
        alertaToUpdate.setValidatedByUserId(adminUserId);
        alertaToUpdate.setValidatedAt(LocalDateTime.now());

        Alerta updated = alertaRepository.update(alertaToUpdate);
        return AlertaDtoMapper.toResponseDto(updated);
    }
}
