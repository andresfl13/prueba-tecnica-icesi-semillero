package co.edu.icesi.vista360.academico.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "Respuesta del servicio: materias matriculadas y notas del estudiante en un periodo")
public record CargaAcademicaResponse(

        EstudianteDto estudiante,

        PeriodoDto periodo,

        @Schema(description = "Materias con matricula INSCRITA. Puede venir vacia si el estudiante no matriculo")
        List<MateriaMatriculadaDto> materias,

        ResumenDto resumen,

        FrescuraDto frescura
) {
}
