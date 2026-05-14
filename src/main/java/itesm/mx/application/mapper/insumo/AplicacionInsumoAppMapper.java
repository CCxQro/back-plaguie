package itesm.mx.application.mapper.insumo;

import itesm.mx.application.dto.AplicacionInsumoResponseDto;
import itesm.mx.application.dto.RegisterAplicacionInsumoDto;
import itesm.mx.domain.models.insumo.AplicacionInsumo;
import itesm.mx.domain.models.marketplace.Product;
import itesm.mx.domain.models.marketplace.Unit;
import itesm.mx.domain.models.parcela.Parcela;
import itesm.mx.domain.models.user.Farmer;

public final class AplicacionInsumoAppMapper {

    private AplicacionInsumoAppMapper() {
    }

    public static AplicacionInsumo toDomain(RegisterAplicacionInsumoDto dto, Long agricultorId) {
        Farmer agricultor = new Farmer();
        agricultor.setFarmerId(agricultorId);

        Product producto = new Product();
        producto.setSkuSellerId(dto.skuIdVendedor);

        Parcela parcela = new Parcela();
        parcela.setParcelaId(dto.parcelaId);

        return new AplicacionInsumo(null, dto.fecha, agricultor, producto, dto.cantidad, parcela);
    }

    public static AplicacionInsumoResponseDto toResponseDto(AplicacionInsumo domain) {
        Long skuIdVendedor = domain.getProducto() != null ? domain.getProducto().getSkuSellerId() : null;

        String productoNombre = null;
        String unidad = null;
        if (domain.getProducto() != null) {
            productoNombre = domain.getProducto().getName();
            Unit unit = domain.getProducto().getUnit();
            if (unit != null) {
                unidad = unit.getName();
            }
        }

        Long parcelaId = domain.getParcela() != null ? domain.getParcela().getParcelaId() : null;
        String parcelaNombre = domain.getParcela() != null ? domain.getParcela().getNombreParcela() : null;

        Long agricultorId = domain.getAgricultor() != null ? domain.getAgricultor().getFarmerId() : null;

        return new AplicacionInsumoResponseDto(
                domain.getAplicacionId(),
                domain.getFecha(),
                skuIdVendedor,
                productoNombre,
                unidad,
                domain.getCantidad(),
                parcelaId,
                parcelaNombre,
                agricultorId
        );
    }
}
