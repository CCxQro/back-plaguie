package itesm.mx.infrastructure.persistence.entity.location;

import jakarta.persistence.*;

@Entity
@Table(name = "Ubicacion")
public class LocationEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_ubicacion")
    public Long locationId;

    @Column(name = "latitud")
    public Double latitude;

    @Column(name = "longitud")
    public Double longitude;

    @Column(name = "id_estado", nullable = false)
    public Long stateId;

    @Column(name = "id_municipio", nullable = false)
    public Long municipalityId;

    @Column(name = "id_localidad", nullable = false)
    public Long localityId;

    @Column(name = "id_predio", nullable = false)
    public Long propertyId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_estado", referencedColumnName = "id_estado", insertable = false, updatable = false)
    public StateEntity state;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_municipio", referencedColumnName = "id_municipio", insertable = false, updatable = false)
    public MunicipalityEntity municipality;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_localidad", referencedColumnName = "id_localidad", insertable = false, updatable = false)
    public LocalityEntity locality;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_predio", referencedColumnName = "id_predio", insertable = false, updatable = false)
    public PropertyEntity property;
}
