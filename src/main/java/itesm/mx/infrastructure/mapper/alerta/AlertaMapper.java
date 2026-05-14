package itesm.mx.infrastructure.mapper.alerta;

import itesm.mx.domain.models.alerta.Alerta;
import itesm.mx.infrastructure.persistence.entity.alerta.AlertaEntity;
import itesm.mx.infrastructure.persistence.entity.marketplace.StatusEntity;

public final class AlertaMapper {

    private AlertaMapper() {
    }

    public static AlertaEntity toEntity(Alerta alerta) {
        AlertaEntity entity = new AlertaEntity();
        entity.alertaId = alerta.getAlertaId();
        entity.titulo = alerta.getTitulo();
        entity.descripcion = alerta.getDescripcion();
        entity.ubicacionId = alerta.getUbicacionId();
        entity.tipoPlaga = alerta.getTipoPlaga();
        entity.hectareas = alerta.getHectareas();
        entity.severidad = alerta.getSeveridad();
        entity.reportedByUserId = alerta.getReportedByUserId();
        entity.createdAt = alerta.getCreatedAt();
        entity.statusId = alerta.getStatusId();
        entity.validatedByUserId = alerta.getValidatedByUserId();
        entity.validatedAt = alerta.getValidatedAt();
        return entity;
    }

    public static Alerta toDomain(AlertaEntity entity) {
        Alerta alerta = new Alerta();
        alerta.setAlertaId(entity.alertaId);
        alerta.setTitulo(entity.titulo);
        alerta.setDescripcion(entity.descripcion);
        alerta.setUbicacionId(entity.ubicacionId);
        alerta.setTipoPlaga(entity.tipoPlaga);
        alerta.setHectareas(entity.hectareas);
        alerta.setSeveridad(entity.severidad);
        alerta.setReportedByUserId(entity.reportedByUserId);
        alerta.setCreatedAt(entity.createdAt);
        alerta.setStatusId(entity.statusId);
        alerta.setStatusName(mapStatusName(entity));
        alerta.setValidatedByUserId(entity.validatedByUserId);
        alerta.setValidatedAt(entity.validatedAt);
        return alerta;
    }

    private static String mapStatusName(AlertaEntity entity) {
        StatusEntity statusEntity = entity.status;
        return statusEntity != null ? statusEntity.name : null;
    }
}
