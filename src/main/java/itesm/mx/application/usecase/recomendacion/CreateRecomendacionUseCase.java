package itesm.mx.application.usecase.recomendacion;

import itesm.mx.application.dto.CreateRecomendacionDto;
import itesm.mx.application.dto.GetRecomendacionResponseDto;
import itesm.mx.application.mapper.recomendacion.RecomendacionDtoMapper;
import itesm.mx.domain.models.recomendacion.Recomendacion;
import itesm.mx.domain.repository.recomendacion.RecomendacionRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

@ApplicationScoped
public class CreateRecomendacionUseCase {

    @Inject
    RecomendacionRepository recomendacionRepository;

    @Transactional
    public GetRecomendacionResponseDto execute(CreateRecomendacionDto dto, Long reportedByUserId) {
        validate(dto);

        Recomendacion recomendacion = RecomendacionDtoMapper.toDomain(dto, reportedByUserId);
        Recomendacion created = recomendacionRepository.save(recomendacion);
        return RecomendacionDtoMapper.toResponseDto(created);
    }

    private void validate(CreateRecomendacionDto dto) {
        if (dto == null) {
            throw new IllegalArgumentException("El cuerpo de la solicitud es requerido");
        }
        if (dto.titulo == null || dto.titulo.isBlank()) {
            throw new IllegalArgumentException("El título es requerido");
        }
        if (dto.tipoPlaga == null || dto.tipoPlaga.isBlank()) {
            throw new IllegalArgumentException("El tipo de plaga es requerido");
        }
        if (dto.productosRecomendados == null || dto.productosRecomendados.isBlank()) {
            throw new IllegalArgumentException("Los productos recomendados son requeridos");
        }
    }
}
