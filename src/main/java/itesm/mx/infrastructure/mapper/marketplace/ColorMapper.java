package itesm.mx.infrastructure.mapper.marketplace;

import itesm.mx.domain.models.marketplace.Color;
import itesm.mx.infrastructure.persistence.entity.marketplace.ColorEntity;

public class ColorMapper {
    public static ColorEntity toEntity(Color color) {
        ColorEntity entity = new ColorEntity();
        entity.colorId = color.getColorId();
        entity.name = color.getName();
        entity.hexa = color.getHexa();
        return entity;
    }

    public static Color toDomain(ColorEntity entity) {
        Color color = new Color();
        color.setColorId(entity.colorId);
        color.setName(entity.name);
        color.setHexa(entity.hexa);
        return color;
    }
}