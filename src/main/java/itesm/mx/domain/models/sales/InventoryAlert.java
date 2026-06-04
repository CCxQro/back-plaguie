package itesm.mx.domain.models.sales;

public class InventoryAlert {
    private Long skuSellerId;
    private String productName;
    private String sku;
    private int remainingStock;

    public InventoryAlert(Long skuSellerId, String productName, String sku, int remainingStock) {
        this.skuSellerId = skuSellerId;
        this.productName = productName;
        this.sku = sku;
        this.remainingStock = remainingStock;
    }

    public Long getSkuSellerId() { return skuSellerId; }
    public String getProductName() { return productName; }
    public String getSku() { return sku; }
    public int getRemainingStock() { return remainingStock; }
}
