package co.edu.icesi.vista360.academico.web;

import co.edu.icesi.vista360.academico.service.RecursoNoEncontradoException;
import jakarta.validation.ConstraintViolationException;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.net.URI;
import java.time.Instant;
import java.util.stream.Collectors;

/**
 * Traduce las excepciones a respuestas de error con el formato RFC 9457
 * (application/problem+json), que es el estandar que Spring soporta de forma nativa.
 * Se prefiere sobre un formato propio para que cualquier consumidor del ecosistema
 * pueda interpretar los errores sin acuerdos particulares.
 */
@RestControllerAdvice
public class ManejadorDeErrores {

    private static final Logger log = LoggerFactory.getLogger(ManejadorDeErrores.class);

    private static final URI TIPO_NO_ENCONTRADO = URI.create("https://vista360.icesi.edu.co/errores/recurso-no-encontrado");
    private static final URI TIPO_PETICION_INVALIDA = URI.create("https://vista360.icesi.edu.co/errores/peticion-invalida");
    private static final URI TIPO_ERROR_INTERNO = URI.create("https://vista360.icesi.edu.co/errores/error-interno");

    @ExceptionHandler(RecursoNoEncontradoException.class)
    public ProblemDetail manejarNoEncontrado(RecursoNoEncontradoException ex) {
        ProblemDetail problema = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
        problema.setType(TIPO_NO_ENCONTRADO);
        problema.setTitle("Recurso no encontrado");
        problema.setProperty("recurso", ex.getTipoRecurso());
        problema.setProperty("marcaDeTiempo", Instant.now());
        return problema;
    }

    /**
     * Validacion nativa de parametros de metodo (Spring Framework 6.1 en adelante).
     * Es la ruta que se ejecuta cuando el codigo del estudiante o el periodo no cumplen
     * el formato declarado en el controlador.
     */
    @ExceptionHandler(HandlerMethodValidationException.class)
    public ProblemDetail manejarValidacionDeParametros(HandlerMethodValidationException ex) {
        String detalle = ex.getAllErrors().stream()
                .map(error -> error.getDefaultMessage())
                .filter(mensaje -> mensaje != null)
                .collect(Collectors.joining(" "));
        return problemaDeValidacion(detalle);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ProblemDetail manejarValidacion(ConstraintViolationException ex) {
        String detalle = ex.getConstraintViolations().stream()
                .map(violacion -> violacion.getMessage())
                .collect(Collectors.joining(" "));
        return problemaDeValidacion(detalle);
    }

    private ProblemDetail problemaDeValidacion(String detalle) {
        ProblemDetail problema = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, detalle);
        problema.setType(TIPO_PETICION_INVALIDA);
        problema.setTitle("Peticion invalida");
        problema.setProperty("marcaDeTiempo", Instant.now());
        return problema;
    }

    /**
     * Red de seguridad. Se registra la traza completa en el log pero al cliente solo se
     * le devuelve un mensaje generico: filtrar detalles internos es parte del manejo de
     * informacion sensible (Parte 4).
     */
    @ExceptionHandler(Exception.class)
    public ProblemDetail manejarInesperado(Exception ex) {
        log.error("Error no controlado atendiendo la peticion", ex);
        ProblemDetail problema = ProblemDetail.forStatusAndDetail(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Ocurrio un error procesando la solicitud. Si persiste, reporte el identificador de la traza.");
        problema.setType(TIPO_ERROR_INTERNO);
        problema.setTitle("Error interno");
        problema.setProperty("marcaDeTiempo", Instant.now());
        return problema;
    }
}
