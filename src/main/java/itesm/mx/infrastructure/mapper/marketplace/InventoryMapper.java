package itesm.mx.infrastructure.mapper.marketplace;

import itesm.mx.domain.models.marketplace.Inventory;
import itesm.mx.domain.models.marketplace.InventoryAction;
import itesm.mx.domain.models.marketplace.Product;
import itesm.mx.infrastructure.persistence.entity.marketplace.InventoryActionEntity;
import itesm.mx.infrastructure.persistence.entity.marketplace.InventoryEntity;
import itesm.mx.infrastructure.persistence.entity.marketplace.ProductEntity;

public class InventoryMapper {

    public static InventoryEntity toEntity(Inventory inventory) {
        InventoryEntity entity = new InventoryEntity();
        entity.inventoryId = inventory.getInventoryId();
        entity.skuSellerId = inventory.getProduct().getSkuSellerId();
        entity.cantidad = inventory.getCantidad();
        entity.actionId = inventory.getAction().getInventoryActionId();
        return entity;
    }

    public static Inventory toDomain(InventoryEntity entity) {
        Inventory inventory = new Inventory();
        inventory.setInventoryId(entity.inventoryId);
        inventory.setProduct(mapProduct(entity));
        inventory.setCantidad(entity.cantidad);
        inventory.setAction(mapAction(entity));
        return inventory;
    }

    private static Product mapProduct(InventoryEntity entity) {
        ProductEntity productEntity = entity.product;
        if (productEntity != null) {
            return ProductMapper.toDomain(productEntity);
        }
        Product product = new Product();
        product.setSkuSellerId(entity.skuSellerId);
        return product;
    }

    private static InventoryAction mapAction(InventoryEntity entity) {
        InventoryActionEntity actionEntity = entity.action;
        if (actionEntity != null) {
            return InventoryActionMapper.toDomain(actionEntity);
        }
        InventoryAction action = new InventoryAction();
        action.setInventoryActionId(entity.actionId);
        return action;
    }
}
