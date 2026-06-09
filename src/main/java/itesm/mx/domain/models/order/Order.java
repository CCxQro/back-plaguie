package itesm.mx.domain.models.order;

import itesm.mx.domain.models.user.Farmer;
import itesm.mx.domain.models.user.TechnicalSeller;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class Order {
    private Long orderId;
    private Farmer farmer;
    private TechnicalSeller seller;
    private LocalDateTime orderDate;
    private OrderStatus orderStatus;
    private BigDecimal totalAmount;
    private List<OrderDetail> details;
    private Boolean providerShared;
    private String shippingStreet;
    private String shippingCity;
    private String shippingState;
    private Double shippingLat;
    private Double shippingLon;

    public Order() {}

    public Order(Long orderId, Farmer farmer, TechnicalSeller seller, LocalDateTime orderDate,
                 OrderStatus orderStatus, BigDecimal totalAmount, List<OrderDetail> details) {
        this.orderId = orderId;
        this.farmer = farmer;
        this.seller = seller;
        this.orderDate = orderDate;
        this.orderStatus = orderStatus;
        this.totalAmount = totalAmount;
        this.details = details;
    }

    public Long getOrderId() { return orderId; }
    public void setOrderId(Long orderId) { this.orderId = orderId; }

    public Farmer getFarmer() { return farmer; }
    public void setFarmer(Farmer farmer) { this.farmer = farmer; }

    public TechnicalSeller getSeller() { return seller; }
    public void setSeller(TechnicalSeller seller) { this.seller = seller; }

    public LocalDateTime getOrderDate() { return orderDate; }
    public void setOrderDate(LocalDateTime orderDate) { this.orderDate = orderDate; }

    public OrderStatus getOrderStatus() { return orderStatus; }
    public void setOrderStatus(OrderStatus orderStatus) { this.orderStatus = orderStatus; }

    public BigDecimal getTotalAmount() { return totalAmount; }
    public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }

    public List<OrderDetail> getDetails() { return details; }
    public void setDetails(List<OrderDetail> details) { this.details = details; }

    public Boolean getProviderShared() { return providerShared; }
    public void setProviderShared(Boolean providerShared) { this.providerShared = providerShared; }

    public String getShippingStreet() { return shippingStreet; }
    public void setShippingStreet(String shippingStreet) { this.shippingStreet = shippingStreet; }

    public String getShippingCity() { return shippingCity; }
    public void setShippingCity(String shippingCity) { this.shippingCity = shippingCity; }

    public String getShippingState() { return shippingState; }
    public void setShippingState(String shippingState) { this.shippingState = shippingState; }

    public Double getShippingLat() { return shippingLat; }
    public void setShippingLat(Double shippingLat) { this.shippingLat = shippingLat; }

    public Double getShippingLon() { return shippingLon; }
    public void setShippingLon(Double shippingLon) { this.shippingLon = shippingLon; }
}
