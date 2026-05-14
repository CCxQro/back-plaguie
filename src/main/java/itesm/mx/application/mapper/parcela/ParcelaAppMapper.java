package itesm.mx.application.mapper.parcela;

import itesm.mx.application.dto.ParcelaResponseDto;
import itesm.mx.domain.models.parcela.EstadoParcela;
import itesm.mx.domain.models.parcela.Parcela;
import itesm.mx.domain.models.parcela.TipoCultivo;

public final class ParcelaAppMapper {

    private ParcelaAppMapper() {
    }

    public static ParcelaResponseDto toResponseDto(Parcela parcela) {
        String tipoCultivoNombre = null;
        TipoCultivo tipoCultivo = parcela.getTipoCultivo();
        if (tipoCultivo != null) {
            tipoCultivoNombre = tipoCultivo.getNombre();
        }

        String estadoParcelaNombre = null;
        EstadoParcela estadoParcela = parcela.getEstadoParcela();
        if (estadoParcela != null) {
            estadoParcelaNombre = estadoParcela.getNombre();
        }

        return new ParcelaResponseDto(
                parcela.getParcelaId(),
                parcela.getNombreParcela(),
                parcela.getTamanoHectareas(),
                tipoCultivoNombre,
                estadoParcelaNombre
        );
    }
}
