package itesm.mx.domain.models.order;

import itesm.mx.domain.models.marketplace.Product;

public class OrderDetail {
    private Long detailId;
    private Long orderId;
    private Product product;
    private Integer quantity;
    private Float unitPrice;

    public OrderDetail() {}

    public OrderDetail(Long detailId, Long orderId, Product product, Integer quantity, Float unitPrice) {
        this.detailId = detailId;
        this.orderId = orderId;
        this.product = product;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
    }

    public Long getDetailId() { return detailId; }
    public void setDetailId(Long detailId) { this.detailId = detailId; }

    public Long getOrderId() { return orderId; }
    public void setOrderId(Long orderId) { this.orderId = orderId; }

    public Product getProduct() { return product; }
    public void setProduct(Product product) { this.product = product; }

    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }

    public Float getUnitPrice() { return unitPrice; }
    public void setUnitPrice(Float unitPrice) { this.unitPrice = unitPrice; }
}
