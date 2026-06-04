package itesm.mx.domain.models.recomendacion;

/**
 * LLM-generated product recommendation for a farmer, personalized to their
 * parcelas, crop types, and regional pest history. (HU-24 CA-02)
 */
public class RecomendacionProducto {

    private String productoSugerido;
    private String plagaRelacionada;
    private String cultivoAfectado;
    /** Bajo | Medio | Alto | Critico */
    private String nivelUrgencia;
    private String razonamiento;
    private String dosisEstimada;

    public RecomendacionProducto() {
    }

    public RecomendacionProducto(String productoSugerido, String plagaRelacionada,
                                  String cultivoAfectado, String nivelUrgencia,
                                  String razonamiento, String dosisEstimada) {
        this.productoSugerido = productoSugerido;
        this.plagaRelacionada = plagaRelacionada;
        this.cultivoAfectado = cultivoAfectado;
        this.nivelUrgencia = nivelUrgencia;
        this.razonamiento = razonamiento;
        this.dosisEstimada = dosisEstimada;
    }

    public String getProductoSugerido() { return productoSugerido; }
    public void setProductoSugerido(String productoSugerido) { this.productoSugerido = productoSugerido; }

    public String getPlagaRelacionada() { return plagaRelacionada; }
    public void setPlagaRelacionada(String plagaRelacionada) { this.plagaRelacionada = plagaRelacionada; }

    public String getCultivoAfectado() { return cultivoAfectado; }
    public void setCultivoAfectado(String cultivoAfectado) { this.cultivoAfectado = cultivoAfectado; }

    public String getNivelUrgencia() { return nivelUrgencia; }
    public void setNivelUrgencia(String nivelUrgencia) { this.nivelUrgencia = nivelUrgencia; }

    public String getRazonamiento() { return razonamiento; }
    public void setRazonamiento(String razonamiento) { this.razonamiento = razonamiento; }

    public String getDosisEstimada() { return dosisEstimada; }
    public void setDosisEstimada(String dosisEstimada) { this.dosisEstimada = dosisEstimada; }
}
