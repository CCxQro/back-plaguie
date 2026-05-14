package itesm.mx.infrastructure.mapper.marketplace;

import itesm.mx.domain.models.marketplace.InventoryAction;
import itesm.mx.infrastructure.persistence.entity.marketplace.InventoryActionEntity;

public class InventoryActionMapper {

    public static InventoryActionEntity toEntity(InventoryAction action) {
        InventoryActionEntity entity = new InventoryActionEntity();
        entity.inventoryActionId = action.getInventoryActionId();
        entity.accion = action.getAccion();
        return entity;
    }

    public static InventoryAction toDomain(InventoryActionEntity entity) {
        InventoryAction action = new InventoryAction();
        action.setInventoryActionId(entity.inventoryActionId);
        action.setAccion(entity.accion);
        return action;
    }
}
