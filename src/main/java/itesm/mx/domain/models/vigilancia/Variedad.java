package itesm.mx.domain.models.vigilancia;

public class Variedad {
    private Long variedadId;
    private String name;

    public Variedad() {
    }

    public Variedad(Long variedadId, String name) {
        this.variedadId = variedadId;
        this.name = name;
    }

    public Long getVariedadId() {
        return variedadId;
    }

    public void setVariedadId(Long variedadId) {
        this.variedadId = variedadId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}