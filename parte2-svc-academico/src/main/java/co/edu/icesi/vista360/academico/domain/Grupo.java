package co.edu.icesi.vista360.academico.domain;

import jakarta.persistence.*;
import org.hibernate.annotations.Immutable;

/** Oferta concreta de una materia en un periodo: es lo que el estudiante matricula. */
@Entity
@Table(name = "grupo")
@Immutable
public class Grupo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "materia_id")
    private Materia materia;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "periodo_id")
    private PeriodoAcademico periodo;

    @Column(name = "codigo_grupo", nullable = false)
    private String codigoGrupo;

    private String docente;

    protected Grupo() {
    }

    public Long getId() {
        return id;
    }

    public Materia getMateria() {
        return materia;
    }

    public PeriodoAcademico getPeriodo() {
        return periodo;
    }

    public String getCodigoGrupo() {
        return codigoGrupo;
    }

    public String getDocente() {
        return docente;
    }
}
