package itesm.mx.domain.models.marketplace;

public class InventoryAction {
    private Long inventoryActionId;
    private String accion;

    public InventoryAction() {}

    public InventoryAction(Long inventoryActionId, String accion) {
        this.inventoryActionId = inventoryActionId;
        this.accion = accion;
    }

    public Long getInventoryActionId() { return inventoryActionId; }
    public void setInventoryActionId(Long inventoryActionId) { this.inventoryActionId = inventoryActionId; }

    public String getAccion() { return accion; }
    public void setAccion(String accion) { this.accion = accion; }
}
