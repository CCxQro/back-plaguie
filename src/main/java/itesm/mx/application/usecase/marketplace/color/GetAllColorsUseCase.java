package itesm.mx.application.usecase.marketplace.color;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import itesm.mx.domain.models.marketplace.Color;
import itesm.mx.domain.repository.marketplace.ColorRepository;

import java.util.List;

@ApplicationScoped
public class GetAllColorsUseCase {

    @Inject
    ColorRepository colorRepository;

    public List<Color> execute() {
        return colorRepository.findAllColors();
    }
}