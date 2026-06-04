package itesm.mx.application.dto;

/**
 * Elemento del historial de eventos de una parcela.
 *
 * TODO: poblar desde la tabla Historial_Parcela cuando sea creada.
 *       Por ahora el endpoint siempre retorna lista vacía.
 */
public class ParcelaHistorialItemDto {

    public Long historialId;
    public String fecha;
    public String descripcion;
    public String tipo;

    public ParcelaHistorialItemDto() {
    }

    public ParcelaHistorialItemDto(Long historialId, String fecha, String descripcion, String tipo) {
        this.historialId = historialId;
        this.fecha = fecha;
        this.descripcion = descripcion;
        this.tipo = tipo;
    }
}
