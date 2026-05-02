package itesm.mx.infrastructure.mapper.marketplace;

import itesm.mx.domain.models.marketplace.Status;
import itesm.mx.infrastructure.persistence.entity.marketplace.StatusEntity;

public class StatusMapper {
    public static StatusEntity toEntity(Status status) {
        StatusEntity entity = new StatusEntity();
        entity.statusId = status.getStatusId();
        entity.name = status.getName();
        return entity;
    }

    public static Status toDomain(StatusEntity entity) {
        Status status = new Status();
        status.setStatusId(entity.statusId);
        status.setName(entity.name);
        return status;
    }
}