package itesm.mx.application.usecase.parcela;

import itesm.mx.application.dto.GetLocationResponseDto;
import itesm.mx.application.dto.ParcelaResponseDto;
import itesm.mx.application.dto.RegisterParcelaDto;
import itesm.mx.application.mapper.location.LocationDtoMapper;
import itesm.mx.application.mapper.parcela.ParcelaAppMapper;
import itesm.mx.application.usecase.location.location.RegisterLocationUseCase;
import itesm.mx.domain.models.location.Location;
import itesm.mx.domain.models.parcela.EstadoParcela;
import itesm.mx.domain.models.parcela.Parcela;
import itesm.mx.domain.models.parcela.SistemaRiego;
import itesm.mx.domain.models.parcela.TipoCultivo;
import itesm.mx.domain.models.user.Farmer;
import itesm.mx.domain.repository.parcela.ParcelaCatalogRepository;
import itesm.mx.domain.repository.parcela.ParcelaRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

@ApplicationScoped
public class RegisterParcelaUseCase {

    @Inject
    RegisterLocationUseCase registerLocationUseCase;

    @Inject
    ParcelaRepository parcelaRepository;

    @Inject
    ParcelaCatalogRepository parcelaCatalogRepository;

    @Transactional
    public ParcelaResponseDto execute(Long farmerId, RegisterParcelaDto dto) {
        validateCatalogIds(dto);

        // Resolve (or create) the location first; this use case owns all the
        // state/municipality/locality/property resolution.
        GetLocationResponseDto location = registerLocationUseCase.execute(
                LocationDtoMapper.toLocationData(dto.ubicacion));

        Parcela parcela = buildParcela(farmerId, dto, location.locationId);
        Parcela saved = parcelaRepository.save(parcela);
        return ParcelaAppMapper.toResponseDto(saved);
    }

    private void validateCatalogIds(RegisterParcelaDto dto) {
        if (!parcelaCatalogRepository.estadoParcelaExists(dto.estadoParcelaId)) {
            throw new IllegalArgumentException("El estado de la parcela indicado no existe");
        }
        if (!parcelaCatalogRepository.tipoCultivoExists(dto.tipoCultivoId)) {
            throw new IllegalArgumentException("El tipo de cultivo indicado no existe");
        }
        if (!parcelaCatalogRepository.sistemaRiegoExists(dto.sistemaRiegoId)) {
            throw new IllegalArgumentException("El sistema de riego indicado no existe");
        }
    }

    private Parcela buildParcela(Long farmerId, RegisterParcelaDto dto, Long locationId) {
        Farmer farmer = new Farmer();
        farmer.setFarmerId(farmerId);

        Location location = new Location();
        location.setLocationId(locationId);

        return new Parcela(
                null,
                dto.nombreParcela,
                dto.tamanoHectareas,
                dto.fechaSiembra,
                dto.fechaCosecha,
                dto.phSuelo,
                farmer,
                location,
                new EstadoParcela(dto.estadoParcelaId, null),
                new TipoCultivo(dto.tipoCultivoId, null, null, null),
                new SistemaRiego(dto.sistemaRiegoId, null),
                true
        );
    }
}
