package itesm.mx.infrastructure.persistence.repository.recomendacion;

import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import itesm.mx.domain.models.recomendacion.Recomendacion;
import itesm.mx.domain.repository.recomendacion.RecomendacionRepository;
import itesm.mx.infrastructure.mapper.recomendacion.RecomendacionMapper;
import itesm.mx.infrastructure.persistence.entity.recomendacion.RecomendacionEntity;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;
import java.util.Optional;

@ApplicationScoped
public class RecomendacionRepositoryImpl implements PanacheRepositoryBase<RecomendacionEntity, Long>, RecomendacionRepository {

    @Override
    public List<Recomendacion> findAllRecomendaciones() {
        return findDetailedQuery().list().stream()
                .map(RecomendacionMapper::toDomain)
                .toList();
    }

    @Override
    public Optional<Recomendacion> findRecomendacionById(Long recomendacionId) {
        return findDetailedById(recomendacionId).map(RecomendacionMapper::toDomain);
    }

    @Override
    public Recomendacion save(Recomendacion recomendacion) {
        RecomendacionEntity entity = RecomendacionMapper.toEntity(recomendacion);
        persistAndFlush(entity);

        return findDetailedById(entity.recomendacionId)
                .map(RecomendacionMapper::toDomain)
                .orElseThrow(() -> new IllegalStateException("No se pudo recuperar la recomendación recién registrada"));
    }

    @Override
    public Recomendacion update(Recomendacion recomendacion) {
        RecomendacionEntity entity = findByIdOptional(recomendacion.getRecomendacionId())
                .orElseThrow(() -> new IllegalArgumentException("Recomendación no encontrada con id: " + recomendacion.getRecomendacionId()));

        if (recomendacion.getTitulo() != null) {
            entity.titulo = recomendacion.getTitulo();
        }
        if (recomendacion.getDescripcion() != null) {
            entity.descripcion = recomendacion.getDescripcion();
        }
        if (recomendacion.getTipoPlaga() != null) {
            entity.tipoPlaga = recomendacion.getTipoPlaga();
        }
        if (recomendacion.getProductosRecomendados() != null) {
            entity.productosRecomendados = recomendacion.getProductosRecomendados();
        }
        if (recomendacion.getStatusId() != null) {
            entity.statusId = recomendacion.getStatusId();
        }
        if (recomendacion.getValidatedByUserId() != null) {
            entity.validatedByUserId = recomendacion.getValidatedByUserId();
        }
        if (recomendacion.getValidatedAt() != null) {
            entity.validatedAt = recomendacion.getValidatedAt();
        }

        persistAndFlush(entity);
        return findDetailedById(entity.recomendacionId)
                .map(RecomendacionMapper::toDomain)
                .orElseThrow(() -> new IllegalStateException("No se pudo recuperar la recomendación actualizada"));
    }

    @Override
    public void delete(Long recomendacionId) {
        deleteById(recomendacionId);
    }

    private Optional<RecomendacionEntity> findDetailedById(Long recomendacionId) {
        return findDetailedQuery("where r.recomendacionId = ?1", recomendacionId).firstResultOptional();
    }

    private io.quarkus.hibernate.orm.panache.PanacheQuery<RecomendacionEntity> findDetailedQuery() {
        return findDetailedQuery("");
    }

    private io.quarkus.hibernate.orm.panache.PanacheQuery<RecomendacionEntity> findDetailedQuery(String whereClause, Object... parameters) {
        String query = """
                select r
                from RecomendacionEntity r
                left join fetch r.reportedBy
                left join fetch r.status
                left join fetch r.validatedBy
                """ + whereClause;

        return find(query, parameters);
    }
}
