package itesm.mx.application.usecase.vigilancia;

import itesm.mx.application.dto.GetVigilanciaFitosanitariaResponseDto;
import itesm.mx.application.mapper.vigilancia.VigilanciaFitosanitariaDtoMapper;
import itesm.mx.domain.repository.vigilancia.VigilanciaFitosanitariaRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class GetVigilanciaFitosanitariaByIdUseCase {

    @Inject
    VigilanciaFitosanitariaRepository vigilanciaFitosanitariaRepository;

    public GetVigilanciaFitosanitariaResponseDto execute(Long vigilanciaFitosanitariaId) {
        if (vigilanciaFitosanitariaId == null || vigilanciaFitosanitariaId <= 0) {
            throw new IllegalArgumentException("El ID de la vigilancia fitosanitaria no es válido");
        }

        return vigilanciaFitosanitariaRepository.findVigilanciaFitosanitariaById(vigilanciaFitosanitariaId)
                .map(VigilanciaFitosanitariaDtoMapper::toResponseDto)
                .orElseThrow(() -> new IllegalStateException("Vigilancia fitosanitaria no encontrada con id: " + vigilanciaFitosanitariaId));
    }
}