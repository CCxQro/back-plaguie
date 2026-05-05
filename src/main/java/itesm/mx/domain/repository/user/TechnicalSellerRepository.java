package itesm.mx.domain.repository.user;

import itesm.mx.domain.models.user.TechnicalSeller;

import java.util.List;
import java.util.Optional;

public interface TechnicalSellerRepository {
    TechnicalSeller save(TechnicalSeller technicalSeller);
    TechnicalSeller update(TechnicalSeller technicalSeller);
    Optional<TechnicalSeller> findByTechnicalSellerId(Long technicalSellerId);
    Optional<TechnicalSeller> findByIdUser(Long userId);
    List<TechnicalSeller> findAllTechnicalSellers();
}

