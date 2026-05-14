package itesm.mx.application.usecase.recomendacion;

import itesm.mx.application.dto.GetRecomendacionResponseDto;
import itesm.mx.application.mapper.recomendacion.RecomendacionDtoMapper;
import itesm.mx.domain.repository.recomendacion.RecomendacionRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.List;

@ApplicationScoped
public class GetAllRecomendacionesUseCase {

    @Inject
    RecomendacionRepository recomendacionRepository;

    public List<GetRecomendacionResponseDto> execute() {
        return recomendacionRepository.findAllRecomendaciones().stream()
                .map(RecomendacionDtoMapper::toResponseDto)
                .toList();
    }
}
