package itesm.mx.infrastructure.persistence.repository.insumo;

import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import itesm.mx.domain.models.insumo.AplicacionInsumo;
import itesm.mx.domain.repository.insumo.AplicacionInsumoRepository;
import itesm.mx.infrastructure.mapper.insumo.AplicacionInsumoMapper;
import itesm.mx.infrastructure.persistence.entity.insumo.AplicacionInsumoEntity;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;

@ApplicationScoped
public class AplicacionInsumoRepositoryImpl
        implements PanacheRepositoryBase<AplicacionInsumoEntity, Long>, AplicacionInsumoRepository {

    private static final String FETCH_QUERY = """
            select a from AplicacionInsumoEntity a
            left join fetch a.agricultor ag
            left join fetch ag.user
            left join fetch a.producto p
            left join fetch p.unit
            left join fetch a.parcela
            """;

    @Override
    public AplicacionInsumo save(AplicacionInsumo domain) {
        AplicacionInsumoEntity entity = AplicacionInsumoMapper.toEntity(domain);
        persistAndFlush(entity);
        return find(FETCH_QUERY + " where a.aplicacionId = ?1", entity.aplicacionId)
                .firstResultOptional()
                .map(AplicacionInsumoMapper::toDomain)
                .orElseThrow(() -> new IllegalStateException("No se pudo recuperar la aplicacion de insumo recién registrada"));
    }

    @Override
    public List<AplicacionInsumo> findByFarmerId(Long farmerId) {
        return find(FETCH_QUERY + " where ag.farmerId = ?1 order by a.fecha desc", farmerId)
                .list()
                .stream()
                .map(AplicacionInsumoMapper::toDomain)
                .toList();
    }

    @Override
    public Double calcularStockDisponible(Long farmerId, Long skuIdVendedor) {
        Number entradas = (Number) getEntityManager()
                .createQuery(
                        "select coalesce(sum(d.quantity), 0) from OrderDetailEntity d " +
                        "join d.order p where p.farmerId = ?1 and p.orderStatusId = 4 and d.productId = ?2")
                .setParameter(1, farmerId)
                .setParameter(2, skuIdVendedor)
                .getSingleResult();

        Number aplicado = (Number) getEntityManager()
                .createQuery(
                        "select coalesce(sum(a.cantidad), 0) from AplicacionInsumoEntity a " +
                        "where a.agricultorId = ?1 and a.skuIdVendedor = ?2")
                .setParameter(1, farmerId)
                .setParameter(2, skuIdVendedor)
                .getSingleResult();

        return entradas.doubleValue() - aplicado.doubleValue();
    }
}
