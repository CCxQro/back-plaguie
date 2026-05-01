package itesm.mx.domain.models.location;

public class Municipality {
    private Long municipalityId;
    private String name;

    public Municipality() {}

    public Municipality(Long municipalityId, String name) {
        this.municipalityId = municipalityId;
        this.name = name;
    }

    public Long getMunicipalityId() {
        return municipalityId;
    }

    public void setMunicipalityId(Long municipalityId) {
        this.municipalityId = municipalityId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
