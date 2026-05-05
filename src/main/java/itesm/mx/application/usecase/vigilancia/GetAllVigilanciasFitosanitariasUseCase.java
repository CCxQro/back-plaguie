package itesm.mx.application.usecase.vigilancia;

import itesm.mx.application.dto.GetVigilanciaFitosanitariaResponseDto;
import itesm.mx.application.mapper.vigilancia.VigilanciaFitosanitariaDtoMapper;
import itesm.mx.domain.repository.vigilancia.VigilanciaFitosanitariaRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.List;

@ApplicationScoped
public class GetAllVigilanciasFitosanitariasUseCase {

    @Inject
    VigilanciaFitosanitariaRepository vigilanciaFitosanitariaRepository;

    public List<GetVigilanciaFitosanitariaResponseDto> execute() {
        return vigilanciaFitosanitariaRepository.findAllVigilanciasFitosanitarias().stream()
                .map(VigilanciaFitosanitariaDtoMapper::toResponseDto)
                .toList();
    }
}