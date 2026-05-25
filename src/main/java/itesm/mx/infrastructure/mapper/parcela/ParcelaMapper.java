package itesm.mx.infrastructure.mapper.parcela;

import itesm.mx.domain.models.location.Location;
import itesm.mx.domain.models.parcela.*;
import itesm.mx.domain.models.user.Farmer;
import itesm.mx.infrastructure.mapper.location.LocationMapper;
import itesm.mx.infrastructure.mapper.user.FarmerMapper;
import itesm.mx.infrastructure.persistence.entity.location.LocationEntity;
import itesm.mx.infrastructure.persistence.entity.parcela.*;
import itesm.mx.infrastructure.persistence.entity.users.FarmerEntity;

public class ParcelaMapper {

    public static ParcelaEntity toEntity(Parcela parcela) {
        ParcelaEntity entity = new ParcelaEntity();
        entity.parcelaId = parcela.getParcelaId();
        entity.nombreParcela = parcela.getNombreParcela();
        entity.tamanoHectareas = parcela.getTamanoHectareas();
        entity.fechaSiembra = parcela.getFechaSiembra();
        entity.fechaCosecha = parcela.getFechaCosecha();
        entity.phSuelo = parcela.getPhSuelo();
        entity.farmerId = parcela.getFarmer().getFarmerId();
        entity.locationId = parcela.getLocation().getLocationId();
        entity.estadoParcelaId = parcela.getEstadoParcela().getEstadoParcelaId();
        entity.tipoCultivoId = parcela.getTipoCultivo().getTipoCultivoId();
        entity.sistemaRiegoId = parcela.getSistemaRiego().getSistemaRiegoId();
        entity.isActive = parcela.getIsActive();
        return entity;
    }

    public static Parcela toDomain(ParcelaEntity entity) {
        Parcela parcela = new Parcela();
        parcela.setParcelaId(entity.parcelaId);
        parcela.setNombreParcela(entity.nombreParcela);
        parcela.setTamanoHectareas(entity.tamanoHectareas);
        parcela.setFechaSiembra(entity.fechaSiembra);
        parcela.setFechaCosecha(entity.fechaCosecha);
        parcela.setPhSuelo(entity.phSuelo);
        parcela.setFarmer(mapFarmer(entity));
        parcela.setLocation(mapLocation(entity));
        parcela.setEstadoParcela(mapEstadoParcela(entity));
        parcela.setTipoCultivo(mapTipoCultivo(entity));
        parcela.setSistemaRiego(mapSistemaRiego(entity));
        parcela.setIsActive(entity.isActive);
        return parcela;
    }

    private static Farmer mapFarmer(ParcelaEntity entity) {
        FarmerEntity farmerEntity = entity.farmer;
        if (farmerEntity != null) {
            return FarmerMapper.toDomain(farmerEntity);
        }

        Farmer farmer = new Farmer();
        farmer.setFarmerId(entity.farmerId);
        return farmer;
    }

    private static Location mapLocation(ParcelaEntity entity) {
        LocationEntity locationEntity = entity.location;
        if (locationEntity != null) {
            return LocationMapper.toDomain(locationEntity);
        }

        Location location = new Location();
        location.setLocationId(entity.locationId);
        return location;
    }

    private static EstadoParcela mapEstadoParcela(ParcelaEntity entity) {
        EstadoParcelaEntity estadoEntity = entity.estadoParcela;
        if (estadoEntity != null) {
            return new EstadoParcela(estadoEntity.estadoParcelaId, estadoEntity.nombre);
        }

        return entity.estadoParcelaId != null ? new EstadoParcela(entity.estadoParcelaId, null) : null;
    }

    private static TipoCultivo mapTipoCultivo(ParcelaEntity entity) {
        TipoCultivoEntity tipoEntity = entity.tipoCultivo;
        if (tipoEntity != null) {
            return new TipoCultivo(tipoEntity.tipoCultivoId, tipoEntity.nombre, tipoEntity.fechaSiembra, tipoEntity.fechaCosecha);
        }

        return entity.tipoCultivoId != null ? new TipoCultivo(entity.tipoCultivoId, null, null, null) : null;
    }

    private static SistemaRiego mapSistemaRiego(ParcelaEntity entity) {
        SistemaRiegoEntity sistemaEntity = entity.sistemaRiego;
        if (sistemaEntity != null) {
            return new SistemaRiego(sistemaEntity.sistemaRiegoId, sistemaEntity.nombre);
        }

        return entity.sistemaRiegoId != null ? new SistemaRiego(entity.sistemaRiegoId, null) : null;
    }
}
