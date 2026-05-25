package itesm.mx.domain.models.reporte;

import java.math.BigDecimal;

public class HistoricoVigilanciaSummary {
    private String plagaNombre;
    private String hospedanteNombre;
    private String especieNombre;
    private String estadoNombre;
    private String municipioNombre;
    private long observaciones;
    private BigDecimal ahospPromedio;

    public HistoricoVigilanciaSummary() {
    }

    public HistoricoVigilanciaSummary(
            String plagaNombre,
            String hospedanteNombre,
            String especieNombre,
            String estadoNombre,
            String municipioNombre,
            long observaciones,
            BigDecimal ahospPromedio
    ) {
        this.plagaNombre = plagaNombre;
        this.hospedanteNombre = hospedanteNombre;
        this.especieNombre = especieNombre;
        this.estadoNombre = estadoNombre;
        this.municipioNombre = municipioNombre;
        this.observaciones = observaciones;
        this.ahospPromedio = ahospPromedio;
    }

    public String getPlagaNombre() {
        return plagaNombre;
    }

    public void setPlagaNombre(String plagaNombre) {
        this.plagaNombre = plagaNombre;
    }

    public String getHospedanteNombre() {
        return hospedanteNombre;
    }

    public void setHospedanteNombre(String hospedanteNombre) {
        this.hospedanteNombre = hospedanteNombre;
    }

    public String getEspecieNombre() {
        return especieNombre;
    }

    public void setEspecieNombre(String especieNombre) {
        this.especieNombre = especieNombre;
    }

    public String getEstadoNombre() {
        return estadoNombre;
    }

    public void setEstadoNombre(String estadoNombre) {
        this.estadoNombre = estadoNombre;
    }

    public String getMunicipioNombre() {
        return municipioNombre;
    }

    public void setMunicipioNombre(String municipioNombre) {
        this.municipioNombre = municipioNombre;
    }

    public long getObservaciones() {
        return observaciones;
    }

    public void setObservaciones(long observaciones) {
        this.observaciones = observaciones;
    }

    public BigDecimal getAhospPromedio() {
        return ahospPromedio;
    }

    public void setAhospPromedio(BigDecimal ahospPromedio) {
        this.ahospPromedio = ahospPromedio;
    }
}
