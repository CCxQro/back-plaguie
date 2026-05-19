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
    private List<Hotspot> hotspots;
    private List<String> recomendaciones;

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
        this(region, temporada, generadoEn, observacionesAnalizadas, resumenEjecutivo,
                predicciones, List.of(), List.of());
    }

    public ReportePredictivoPlagas(
            String region,
            Temporada temporada,
            LocalDateTime generadoEn,
            long observacionesAnalizadas,
            String resumenEjecutivo,
            List<PrediccionPlaga> predicciones,
            List<Hotspot> hotspots,
            List<String> recomendaciones
    ) {
        this.region = region;
        this.temporada = temporada;
        this.generadoEn = generadoEn;
        this.observacionesAnalizadas = observacionesAnalizadas;
        this.resumenEjecutivo = resumenEjecutivo;
        this.predicciones = predicciones;
        this.hotspots = hotspots;
        this.recomendaciones = recomendaciones;
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

    public List<Hotspot> getHotspots() {
        return hotspots;
    }

    public void setHotspots(List<Hotspot> hotspots) {
        this.hotspots = hotspots;
    }

    public List<String> getRecomendaciones() {
        return recomendaciones;
    }

    public void setRecomendaciones(List<String> recomendaciones) {
        this.recomendaciones = recomendaciones;
    }
}
