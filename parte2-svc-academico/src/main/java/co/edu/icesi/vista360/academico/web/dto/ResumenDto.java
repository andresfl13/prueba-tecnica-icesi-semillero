package co.edu.icesi.vista360.academico.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;

@Schema(description = "Totales del periodo, precalculados para que el consumidor no tenga que sumarlos")
public record ResumenDto(

        @Schema(example = "3")
        int totalMaterias,

        @Schema(example = "9")
        int totalCreditos,

        @Schema(description = """
                Promedio ponderado por creditos sobre lo evaluado hasta ahora.
                Es null si ninguna materia tiene notas todavia.""", example = "3.95")
        BigDecimal promedioAcumulado
) {
}
