package itesm.mx.infrastructure.persistence.entity.marketplace;

import jakarta.persistence.*;

@Entity
@Table(name = "Acciones_Inventario")
public class InventoryActionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_accion_inventario")
    public Long inventoryActionId;

    @Column(name = "accion", length = 100, nullable = false)
    public String accion;
}
