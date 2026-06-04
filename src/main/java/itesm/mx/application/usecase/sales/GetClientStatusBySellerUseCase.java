package itesm.mx.application.usecase.sales;

import itesm.mx.application.dto.ClientAlertaSummaryDto;
import itesm.mx.application.dto.ClientParcelaSummaryDto;
import itesm.mx.application.dto.ClientStatusDto;
import itesm.mx.application.mapper.sales.ClientDtoMapper;
import itesm.mx.domain.models.alerta.Alerta;
import itesm.mx.domain.models.order.Order;
import itesm.mx.domain.models.parcela.Parcela;
import itesm.mx.domain.models.user.Farmer;
import itesm.mx.domain.repository.alerta.AlertaRepository;
import itesm.mx.domain.repository.order.OrderRepository;
import itesm.mx.domain.repository.parcela.ParcelaRepository;
import itesm.mx.domain.repository.user.FarmerRepository;
import itesm.mx.domain.repository.user.TechnicalSellerRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@ApplicationScoped
public class GetClientStatusBySellerUseCase {

    @Inject TechnicalSellerRepository technicalSellerRepository;
    @Inject FarmerRepository farmerRepository;
    @Inject OrderRepository orderRepository;
    @Inject ParcelaRepository parcelaRepository;
    @Inject AlertaRepository alertaRepository;

    @Transactional
    public ClientStatusDto execute(Long userId, Long farmerId) {
        if (userId == null) {
            throw new IllegalArgumentException("El id de usuario es requerido");
        }
        if (farmerId == null) {
            throw new IllegalArgumentException("El id del agricultor es requerido");
        }

        Long sellerId = technicalSellerRepository.findByIdUser(userId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "No se encontró vendedor para el usuario con id: " + userId))
                .getTechnicalSellerId();

        List<Order> orders = orderRepository.findAllBySellerIdAndFarmerId(sellerId, farmerId);
        if (orders.isEmpty()) {
            throw new IllegalStateException(
                    "El agricultor con id " + farmerId + " no es cliente del vendedor actual");
        }

        Farmer farmer = farmerRepository.findByFarmerId(farmerId)
                .orElseThrow(() -> new IllegalStateException(
                        "Agricultor no encontrado con id: " + farmerId));

        List<Parcela> parcelas = parcelaRepository.findByFarmerId(farmerId);
        
        LocalDateTime since = LocalDateTime.now().minusDays(15);
        List<Alerta> alertas = farmer.getUser() != null
                ? alertaRepository.findActiveByReportedUserIdSince(farmer.getUser().getUserId(), since)
                : List.of();

        List<ClientParcelaSummaryDto> parcelaDtos = parcelas.stream()
                .map(ClientDtoMapper::toParcelaSummary)
                .toList();

        List<ClientAlertaSummaryDto> alertaDtos = alertas.stream()
                .map(ClientDtoMapper::toAlertaSummary)
                .toList();

        return new ClientStatusDto(farmerId, parcelaDtos, alertaDtos);
    }
}
