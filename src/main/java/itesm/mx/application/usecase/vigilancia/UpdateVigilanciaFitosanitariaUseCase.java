package itesm.mx.application.usecase.vigilancia;

import itesm.mx.application.dto.GetVigilanciaFitosanitariaResponseDto;
import itesm.mx.application.dto.UpdateVigilanciaFitosanitariaDto;
import itesm.mx.application.mapper.vigilancia.VigilanciaFitosanitariaDtoMapper;
import itesm.mx.domain.models.vigilancia.VigilanciaFitosanitaria;
import itesm.mx.domain.repository.vigilancia.VigilanciaFitosanitariaRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import java.math.BigDecimal;

@ApplicationScoped
public class UpdateVigilanciaFitosanitariaUseCase {

    @Inject
    VigilanciaFitosanitariaRepository vigilanciaFitosanitariaRepository;

    @Transactional
    public GetVigilanciaFitosanitariaResponseDto execute(Long vigilanciaFitosanitariaId, UpdateVigilanciaFitosanitariaDto dto) {
        if (vigilanciaFitosanitariaId == null || vigilanciaFitosanitariaId <= 0) {
            throw new IllegalArgumentException("El ID de la vigilancia fitosanitaria no es válido");
        }
        if (dto == null) {
            throw new IllegalArgumentException("El cuerpo de la solicitud es requerido");
        }
        if (isUpdateEmpty(dto)) {
            throw new IllegalArgumentException("Debe proporcionar al menos un campo para actualizar");
        }

        vigilanciaFitosanitariaRepository.findVigilanciaFitosanitariaById(vigilanciaFitosanitariaId)
                .orElseThrow(() -> new IllegalStateException("Vigilancia fitosanitaria no encontrada con id: " + vigilanciaFitosanitariaId));

        validate(dto);

        VigilanciaFitosanitaria vigilanciaToUpdate = VigilanciaFitosanitariaDtoMapper.toDomain(dto);
        vigilanciaToUpdate.setVigilanciaFitosanitariaId(vigilanciaFitosanitariaId);

        VigilanciaFitosanitaria updated = vigilanciaFitosanitariaRepository.update(vigilanciaToUpdate);
        return VigilanciaFitosanitariaDtoMapper.toResponseDto(updated);
    }

    private void validate(UpdateVigilanciaFitosanitariaDto dto) {
        validatePositiveId(dto.systemMonitoringId, "El id del sistema de monitoreo no es válido");
        validatePositiveId(dto.identificationKeyId, "El id de la clave de identificación de plaga no es válido");
        validatePositiveId(dto.locationId, "El id de la ubicación no es válido");
        validatePositiveId(dto.plagueId, "El id de la plaga no es válido");
        validatePositiveId(dto.hostId, "El id del hospedante no es válido");
        validatePositiveId(dto.varietyId, "El id de la variedad no es válido");
        validatePositiveId(dto.speciesId, "El id de la especie no es válido");

        validateCoordinates(dto.latitude, dto.longitude);
        validateAhosp(dto.ahosp);
    }

    private boolean isUpdateEmpty(UpdateVigilanciaFitosanitariaDto dto) {
        return dto.systemMonitoringId == null
                && dto.identificationKeyId == null
                && dto.latitude == null
                && dto.longitude == null
                && dto.locationId == null
                && dto.plagueId == null
                && dto.hostId == null
                && dto.varietyId == null
                && dto.speciesId == null
                && dto.ahosp == null;
    }

    private void validatePositiveId(Long value, String message) {
        if (value != null && value <= 0) {
            throw new IllegalArgumentException(message);
        }
    }

    private void validateCoordinates(BigDecimal latitude, BigDecimal longitude) {
        if (latitude != null) {
            double latitudeValue = latitude.doubleValue();
            if (latitudeValue < -90 || latitudeValue > 90) {
                throw new IllegalArgumentException("La latitud no es valida");
            }
        }

        if (longitude != null) {
            double longitudeValue = longitude.doubleValue();
            if (longitudeValue < -180 || longitudeValue > 180) {
                throw new IllegalArgumentException("La longitud no es valida");
            }
        }
    }

    private void validateAhosp(BigDecimal ahosp) {
        if (ahosp != null && ahosp.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("El ahosp no puede ser negativo");
        }
    }
}