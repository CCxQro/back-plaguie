package itesm.mx.application.usecase.recomendacion;

import itesm.mx.application.dto.GetRecomendacionesPersonalizadasResponseDto;
import itesm.mx.application.dto.GetRecomendacionesPersonalizadasResponseDto.RecomendacionProductoDto;
import itesm.mx.application.dto.ParcelaResponseDto;
import itesm.mx.application.usecase.marketplace.product.GetAllProductsUseCase;
import itesm.mx.application.usecase.parcela.GetParcelasByFarmerUseCase;
import itesm.mx.domain.models.marketplace.Product;
import itesm.mx.domain.models.reporte.HistoricoVigilanciaSummary;
import itesm.mx.domain.models.reporte.Temporada;
import itesm.mx.domain.models.user.Farmer;
import itesm.mx.domain.models.user.User;
import itesm.mx.domain.repository.reporte.HistoricoVigilanciaRepository;
import itesm.mx.domain.repository.user.FarmerRepository;
import itesm.mx.domain.repository.user.UserRepository;
import itesm.mx.infrastructure.gemini.GeminiProductRecommendationProvider;
import itesm.mx.infrastructure.gemini.GeminiProductRecommendationProvider.RecomendacionResult;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Generates personalized product recommendations for an authenticated farmer.
 *
 * <ol>
 *   <li>Resolves the farmer record from their user id.</li>
 *   <li>Fetches the farmer's active parcelas (crop types, sizes).</li>
 *   <li>Resolves the farmer's state/region from their user location.</li>
 *   <li>Derives the current season from the system date.</li>
 *   <li>Queries the historical pest surveillance for that region + season.</li>
 *   <li>Calls Gemini (or the local fallback) to produce recommendations.</li>
 * </ol>
 *
 * HU-24 CA-02: personalized product recommendations via LLM.
 */
@ApplicationScoped
public class GenerarRecomendacionesFarmerUseCase {

    @Inject
    FarmerRepository farmerRepository;

    @Inject
    UserRepository userRepository;

    @Inject
    GetParcelasByFarmerUseCase getParcelasByFarmerUseCase;

    @Inject
    HistoricoVigilanciaRepository historicoVigilanciaRepository;

    @Inject
    GetAllProductsUseCase getAllProductsUseCase;

    @Inject
    GeminiProductRecommendationProvider geminiProvider;

    public GetRecomendacionesPersonalizadasResponseDto execute(Long userId) {
        if (userId == null || userId <= 0) {
            throw new IllegalArgumentException("El ID de usuario no es válido");
        }

        // 1. Resolve farmer
        Farmer farmer = farmerRepository.findByIdUser(userId)
                .orElseThrow(() -> new IllegalStateException(
                        "Agricultor no encontrado para el usuario con id: " + userId));

        // 2. Fetch parcelas
        List<ParcelaResponseDto> parcelas = getParcelasByFarmerUseCase.execute(farmer.getFarmerId())
                .stream()
                .filter(p -> Boolean.TRUE.equals(p.isActive))
                .collect(Collectors.toList());

        // 3. Resolve region (state name from user location)
        String region = resolveRegion(userId);

        // 4. Current season
        Temporada temporada = currentSeason();

        // 5. Historical pest data for the region + season
        List<HistoricoVigilanciaSummary> historico = region != null
                ? historicoVigilanciaRepository.findResumenPorRegionYTemporada(region, temporada)
                : List.of();

        // 6. Available marketplace products → so the LLM recommends real, buyable
        //    products from the catalog (and the app can offer "add to cart").
        List<Product> availableProducts = getAllProductsUseCase.execute().stream()
                .filter(p -> !Boolean.FALSE.equals(p.getIsActive()))
                .filter(p -> p.getStock() != null && p.getStock() > 0)
                .collect(Collectors.toList());

        // 7. LLM call (or fallback) grounded on the catalog
        RecomendacionResult result = geminiProvider.recommend(
                region != null ? region : "México",
                temporada,
                parcelas,
                historico,
                availableProducts
        );

        // 8. Map to DTO
        List<RecomendacionProductoDto> recDtos = result.getRecomendaciones().stream()
                .map(r -> new RecomendacionProductoDto(
                        r.getProductoSugerido(),
                        r.getPlagaRelacionada(),
                        r.getCultivoAfectado(),
                        r.getNivelUrgencia(),
                        r.getRazonamiento(),
                        r.getDosisEstimada()))
                .collect(Collectors.toList());

        List<String> parcelasAnalizadas = parcelas.stream()
                .map(p -> p.nombre)
                .collect(Collectors.toList());

        return new GetRecomendacionesPersonalizadasResponseDto(
                result.getResumenSituacion(),
                recDtos,
                parcelasAnalizadas,
                region,
                temporada.getDisplayName()
        );
    }

    // ─── Helpers ─────────────────────────────────────────────────────────────

    private String resolveRegion(Long userId) {
        return userRepository.findUserById(userId)
                .map(User::getLocation)
                .map(loc -> {
                    if (loc == null) return null;
                    // Prefer state name; fall back to municipality
                    if (loc.getState() != null && loc.getState().getName() != null
                            && !loc.getState().getName().isBlank()) {
                        return loc.getState().getName();
                    }
                    if (loc.getMunicipality() != null && loc.getMunicipality().getName() != null) {
                        return loc.getMunicipality().getName();
                    }
                    return null;
                })
                .orElse(null);
    }

    /**
     * Derives the current {@link Temporada} from the system month.
     * Mexico's seasons: spring Mar-May, summer Jun-Aug, autumn Sep-Nov, winter Dec-Feb.
     */
    static Temporada currentSeason() {
        int month = LocalDate.now().getMonthValue();
        for (Temporada t : Temporada.values()) {
            if (t.getMeses().contains(month)) return t;
        }
        return Temporada.INVIERNO;
    }
}
