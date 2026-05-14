package itesm.mx.infrastructure.persistence.repository.parcela;

import itesm.mx.domain.models.parcela.Parcela;
import itesm.mx.domain.repository.parcela.ParcelaRepository;
import itesm.mx.infrastructure.mapper.parcela.ParcelaMapper;
import itesm.mx.infrastructure.persistence.entity.parcela.ParcelaEntity;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;

import java.util.List;
import java.util.Optional;

@ApplicationScoped
public class ParcelaRepositoryImpl implements ParcelaRepository {

    @Inject
    EntityManager entityManager;

    private static final String FETCH_QUERY = """
            select p
            from ParcelaEntity p
            left join fetch p.farmer f
            left join fetch f.user
            left join fetch p.location l
            left join fetch l.state
            left join fetch l.municipality
            left join fetch l.locality
            left join fetch l.property
            left join fetch p.estadoParcela
            left join fetch p.tipoCultivo
            left join fetch p.sistemaRiego
            """;

    @Override
    public List<Parcela> findAllParcelas() {
        return find(FETCH_QUERY)
                .list()
                .stream()
                .map(ParcelaMapper::toDomain)
                .toList();
    }

    @Override
    public Optional<Parcela> findParcelaById(Long parcelaId) {
        return find(FETCH_QUERY + " where p.parcelaId = ?1", parcelaId)
                .firstResultOptional()
                .map(ParcelaMapper::toDomain);
    }

    @Override
    public List<Parcela> findByFarmerId(Long farmerId) {
        return entityManager.createQuery(FETCH_QUERY + " where p.farmerId = :farmerId", ParcelaEntity.class)
                .setParameter("farmerId", farmerId)
                .getResultStream()
                .map(ParcelaMapper::toDomain)
                .toList();
    }

    @Override
    public Parcela save(Parcela parcela) {
        ParcelaEntity entity = ParcelaMapper.toEntity(parcela);
        persistAndFlush(entity);
        return findParcelaById(entity.parcelaId)
                .orElseThrow(() -> new IllegalStateException("No se pudo recuperar la parcela recién registrada"));
    }

    @Override
    public Parcela update(Parcela parcela) {
        ParcelaEntity entity = entityManager.find(ParcelaEntity.class, parcela.getParcelaId());
        if (entity == null) {
            throw new IllegalArgumentException("Parcela no encontrada: " + parcela.getParcelaId());
        }
        entity.nombreParcela = parcela.getNombreParcela();
        entity.tamanoHectareas = parcela.getTamanoHectareas();
        entity.fechaSiembra = parcela.getFechaSiembra();
        entity.fechaCosecha = parcela.getFechaCosecha();
        entity.phSuelo = parcela.getPhSuelo();
        entity.farmerId = parcela.getFarmer().getFarmerId();
        entity.locationId = parcela.getLocation().getLocationId();
        entity.estadoParcelaId = parcela.getEstadoParcela().getEstadoParcelaId();
        entity.tipoCultivoId = parcela.getTipoCultivo().getTipoCultivoId();
        entity.sistemaRiegoId = parcela.getSistemaRiego().getSistemaRiegoId();
        persistAndFlush(entity);
        return findParcelaById(entity.parcelaId)
                .orElseThrow(() -> new IllegalStateException("No se pudo recuperar la parcela actualizada"));
    }

    @Override
    public void delete(Long parcelaId) {
        ParcelaEntity entity = entityManager.find(ParcelaEntity.class, parcelaId);
        if (entity != null) {
            entityManager.remove(entity);
        }
    }
}
