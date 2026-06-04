package itesm.mx.application.usecase;

import itesm.mx.application.dto.GetAlertaResponseDto;
import itesm.mx.application.usecase.alerta.GetAlertasCercanasParcelaUseCase;
import itesm.mx.domain.models.alerta.Alerta;
import itesm.mx.domain.repository.alerta.AlertaRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetAlertasCercanasParcelaUseCaseTest {

    @Mock
    private AlertaRepository alertaRepository;

    @InjectMocks
    private GetAlertasCercanasParcelaUseCase useCase;

    // Parcela de referencia: Guadalajara, Jalisco (20.66, -103.35)
    private static final double LAT = 20.66;
    private static final double LON = -103.35;

    private Alerta alertaAt(long id, double lat, double lon) {
        Alerta a = new Alerta();
        a.setAlertaId(id);
        a.setTitulo("Alerta " + id);
        a.setSeveridad("critico");
        a.setStatusId(1L);
        a.setStatusName("Accepted");
        a.setCreatedAt(LocalDateTime.now());
        a.setLatitude(lat);
        a.setLongitude(lon);
        return a;
    }

    @Test
    void execute_retornaAlertas_dentroDelRadio() {
        // ~6 km — dentro del radio de 50 km
        Alerta dentro = alertaAt(1L, 20.70, -103.38);
        // ~295 km (Ciudad de México) — fuera del radio de 50 km
        Alerta fuera = alertaAt(2L, 19.4326, -99.1332);
        // Borde exacto: ~50 km al norte (20.66 + ~0.45° ≈ 50 km)
        double bordeLatitud = LAT + (50.0 / 111.0); // aprox 50 km en grados lat
        Alerta borde = alertaAt(3L, bordeLatitud, LON);

        when(alertaRepository.findValidatedSince(any(LocalDateTime.class)))
                .thenReturn(List.of(dentro, fuera, borde));

        List<GetAlertaResponseDto> resultado = useCase.execute(LAT, LON, 50.0);

        // La alerta de borde puede estar justo en el límite — verificamos al menos
        // que la de dentro esté y la de fuera no.
        assertTrue(resultado.stream().anyMatch(a -> a.alertaId.equals(1L)),
                "La alerta cercana (~6 km) debe estar en el resultado");
        assertTrue(resultado.stream().noneMatch(a -> a.alertaId.equals(2L)),
                "La alerta de Ciudad de México (~295 km) no debe estar en el resultado");
    }

    @Test
    void execute_soloAlertaDentro_retornaUna() {
        Alerta dentro = alertaAt(10L, 20.70, -103.38);  // ~6 km
        Alerta fuera = alertaAt(11L, 25.00, -100.00);   // muy lejos

        when(alertaRepository.findValidatedSince(any(LocalDateTime.class)))
                .thenReturn(List.of(dentro, fuera));

        List<GetAlertaResponseDto> resultado = useCase.execute(LAT, LON, 50.0);

        assertEquals(1, resultado.size());
        assertEquals(10L, resultado.get(0).alertaId);
    }

    @Test
    void execute_sinAlertasDentro_retornaVacio() {
        Alerta fuera = alertaAt(20L, 25.00, -100.00);

        when(alertaRepository.findValidatedSince(any(LocalDateTime.class)))
                .thenReturn(List.of(fuera));

        List<GetAlertaResponseDto> resultado = useCase.execute(LAT, LON, 50.0);

        assertTrue(resultado.isEmpty());
    }

    @Test
    void execute_radioNegativo_usaValorDefault() {
        Alerta dentro = alertaAt(30L, 20.70, -103.38); // ~6 km — dentro de 50 km default

        when(alertaRepository.findValidatedSince(any(LocalDateTime.class)))
                .thenReturn(List.of(dentro));

        // Radio 0 o negativo → usa DEFAULT_RADIO_KM = 50
        List<GetAlertaResponseDto> resultado = useCase.execute(LAT, LON, 0.0);

        assertEquals(1, resultado.size());
    }

    @Test
    void execute_alertaSinCoordenadas_esIgnorada() {
        Alerta sinCoords = alertaAt(40L, 0.0, 0.0);
        sinCoords.setLatitude(null);
        sinCoords.setLongitude(null);

        when(alertaRepository.findValidatedSince(any(LocalDateTime.class)))
                .thenReturn(List.of(sinCoords));

        List<GetAlertaResponseDto> resultado = useCase.execute(LAT, LON, 50.0);

        assertTrue(resultado.isEmpty());
    }

    @Test
    void haversineKm_mismaUbicacion_retornaCero() {
        double dist = GetAlertasCercanasParcelaUseCase.haversineKm(LAT, LON, LAT, LON);
        assertEquals(0.0, dist, 0.001);
    }

    @Test
    void haversineKm_guadalajaraAMexico_aprox440km() {
        // Guadalajara → Ciudad de México ≈ 440-460 km
        double dist = GetAlertasCercanasParcelaUseCase.haversineKm(20.66, -103.35, 19.4326, -99.1332);
        assertTrue(dist > 400 && dist < 500,
                "Distancia Guadalajara-CDMX esperada ~440 km, obtenida: " + dist);
    }
}
