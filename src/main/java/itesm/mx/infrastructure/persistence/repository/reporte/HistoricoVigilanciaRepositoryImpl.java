package itesm.mx.infrastructure.persistence.repository.reporte;

import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import itesm.mx.domain.models.reporte.HistoricoVigilanciaSummary;
import itesm.mx.domain.models.reporte.Temporada;
import itesm.mx.domain.repository.reporte.HistoricoVigilanciaRepository;
import itesm.mx.infrastructure.persistence.entity.vigilancia.VigilanciaFitosanitariaEntity;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

@ApplicationScoped
public class HistoricoVigilanciaRepositoryImpl implements PanacheRepositoryBase<VigilanciaFitosanitariaEntity, Long>, HistoricoVigilanciaRepository {

    @PersistenceContext
    EntityManager entityManager;

    @Override
    public List<HistoricoVigilanciaSummary> findResumenPorRegionYTemporada(String region, Temporada temporada) {
        if (region == null || region.isBlank() || temporada == null) {
            return List.of();
        }

        String regionLike = "%" + region.trim().toLowerCase() + "%";
        List<Integer> meses = temporada.getMeses();

        String jpql = """
                select plaga.name,
                       hospedante.name,
                       especie.name,
                       state.name,
                       municipality.name,
                       count(v),
                       avg(v.ahosp)
                from VigilanciaFitosanitariaEntity v
                left join v.plaga plaga
                left join v.hospedante hospedante
                left join v.especie especie
                left join v.ubicacion ubicacion
                left join ubicacion.state state
                left join ubicacion.municipality municipality
                where v.statusId = 1
                  and (lower(coalesce(state.name, '')) like :region
                       or lower(coalesce(municipality.name, '')) like :region)
                  and (v.validatedAt is null or month(v.validatedAt) in :meses)
                group by plaga.name, hospedante.name, especie.name, state.name, municipality.name
                order by count(v) desc
                """;

        @SuppressWarnings("unchecked")
        List<Object[]> rows = entityManager.createQuery(jpql)
                .setParameter("region", regionLike)
                .setParameter("meses", meses)
                .getResultList();

        List<HistoricoVigilanciaSummary> result = new ArrayList<>(rows.size());
        for (Object[] row : rows) {
            String plagaNombre = (String) row[0];
            String hospedanteNombre = (String) row[1];
            String especieNombre = (String) row[2];
            String estadoNombre = (String) row[3];
            String municipioNombre = (String) row[4];
            long observaciones = ((Number) row[5]).longValue();
            BigDecimal ahospPromedio = row[6] == null
                    ? BigDecimal.ZERO
                    : new BigDecimal(row[6].toString()).setScale(2, RoundingMode.HALF_UP);

            result.add(new HistoricoVigilanciaSummary(
                    plagaNombre,
                    hospedanteNombre,
                    especieNombre,
                    estadoNombre,
                    municipioNombre,
                    observaciones,
                    ahospPromedio
            ));
        }
        return result;
    }
}
