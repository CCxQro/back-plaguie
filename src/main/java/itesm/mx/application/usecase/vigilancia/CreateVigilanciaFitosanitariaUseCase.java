package itesm.mx.application.usecase.vigilancia;

import itesm.mx.application.dto.CreateVigilanciaFitosanitariaDto;
import itesm.mx.application.dto.GetVigilanciaFitosanitariaResponseDto;
import itesm.mx.application.mapper.vigilancia.VigilanciaFitosanitariaDtoMapper;
import itesm.mx.domain.models.vigilancia.VigilanciaFitosanitaria;
import itesm.mx.domain.repository.vigilancia.VigilanciaFitosanitariaRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import java.math.BigDecimal;

@ApplicationScoped
public class CreateVigilanciaFitosanitariaUseCase {

    @Inject
    VigilanciaFitosanitariaRepository vigilanciaFitosanitariaRepository;

    @Transactional
    public GetVigilanciaFitosanitariaResponseDto execute(CreateVigilanciaFitosanitariaDto dto) {
        validate(dto);

        VigilanciaFitosanitaria created = vigilanciaFitosanitariaRepository.save(VigilanciaFitosanitariaDtoMapper.toDomain(dto));
        return VigilanciaFitosanitariaDtoMapper.toResponseDto(created);
    }

    private void validate(CreateVigilanciaFitosanitariaDto dto) {
        if (dto == null) {
            throw new IllegalArgumentException("El cuerpo de la solicitud es requerido");
        }

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

    private void validatePositiveId(Long value, String message) {
        if (value != null && value <= 0) {
            throw new IllegalArgumentException(message);
        }
    }

    private void validateCoordinates(BigDecimal latitude, BigDecimal longitude) {
        if (latitude == null) {
            throw new IllegalArgumentException("La latitud es requerida");
        }
        if (longitude == null) {
            throw new IllegalArgumentException("La longitud es requerida");
        }

        double latitudeValue = latitude.doubleValue();
        double longitudeValue = longitude.doubleValue();

        if (latitudeValue < -90 || latitudeValue > 90) {
            throw new IllegalArgumentException("La latitud no es valida");
        }
        if (longitudeValue < -180 || longitudeValue > 180) {
            throw new IllegalArgumentException("La longitud no es valida");
        }
    }

    private void validateAhosp(BigDecimal ahosp) {
        if (ahosp != null && ahosp.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("El ahosp no puede ser negativo");
        }
    }
}