package itesm.mx.application.mapper.vigilancia;

import itesm.mx.application.dto.CreateVigilanciaFitosanitariaDto;
import itesm.mx.application.dto.GetVigilanciaFitosanitariaResponseDto;
import itesm.mx.application.dto.UpdateVigilanciaFitosanitariaDto;
import itesm.mx.domain.models.location.Location;
import itesm.mx.domain.models.vigilancia.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public final class VigilanciaFitosanitariaDtoMapper {

    private VigilanciaFitosanitariaDtoMapper() {
    }

    public static VigilanciaFitosanitaria toDomain(CreateVigilanciaFitosanitariaDto dto) {
        VigilanciaFitosanitaria v = new VigilanciaFitosanitaria(
                null,
                toSistemaMonitoreo(dto.systemMonitoringId),
                toClaveIdentificacionPlaga(dto.identificationKeyId),
                dto.latitude,
                dto.longitude,
                toLocation(dto.locationId),
                toPlaga(dto.plagueId),
                toHospedante(dto.hostId),
                toVariedad(dto.varietyId),
                toEspecie(dto.speciesId),
                dto.ahosp != null ? dto.ahosp : BigDecimal.ZERO
        );
        v.setStatusId(2L); // Default: Revision (pending validation)
        return v;
    }

    public static VigilanciaFitosanitaria toDomain(UpdateVigilanciaFitosanitariaDto dto) {
        VigilanciaFitosanitaria vigilanciaFitosanitaria = new VigilanciaFitosanitaria();
        vigilanciaFitosanitaria.setSistemaMonitoreo(toSistemaMonitoreo(dto.systemMonitoringId));
        vigilanciaFitosanitaria.setClaveIdentificacionPlaga(toClaveIdentificacionPlaga(dto.identificationKeyId));
        vigilanciaFitosanitaria.setLatitude(dto.latitude);
        vigilanciaFitosanitaria.setLongitude(dto.longitude);
        vigilanciaFitosanitaria.setUbicacion(toLocation(dto.locationId));
        vigilanciaFitosanitaria.setPlaga(toPlaga(dto.plagueId));
        vigilanciaFitosanitaria.setHospedante(toHospedante(dto.hostId));
        vigilanciaFitosanitaria.setVariedad(toVariedad(dto.varietyId));
        vigilanciaFitosanitaria.setEspecie(toEspecie(dto.speciesId));
        vigilanciaFitosanitaria.setAhosp(dto.ahosp);
        return vigilanciaFitosanitaria;
    }

    public static GetVigilanciaFitosanitariaResponseDto toResponseDto(VigilanciaFitosanitaria vigilanciaFitosanitaria) {
        LocalDateTime validatedAt = vigilanciaFitosanitaria.getValidatedAt();
        return new GetVigilanciaFitosanitariaResponseDto(
                vigilanciaFitosanitaria.getVigilanciaFitosanitariaId(),
                getSistemaMonitoreoId(vigilanciaFitosanitaria.getSistemaMonitoreo()),
                getSistemaMonitoreoName(vigilanciaFitosanitaria.getSistemaMonitoreo()),
                getClaveIdentificacionPlagaId(vigilanciaFitosanitaria.getClaveIdentificacionPlaga()),
                getClaveIdentificacionPlagaName(vigilanciaFitosanitaria.getClaveIdentificacionPlaga()),
                vigilanciaFitosanitaria.getLatitude(),
                vigilanciaFitosanitaria.getLongitude(),
                getLocationId(vigilanciaFitosanitaria.getUbicacion()),
                getPlagaId(vigilanciaFitosanitaria.getPlaga()),
                getPlagaName(vigilanciaFitosanitaria.getPlaga()),
                getHospedanteId(vigilanciaFitosanitaria.getHospedante()),
                getHospedanteName(vigilanciaFitosanitaria.getHospedante()),
                getVariedadId(vigilanciaFitosanitaria.getVariedad()),
                getVariedadName(vigilanciaFitosanitaria.getVariedad()),
                getEspecieId(vigilanciaFitosanitaria.getEspecie()),
                getEspecieName(vigilanciaFitosanitaria.getEspecie()),
                vigilanciaFitosanitaria.getAhosp(),
                vigilanciaFitosanitaria.getStatusId(),
                vigilanciaFitosanitaria.getStatusName(),
                vigilanciaFitosanitaria.getValidatedByUserId(),
                validatedAt != null ? validatedAt.toString() : null
        );
    }

    private static SistemaMonitoreo toSistemaMonitoreo(Long sistemaMonitoreoId) {
        return sistemaMonitoreoId != null ? new SistemaMonitoreo(sistemaMonitoreoId, null) : null;
    }

    private static ClaveIdentificacionPlaga toClaveIdentificacionPlaga(Long cidId) {
        return cidId != null ? new ClaveIdentificacionPlaga(cidId, null) : null;
    }

    private static Location toLocation(Long locationId) {
        return locationId != null ? new Location(locationId, null, null, null, null, null) : null;
    }

    private static Plaga toPlaga(Long plagaId) {
        return plagaId != null ? new Plaga(plagaId, null) : null;
    }

    private static Hospedante toHospedante(Long hospedanteId) {
        return hospedanteId != null ? new Hospedante(hospedanteId, null) : null;
    }

    private static Variedad toVariedad(Long variedadId) {
        return variedadId != null ? new Variedad(variedadId, null) : null;
    }

    private static Especie toEspecie(Long especieId) {
        return especieId != null ? new Especie(especieId, null) : null;
    }

    private static Long getSistemaMonitoreoId(SistemaMonitoreo sistemaMonitoreo) {
        return sistemaMonitoreo != null ? sistemaMonitoreo.getSistemaMonitoreoId() : null;
    }

    private static String getSistemaMonitoreoName(SistemaMonitoreo sistemaMonitoreo) {
        return sistemaMonitoreo != null ? sistemaMonitoreo.getName() : null;
    }

    private static Long getClaveIdentificacionPlagaId(ClaveIdentificacionPlaga claveIdentificacionPlaga) {
        return claveIdentificacionPlaga != null ? claveIdentificacionPlaga.getCidId() : null;
    }

    private static String getClaveIdentificacionPlagaName(ClaveIdentificacionPlaga claveIdentificacionPlaga) {
        return claveIdentificacionPlaga != null ? claveIdentificacionPlaga.getName() : null;
    }

    private static Long getLocationId(Location location) {
        return location != null ? location.getLocationId() : null;
    }

    private static Long getPlagaId(Plaga plaga) {
        return plaga != null ? plaga.getPlagaId() : null;
    }

    private static String getPlagaName(Plaga plaga) {
        return plaga != null ? plaga.getName() : null;
    }

    private static Long getHospedanteId(Hospedante hospedante) {
        return hospedante != null ? hospedante.getHospedanteId() : null;
    }

    private static String getHospedanteName(Hospedante hospedante) {
        return hospedante != null ? hospedante.getName() : null;
    }

    private static Long getVariedadId(Variedad variedad) {
        return variedad != null ? variedad.getVariedadId() : null;
    }

    private static String getVariedadName(Variedad variedad) {
        return variedad != null ? variedad.getName() : null;
    }

    private static Long getEspecieId(Especie especie) {
        return especie != null ? especie.getEspecieId() : null;
    }

    private static String getEspecieName(Especie especie) {
        return especie != null ? especie.getName() : null;
    }
}