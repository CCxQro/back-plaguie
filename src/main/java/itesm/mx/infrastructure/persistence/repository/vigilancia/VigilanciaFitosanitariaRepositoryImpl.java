package itesm.mx.infrastructure.persistence.repository.vigilancia;

import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import itesm.mx.domain.models.vigilancia.VigilanciaFitosanitaria;
import itesm.mx.domain.repository.vigilancia.VigilanciaFitosanitariaRepository;
import itesm.mx.infrastructure.mapper.vigilancia.VigilanciaFitosanitariaMapper;
import itesm.mx.infrastructure.persistence.entity.vigilancia.VigilanciaFitosanitariaEntity;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;
import java.util.Optional;

@ApplicationScoped
public class VigilanciaFitosanitariaRepositoryImpl implements PanacheRepositoryBase<VigilanciaFitosanitariaEntity, Long>, VigilanciaFitosanitariaRepository {

    @Override
    public List<VigilanciaFitosanitaria> findAllVigilanciasFitosanitarias() {
        return findDetailedQuery().list().stream()
                .map(VigilanciaFitosanitariaMapper::toDomain)
                .toList();
    }

    @Override
    public Optional<VigilanciaFitosanitaria> findVigilanciaFitosanitariaById(Long vigilanciaFitosanitariaId) {
        return findDetailedById(vigilanciaFitosanitariaId).map(VigilanciaFitosanitariaMapper::toDomain);
    }

    @Override
    public VigilanciaFitosanitaria save(VigilanciaFitosanitaria vigilanciaFitosanitaria) {
        VigilanciaFitosanitariaEntity entity = VigilanciaFitosanitariaMapper.toEntity(vigilanciaFitosanitaria);
        persistAndFlush(entity);

        return findDetailedById(entity.vigilanciaFitosanitariaId)
                .map(VigilanciaFitosanitariaMapper::toDomain)
                .orElseThrow(() -> new IllegalStateException("No se pudo recuperar la vigilancia fitosanitaria recien registrada"));
    }

    @Override
    public VigilanciaFitosanitaria update(VigilanciaFitosanitaria vigilanciaFitosanitaria) {
        VigilanciaFitosanitariaEntity entity = findByIdOptional(vigilanciaFitosanitaria.getVigilanciaFitosanitariaId())
                .orElseThrow(() -> new IllegalArgumentException("Vigilancia fitosanitaria no encontrada con id: " + vigilanciaFitosanitaria.getVigilanciaFitosanitariaId()));

        if (vigilanciaFitosanitaria.getSistemaMonitoreo() != null && vigilanciaFitosanitaria.getSistemaMonitoreo().getSistemaMonitoreoId() != null) {
            entity.sistemaMonitoreoId = vigilanciaFitosanitaria.getSistemaMonitoreo().getSistemaMonitoreoId();
        }
        if (vigilanciaFitosanitaria.getClaveIdentificacionPlaga() != null && vigilanciaFitosanitaria.getClaveIdentificacionPlaga().getCidId() != null) {
            entity.cidId = vigilanciaFitosanitaria.getClaveIdentificacionPlaga().getCidId();
        }
        if (vigilanciaFitosanitaria.getLatitude() != null) {
            entity.latitude = vigilanciaFitosanitaria.getLatitude();
        }
        if (vigilanciaFitosanitaria.getLongitude() != null) {
            entity.longitude = vigilanciaFitosanitaria.getLongitude();
        }
        if (vigilanciaFitosanitaria.getUbicacion() != null && vigilanciaFitosanitaria.getUbicacion().getLocationId() != null) {
            entity.ubicacionId = vigilanciaFitosanitaria.getUbicacion().getLocationId();
        }
        if (vigilanciaFitosanitaria.getPlaga() != null && vigilanciaFitosanitaria.getPlaga().getPlagaId() != null) {
            entity.plagaId = vigilanciaFitosanitaria.getPlaga().getPlagaId();
        }
        if (vigilanciaFitosanitaria.getHospedante() != null && vigilanciaFitosanitaria.getHospedante().getHospedanteId() != null) {
            entity.hospedanteId = vigilanciaFitosanitaria.getHospedante().getHospedanteId();
        }
        if (vigilanciaFitosanitaria.getVariedad() != null && vigilanciaFitosanitaria.getVariedad().getVariedadId() != null) {
            entity.variedadId = vigilanciaFitosanitaria.getVariedad().getVariedadId();
        }
        if (vigilanciaFitosanitaria.getEspecie() != null && vigilanciaFitosanitaria.getEspecie().getEspecieId() != null) {
            entity.especieId = vigilanciaFitosanitaria.getEspecie().getEspecieId();
        }
        if (vigilanciaFitosanitaria.getAhosp() != null) {
            entity.ahosp = vigilanciaFitosanitaria.getAhosp();
        }
        if (vigilanciaFitosanitaria.getStatusId() != null) {
            entity.statusId = vigilanciaFitosanitaria.getStatusId();
        }
        if (vigilanciaFitosanitaria.getValidatedByUserId() != null) {
            entity.validatedByUserId = vigilanciaFitosanitaria.getValidatedByUserId();
        }
        if (vigilanciaFitosanitaria.getValidatedAt() != null) {
            entity.validatedAt = vigilanciaFitosanitaria.getValidatedAt();
        }

        persistAndFlush(entity);
        return findDetailedById(entity.vigilanciaFitosanitariaId)
                .map(VigilanciaFitosanitariaMapper::toDomain)
                .orElseThrow(() -> new IllegalStateException("No se pudo recuperar la vigilancia fitosanitaria actualizada"));
    }

    @Override
    public void delete(Long vigilanciaFitosanitariaId) {
        deleteById(vigilanciaFitosanitariaId);
    }

    private Optional<VigilanciaFitosanitariaEntity> findDetailedById(Long vigilanciaFitosanitariaId) {
        return findDetailedQuery("where v.vigilanciaFitosanitariaId = ?1", vigilanciaFitosanitariaId).firstResultOptional();
    }

    private io.quarkus.hibernate.orm.panache.PanacheQuery<VigilanciaFitosanitariaEntity> findDetailedQuery() {
        return findDetailedQuery("");
    }

    private io.quarkus.hibernate.orm.panache.PanacheQuery<VigilanciaFitosanitariaEntity> findDetailedQuery(String whereClause, Object... parameters) {
        String query = """
                select v
                from VigilanciaFitosanitariaEntity v
                left join fetch v.sistemaMonitoreo
                left join fetch v.claveIdentificacionPlaga
                left join fetch v.ubicacion
                left join fetch v.plaga
                left join fetch v.hospedante
                left join fetch v.variedad
                left join fetch v.especie
                left join fetch v.status
                left join fetch v.validatedBy
                """ + whereClause;

        return find(query, parameters);
    }
}