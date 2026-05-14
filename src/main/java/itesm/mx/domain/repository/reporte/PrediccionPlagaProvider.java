package itesm.mx.domain.repository.reporte;

import itesm.mx.domain.models.reporte.HistoricoVigilanciaSummary;
import itesm.mx.domain.models.reporte.PrediccionPlaga;
import itesm.mx.domain.models.reporte.Temporada;

import java.util.List;

public interface PrediccionPlagaProvider {
    PrediccionResult predict(String region, Temporada temporada, List<HistoricoVigilanciaSummary> historico);

    class PrediccionResult {
        private final String resumenEjecutivo;
        private final List<PrediccionPlaga> predicciones;

        public PrediccionResult(String resumenEjecutivo, List<PrediccionPlaga> predicciones) {
            this.resumenEjecutivo = resumenEjecutivo;
            this.predicciones = predicciones;
        }

        public String getResumenEjecutivo() {
            return resumenEjecutivo;
        }

        public List<PrediccionPlaga> getPredicciones() {
            return predicciones;
        }
    }
}
