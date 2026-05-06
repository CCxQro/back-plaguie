package itesm.mx.application.usecase.marketplace.color;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import itesm.mx.domain.models.marketplace.Color;
import itesm.mx.domain.repository.marketplace.ColorRepository;

@ApplicationScoped
public class RegisterColorUseCase {

    @Inject
    ColorRepository colorRepository;

    @Transactional
    public Color execute(Color color) {
        if (color == null || color.getName() == null || color.getName().isBlank()) {
            throw new IllegalArgumentException("Color name is required");
        }
        if (color.getHexa() == null || color.getHexa().isBlank()) {
            throw new IllegalArgumentException("Color hex value is required");
        }
        return colorRepository.save(color);
    }
}