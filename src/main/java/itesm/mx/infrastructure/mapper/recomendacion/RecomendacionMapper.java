package itesm.mx.infrastructure.mapper.recomendacion;

import itesm.mx.domain.models.recomendacion.Recomendacion;
import itesm.mx.infrastructure.persistence.entity.marketplace.StatusEntity;
import itesm.mx.infrastructure.persistence.entity.recomendacion.RecomendacionEntity;

public final class RecomendacionMapper {

    private RecomendacionMapper() {
    }

    public static RecomendacionEntity toEntity(Recomendacion recomendacion) {
        RecomendacionEntity entity = new RecomendacionEntity();
        entity.recomendacionId = recomendacion.getRecomendacionId();
        entity.titulo = recomendacion.getTitulo();
        entity.descripcion = recomendacion.getDescripcion();
        entity.tipoPlaga = recomendacion.getTipoPlaga();
        entity.productosRecomendados = recomendacion.getProductosRecomendados();
        entity.reportedByUserId = recomendacion.getReportedByUserId();
        entity.createdAt = recomendacion.getCreatedAt();
        entity.statusId = recomendacion.getStatusId();
        entity.validatedByUserId = recomendacion.getValidatedByUserId();
        entity.validatedAt = recomendacion.getValidatedAt();
        return entity;
    }

    public static Recomendacion toDomain(RecomendacionEntity entity) {
        Recomendacion recomendacion = new Recomendacion();
        recomendacion.setRecomendacionId(entity.recomendacionId);
        recomendacion.setTitulo(entity.titulo);
        recomendacion.setDescripcion(entity.descripcion);
        recomendacion.setTipoPlaga(entity.tipoPlaga);
        recomendacion.setProductosRecomendados(entity.productosRecomendados);
        recomendacion.setReportedByUserId(entity.reportedByUserId);
        recomendacion.setCreatedAt(entity.createdAt);
        recomendacion.setStatusId(entity.statusId);
        recomendacion.setStatusName(mapStatusName(entity));
        recomendacion.setValidatedByUserId(entity.validatedByUserId);
        recomendacion.setValidatedAt(entity.validatedAt);
        return recomendacion;
    }

    private static String mapStatusName(RecomendacionEntity entity) {
        StatusEntity statusEntity = entity.status;
        return statusEntity != null ? statusEntity.name : null;
    }
}
