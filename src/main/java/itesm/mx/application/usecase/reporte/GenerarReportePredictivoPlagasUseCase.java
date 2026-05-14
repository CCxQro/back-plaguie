package itesm.mx.application.usecase.reporte;

import itesm.mx.domain.models.reporte.HistoricoVigilanciaSummary;
import itesm.mx.domain.models.reporte.ReportePredictivoPlagas;
import itesm.mx.domain.models.reporte.Temporada;
import itesm.mx.domain.repository.reporte.HistoricoVigilanciaRepository;
import itesm.mx.domain.repository.reporte.PrediccionPlagaProvider;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.time.LocalDateTime;
import java.util.List;

@ApplicationScoped
public class GenerarReportePredictivoPlagasUseCase {

    @Inject
    HistoricoVigilanciaRepository historicoVigilanciaRepository;

    @Inject
    PrediccionPlagaProvider prediccionPlagaProvider;

    public ReportePredictivoPlagas execute(String region, String temporada) {
        if (region == null || region.isBlank()) {
            throw new IllegalArgumentException("La region es requerida");
        }
        Temporada temporadaEnum = Temporada.fromString(temporada);
        String regionNormalizada = region.trim();

        List<HistoricoVigilanciaSummary> historico =
                historicoVigilanciaRepository.findResumenPorRegionYTemporada(regionNormalizada, temporadaEnum);

        long totalObservaciones = historico.stream()
                .mapToLong(HistoricoVigilanciaSummary::getObservaciones)
                .sum();

        PrediccionPlagaProvider.PrediccionResult resultado =
                prediccionPlagaProvider.predict(regionNormalizada, temporadaEnum, historico);

        return new ReportePredictivoPlagas(
                regionNormalizada,
                temporadaEnum,
                LocalDateTime.now(),
                totalObservaciones,
                resultado.getResumenEjecutivo(),
                resultado.getPredicciones()
        );
    }
}
