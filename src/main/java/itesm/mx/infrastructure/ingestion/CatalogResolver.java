package itesm.mx.infrastructure.ingestion;

import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import itesm.mx.infrastructure.persistence.entity.vigilancia.EspecieEntity;
import itesm.mx.infrastructure.persistence.entity.vigilancia.HospedanteEntity;
import itesm.mx.infrastructure.persistence.entity.vigilancia.PlagaEntity;
import itesm.mx.infrastructure.persistence.entity.location.StateEntity;
import itesm.mx.infrastructure.persistence.entity.location.MunicipalityEntity;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;

/**
 * Get-or-create helpers for catalog tables used by VigilanciaFitosanitaria.
 * All methods must run inside an existing transaction (called from @Transactional callers).
 */
@ApplicationScoped
public class CatalogResolver {

    @Inject
    EntityManager em;

    public Long resolveEstado(String nombre) {
        if (nombre == null || nombre.isBlank()) return null;
        String normalized = nombre.trim();
        StateEntity existing = em.createQuery(
                        "SELECT e FROM StateEntity e WHERE LOWER(e.name) = LOWER(:name)", StateEntity.class)
                .setParameter("name", normalized)
                .getResultStream()
                .findFirst()
                .orElse(null);
        if (existing != null) return existing.stateId;
        StateEntity e = new StateEntity();
        e.name = normalized;
        em.persist(e);
        em.flush();
        return e.stateId;
    }

    public Long resolveMunicipio(String nombre) {
        if (nombre == null || nombre.isBlank()) return null;
        String normalized = nombre.trim();
        MunicipalityEntity existing = em.createQuery(
                        "SELECT m FROM MunicipalityEntity m WHERE LOWER(m.name) = LOWER(:name)", MunicipalityEntity.class)
                .setParameter("name", normalized)
                .getResultStream()
                .findFirst()
                .orElse(null);
        if (existing != null) return existing.municipalityId;
        MunicipalityEntity m = new MunicipalityEntity();
        m.name = normalized;
        em.persist(m);
        em.flush();
        return m.municipalityId;
    }

    public Long resolvePlaga(String nombre) {
        if (nombre == null || nombre.isBlank()) return null;
        String normalized = nombre.trim();
        PlagaEntity existing = em.createQuery(
                        "SELECT p FROM PlagaEntity p WHERE LOWER(p.name) = LOWER(:name)", PlagaEntity.class)
                .setParameter("name", normalized)
                .getResultStream()
                .findFirst()
                .orElse(null);
        if (existing != null) return existing.plagaId;
        PlagaEntity p = new PlagaEntity();
        p.name = normalized;
        em.persist(p);
        em.flush();
        return p.plagaId;
    }

    public Long resolveHospedante(String nombre) {
        if (nombre == null || nombre.isBlank()) return null;
        String normalized = nombre.trim();
        HospedanteEntity existing = em.createQuery(
                        "SELECT h FROM HospedanteEntity h WHERE LOWER(h.name) = LOWER(:name)", HospedanteEntity.class)
                .setParameter("name", normalized)
                .getResultStream()
                .findFirst()
                .orElse(null);
        if (existing != null) return existing.hospedanteId;
        HospedanteEntity h = new HospedanteEntity();
        h.name = normalized;
        em.persist(h);
        em.flush();
        return h.hospedanteId;
    }

    public Long resolveEspecie(String nombre) {
        if (nombre == null || nombre.isBlank()) return null;
        String normalized = nombre.trim();
        EspecieEntity existing = em.createQuery(
                        "SELECT e FROM EspecieEntity e WHERE LOWER(e.name) = LOWER(:name)", EspecieEntity.class)
                .setParameter("name", normalized)
                .getResultStream()
                .findFirst()
                .orElse(null);
        if (existing != null) return existing.especieId;
        EspecieEntity e = new EspecieEntity();
        e.name = normalized;
        em.persist(e);
        em.flush();
        return e.especieId;
    }
}
