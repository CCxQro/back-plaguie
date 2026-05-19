package itesm.mx.domain.repository.reporte;

import itesm.mx.domain.models.reporte.HistoricoVigilanciaSummary;
import itesm.mx.domain.models.reporte.Temporada;

import java.util.List;

public interface HistoricoVigilanciaRepository {
    List<HistoricoVigilanciaSummary> findResumenPorRegionYTemporada(String region, Temporada temporada);
}
