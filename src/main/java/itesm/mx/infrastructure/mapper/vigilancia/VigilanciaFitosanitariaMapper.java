package itesm.mx.infrastructure.mapper.vigilancia;

import itesm.mx.domain.models.location.Location;
import itesm.mx.domain.models.vigilancia.*;
import itesm.mx.infrastructure.mapper.location.LocationMapper;
import itesm.mx.infrastructure.persistence.entity.location.LocationEntity;
import itesm.mx.infrastructure.persistence.entity.marketplace.StatusEntity;
import itesm.mx.infrastructure.persistence.entity.users.UserEntity;
import itesm.mx.infrastructure.persistence.entity.vigilancia.*;

public final class VigilanciaFitosanitariaMapper {

    private VigilanciaFitosanitariaMapper() {
    }

    public static VigilanciaFitosanitariaEntity toEntity(VigilanciaFitosanitaria vigilanciaFitosanitaria) {
        VigilanciaFitosanitariaEntity entity = new VigilanciaFitosanitariaEntity();
        entity.vigilanciaFitosanitariaId = vigilanciaFitosanitaria.getVigilanciaFitosanitariaId();
        entity.sistemaMonitoreoId = getSistemaMonitoreoId(vigilanciaFitosanitaria.getSistemaMonitoreo());
        entity.cidId = getCidId(vigilanciaFitosanitaria.getClaveIdentificacionPlaga());
        entity.latitude = vigilanciaFitosanitaria.getLatitude();
        entity.longitude = vigilanciaFitosanitaria.getLongitude();
        entity.ubicacionId = getLocationId(vigilanciaFitosanitaria.getUbicacion());
        entity.plagaId = getPlagaId(vigilanciaFitosanitaria.getPlaga());
        entity.hospedanteId = getHospedanteId(vigilanciaFitosanitaria.getHospedante());
        entity.variedadId = getVariedadId(vigilanciaFitosanitaria.getVariedad());
        entity.especieId = getEspecieId(vigilanciaFitosanitaria.getEspecie());
        entity.ahosp = vigilanciaFitosanitaria.getAhosp();
        entity.statusId = vigilanciaFitosanitaria.getStatusId();
        entity.validatedByUserId = vigilanciaFitosanitaria.getValidatedByUserId();
        entity.validatedAt = vigilanciaFitosanitaria.getValidatedAt();
        return entity;
    }

    public static VigilanciaFitosanitaria toDomain(VigilanciaFitosanitariaEntity entity) {
        VigilanciaFitosanitaria vigilanciaFitosanitaria = new VigilanciaFitosanitaria();
        vigilanciaFitosanitaria.setVigilanciaFitosanitariaId(entity.vigilanciaFitosanitariaId);
        vigilanciaFitosanitaria.setSistemaMonitoreo(mapSistemaMonitoreo(entity));
        vigilanciaFitosanitaria.setClaveIdentificacionPlaga(mapClaveIdentificacionPlaga(entity));
        vigilanciaFitosanitaria.setLatitude(entity.latitude);
        vigilanciaFitosanitaria.setLongitude(entity.longitude);
        vigilanciaFitosanitaria.setUbicacion(mapLocation(entity));
        vigilanciaFitosanitaria.setPlaga(mapPlaga(entity));
        vigilanciaFitosanitaria.setHospedante(mapHospedante(entity));
        vigilanciaFitosanitaria.setVariedad(mapVariedad(entity));
        vigilanciaFitosanitaria.setEspecie(mapEspecie(entity));
        vigilanciaFitosanitaria.setAhosp(entity.ahosp);
        vigilanciaFitosanitaria.setStatusId(entity.statusId);
        vigilanciaFitosanitaria.setStatusName(mapStatusName(entity));
        vigilanciaFitosanitaria.setValidatedByUserId(entity.validatedByUserId);
        vigilanciaFitosanitaria.setValidatedAt(entity.validatedAt);
        return vigilanciaFitosanitaria;
    }

    private static String mapStatusName(VigilanciaFitosanitariaEntity entity) {
        StatusEntity statusEntity = entity.status;
        return statusEntity != null ? statusEntity.name : null;
    }

    private static SistemaMonitoreo mapSistemaMonitoreo(VigilanciaFitosanitariaEntity entity) {
        SistemaMonitoreoEntity sistemaMonitoreoEntity = entity.sistemaMonitoreo;
        if (sistemaMonitoreoEntity != null) {
            return new SistemaMonitoreo(sistemaMonitoreoEntity.sistemaMonitoreoId, sistemaMonitoreoEntity.name);
        }

        return entity.sistemaMonitoreoId != null ? new SistemaMonitoreo(entity.sistemaMonitoreoId, null) : null;
    }

    private static ClaveIdentificacionPlaga mapClaveIdentificacionPlaga(VigilanciaFitosanitariaEntity entity) {
        ClaveIdentificacionPlagaEntity claveIdentificacionPlagaEntity = entity.claveIdentificacionPlaga;
        if (claveIdentificacionPlagaEntity != null) {
            return new ClaveIdentificacionPlaga(claveIdentificacionPlagaEntity.cidId, claveIdentificacionPlagaEntity.name);
        }

        return entity.cidId != null ? new ClaveIdentificacionPlaga(entity.cidId, null) : null;
    }

    private static Location mapLocation(VigilanciaFitosanitariaEntity entity) {
        LocationEntity locationEntity = entity.ubicacion;
        if (locationEntity != null) {
            return LocationMapper.toDomain(locationEntity);
        }

        return entity.ubicacionId != null ? new Location(entity.ubicacionId, null, null, null, null, null) : null;
    }

    private static Plaga mapPlaga(VigilanciaFitosanitariaEntity entity) {
        PlagaEntity plagaEntity = entity.plaga;
        if (plagaEntity != null) {
            return new Plaga(plagaEntity.plagaId, plagaEntity.name);
        }

        return entity.plagaId != null ? new Plaga(entity.plagaId, null) : null;
    }

    private static Hospedante mapHospedante(VigilanciaFitosanitariaEntity entity) {
        HospedanteEntity hospedanteEntity = entity.hospedante;
        if (hospedanteEntity != null) {
            return new Hospedante(hospedanteEntity.hospedanteId, hospedanteEntity.name);
        }

        return entity.hospedanteId != null ? new Hospedante(entity.hospedanteId, null) : null;
    }

    private static Variedad mapVariedad(VigilanciaFitosanitariaEntity entity) {
        VariedadEntity variedadEntity = entity.variedad;
        if (variedadEntity != null) {
            return new Variedad(variedadEntity.variedadId, variedadEntity.name);
        }

        return entity.variedadId != null ? new Variedad(entity.variedadId, null) : null;
    }

    private static Especie mapEspecie(VigilanciaFitosanitariaEntity entity) {
        EspecieEntity especieEntity = entity.especie;
        if (especieEntity != null) {
            return new Especie(especieEntity.especieId, especieEntity.name);
        }

        return entity.especieId != null ? new Especie(entity.especieId, null) : null;
    }

    private static Long getSistemaMonitoreoId(SistemaMonitoreo sistemaMonitoreo) {
        return sistemaMonitoreo != null ? sistemaMonitoreo.getSistemaMonitoreoId() : null;
    }

    private static Long getCidId(ClaveIdentificacionPlaga claveIdentificacionPlaga) {
        return claveIdentificacionPlaga != null ? claveIdentificacionPlaga.getCidId() : null;
    }

    private static Long getLocationId(Location location) {
        return location != null ? location.getLocationId() : null;
    }

    private static Long getPlagaId(Plaga plaga) {
        return plaga != null ? plaga.getPlagaId() : null;
    }

    private static Long getHospedanteId(Hospedante hospedante) {
        return hospedante != null ? hospedante.getHospedanteId() : null;
    }

    private static Long getVariedadId(Variedad variedad) {
        return variedad != null ? variedad.getVariedadId() : null;
    }

    private static Long getEspecieId(Especie especie) {
        return especie != null ? especie.getEspecieId() : null;
    }
}