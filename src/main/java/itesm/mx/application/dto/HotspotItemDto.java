package itesm.mx.application.dto;

public class HotspotItemDto {
    public String municipio;
    public String estado;
    public long observaciones;
    public int plagasDistintas;
    public String nivelRiesgo;

    public HotspotItemDto() {
    }

    public HotspotItemDto(
            String municipio,
            String estado,
            long observaciones,
            int plagasDistintas,
            String nivelRiesgo
    ) {
        this.municipio = municipio;
        this.estado = estado;
        this.observaciones = observaciones;
        this.plagasDistintas = plagasDistintas;
        this.nivelRiesgo = nivelRiesgo;
    }
}
