package co.edu.icesi.vista360.academico.domain;

import jakarta.persistence.*;
import org.hibernate.annotations.Immutable;

/** Catalogo de materias, independiente del periodo. */
@Entity
@Table(name = "materia")
@Immutable
public class Materia {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String codigo;

    @Column(nullable = false)
    private String nombre;

    @Column(nullable = false)
    private short creditos;

    protected Materia() {
    }

    public Long getId() {
        return id;
    }

    public String getCodigo() {
        return codigo;
    }

    public String getNombre() {
        return nombre;
    }

    public short getCreditos() {
        return creditos;
    }
}
