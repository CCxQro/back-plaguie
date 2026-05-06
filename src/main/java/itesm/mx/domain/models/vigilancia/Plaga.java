package itesm.mx.domain.models.vigilancia;

public class Plaga {
    private Long plagaId;
    private String name;

    public Plaga() {
    }

    public Plaga(Long plagaId, String name) {
        this.plagaId = plagaId;
        this.name = name;
    }

    public Long getPlagaId() {
        return plagaId;
    }

    public void setPlagaId(Long plagaId) {
        this.plagaId = plagaId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}