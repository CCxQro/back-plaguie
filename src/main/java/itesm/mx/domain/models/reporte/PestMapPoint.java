package itesm.mx.domain.models.reporte;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * A single validated phytosanitary surveillance observation, projected for the
 * interactive pest map (HU-27). Carries its coordinates, pest/host/species and
 * the resolved state/municipality so the client can plot and aggregate by zone.
 */
public class PestMapPoint {

    private Long vigilanciaId;
    private BigDecimal latitude;
    private BigDecimal longitude;
    private String plagaNombre;
    private String hospedanteNombre;
    private String especieNombre;
    private String estadoNombre;
    private String municipioNombre;
    private BigDecimal ahosp;
    private LocalDateTime validatedAt;

    public PestMapPoint() {
    }

    public PestMapPoint(Long vigilanciaId, BigDecimal latitude, BigDecimal longitude,
                        String plagaNombre, String hospedanteNombre, String especieNombre,
                        String estadoNombre, String municipioNombre, BigDecimal ahosp,
                        LocalDateTime validatedAt) {
        this.vigilanciaId = vigilanciaId;
        this.latitude = latitude;
        this.longitude = longitude;
        this.plagaNombre = plagaNombre;
        this.hospedanteNombre = hospedanteNombre;
        this.especieNombre = especieNombre;
        this.estadoNombre = estadoNombre;
        this.municipioNombre = municipioNombre;
        this.ahosp = ahosp;
        this.validatedAt = validatedAt;
    }

    public Long getVigilanciaId() { return vigilanciaId; }
    public void setVigilanciaId(Long vigilanciaId) { this.vigilanciaId = vigilanciaId; }

    public BigDecimal getLatitude() { return latitude; }
    public void setLatitude(BigDecimal latitude) { this.latitude = latitude; }

    public BigDecimal getLongitude() { return longitude; }
    public void setLongitude(BigDecimal longitude) { this.longitude = longitude; }

    public String getPlagaNombre() { return plagaNombre; }
    public void setPlagaNombre(String plagaNombre) { this.plagaNombre = plagaNombre; }

    public String getHospedanteNombre() { return hospedanteNombre; }
    public void setHospedanteNombre(String hospedanteNombre) { this.hospedanteNombre = hospedanteNombre; }

    public String getEspecieNombre() { return especieNombre; }
    public void setEspecieNombre(String especieNombre) { this.especieNombre = especieNombre; }

    public String getEstadoNombre() { return estadoNombre; }
    public void setEstadoNombre(String estadoNombre) { this.estadoNombre = estadoNombre; }

    public String getMunicipioNombre() { return municipioNombre; }
    public void setMunicipioNombre(String municipioNombre) { this.municipioNombre = municipioNombre; }

    public BigDecimal getAhosp() { return ahosp; }
    public void setAhosp(BigDecimal ahosp) { this.ahosp = ahosp; }

    public LocalDateTime getValidatedAt() { return validatedAt; }
    public void setValidatedAt(LocalDateTime validatedAt) { this.validatedAt = validatedAt; }
}
