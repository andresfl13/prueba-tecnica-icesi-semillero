package co.edu.icesi.vista360.academico.domain;

import jakarta.persistence.*;
import org.hibernate.annotations.Immutable;

import java.math.BigDecimal;

/** Componente del esquema de evaluacion de un grupo, con su peso sobre la nota final. */
@Entity
@Table(name = "evaluacion")
@Immutable
public class Evaluacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "grupo_id")
    private Grupo grupo;

    @Column(nullable = false)
    private String nombre;

    @Column(nullable = false)
    private BigDecimal porcentaje;

    @Column(nullable = false)
    private short orden;

    protected Evaluacion() {
    }

    public Long getId() {
        return id;
    }

    public Grupo getGrupo() {
        return grupo;
    }

    public String getNombre() {
        return nombre;
    }

    public BigDecimal getPorcentaje() {
        return porcentaje;
    }

    public short getOrden() {
        return orden;
    }
}
