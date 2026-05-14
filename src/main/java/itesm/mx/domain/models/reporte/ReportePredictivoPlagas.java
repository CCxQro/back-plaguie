package itesm.mx.domain.models.reporte;

import java.time.LocalDateTime;
import java.util.List;

public class ReportePredictivoPlagas {
    private String region;
    private Temporada temporada;
    private LocalDateTime generadoEn;
    private long observacionesAnalizadas;
    private String resumenEjecutivo;
    private List<PrediccionPlaga> predicciones;

    public ReportePredictivoPlagas() {
    }

    public ReportePredictivoPlagas(
            String region,
            Temporada temporada,
            LocalDateTime generadoEn,
            long observacionesAnalizadas,
            String resumenEjecutivo,
            List<PrediccionPlaga> predicciones
    ) {
        this.region = region;
        this.temporada = temporada;
        this.generadoEn = generadoEn;
        this.observacionesAnalizadas = observacionesAnalizadas;
        this.resumenEjecutivo = resumenEjecutivo;
        this.predicciones = predicciones;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public Temporada getTemporada() {
        return temporada;
    }

    public void setTemporada(Temporada temporada) {
        this.temporada = temporada;
    }

    public LocalDateTime getGeneradoEn() {
        return generadoEn;
    }

    public void setGeneradoEn(LocalDateTime generadoEn) {
        this.generadoEn = generadoEn;
    }

    public long getObservacionesAnalizadas() {
        return observacionesAnalizadas;
    }

    public void setObservacionesAnalizadas(long observacionesAnalizadas) {
        this.observacionesAnalizadas = observacionesAnalizadas;
    }

    public String getResumenEjecutivo() {
        return resumenEjecutivo;
    }

    public void setResumenEjecutivo(String resumenEjecutivo) {
        this.resumenEjecutivo = resumenEjecutivo;
    }

    public List<PrediccionPlaga> getPredicciones() {
        return predicciones;
    }

    public void setPredicciones(List<PrediccionPlaga> predicciones) {
        this.predicciones = predicciones;
    }
}
