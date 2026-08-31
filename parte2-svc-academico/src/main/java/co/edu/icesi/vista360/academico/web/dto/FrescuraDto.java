package co.edu.icesi.vista360.academico.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.OffsetDateTime;

/**
 * Metadato de frescura del dato replicado.
 * Es parte deliberada del contrato: el servicio sirve una copia del ERP y el consumidor
 * tiene derecho a saberlo para decidir si le sirve o necesita ir a la fuente.
 */
@Schema(description = "De donde viene el dato y que tan fresco es")
public record FrescuraDto(

        @Schema(description = "Sistema fuente de verdad del dato", example = "ERP")
        String origen,

        @Schema(description = "Ultima sincronizacion exitosa contra el sistema fuente")
        OffsetDateTime sincronizadoEn,

        @Schema(description = "Antiguedad del dato en segundos al momento de responder", example = "240")
        long antiguedadSegundos
) {
}
