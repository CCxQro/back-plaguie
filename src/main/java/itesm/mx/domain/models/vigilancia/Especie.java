package itesm.mx.domain.models.vigilancia;

public class Especie {
    private Long especieId;
    private String name;

    public Especie() {
    }

    public Especie(Long especieId, String name) {
        this.especieId = especieId;
        this.name = name;
    }

    public Long getEspecieId() {
        return especieId;
    }

    public void setEspecieId(Long especieId) {
        this.especieId = especieId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}