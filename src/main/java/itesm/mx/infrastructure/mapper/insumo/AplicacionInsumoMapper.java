package itesm.mx.infrastructure.mapper.insumo;

import itesm.mx.domain.models.insumo.AplicacionInsumo;
import itesm.mx.domain.models.marketplace.Product;
import itesm.mx.domain.models.parcela.Parcela;
import itesm.mx.domain.models.user.Farmer;
import itesm.mx.infrastructure.mapper.marketplace.ProductMapper;
import itesm.mx.infrastructure.mapper.parcela.ParcelaMapper;
import itesm.mx.infrastructure.mapper.user.FarmerMapper;
import itesm.mx.infrastructure.persistence.entity.insumo.AplicacionInsumoEntity;
import itesm.mx.infrastructure.persistence.entity.marketplace.ProductEntity;
import itesm.mx.infrastructure.persistence.entity.parcela.ParcelaEntity;
import itesm.mx.infrastructure.persistence.entity.users.FarmerEntity;

public class AplicacionInsumoMapper {

    public static AplicacionInsumoEntity toEntity(AplicacionInsumo domain) {
        AplicacionInsumoEntity entity = new AplicacionInsumoEntity();
        entity.aplicacionId = domain.getAplicacionId();
        entity.fecha = domain.getFecha();
        entity.agricultorId = domain.getAgricultor() != null ? domain.getAgricultor().getFarmerId() : null;
        entity.skuIdVendedor = domain.getProducto() != null ? domain.getProducto().getSkuSellerId() : null;
        entity.cantidad = domain.getCantidad();
        entity.parcelaId = domain.getParcela() != null ? domain.getParcela().getParcelaId() : null;
        return entity;
    }

    public static AplicacionInsumo toDomain(AplicacionInsumoEntity entity) {
        AplicacionInsumo domain = new AplicacionInsumo();
        domain.setAplicacionId(entity.aplicacionId);
        domain.setFecha(entity.fecha);
        domain.setCantidad(entity.cantidad);
        domain.setAgricultor(mapAgricultor(entity));
        domain.setProducto(mapProducto(entity));
        domain.setParcela(mapParcela(entity));
        return domain;
    }

    private static Farmer mapAgricultor(AplicacionInsumoEntity entity) {
        FarmerEntity farmerEntity = entity.agricultor;
        if (farmerEntity != null) {
            return FarmerMapper.toDomain(farmerEntity);
        }
        Farmer stub = new Farmer();
        stub.setFarmerId(entity.agricultorId);
        return stub;
    }

    private static Product mapProducto(AplicacionInsumoEntity entity) {
        ProductEntity productEntity = entity.producto;
        if (productEntity != null) {
            return ProductMapper.toDomain(productEntity);
        }
        Product stub = new Product();
        stub.setSkuSellerId(entity.skuIdVendedor);
        return stub;
    }

    private static Parcela mapParcela(AplicacionInsumoEntity entity) {
        ParcelaEntity parcelaEntity = entity.parcela;
        if (parcelaEntity != null) {
            return ParcelaMapper.toDomain(parcelaEntity);
        }
        Parcela stub = new Parcela();
        stub.setParcelaId(entity.parcelaId);
        return stub;
    }
}
