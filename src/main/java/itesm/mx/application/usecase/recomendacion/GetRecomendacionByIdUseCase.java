package itesm.mx.application.usecase.recomendacion;

import itesm.mx.application.dto.GetRecomendacionResponseDto;
import itesm.mx.application.mapper.recomendacion.RecomendacionDtoMapper;
import itesm.mx.domain.repository.recomendacion.RecomendacionRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class GetRecomendacionByIdUseCase {

    @Inject
    RecomendacionRepository recomendacionRepository;

    public GetRecomendacionResponseDto execute(Long recomendacionId) {
        if (recomendacionId == null || recomendacionId <= 0) {
            throw new IllegalArgumentException("El ID de la recomendación no es válido");
        }

        return recomendacionRepository.findRecomendacionById(recomendacionId)
                .map(RecomendacionDtoMapper::toResponseDto)
                .orElseThrow(() -> new IllegalStateException("Recomendación no encontrada con id: " + recomendacionId));
    }
}
