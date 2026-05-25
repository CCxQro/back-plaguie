package itesm.mx.domain.models.location;

import org.locationtech.jts.geom.Point;

public class Location {
    private Long locationId;
    private Point coordinates;
    private State state;
    private Municipality municipality;
    private Locality locality;
    private Property property;

    public Location() {
    }

    public Location(
            Long locationId,
            Point coordinates,
            State state,
            Municipality municipality,
            Locality locality,
            Property property
    ) {
        this.locationId = locationId;
        this.coordinates = coordinates;
        this.state = state;
        this.municipality = municipality;
        this.locality = locality;
        this.property = property;
    }

    public Long getLocationId() {
        return locationId;
    }

    public void setLocationId(Long locationId) {
        this.locationId = locationId;
    }

    public Point getCoordinates() {
        return coordinates;
    }

    public void setCoordinates(Point coordinates) {
        this.coordinates = coordinates;
    }

    public State getState() {
        return state;
    }

    public void setState(State state) {
        this.state = state;
    }

    public Municipality getMunicipality() {
        return municipality;
    }

    public void setMunicipality(Municipality municipality) {
        this.municipality = municipality;
    }

    public Locality getLocality() {
        return locality;
    }

    public void setLocality(Locality locality) {
        this.locality = locality;
    }

    public Property getProperty() {
        return property;
    }

    public void setProperty(Property property) {
        this.property = property;
    }
}
