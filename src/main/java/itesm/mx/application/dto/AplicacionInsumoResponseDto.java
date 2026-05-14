package itesm.mx.application.dto;

import java.time.LocalDate;

public class AplicacionInsumoResponseDto {

    public Long aplicacionId;
    public LocalDate fecha;
    public Long skuIdVendedor;
    public String productoNombre;
    public String unidad;
    public Double cantidad;
    public Long parcelaId;
    public String parcelaNombre;
    public Long agricultorId;

    public AplicacionInsumoResponseDto() {
    }

    public AplicacionInsumoResponseDto(Long aplicacionId, LocalDate fecha, Long skuIdVendedor,
                                        String productoNombre, String unidad, Double cantidad,
                                        Long parcelaId, String parcelaNombre, Long agricultorId) {
        this.aplicacionId = aplicacionId;
        this.fecha = fecha;
        this.skuIdVendedor = skuIdVendedor;
        this.productoNombre = productoNombre;
        this.unidad = unidad;
        this.cantidad = cantidad;
        this.parcelaId = parcelaId;
        this.parcelaNombre = parcelaNombre;
        this.agricultorId = agricultorId;
    }
}
