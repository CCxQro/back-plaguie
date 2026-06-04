package itesm.mx.application.dto;

/**
 * Minimal id + name projection for the parcela catalogs
 * (estado_parcela, tipo_cultivo, sistema_riego) used to populate
 * the dropdowns on the register-farm screen.
 */
public class ParcelaCatalogItemDto {
    public Long id;
    public String nombre;

    public ParcelaCatalogItemDto() {
    }

    public ParcelaCatalogItemDto(Long id, String nombre) {
        this.id = id;
        this.nombre = nombre;
    }
}
