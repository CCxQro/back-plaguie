package itesm.mx.domain.models.order;

public class OrderStatus {
    private Long orderStatusId;
    private String estado;

    public OrderStatus() {}

    public OrderStatus(Long orderStatusId, String estado) {
        this.orderStatusId = orderStatusId;
        this.estado = estado;
    }

    public Long getOrderStatusId() { return orderStatusId; }
    public void setOrderStatusId(Long orderStatusId) { this.orderStatusId = orderStatusId; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }
}
