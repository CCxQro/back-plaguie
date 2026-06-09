package itesm.mx.application.usecase.order;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import itesm.mx.application.dto.ShareOrderResponseDto;
import itesm.mx.domain.models.order.FarmerDataSharing;
import itesm.mx.domain.models.order.Order;
import itesm.mx.domain.models.parcela.Parcela;
import itesm.mx.domain.models.user.Farmer;
import itesm.mx.domain.repository.order.FarmerDataSharingRepository;
import itesm.mx.domain.repository.order.OrderRepository;
import itesm.mx.domain.repository.parcela.ParcelaRepository;
import itesm.mx.domain.repository.user.FarmerRepository;

import java.time.LocalDateTime;
import java.util.List;

@ApplicationScoped
public class ShareOrderWithProviderUseCase {

    @Inject OrderRepository orderRepository;
    @Inject FarmerRepository farmerRepository;
    @Inject FarmerDataSharingRepository farmerDataSharingRepository;
    @Inject ParcelaRepository parcelaRepository;

    @Transactional
    public ShareOrderResponseDto execute(Long orderId, Long userId) {
        if (orderId == null) {
            throw new IllegalArgumentException("El id del pedido es requerido");
        }
        if (userId == null) {
            throw new IllegalArgumentException("El id de usuario es requerido");
        }

        Farmer farmer = farmerRepository.findByIdUser(userId)
                .orElseThrow(() -> new IllegalStateException(
                        "No se encontró agricultor para el usuario con id: " + userId));

        Order order = orderRepository.findByIdWithDetails(orderId)
                .orElseThrow(() -> new IllegalStateException(
                        "Pedido no encontrado con id: " + orderId));

        if (!farmer.getFarmerId().equals(order.getFarmer().getFarmerId())) {
            throw new SecurityException("El pedido no pertenece al agricultor autenticado");
        }

        // Determine the provider from the first product detail
        Long providerId = null;
        if (order.getDetails() != null && !order.getDetails().isEmpty()
                && order.getDetails().get(0).getProduct() != null
                && order.getDetails().get(0).getProduct().getProvider() != null) {
            providerId = order.getDetails().get(0).getProduct().getProvider().getProviderId();
        }
        if (providerId == null) {
            throw new IllegalStateException(
                    "No se pudo determinar el proveedor del pedido con id: " + orderId);
        }

        // Build snapshot JSON with farmer contact info and parcelas
        String snapshotJson = buildSnapshotJson(farmer, order);

        FarmerDataSharing sharing = new FarmerDataSharing();
        sharing.setOrderId(orderId);
        sharing.setFarmerId(farmer.getFarmerId());
        sharing.setProviderId(providerId);
        sharing.setSharedAt(LocalDateTime.now());
        sharing.setSnapshotJson(snapshotJson);

        farmerDataSharingRepository.save(sharing);

        return new ShareOrderResponseDto(
                orderId,
                true,
                "Los datos del agricultor han sido compartidos con el proveedor exitosamente"
        );
    }

    private String buildSnapshotJson(Farmer farmer, Order order) {
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        sb.append("\"farmerId\":").append(farmer.getFarmerId()).append(",");

        if (farmer.getUser() != null) {
            String name = farmer.getUser().getName() != null
                    ? farmer.getUser().getName().replace("\"", "\\\"") : "";
            String email = farmer.getUser().getEmail() != null
                    ? farmer.getUser().getEmail().replace("\"", "\\\"") : "";
            sb.append("\"farmerName\":\"").append(name).append("\",");
            sb.append("\"farmerEmail\":\"").append(email).append("\",");
        }

        sb.append("\"orderId\":").append(order.getOrderId()).append(",");
        sb.append("\"orderDate\":\"").append(order.getOrderDate()).append("\",");

        // Parcelas
        List<Parcela> parcelas = parcelaRepository.findByFarmerId(farmer.getFarmerId());
        sb.append("\"parcelas\":[");
        for (int i = 0; i < parcelas.size(); i++) {
            Parcela p = parcelas.get(i);
            if (i > 0) sb.append(",");
            sb.append("{");
            sb.append("\"parcelaId\":").append(p.getParcelaId()).append(",");
            String nombre = p.getNombreParcela() != null
                    ? p.getNombreParcela().replace("\"", "\\\"") : "";
            sb.append("\"nombre\":\"").append(nombre).append("\",");
            sb.append("\"tamanoHectareas\":").append(p.getTamanoHectareas()).append(",");
            if (p.getTipoCultivo() != null && p.getTipoCultivo().getNombre() != null) {
                String cultivo = p.getTipoCultivo().getNombre().replace("\"", "\\\"");
                sb.append("\"tipoCultivo\":\"").append(cultivo).append("\"");
            } else {
                sb.append("\"tipoCultivo\":null");
            }
            sb.append("}");
        }
        sb.append("]");
        sb.append("}");
        return sb.toString();
    }
}
