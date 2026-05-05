package itesm.mx.domain.models.location;

public class Locality {
    private Long localityId;
    private String name;

    public Locality() {}

    public Locality(Long localityId, String name) {
        this.localityId = localityId;
        this.name = name;
    }

    public Long getLocalityId() {
        return localityId;
    }

    public void setLocalityId(Long localityId) {
        this.localityId = localityId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
