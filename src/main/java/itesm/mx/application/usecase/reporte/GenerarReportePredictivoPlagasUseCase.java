package itesm.mx.application.usecase.reporte;

import itesm.mx.domain.models.reporte.HistoricoVigilanciaSummary;
import itesm.mx.domain.models.reporte.Hotspot;
import itesm.mx.domain.models.reporte.ReportePredictivoPlagas;
import itesm.mx.domain.models.reporte.Temporada;
import itesm.mx.domain.repository.reporte.HistoricoVigilanciaRepository;
import itesm.mx.domain.repository.reporte.PrediccionPlagaProvider;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

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

        List<Hotspot> hotspots = computeHotspots(historico, totalObservaciones);

        return new ReportePredictivoPlagas(
                regionNormalizada,
                temporadaEnum,
                LocalDateTime.now(),
                totalObservaciones,
                resultado.getResumenEjecutivo(),
                resultado.getPredicciones(),
                hotspots,
                resultado.getRecomendaciones()
        );
    }

    /**
     * Aggregate the historical surveillance summary by municipality and rank them
     * to surface "where pest pressure concentrates". Returns the top 5 municipalities,
     * each tagged with a risk level derived from its share of total observations.
     */
    private List<Hotspot> computeHotspots(List<HistoricoVigilanciaSummary> historico, long totalObservaciones) {
        if (historico == null || historico.isEmpty() || totalObservaciones == 0) {
            return List.of();
        }

        // Aggregate observations per (estado, municipio) and collect distinct plagas
        Map<String, long[]> observacionesPorClave = new HashMap<>();
        Map<String, Set<String>> plagasPorClave = new HashMap<>();
        Map<String, String[]> nombresPorClave = new HashMap<>();

        for (HistoricoVigilanciaSummary h : historico) {
            String municipio = h.getMunicipioNombre();
            String estado = h.getEstadoNombre();
            if (municipio == null || municipio.isBlank()) {
                continue;
            }
            String clave = (estado == null ? "" : estado) + "||" + municipio;
            observacionesPorClave.computeIfAbsent(clave, k -> new long[]{0})[0] += h.getObservaciones();
            if (h.getPlagaNombre() != null && !h.getPlagaNombre().isBlank()) {
                plagasPorClave.computeIfAbsent(clave, k -> new HashSet<>()).add(h.getPlagaNombre());
            }
            nombresPorClave.putIfAbsent(clave, new String[]{municipio, estado});
        }

        List<Hotspot> hotspots = new ArrayList<>();
        for (Map.Entry<String, long[]> entry : observacionesPorClave.entrySet()) {
            String clave = entry.getKey();
            long obs = entry.getValue()[0];
            int plagas = plagasPorClave.getOrDefault(clave, Set.of()).size();
            String[] nombres = nombresPorClave.get(clave);
            double share = (double) obs / totalObservaciones;
            String nivel = share >= 0.30 ? "Alto" : share >= 0.15 ? "Medio" : "Bajo";
            hotspots.add(new Hotspot(nombres[0], nombres[1], obs, plagas, nivel));
        }

        hotspots.sort(Comparator.comparingLong(Hotspot::getObservaciones).reversed());
        return hotspots.size() <= 5 ? hotspots : hotspots.subList(0, 5);
    }
}
