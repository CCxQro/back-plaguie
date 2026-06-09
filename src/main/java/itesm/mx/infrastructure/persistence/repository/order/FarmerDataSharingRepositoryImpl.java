package itesm.mx.infrastructure.persistence.repository.order;

import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;
import itesm.mx.domain.models.order.FarmerDataSharing;
import itesm.mx.domain.repository.order.FarmerDataSharingRepository;
import itesm.mx.infrastructure.persistence.entity.order.FarmerDataSharingEntity;

@ApplicationScoped
public class FarmerDataSharingRepositoryImpl
        implements PanacheRepositoryBase<FarmerDataSharingEntity, Long>, FarmerDataSharingRepository {

    @Override
    public FarmerDataSharing save(FarmerDataSharing farmerDataSharing) {
        FarmerDataSharingEntity entity = toEntity(farmerDataSharing);
        persistAndFlush(entity);
        return toDomain(entity);
    }

    private static FarmerDataSharingEntity toEntity(FarmerDataSharing domain) {
        FarmerDataSharingEntity entity = new FarmerDataSharingEntity();
        entity.orderId = domain.getOrderId();
        entity.farmerId = domain.getFarmerId();
        entity.providerId = domain.getProviderId();
        entity.sharedAt = domain.getSharedAt();
        entity.snapshotJson = domain.getSnapshotJson();
        return entity;
    }

    private static FarmerDataSharing toDomain(FarmerDataSharingEntity entity) {
        return new FarmerDataSharing(
                entity.id,
                entity.orderId,
                entity.farmerId,
                entity.providerId,
                entity.sharedAt,
                entity.snapshotJson
        );
    }
}
