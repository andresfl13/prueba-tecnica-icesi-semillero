package co.edu.icesi.vista360.academico.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Schema(description = "Nota de una evaluacion concreta dentro de la materia")
public record NotaDto(

        @Schema(description = "Nombre de la evaluacion", example = "Parcial 1")
        String evaluacion,

        @Schema(description = "Peso de la evaluacion sobre la nota final, en porcentaje", example = "30.00")
        BigDecimal porcentaje,

        @Schema(description = "Nota obtenida, en escala 0.0 a 5.0", example = "4.30")
        BigDecimal valor,

        @Schema(description = "Momento en que el docente registro la nota en el ERP")
        OffsetDateTime fechaRegistro
) {
}
