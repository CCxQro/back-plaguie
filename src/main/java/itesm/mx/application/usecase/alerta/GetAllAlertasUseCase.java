package itesm.mx.application.usecase.alerta;

import itesm.mx.application.dto.GetAlertaResponseDto;
import itesm.mx.application.mapper.alerta.AlertaDtoMapper;
import itesm.mx.domain.repository.alerta.AlertaRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.List;

@ApplicationScoped
public class GetAllAlertasUseCase {

    @Inject
    AlertaRepository alertaRepository;

    public List<GetAlertaResponseDto> execute() {
        return alertaRepository.findAllAlertas().stream()
                .map(AlertaDtoMapper::toResponseDto)
                .toList();
    }
}
