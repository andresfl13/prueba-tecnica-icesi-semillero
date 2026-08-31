package co.edu.icesi.vista360.academico.domain;

/**
 * Estado de una matricula. A diferencia del estado academico del estudiante, este si
 * es un enum: son los tres valores que el contrato del servicio distingue, y una
 * matricula que no este INSCRITA no forma parte de la carga vigente.
 */
public enum EstadoMatricula {
    INSCRITA,
    CANCELADA,
    RETIRADA
}
