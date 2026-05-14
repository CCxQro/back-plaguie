package itesm.mx.application.mapper.marketplace;

import itesm.mx.application.dto.InventoryResponseDto;
import itesm.mx.domain.models.marketplace.Inventory;

public class InventoryDtoMapper {

    public static InventoryResponseDto toResponseDto(Inventory inventory) {
        InventoryResponseDto dto = new InventoryResponseDto();
        dto.inventoryId = inventory.getInventoryId();
        dto.skuSellerId = inventory.getProduct() != null ? inventory.getProduct().getSkuSellerId() : null;
        if (inventory.getAction() != null) {
            dto.actionId = inventory.getAction().getInventoryActionId();
            dto.actionName = inventory.getAction().getAccion();
        }
        dto.cantidad = inventory.getCantidad();
        return dto;
    }
}
