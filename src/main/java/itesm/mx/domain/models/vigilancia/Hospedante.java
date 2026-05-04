package itesm.mx.domain.models.vigilancia;

public class Hospedante {
    private Long hospedanteId;
    private String name;

    public Hospedante() {
    }

    public Hospedante(Long hospedanteId, String name) {
        this.hospedanteId = hospedanteId;
        this.name = name;
    }

    public Long getHospedanteId() {
        return hospedanteId;
    }

    public void setHospedanteId(Long hospedanteId) {
        this.hospedanteId = hospedanteId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}