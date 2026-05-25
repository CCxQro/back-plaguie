package itesm.mx.domain.models.parcela;

import itesm.mx.domain.models.location.Location;
import itesm.mx.domain.models.user.Farmer;

import java.time.LocalDate;

public class Parcela {
    private Long parcelaId;
    private String nombreParcela;
    private Double tamanoHectareas;
    private LocalDate fechaSiembra;
    private LocalDate fechaCosecha;
    private Double phSuelo;
    private Farmer farmer;
    private Location location;
    private EstadoParcela estadoParcela;
    private TipoCultivo tipoCultivo;
    private SistemaRiego sistemaRiego;
    private Boolean isActive;

    public Parcela() {
    }

    public Parcela(
            Long parcelaId,
            String nombreParcela,
            Double tamanoHectareas,
            LocalDate fechaSiembra,
            LocalDate fechaCosecha,
            Double phSuelo,
            Farmer farmer,
            Location location,
            EstadoParcela estadoParcela,
            TipoCultivo tipoCultivo,
            SistemaRiego sistemaRiego,
            Boolean isActive
    ) {
        this.parcelaId = parcelaId;
        this.nombreParcela = nombreParcela;
        this.tamanoHectareas = tamanoHectareas;
        this.fechaSiembra = fechaSiembra;
        this.fechaCosecha = fechaCosecha;
        this.phSuelo = phSuelo;
        this.farmer = farmer;
        this.location = location;
        this.estadoParcela = estadoParcela;
        this.tipoCultivo = tipoCultivo;
        this.sistemaRiego = sistemaRiego;
        this.isActive = isActive;
    }

    public Long getParcelaId() {
        return parcelaId;
    }

    public void setParcelaId(Long parcelaId) {
        this.parcelaId = parcelaId;
    }

    public String getNombreParcela() {
        return nombreParcela;
    }

    public void setNombreParcela(String nombreParcela) {
        this.nombreParcela = nombreParcela;
    }

    public Double getTamanoHectareas() {
        return tamanoHectareas;
    }

    public void setTamanoHectareas(Double tamanoHectareas) {
        this.tamanoHectareas = tamanoHectareas;
    }

    public LocalDate getFechaSiembra() {
        return fechaSiembra;
    }

    public void setFechaSiembra(LocalDate fechaSiembra) {
        this.fechaSiembra = fechaSiembra;
    }

    public LocalDate getFechaCosecha() {
        return fechaCosecha;
    }

    public void setFechaCosecha(LocalDate fechaCosecha) {
        this.fechaCosecha = fechaCosecha;
    }

    public Double getPhSuelo() {
        return phSuelo;
    }

    public void setPhSuelo(Double phSuelo) {
        this.phSuelo = phSuelo;
    }

    public Farmer getFarmer() {
        return farmer;
    }

    public void setFarmer(Farmer farmer) {
        this.farmer = farmer;
    }

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    public EstadoParcela getEstadoParcela() {
        return estadoParcela;
    }

    public void setEstadoParcela(EstadoParcela estadoParcela) {
        this.estadoParcela = estadoParcela;
    }

    public TipoCultivo getTipoCultivo() {
        return tipoCultivo;
    }

    public void setTipoCultivo(TipoCultivo tipoCultivo) {
        this.tipoCultivo = tipoCultivo;
    }

    public SistemaRiego getSistemaRiego() {
        return sistemaRiego;
    }

    public void setSistemaRiego(SistemaRiego sistemaRiego) {
        this.sistemaRiego = sistemaRiego;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }
}
