package itesm.mx.application.dto;

import java.util.List;

/**
 * Detalle completo de una parcela.
 *
 * historial: lista de eventos históricos de la parcela.
 *   Requiere una tabla dedicada (p.ej. Historial_Parcela) que aún no existe
 *   en el esquema; por ahora siempre retorna lista vacía.
 *   TODO: implementar cuando se cree la tabla Historial_Parcela.
 */
public class ParcelaDetailDto {

    // Resumen base (id, nombre, tamano, tipoCultivo, estadoParcela, isActive)
    public ParcelaResponseDto parcela;

    // Porcentaje de salud derivado de las sugerencias activas (0–100)
    public Double saludPorcentaje;

    /** @deprecated use saludPorcentaje */
    public Double healthPercentage;

    // Coordenadas de la ubicación de la parcela
    public Double latitud;
    public Double longitud;

    // Datos agronómicos extendidos
    public String sistemaRiego;
    public Double phSuelo;
    public String fechaSiembra;
    public String fechaCosecha;

    // Sugerencias agronómicas activas (vacío si no hay alertas)
    public List<String> sugerencias;

    /** @deprecated use sugerencias */
    public List<String> suggestions;

    /**
     * Historial de eventos de la parcela.
     * TODO: implementar cuando exista la tabla Historial_Parcela.
     *       Por ahora siempre retorna lista vacía.
     */
    public List<ParcelaHistorialItemDto> historial;
}
