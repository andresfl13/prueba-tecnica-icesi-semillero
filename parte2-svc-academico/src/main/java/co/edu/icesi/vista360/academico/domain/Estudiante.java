package co.edu.icesi.vista360.academico.domain;

import jakarta.persistence.*;
import org.hibernate.annotations.Immutable;

import java.time.OffsetDateTime;

/**
 * Replica del estudiante. La fuente de verdad es el ERP institucional: este servicio
 * nunca escribe sobre estos datos, solo los sirve indicando su frescura.
 */
@Entity
@Table(name = "estudiante")
@Immutable
public class Estudiante {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Identificador publico. Es el que viaja en la URL; la PK interna no se expone. */
    @Column(name = "codigo_institucional", nullable = false, unique = true)
    private String codigoInstitucional;

    @Column(nullable = false)
    private String nombres;

    @Column(nullable = false)
    private String apellidos;

    @Column(nullable = false)
    private String programa;

    private Short semestre;

    /**
     * Se guarda como texto y no como enum a proposito: el catalogo de estados lo define
     * el ERP, y un valor nuevo alla no debe romper la deserializacion aca.
     */
    @Column(name = "estado_academico", nullable = false)
    private String estadoAcademico;

    @Column(nullable = false)
    private String origen;

    @Column(name = "actualizado_en", nullable = false)
    private OffsetDateTime actualizadoEn;

    protected Estudiante() {
    }

    public Long getId() {
        return id;
    }

    public String getCodigoInstitucional() {
        return codigoInstitucional;
    }

    public String getNombres() {
        return nombres;
    }

    public String getApellidos() {
        return apellidos;
    }

    public String getNombreCompleto() {
        return nombres + " " + apellidos;
    }

    public String getPrograma() {
        return programa;
    }

    public Short getSemestre() {
        return semestre;
    }

    public String getEstadoAcademico() {
        return estadoAcademico;
    }

    public String getOrigen() {
        return origen;
    }

    public OffsetDateTime getActualizadoEn() {
        return actualizadoEn;
    }
}
