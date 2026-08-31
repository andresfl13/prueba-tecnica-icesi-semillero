package co.edu.icesi.vista360.academico.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.util.List;

@Schema(description = "Materia matriculada por el estudiante en el periodo, con sus notas")
public record MateriaMatriculadaDto(

        @Schema(example = "TIC-3011")
        String codigo,

        @Schema(example = "Computacion en Internet II")
        String nombre,

        @Schema(example = "3")
        int creditos,

        @Schema(description = "Grupo en el que quedo inscrito", example = "01")
        String grupo,

        @Schema(example = "Carlos Andres Delgado")
        String docente,

        @Schema(description = "Notas registradas hasta el momento. Vacia si aun no hay ninguna")
        List<NotaDto> notas,

        @Schema(description = """
                Nota acumulada sobre lo evaluado hasta ahora, NO la nota final proyectada.
                Es null cuando todavia no hay ninguna nota registrada.""", example = "4.40")
        BigDecimal notaAcumulada,

        @Schema(description = "Porcentaje del curso que ya fue evaluado. Da contexto a la nota acumulada", example = "45.00")
        BigDecimal porcentajeEvaluado
) {
}
