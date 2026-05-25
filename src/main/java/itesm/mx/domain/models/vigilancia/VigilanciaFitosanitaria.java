package itesm.mx.domain.models.vigilancia;

import itesm.mx.domain.models.location.Location;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class VigilanciaFitosanitaria {
    private Long vigilanciaFitosanitariaId;
    private SistemaMonitoreo sistemaMonitoreo;
    private ClaveIdentificacionPlaga claveIdentificacionPlaga;
    private BigDecimal latitude;
    private BigDecimal longitude;
    private Location ubicacion;
    private Plaga plaga;
    private Hospedante hospedante;
    private Variedad variedad;
    private Especie especie;
    private BigDecimal ahosp;
    private Long statusId;
    private String statusName;
    private Long validatedByUserId;
    private LocalDateTime validatedAt;

    public VigilanciaFitosanitaria() {
    }

    public VigilanciaFitosanitaria(
            Long vigilanciaFitosanitariaId,
            SistemaMonitoreo sistemaMonitoreo,
            ClaveIdentificacionPlaga claveIdentificacionPlaga,
            BigDecimal latitude,
            BigDecimal longitude,
            Location ubicacion,
            Plaga plaga,
            Hospedante hospedante,
            Variedad variedad,
            Especie especie,
            BigDecimal ahosp
    ) {
        this.vigilanciaFitosanitariaId = vigilanciaFitosanitariaId;
        this.sistemaMonitoreo = sistemaMonitoreo;
        this.claveIdentificacionPlaga = claveIdentificacionPlaga;
        this.latitude = latitude;
        this.longitude = longitude;
        this.ubicacion = ubicacion;
        this.plaga = plaga;
        this.hospedante = hospedante;
        this.variedad = variedad;
        this.especie = especie;
        this.ahosp = ahosp;
    }

    public Long getVigilanciaFitosanitariaId() {
        return vigilanciaFitosanitariaId;
    }

    public void setVigilanciaFitosanitariaId(Long vigilanciaFitosanitariaId) {
        this.vigilanciaFitosanitariaId = vigilanciaFitosanitariaId;
    }

    public SistemaMonitoreo getSistemaMonitoreo() {
        return sistemaMonitoreo;
    }

    public void setSistemaMonitoreo(SistemaMonitoreo sistemaMonitoreo) {
        this.sistemaMonitoreo = sistemaMonitoreo;
    }

    public ClaveIdentificacionPlaga getClaveIdentificacionPlaga() {
        return claveIdentificacionPlaga;
    }

    public void setClaveIdentificacionPlaga(ClaveIdentificacionPlaga claveIdentificacionPlaga) {
        this.claveIdentificacionPlaga = claveIdentificacionPlaga;
    }

    public BigDecimal getLatitude() {
        return latitude;
    }

    public void setLatitude(BigDecimal latitude) {
        this.latitude = latitude;
    }

    public BigDecimal getLongitude() {
        return longitude;
    }

    public void setLongitude(BigDecimal longitude) {
        this.longitude = longitude;
    }

    public Location getUbicacion() {
        return ubicacion;
    }

    public void setUbicacion(Location ubicacion) {
        this.ubicacion = ubicacion;
    }

    public Plaga getPlaga() {
        return plaga;
    }

    public void setPlaga(Plaga plaga) {
        this.plaga = plaga;
    }

    public Hospedante getHospedante() {
        return hospedante;
    }

    public void setHospedante(Hospedante hospedante) {
        this.hospedante = hospedante;
    }

    public Variedad getVariedad() {
        return variedad;
    }

    public void setVariedad(Variedad variedad) {
        this.variedad = variedad;
    }

    public Especie getEspecie() {
        return especie;
    }

    public void setEspecie(Especie especie) {
        this.especie = especie;
    }

    public BigDecimal getAhosp() {
        return ahosp;
    }

    public void setAhosp(BigDecimal ahosp) {
        this.ahosp = ahosp;
    }

    public Long getStatusId() {
        return statusId;
    }

    public void setStatusId(Long statusId) {
        this.statusId = statusId;
    }

    public String getStatusName() {
        return statusName;
    }

    public void setStatusName(String statusName) {
        this.statusName = statusName;
    }

    public Long getValidatedByUserId() {
        return validatedByUserId;
    }

    public void setValidatedByUserId(Long validatedByUserId) {
        this.validatedByUserId = validatedByUserId;
    }

    public LocalDateTime getValidatedAt() {
        return validatedAt;
    }

    public void setValidatedAt(LocalDateTime validatedAt) {
        this.validatedAt = validatedAt;
    }
}