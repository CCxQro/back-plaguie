package itesm.mx.application.dto;

import java.time.LocalDate;

public class ClientParcelaSummaryDto {
    public Long parcelaId;
    public String nombreParcela;
    public Double tamanoHectareas;
    public String tipoCultivo;
    public String estadoParcela;
    public String sistemaRiego;
    public Double phSuelo;
    public LocalDate fechaSiembra;
    public LocalDate fechaCosecha;
    public Boolean isActive;

    public ClientParcelaSummaryDto() {}

    public ClientParcelaSummaryDto(Long parcelaId, String nombreParcela, Double tamanoHectareas,
                                    String tipoCultivo, String estadoParcela, String sistemaRiego,
                                    Double phSuelo, LocalDate fechaSiembra, LocalDate fechaCosecha,
                                    Boolean isActive) {
        this.parcelaId = parcelaId;
        this.nombreParcela = nombreParcela;
        this.tamanoHectareas = tamanoHectareas;
        this.tipoCultivo = tipoCultivo;
        this.estadoParcela = estadoParcela;
        this.sistemaRiego = sistemaRiego;
        this.phSuelo = phSuelo;
        this.fechaSiembra = fechaSiembra;
        this.fechaCosecha = fechaCosecha;
        this.isActive = isActive;
    }
}
