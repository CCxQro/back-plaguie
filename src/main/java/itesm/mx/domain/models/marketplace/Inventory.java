package itesm.mx.domain.models.marketplace;

public class Inventory {
    private Long inventoryId;
    private Product product;
    private Integer cantidad;
    private InventoryAction action;

    public Inventory() {}

    public Inventory(Long inventoryId, Product product, Integer cantidad, InventoryAction action) {
        this.inventoryId = inventoryId;
        this.product = product;
        this.cantidad = cantidad;
        this.action = action;
    }

    public Long getInventoryId() { return inventoryId; }
    public void setInventoryId(Long inventoryId) { this.inventoryId = inventoryId; }

    public Product getProduct() { return product; }
    public void setProduct(Product product) { this.product = product; }

    public Integer getCantidad() { return cantidad; }
    public void setCantidad(Integer cantidad) { this.cantidad = cantidad; }

    public InventoryAction getAction() { return action; }
    public void setAction(InventoryAction action) { this.action = action; }
}
