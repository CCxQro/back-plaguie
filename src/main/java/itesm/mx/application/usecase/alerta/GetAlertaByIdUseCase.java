package itesm.mx.application.usecase.alerta;

import itesm.mx.application.dto.GetAlertaResponseDto;
import itesm.mx.application.mapper.alerta.AlertaDtoMapper;
import itesm.mx.domain.repository.alerta.AlertaRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class GetAlertaByIdUseCase {

    @Inject
    AlertaRepository alertaRepository;

    public GetAlertaResponseDto execute(Long alertaId) {
        if (alertaId == null || alertaId <= 0) {
            throw new IllegalArgumentException("El ID de la alerta no es válido");
        }

        return alertaRepository.findAlertaById(alertaId)
                .map(AlertaDtoMapper::toResponseDto)
                .orElseThrow(() -> new IllegalStateException("Alerta no encontrada con id: " + alertaId));
    }
}
