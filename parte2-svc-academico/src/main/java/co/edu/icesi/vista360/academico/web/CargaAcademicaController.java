package co.edu.icesi.vista360.academico.web;

import co.edu.icesi.vista360.academico.service.CargaAcademicaService;
import co.edu.icesi.vista360.academico.web.dto.CargaAcademicaResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Pattern;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/estudiantes")
@Tag(name = "Carga academica", description = "Consulta de materias matriculadas y notas del periodo")
public class CargaAcademicaController {

    private final CargaAcademicaService servicio;

    public CargaAcademicaController(CargaAcademicaService servicio) {
        this.servicio = servicio;
    }

    /**
     * El recurso se modela como subrecurso del estudiante
     * ({@code /estudiantes/{codigo}/carga-academica}) y no como {@code /carga-academica?estudiante=}
     * porque la carga academica no existe sin un estudiante: la jerarquia de la URL
     * refleja la del dominio y deja el camino abierto a otros subrecursos del mismo
     * estudiante sin rediseñar el contrato.
     */
    @GetMapping(value = "/{codigoEstudiante}/carga-academica", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(
            summary = "Materias matriculadas y notas de un estudiante",
            description = """
                    Devuelve las materias con matricula INSCRITA en el periodo indicado y las notas
                    registradas hasta el momento. Si no se indica periodo, se usa el vigente.

                    Los datos academicos son una replica del ERP institucional: la respuesta incluye
                    un bloque 'frescura' con el origen y la antiguedad del dato.""")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Carga academica del estudiante"),
            @ApiResponse(responseCode = "400", description = "El codigo del estudiante no tiene el formato esperado",
                    content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "404", description = "El estudiante o el periodo no existen",
                    content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class)))
    })
    public CargaAcademicaResponse consultarCargaAcademica(

            @Parameter(description = "Codigo institucional del estudiante", example = "A00398123", required = true)
            @PathVariable
            @Pattern(regexp = "^A\\d{8}$", message = "El codigo del estudiante debe tener el formato A seguido de 8 digitos, por ejemplo A00398123")
            String codigoEstudiante,

            @Parameter(description = "Periodo academico a consultar. Si se omite, se usa el vigente", example = "2026-01")
            @RequestParam(required = false)
            @Pattern(regexp = "^\\d{4}-\\d{2}$", message = "El periodo debe tener el formato AAAA-NN, por ejemplo 2026-01")
            String periodo) {

        return servicio.consultar(codigoEstudiante, periodo);
    }
}
