package itesm.mx.application.usecase.sales;

import itesm.mx.application.dto.ClientStatusDto;
import itesm.mx.domain.models.alerta.Alerta;
import itesm.mx.domain.models.order.Order;
import itesm.mx.domain.models.parcela.Parcela;
import itesm.mx.domain.models.user.Farmer;
import itesm.mx.domain.models.user.TechnicalSeller;
import itesm.mx.domain.models.user.User;
import itesm.mx.domain.repository.alerta.AlertaRepository;
import itesm.mx.domain.repository.order.OrderRepository;
import itesm.mx.domain.repository.parcela.ParcelaRepository;
import itesm.mx.domain.repository.user.FarmerRepository;
import itesm.mx.domain.repository.user.TechnicalSellerRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetClientStatusBySellerUseCaseTest {

    @Mock TechnicalSellerRepository technicalSellerRepository;
    @Mock FarmerRepository farmerRepository;
    @Mock OrderRepository orderRepository;
    @Mock ParcelaRepository parcelaRepository;
    @Mock AlertaRepository alertaRepository;

    @InjectMocks
    GetClientStatusBySellerUseCase useCase;

    private TechnicalSeller seller() {
        TechnicalSeller s = new TechnicalSeller();
        s.setTechnicalSellerId(1L);
        return s;
    }

    private Farmer farmer() {
        User user = new User();
        user.setUserId(100L);

        Farmer f = new Farmer();
        f.setFarmerId(10L);
        f.setUser(user);
        return f;
    }

    @Test
    void execute_WhenUserIdIsNull_Throws() {
        assertThrows(IllegalArgumentException.class, () -> useCase.execute(null, 1L));
    }

    @Test
    void execute_WhenFarmerIdIsNull_Throws() {
        assertThrows(IllegalArgumentException.class, () -> useCase.execute(1L, null));
    }

    @Test
    void execute_WhenSellerNotFound_Throws() {
        when(technicalSellerRepository.findByIdUser(1L)).thenReturn(Optional.empty());
        assertThrows(IllegalArgumentException.class, () -> useCase.execute(1L, 10L));
    }

    @Test
    void execute_WhenFarmerIsNotClient_Throws() {
        when(technicalSellerRepository.findByIdUser(1L)).thenReturn(Optional.of(seller()));
        when(orderRepository.findAllBySellerIdAndFarmerId(1L, 10L)).thenReturn(List.of());
        assertThrows(IllegalStateException.class, () -> useCase.execute(1L, 10L));
    }

    @Test
    void execute_ReturnsStatus() {
        when(technicalSellerRepository.findByIdUser(1L)).thenReturn(Optional.of(seller()));
        when(orderRepository.findAllBySellerIdAndFarmerId(1L, 10L)).thenReturn(List.of(new Order()));
        when(farmerRepository.findByFarmerId(10L)).thenReturn(Optional.of(farmer()));
        
        Parcela p = new Parcela();
        p.setParcelaId(1L);
        p.setNombreParcela("Parcela 1");
        when(parcelaRepository.findByFarmerId(10L)).thenReturn(List.of(p));

        Alerta a = new Alerta();
        a.setAlertaId(1L);
        a.setTitulo("Alerta 1");
        when(alertaRepository.findActiveByReportedUserIdSince(eq(100L), any())).thenReturn(List.of(a));

        ClientStatusDto status = useCase.execute(1L, 10L);

        assertEquals(10L, status.farmerId);
        assertEquals(1, status.parcelas.size());
        assertEquals("Parcela 1", status.parcelas.get(0).nombreParcela);
        assertEquals(1, status.recentActiveAlerts.size());
        assertEquals("Alerta 1", status.recentActiveAlerts.get(0).titulo);
    }
}
