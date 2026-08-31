package co.edu.icesi.vista360.academico.domain;

import jakarta.persistence.*;
import org.hibernate.annotations.Immutable;

import java.time.LocalDate;
import java.time.OffsetDateTime;

/** Inscripcion de un estudiante en un grupo. Es el ancla de sus notas. */
@Entity
@Table(name = "matricula")
@Immutable
public class Matricula {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "estudiante_id")
    private Estudiante estudiante;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "grupo_id")
    private Grupo grupo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoMatricula estado;

    @Column(name = "fecha_matricula", nullable = false)
    private LocalDate fechaMatricula;

    @Column(nullable = false)
    private String origen;

    @Column(name = "actualizado_en", nullable = false)
    private OffsetDateTime actualizadoEn;

    protected Matricula() {
    }

    public Long getId() {
        return id;
    }

    public Estudiante getEstudiante() {
        return estudiante;
    }

    public Grupo getGrupo() {
        return grupo;
    }

    public EstadoMatricula getEstado() {
        return estado;
    }

    public LocalDate getFechaMatricula() {
        return fechaMatricula;
    }

    public OffsetDateTime getActualizadoEn() {
        return actualizadoEn;
    }
}
