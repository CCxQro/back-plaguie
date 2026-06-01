package itesm.mx.infrastructure.persistence.repository.parcela;

import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import itesm.mx.domain.models.parcela.Parcela;
import itesm.mx.domain.repository.parcela.ParcelaRepository;
import itesm.mx.infrastructure.mapper.parcela.ParcelaMapper;
import itesm.mx.infrastructure.persistence.entity.parcela.ParcelaEntity;

import java.util.List;
import java.util.Optional;

@ApplicationScoped
public class ParcelaRepositoryImpl implements PanacheRepositoryBase<ParcelaEntity, Long>, ParcelaRepository {

    @PersistenceContext
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
        return getEntityManager().createQuery(FETCH_QUERY, ParcelaEntity.class)
            .getResultList()
                .stream()
                .map(ParcelaMapper::toDomain)
                .toList();
    }

    @Override
    public Optional<Parcela> findParcelaById(Long parcelaId) {
        return getEntityManager().createQuery(FETCH_QUERY + " where p.parcelaId = :parcelaId", ParcelaEntity.class)
            .setParameter("parcelaId", parcelaId)
            .getResultStream()
            .findFirst()
                .map(ParcelaMapper::toDomain);
    }

    @Override
    public List<Parcela> findByFarmerId(Long farmerId) {
        return find(FETCH_QUERY + " where p.farmerId = ?1", farmerId)
                .list()
                .stream()
                .map(ParcelaMapper::toDomain)
                .toList();
    }

    @Override
    public Parcela save(Parcela parcela) {
        ParcelaEntity entity = ParcelaMapper.toEntity(parcela);
        getEntityManager().persist(entity);
        getEntityManager().flush();
        // Detach so the JOIN FETCH re-fetch loads a clean graph from the DB.
        // Otherwise the managed instance (with null to-one associations but set
        // FK columns) makes Hibernate raise FetchNotFoundException on re-read.
        Long newId = entity.parcelaId;
        getEntityManager().detach(entity);
        return findParcelaById(newId)
                .orElseThrow(() -> new IllegalStateException("No se pudo recuperar la parcela recién registrada"));
    }

    @Override
    public Parcela update(Parcela parcela) {
        ParcelaEntity entity = getEntityManager().find(ParcelaEntity.class, parcela.getParcelaId());
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
        entity.isActive = parcela.getIsActive();
        getEntityManager().flush();
        return findParcelaById(entity.parcelaId)
                .orElseThrow(() -> new IllegalStateException("No se pudo recuperar la parcela actualizada"));
    }

    @Override
    public void delete(Long parcelaId) {
        deleteById(parcelaId);
    }

    @Override
    public void setActiveByFarmerId(Long farmerId, boolean isActive) {
        update("isActive = ?1 where farmerId = ?2", isActive, farmerId);
    }
}
