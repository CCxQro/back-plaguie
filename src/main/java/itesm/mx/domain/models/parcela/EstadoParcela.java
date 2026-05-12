package itesm.mx.domain.models.parcela;

public class EstadoParcela {
    private Long estadoParcelaId;
    private String nombre;

    public EstadoParcela() {
    }

    public EstadoParcela(Long estadoParcelaId, String nombre) {
        this.estadoParcelaId = estadoParcelaId;
        this.nombre = nombre;
    }

    public Long getEstadoParcelaId() {
        return estadoParcelaId;
    }

    public void setEstadoParcelaId(Long estadoParcelaId) {
        this.estadoParcelaId = estadoParcelaId;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }
}
