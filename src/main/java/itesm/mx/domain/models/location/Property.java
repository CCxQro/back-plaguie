package itesm.mx.domain.models.location;

public class Property {
    private Long propertyId;
    private String name;

    public Property() {}

    public Property(Long propertyId, String name) {
        this.propertyId = propertyId;
        this.name = name;
    }

    public Long getPropertyId() {
        return propertyId;
    }

    public void setPropertyId(Long propertyId) {
        this.propertyId = propertyId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
