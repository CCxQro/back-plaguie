package itesm.mx.domain.models.reporte;

public class PrediccionPlaga {
    private String plagaNombre;
    private Integer probabilidad;
    private String periodoEstimado;
    private String nivelRiesgo;
    private String hospedanteAfectado;
    private String justificacion;
    private String productoSugerido;

    public PrediccionPlaga() {
    }

    public PrediccionPlaga(
            String plagaNombre,
            Integer probabilidad,
            String periodoEstimado,
            String nivelRiesgo,
            String hospedanteAfectado,
            String justificacion,
            String productoSugerido
    ) {
        this.plagaNombre = plagaNombre;
        this.probabilidad = probabilidad;
        this.periodoEstimado = periodoEstimado;
        this.nivelRiesgo = nivelRiesgo;
        this.hospedanteAfectado = hospedanteAfectado;
        this.justificacion = justificacion;
        this.productoSugerido = productoSugerido;
    }

    public String getPlagaNombre() {
        return plagaNombre;
    }

    public void setPlagaNombre(String plagaNombre) {
        this.plagaNombre = plagaNombre;
    }

    public Integer getProbabilidad() {
        return probabilidad;
    }

    public void setProbabilidad(Integer probabilidad) {
        this.probabilidad = probabilidad;
    }

    public String getPeriodoEstimado() {
        return periodoEstimado;
    }

    public void setPeriodoEstimado(String periodoEstimado) {
        this.periodoEstimado = periodoEstimado;
    }

    public String getNivelRiesgo() {
        return nivelRiesgo;
    }

    public void setNivelRiesgo(String nivelRiesgo) {
        this.nivelRiesgo = nivelRiesgo;
    }

    public String getHospedanteAfectado() {
        return hospedanteAfectado;
    }

    public void setHospedanteAfectado(String hospedanteAfectado) {
        this.hospedanteAfectado = hospedanteAfectado;
    }

    public String getJustificacion() {
        return justificacion;
    }

    public void setJustificacion(String justificacion) {
        this.justificacion = justificacion;
    }

    public String getProductoSugerido() {
        return productoSugerido;
    }

    public void setProductoSugerido(String productoSugerido) {
        this.productoSugerido = productoSugerido;
    }
}
