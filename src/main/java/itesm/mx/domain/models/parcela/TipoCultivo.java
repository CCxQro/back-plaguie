package itesm.mx.domain.models.parcela;

import java.time.LocalDate;

public class TipoCultivo {
    private Long tipoCultivoId;
    private String nombre;
    private LocalDate fechaSiembra;
    private LocalDate fechaCosecha;

    public TipoCultivo() {
    }

    public TipoCultivo(Long tipoCultivoId, String nombre, LocalDate fechaSiembra, LocalDate fechaCosecha) {
        this.tipoCultivoId = tipoCultivoId;
        this.nombre = nombre;
        this.fechaSiembra = fechaSiembra;
        this.fechaCosecha = fechaCosecha;
    }

    public Long getTipoCultivoId() {
        return tipoCultivoId;
    }

    public void setTipoCultivoId(Long tipoCultivoId) {
        this.tipoCultivoId = tipoCultivoId;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
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
}
