package itesm.mx.domain.models.vigilancia;

public class ClaveIdentificacionPlaga {
    private Long cidId;
    private String name;

    public ClaveIdentificacionPlaga() {
    }

    public ClaveIdentificacionPlaga(Long cidId, String name) {
        this.cidId = cidId;
        this.name = name;
    }

    public Long getCidId() {
        return cidId;
    }

    public void setCidId(Long cidId) {
        this.cidId = cidId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}