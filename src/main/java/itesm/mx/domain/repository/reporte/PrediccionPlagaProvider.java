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
        private final List<String> recomendaciones;

        public PrediccionResult(String resumenEjecutivo, List<PrediccionPlaga> predicciones) {
            this(resumenEjecutivo, predicciones, List.of());
        }

        public PrediccionResult(String resumenEjecutivo, List<PrediccionPlaga> predicciones, List<String> recomendaciones) {
            this.resumenEjecutivo = resumenEjecutivo;
            this.predicciones = predicciones;
            this.recomendaciones = recomendaciones == null ? List.of() : recomendaciones;
        }

        public String getResumenEjecutivo() {
            return resumenEjecutivo;
        }

        public List<PrediccionPlaga> getPredicciones() {
            return predicciones;
        }

        public List<String> getRecomendaciones() {
            return recomendaciones;
        }
    }
}
