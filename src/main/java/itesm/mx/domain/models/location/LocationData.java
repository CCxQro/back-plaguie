package itesm.mx.domain.models.location;

import org.locationtech.jts.geom.Point;

public class LocationData {
    private Point coordinates;
    private String stateName;
    private String municipalityName;
    private String localityName;
    private String propertyName;

    public LocationData() {
    }

    public LocationData(
            Point coordinates,
            String stateName,
            String municipalityName,
            String localityName,
            String propertyName
    ) {
        this.coordinates = coordinates;
        this.stateName = stateName;
        this.municipalityName = municipalityName;
        this.localityName = localityName;
        this.propertyName = propertyName;
    }

    public Point getCoordinates() {
        return coordinates;
    }

    public void setCoordinates(Point coordinates) {
        this.coordinates = coordinates;
    }

    public String getStateName() {
        return stateName;
    }

    public void setStateName(String stateName) {
        this.stateName = stateName;
    }

    public String getMunicipalityName() {
        return municipalityName;
    }

    public void setMunicipalityName(String municipalityName) {
        this.municipalityName = municipalityName;
    }

    public String getLocalityName() {
        return localityName;
    }

    public void setLocalityName(String localityName) {
        this.localityName = localityName;
    }

    public String getPropertyName() {
        return propertyName;
    }

    public void setPropertyName(String propertyName) {
        this.propertyName = propertyName;
    }
}
