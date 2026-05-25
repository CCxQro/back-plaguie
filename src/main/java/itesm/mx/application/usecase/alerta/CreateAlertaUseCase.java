package itesm.mx.application.usecase.alerta;

import itesm.mx.application.dto.CreateAlertaDto;
import itesm.mx.application.dto.GetAlertaResponseDto;
import itesm.mx.application.mapper.alerta.AlertaDtoMapper;
import itesm.mx.domain.models.alerta.Alerta;
import itesm.mx.domain.repository.alerta.AlertaRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import java.util.Set;

@ApplicationScoped
public class CreateAlertaUseCase {

    private static final Set<String> VALID_SEVERIDADES = Set.of("critico", "advertencia", "informacion");

    @Inject
    AlertaRepository alertaRepository;

    @Transactional
    public GetAlertaResponseDto execute(CreateAlertaDto dto, Long reportedByUserId) {
        validate(dto);

        Alerta alerta = AlertaDtoMapper.toDomain(dto, reportedByUserId);
        Alerta created = alertaRepository.save(alerta);
        return AlertaDtoMapper.toResponseDto(created);
    }

    private void validate(CreateAlertaDto dto) {
        if (dto == null) {
            throw new IllegalArgumentException("El cuerpo de la solicitud es requerido");
        }
        if (dto.titulo == null || dto.titulo.isBlank()) {
            throw new IllegalArgumentException("El título es requerido");
        }
        if (dto.ubicacionId == null || dto.ubicacionId <= 0) {
            throw new IllegalArgumentException("El id de la ubicación no es válido");
        }
        if (dto.tipoPlaga == null || dto.tipoPlaga.isBlank()) {
            throw new IllegalArgumentException("El tipo de plaga es requerido");
        }
        if (dto.severidad == null || !VALID_SEVERIDADES.contains(dto.severidad)) {
            throw new IllegalArgumentException("La severidad debe ser: critico, advertencia o informacion");
        }
        if (dto.hectareas != null && dto.hectareas.signum() < 0) {
            throw new IllegalArgumentException("Las hectáreas no pueden ser negativas");
        }
    }
}
