package itesm.mx.domain.repository.insumo;

import itesm.mx.domain.models.insumo.AplicacionInsumo;

import java.util.List;

public interface AplicacionInsumoRepository {
    AplicacionInsumo save(AplicacionInsumo aplicacionInsumo);
    List<AplicacionInsumo> findByFarmerId(Long farmerId);
    Double calcularStockDisponible(Long farmerId, Long skuIdVendedor);
}
