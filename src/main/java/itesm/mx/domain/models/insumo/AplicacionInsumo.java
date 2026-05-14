package itesm.mx.domain.models.insumo;

import itesm.mx.domain.models.marketplace.Product;
import itesm.mx.domain.models.parcela.Parcela;
import itesm.mx.domain.models.user.Farmer;

import java.time.LocalDate;

public class AplicacionInsumo {
    private Long aplicacionId;
    private LocalDate fecha;
    private Farmer agricultor;
    private Product producto;
    private Double cantidad;
    private Parcela parcela;

    public AplicacionInsumo() {
    }

    public AplicacionInsumo(Long aplicacionId, LocalDate fecha, Farmer agricultor, Product producto,
                             Double cantidad, Parcela parcela) {
        this.aplicacionId = aplicacionId;
        this.fecha = fecha;
        this.agricultor = agricultor;
        this.producto = producto;
        this.cantidad = cantidad;
        this.parcela = parcela;
    }

    public Long getAplicacionId() {
        return aplicacionId;
    }

    public void setAplicacionId(Long aplicacionId) {
        this.aplicacionId = aplicacionId;
    }

    public LocalDate getFecha() {
        return fecha;
    }

    public void setFecha(LocalDate fecha) {
        this.fecha = fecha;
    }

    public Farmer getAgricultor() {
        return agricultor;
    }

    public void setAgricultor(Farmer agricultor) {
        this.agricultor = agricultor;
    }

    public Product getProducto() {
        return producto;
    }

    public void setProducto(Product producto) {
        this.producto = producto;
    }

    public Double getCantidad() {
        return cantidad;
    }

    public void setCantidad(Double cantidad) {
        this.cantidad = cantidad;
    }

    public Parcela getParcela() {
        return parcela;
    }

    public void setParcela(Parcela parcela) {
        this.parcela = parcela;
    }
}
