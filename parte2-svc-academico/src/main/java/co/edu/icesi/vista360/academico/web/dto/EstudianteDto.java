package co.edu.icesi.vista360.academico.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Datos basicos del estudiante consultado")
public record EstudianteDto(

        @Schema(example = "A00398123")
        String codigo,

        @Schema(example = "Andres Felipe Lopez Reyes")
        String nombreCompleto,

        @Schema(example = "Ingenieria de Sistemas")
        String programa,

        @Schema(example = "7")
        Integer semestre,

        @Schema(description = "Estado academico segun el ERP", example = "ACTIVO")
        String estadoAcademico
) {
}
