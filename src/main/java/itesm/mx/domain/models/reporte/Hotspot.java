package itesm.mx.domain.models.reporte;

public class Hotspot {
    private String municipio;
    private String estado;
    private long observaciones;
    private int plagasDistintas;
    private String nivelRiesgo;

    public Hotspot() {
    }

    public Hotspot(String municipio, String estado, long observaciones, int plagasDistintas, String nivelRiesgo) {
        this.municipio = municipio;
        this.estado = estado;
        this.observaciones = observaciones;
        this.plagasDistintas = plagasDistintas;
        this.nivelRiesgo = nivelRiesgo;
    }

    public String getMunicipio() {
        return municipio;
    }

    public void setMunicipio(String municipio) {
        this.municipio = municipio;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public long getObservaciones() {
        return observaciones;
    }

    public void setObservaciones(long observaciones) {
        this.observaciones = observaciones;
    }

    public int getPlagasDistintas() {
        return plagasDistintas;
    }

    public void setPlagasDistintas(int plagasDistintas) {
        this.plagasDistintas = plagasDistintas;
    }

    public String getNivelRiesgo() {
        return nivelRiesgo;
    }

    public void setNivelRiesgo(String nivelRiesgo) {
        this.nivelRiesgo = nivelRiesgo;
    }
}
