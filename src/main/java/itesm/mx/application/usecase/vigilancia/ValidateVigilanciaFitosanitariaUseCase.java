package itesm.mx.application.usecase.vigilancia;

import itesm.mx.application.dto.GetVigilanciaFitosanitariaResponseDto;
import itesm.mx.application.mapper.vigilancia.VigilanciaFitosanitariaDtoMapper;
import itesm.mx.domain.models.vigilancia.VigilanciaFitosanitaria;
import itesm.mx.domain.repository.vigilancia.VigilanciaFitosanitariaRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import java.time.LocalDateTime;
import java.util.Set;

@ApplicationScoped
public class ValidateVigilanciaFitosanitariaUseCase {

    private static final Set<Long> VALID_VALIDATION_STATUSES = Set.of(1L, 3L); // 1=Accepted, 3=Rejected

    @Inject
    VigilanciaFitosanitariaRepository vigilanciaFitosanitariaRepository;

    @Transactional
    public GetVigilanciaFitosanitariaResponseDto execute(Long vigilanciaFitosanitariaId, Long statusId, Long adminUserId) {
        if (vigilanciaFitosanitariaId == null || vigilanciaFitosanitariaId <= 0) {
            throw new IllegalArgumentException("El ID de la vigilancia fitosanitaria no es válido");
        }
        if (statusId == null || !VALID_VALIDATION_STATUSES.contains(statusId)) {
            throw new IllegalArgumentException("El statusId debe ser 1 (Accepted) o 3 (Rejected)");
        }
        if (adminUserId == null || adminUserId <= 0) {
            throw new IllegalArgumentException("El ID del administrador no es válido");
        }

        vigilanciaFitosanitariaRepository.findVigilanciaFitosanitariaById(vigilanciaFitosanitariaId)
                .orElseThrow(() -> new IllegalStateException("Vigilancia fitosanitaria no encontrada con id: " + vigilanciaFitosanitariaId));

        VigilanciaFitosanitaria vigilanciaToUpdate = new VigilanciaFitosanitaria();
        vigilanciaToUpdate.setVigilanciaFitosanitariaId(vigilanciaFitosanitariaId);
        vigilanciaToUpdate.setStatusId(statusId);
        vigilanciaToUpdate.setValidatedByUserId(adminUserId);
        vigilanciaToUpdate.setValidatedAt(LocalDateTime.now());

        VigilanciaFitosanitaria updated = vigilanciaFitosanitariaRepository.update(vigilanciaToUpdate);
        return VigilanciaFitosanitariaDtoMapper.toResponseDto(updated);
    }
}
