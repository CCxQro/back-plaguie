package itesm.mx.application.usecase.marketplace.color;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import itesm.mx.domain.repository.marketplace.ColorRepository;

@ApplicationScoped
public class DeleteColorUseCase {

    @Inject
    ColorRepository colorRepository;

    @Transactional
    public void execute(Long colorId) {
        if (colorId == null) {
            throw new IllegalArgumentException("Color id is required");
        }
        colorRepository.delete(colorId);
    }
}