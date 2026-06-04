package itesm.mx.application.usecase.parcela;

import itesm.mx.application.dto.ParcelaCatalogItemDto;
import itesm.mx.domain.repository.parcela.ParcelaCatalogRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.List;

/**
 * Read-only catalogs (id + nombre) for the register-farm form:
 * estado de parcela, tipo de cultivo and sistema de riego.
 */
@ApplicationScoped
public class GetParcelaCatalogsUseCase {

    @Inject
    ParcelaCatalogRepository parcelaCatalogRepository;

    public List<ParcelaCatalogItemDto> getEstadosParcela() {
        return parcelaCatalogRepository.findAllEstadosParcela()
                .stream()
                .map(e -> new ParcelaCatalogItemDto(e.getEstadoParcelaId(), e.getNombre()))
                .toList();
    }

    public List<ParcelaCatalogItemDto> getTiposCultivo() {
        return parcelaCatalogRepository.findAllTiposCultivo()
                .stream()
                .map(t -> new ParcelaCatalogItemDto(t.getTipoCultivoId(), t.getNombre()))
                .toList();
    }

    public List<ParcelaCatalogItemDto> getSistemasRiego() {
        return parcelaCatalogRepository.findAllSistemasRiego()
                .stream()
                .map(s -> new ParcelaCatalogItemDto(s.getSistemaRiegoId(), s.getNombre()))
                .toList();
    }
}
