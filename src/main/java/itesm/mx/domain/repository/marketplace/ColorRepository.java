package itesm.mx.domain.repository.marketplace;

import itesm.mx.domain.models.marketplace.Color;

import java.util.List;

public interface ColorRepository {
    Color save(Color color);
    List<Color> findAllColors();
    void delete(Long colorId);
}