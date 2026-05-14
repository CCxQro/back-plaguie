package itesm.mx.infrastructure.persistence.repository.alerta;

import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import itesm.mx.domain.models.alerta.Alerta;
import itesm.mx.domain.repository.alerta.AlertaRepository;
import itesm.mx.infrastructure.mapper.alerta.AlertaMapper;
import itesm.mx.infrastructure.persistence.entity.alerta.AlertaEntity;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;
import java.util.Optional;

@ApplicationScoped
public class AlertaRepositoryImpl implements PanacheRepositoryBase<AlertaEntity, Long>, AlertaRepository {

    @Override
    public List<Alerta> findAllAlertas() {
        return findDetailedQuery().list().stream()
                .map(AlertaMapper::toDomain)
                .toList();
    }

    @Override
    public Optional<Alerta> findAlertaById(Long alertaId) {
        return findDetailedById(alertaId).map(AlertaMapper::toDomain);
    }

    @Override
    public Alerta save(Alerta alerta) {
        AlertaEntity entity = AlertaMapper.toEntity(alerta);
        persistAndFlush(entity);

        return findDetailedById(entity.alertaId)
                .map(AlertaMapper::toDomain)
                .orElseThrow(() -> new IllegalStateException("No se pudo recuperar la alerta recién registrada"));
    }

    @Override
    public Alerta update(Alerta alerta) {
        AlertaEntity entity = findByIdOptional(alerta.getAlertaId())
                .orElseThrow(() -> new IllegalArgumentException("Alerta no encontrada con id: " + alerta.getAlertaId()));

        if (alerta.getTitulo() != null) {
            entity.titulo = alerta.getTitulo();
        }
        if (alerta.getDescripcion() != null) {
            entity.descripcion = alerta.getDescripcion();
        }
        if (alerta.getUbicacionId() != null) {
            entity.ubicacionId = alerta.getUbicacionId();
        }
        if (alerta.getTipoPlaga() != null) {
            entity.tipoPlaga = alerta.getTipoPlaga();
        }
        if (alerta.getHectareas() != null) {
            entity.hectareas = alerta.getHectareas();
        }
        if (alerta.getSeveridad() != null) {
            entity.severidad = alerta.getSeveridad();
        }
        if (alerta.getStatusId() != null) {
            entity.statusId = alerta.getStatusId();
        }
        if (alerta.getValidatedByUserId() != null) {
            entity.validatedByUserId = alerta.getValidatedByUserId();
        }
        if (alerta.getValidatedAt() != null) {
            entity.validatedAt = alerta.getValidatedAt();
        }

        flush();
        getEntityManager().clear();
        return findDetailedById(entity.alertaId)
                .map(AlertaMapper::toDomain)
                .orElseThrow(() -> new IllegalStateException("No se pudo recuperar la alerta actualizada"));
    }

    @Override
    public void delete(Long alertaId) {
        deleteById(alertaId);
    }

    private Optional<AlertaEntity> findDetailedById(Long alertaId) {
        return findDetailedQuery("where a.alertaId = ?1", alertaId).firstResultOptional();
    }

    private io.quarkus.hibernate.orm.panache.PanacheQuery<AlertaEntity> findDetailedQuery() {
        return findDetailedQuery("");
    }

    private io.quarkus.hibernate.orm.panache.PanacheQuery<AlertaEntity> findDetailedQuery(String whereClause, Object... parameters) {
        String query = """
                select a
                from AlertaEntity a
                left join fetch a.ubicacion
                left join fetch a.reportedBy
                left join fetch a.status
                left join fetch a.validatedBy
                """ + whereClause;

        return find(query, parameters);
    }
}
