package itesm.mx.application.dto.sales;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema(description = "Inventory alert for a specific product")
public class InventoryAlertDto {

    @Schema(description = "Product SKU Seller ID")
    public Long skuSellerId;

    @Schema(description = "Product name")
    public String productName;

    @Schema(description = "Product SKU")
    public String sku;

    @Schema(description = "Remaining stock")
    public int remainingStock;

    public InventoryAlertDto() {}

    public InventoryAlertDto(Long skuSellerId, String productName, String sku, int remainingStock) {
        this.skuSellerId = skuSellerId;
        this.productName = productName;
        this.sku = sku;
        this.remainingStock = remainingStock;
    }
}
