package itesm.mx.infrastructure.mapper.location;

import itesm.mx.domain.models.location.Property;
import itesm.mx.infrastructure.persistence.entity.location.PropertyEntity;

public class PropertyMapper {
    public static PropertyEntity toEntity(Property property) {
        PropertyEntity entity = new PropertyEntity();
        entity.propertyId = property.getPropertyId();
        entity.name = property.getName();
        return entity;
    }

    public static Property toDomain(PropertyEntity entity) {
        Property property = new Property();
        property.setPropertyId(entity.propertyId);
        property.setName(entity.name);
        return property;
    }
}
