package itesm.mx.domain.repository;

import itesm.mx.domain.models.user.Farmer;

import java.util.List;
import java.util.Optional;

public interface FarmerRepository {
    Farmer save(Farmer farmer);
    Farmer update(Farmer farmer);
    Optional<Farmer> findByFarmerId(Long farmerId);
    Optional<Farmer> findByIdUser(Long userId);
    List<Farmer> findAllFarmers();
}

