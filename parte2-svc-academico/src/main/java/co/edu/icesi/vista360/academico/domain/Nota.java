package co.edu.icesi.vista360.academico.domain;

import jakarta.persistence.*;
import org.hibernate.annotations.Immutable;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

/**
 * Nota de una evaluacion dentro de una matricula concreta.
 * Cuelga de (matricula, evaluacion) y no de (estudiante, materia): si el estudiante
 * repite la materia, cada cursada tiene sus propias notas sin ambiguedad.
 */
@Entity
@Table(name = "nota")
@Immutable
public class Nota {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "matricula_id")
    private Matricula matricula;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "evaluacion_id")
    private Evaluacion evaluacion;

    @Column(nullable = false)
    private BigDecimal valor;

    @Column(name = "fecha_registro", nullable = false)
    private OffsetDateTime fechaRegistro;

    @Column(nullable = false)
    private String origen;

    @Column(name = "actualizado_en", nullable = false)
    private OffsetDateTime actualizadoEn;

    protected Nota() {
    }

    public Long getId() {
        return id;
    }

    public Matricula getMatricula() {
        return matricula;
    }

    public Evaluacion getEvaluacion() {
        return evaluacion;
    }

    public BigDecimal getValor() {
        return valor;
    }

    public OffsetDateTime getFechaRegistro() {
        return fechaRegistro;
    }
}
