package co.edu.icesi.vista360.academico.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;

@Schema(description = "Periodo academico al que corresponde la carga devuelta")
public record PeriodoDto(

        @Schema(example = "2026-01")
        String codigo,

        @Schema(example = "Primer semestre 2026")
        String nombre,

        LocalDate fechaInicio,

        LocalDate fechaFin,

        @Schema(description = "Indica si es el periodo vigente o uno consultado explicitamente")
        boolean vigente
) {
}
