package itesm.mx.infrastructure.persistence.entity.insumo;

import itesm.mx.infrastructure.persistence.entity.marketplace.ProductEntity;
import itesm.mx.infrastructure.persistence.entity.parcela.ParcelaEntity;
import itesm.mx.infrastructure.persistence.entity.users.FarmerEntity;
import jakarta.persistence.*;

import java.time.LocalDate;

@Entity
@Table(name = "Aplicacion_Insumo")
public class AplicacionInsumoEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_aplicacion")
    public Long aplicacionId;

    @Column(name = "fecha")
    public LocalDate fecha;

    @Column(name = "id_agricultor")
    public Long agricultorId;

    @Column(name = "sku_id_vendedor")
    public Long skuIdVendedor;

    @Column(name = "cantidad")
    public Double cantidad;

    @Column(name = "id_parcela")
    public Long parcelaId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_agricultor", referencedColumnName = "id_agricultor",
            insertable = false, updatable = false,
            foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT))
    public FarmerEntity agricultor;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sku_id_vendedor", referencedColumnName = "sku_id_vendedor",
            insertable = false, updatable = false,
            foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT))
    public ProductEntity producto;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_parcela", referencedColumnName = "id_parcela",
            insertable = false, updatable = false,
            foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT))
    public ParcelaEntity parcela;
}
