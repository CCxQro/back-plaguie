package itesm.mx.infrastructure.persistence.repository.reporte;

import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import itesm.mx.domain.models.reporte.PestMapPoint;
import itesm.mx.domain.repository.reporte.PestMapRepository;
import itesm.mx.infrastructure.persistence.entity.vigilancia.VigilanciaFitosanitariaEntity;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@ApplicationScoped
public class PestMapRepositoryImpl
        implements PanacheRepositoryBase<VigilanciaFitosanitariaEntity, Long>, PestMapRepository {

    @PersistenceContext
    EntityManager entityManager;

    @Override
    public List<PestMapPoint> findValidatedMapPoints() {
        String jpql = """
                select v.vigilanciaFitosanitariaId,
                       v.latitude,
                       v.longitude,
                       plaga.name,
                       hospedante.name,
                       especie.name,
                       state.name,
                       municipality.name,
                       v.ahosp,
                       v.validatedAt
                from VigilanciaFitosanitariaEntity v
                left join v.plaga plaga
                left join v.hospedante hospedante
                left join v.especie especie
                left join v.ubicacion ubicacion
                left join ubicacion.state state
                left join ubicacion.municipality municipality
                where v.statusId = 1
                  and v.latitude is not null
                  and v.longitude is not null
                order by v.validatedAt desc
                """;

        @SuppressWarnings("unchecked")
        List<Object[]> rows = entityManager.createQuery(jpql).getResultList();

        List<PestMapPoint> result = new ArrayList<>(rows.size());
        for (Object[] row : rows) {
            result.add(new PestMapPoint(
                    (Long) row[0],
                    (BigDecimal) row[1],
                    (BigDecimal) row[2],
                    (String) row[3],
                    (String) row[4],
                    (String) row[5],
                    (String) row[6],
                    (String) row[7],
                    (BigDecimal) row[8],
                    (LocalDateTime) row[9]
            ));
        }
        return result;
    }
}
