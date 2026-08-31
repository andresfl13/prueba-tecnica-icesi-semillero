package co.edu.icesi.vista360.academico.service;

/**
 * Un recurso identificado en la peticion no existe.
 * Se traduce a 404 en {@link co.edu.icesi.vista360.academico.web.ManejadorDeErrores}.
 */
public class RecursoNoEncontradoException extends RuntimeException {

    private final String tipoRecurso;

    public RecursoNoEncontradoException(String tipoRecurso, String mensaje) {
        super(mensaje);
        this.tipoRecurso = tipoRecurso;
    }

    public String getTipoRecurso() {
        return tipoRecurso;
    }

    public static RecursoNoEncontradoException estudiante(String codigo) {
        return new RecursoNoEncontradoException("estudiante",
                "No existe un estudiante con codigo " + codigo + " en la replica academica.");
    }

    public static RecursoNoEncontradoException periodo(String codigo) {
        return new RecursoNoEncontradoException("periodo",
                "No existe el periodo academico " + codigo + ".");
    }

    public static RecursoNoEncontradoException periodoVigente() {
        return new RecursoNoEncontradoException("periodo",
                "No hay un periodo academico marcado como vigente. Indique uno con el parametro 'periodo'.");
    }
}
