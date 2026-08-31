package co.edu.icesi.vista360.academico.domain;

import jakarta.persistence.*;
import org.hibernate.annotations.Immutable;

import java.time.OffsetDateTime;

/**
 * Marca de frescura de la replica. Alimenta el bloque "frescura" de la respuesta:
 * el consumidor debe poder saber que esta viendo una copia y de cuando es.
 */
@Entity
@Table(name = "sincronizacion_replica")
@Immutable
public class SincronizacionReplica {

    @Id
    private String entidad;

    @Column(name = "ultima_sincronizacion", nullable = false)
    private OffsetDateTime ultimaSincronizacion;

    @Column(nullable = false)
    private String origen;

    protected SincronizacionReplica() {
    }

    public String getEntidad() {
        return entidad;
    }

    public OffsetDateTime getUltimaSincronizacion() {
        return ultimaSincronizacion;
    }

    public String getOrigen() {
        return origen;
    }
}
