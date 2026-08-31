package co.edu.icesi.vista360.academico.web;

import co.edu.icesi.vista360.academico.service.CargaAcademicaService;
import co.edu.icesi.vista360.academico.service.RecursoNoEncontradoException;
import co.edu.icesi.vista360.academico.web.dto.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Pruebas del contrato HTTP: codigos de estado, formato de la respuesta y validacion
 * de la entrada. El servicio va simulado a proposito: aqui se verifica la frontera web,
 * no la logica de negocio.
 */
@WebMvcTest(CargaAcademicaController.class)
class CargaAcademicaControllerTest {

    private static final String CODIGO_VALIDO = "A00398123";

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CargaAcademicaService servicio;

    @Test
    @DisplayName("devuelve 200 con las materias y notas del estudiante")
    void devuelveLaCargaAcademica() throws Exception {
        given(servicio.consultar(eq(CODIGO_VALIDO), any())).willReturn(respuestaDeEjemplo());

        mockMvc.perform(get("/api/v1/estudiantes/{codigo}/carga-academica", CODIGO_VALIDO))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estudiante.codigo").value(CODIGO_VALIDO))
                .andExpect(jsonPath("$.periodo.codigo").value("2026-01"))
                .andExpect(jsonPath("$.materias.length()").value(1))
                .andExpect(jsonPath("$.materias[0].codigo").value("TIC-3011"))
                .andExpect(jsonPath("$.materias[0].notas.length()").value(1))
                .andExpect(jsonPath("$.materias[0].notaAcumulada").value(4.30))
                .andExpect(jsonPath("$.frescura.origen").value("ERP"));
    }

    @Test
    @DisplayName("rechaza con 400 un codigo de estudiante con formato invalido")
    void rechazaCodigoInvalido() throws Exception {
        mockMvc.perform(get("/api/v1/estudiantes/{codigo}/carga-academica", "hola"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Peticion invalida"));
    }

    @Test
    @DisplayName("rechaza con 400 un periodo con formato invalido")
    void rechazaPeriodoInvalido() throws Exception {
        mockMvc.perform(get("/api/v1/estudiantes/{codigo}/carga-academica", CODIGO_VALIDO)
                        .param("periodo", "primer-semestre"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("devuelve 404 con problem+json cuando el estudiante no existe")
    void devuelve404SiNoExiste() throws Exception {
        willThrow(RecursoNoEncontradoException.estudiante("A00999999"))
                .given(servicio).consultar(eq("A00999999"), any());

        mockMvc.perform(get("/api/v1/estudiantes/{codigo}/carga-academica", "A00999999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title").value("Recurso no encontrado"))
                .andExpect(jsonPath("$.recurso").value("estudiante"));
    }

    private CargaAcademicaResponse respuestaDeEjemplo() {
        return new CargaAcademicaResponse(
                new EstudianteDto(CODIGO_VALIDO, "Andres Felipe Lopez Reyes", "Ingenieria de Sistemas", 7, "ACTIVO"),
                new PeriodoDto("2026-01", "Primer semestre 2026",
                        LocalDate.of(2026, 1, 19), LocalDate.of(2026, 5, 23), true),
                List.of(new MateriaMatriculadaDto(
                        "TIC-3011", "Computacion en Internet II", 3, "01", "Carlos Andres Delgado",
                        List.of(new NotaDto("Parcial 1", new BigDecimal("30.00"), new BigDecimal("4.30"),
                                OffsetDateTime.parse("2026-03-06T15:20:00-05:00"))),
                        new BigDecimal("4.30"), new BigDecimal("30.00"))),
                new ResumenDto(1, 3, new BigDecimal("4.30")),
                new FrescuraDto("ERP", OffsetDateTime.parse("2026-03-21T10:00:00Z"), 240));
    }
}
