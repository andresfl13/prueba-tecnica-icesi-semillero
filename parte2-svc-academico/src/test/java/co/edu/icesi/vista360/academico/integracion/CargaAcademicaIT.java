package co.edu.icesi.vista360.academico.integracion;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Prueba de integracion sobre PostgreSQL real levantado con Testcontainers.
 *
 * <p>Se usa una base real y no una en memoria porque lo que aqui interesa validar es
 * justamente lo que una base embebida no reproduce fielmente: las migraciones de Flyway,
 * los indices unicos parciales del esquema y el comportamiento del motor sobre el que
 * el servicio va a correr de verdad.</p>
 *
 * <p>Los casos cubiertos corresponden a los datos semilla, disenados para ejercitar
 * los limites del contrato.</p>
 */
@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
class CargaAcademicaIT {

    @Container
    @ServiceConnection
    static PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:17-alpine");

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("devuelve las materias del periodo vigente con sus notas y el resumen")
    void devuelveLaCargaDelPeriodoVigente() throws Exception {
        mockMvc.perform(get("/api/v1/estudiantes/A00398123/carga-academica"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estudiante.nombreCompleto").value("Andres Felipe Lopez Reyes"))
                .andExpect(jsonPath("$.periodo.codigo").value("2026-01"))
                .andExpect(jsonPath("$.periodo.vigente").value(true))
                .andExpect(jsonPath("$.materias.length()").value(3))
                .andExpect(jsonPath("$.resumen.totalMaterias").value(3))
                .andExpect(jsonPath("$.resumen.totalCreditos").value(9))
                .andExpect(jsonPath("$.frescura.origen").value("ERP"));
    }

    @Test
    @DisplayName("excluye las materias con matricula cancelada")
    void excluyeMatriculasCanceladas() throws Exception {
        // A00398123 tiene TEL-3100 cancelada: no es una materia que "tenga inscrita actualmente".
        mockMvc.perform(get("/api/v1/estudiantes/A00398123/carga-academica"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.materias[?(@.codigo == 'TEL-3100')]").isEmpty());
    }

    @Test
    @DisplayName("no mezcla la carga de periodos anteriores")
    void noMezclaPeriodos() throws Exception {
        // MAT-1013 la curso en 2025-02; no debe aparecer al consultar el periodo vigente.
        mockMvc.perform(get("/api/v1/estudiantes/A00398123/carga-academica"))
                .andExpect(jsonPath("$.materias[?(@.codigo == 'MAT-1013')]").isEmpty());

        mockMvc.perform(get("/api/v1/estudiantes/A00398123/carga-academica").param("periodo", "2025-02"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.periodo.vigente").value(false))
                .andExpect(jsonPath("$.materias.length()").value(1))
                .andExpect(jsonPath("$.materias[0].codigo").value("MAT-1013"));
    }

    @Test
    @DisplayName("pondera la nota acumulada contra lo evaluado, no contra el 100%")
    void calculaLaNotaAcumuladaSobreLoEvaluado() throws Exception {
        // TIC-3011: Parcial 1 = 4.30 (30%) y Talleres = 4.60 (15%).
        // Evaluado = 45%. Acumulada = (4.30*30 + 4.60*15) / 45 = 4.40, no 1.98.
        mockMvc.perform(get("/api/v1/estudiantes/A00398123/carga-academica"))
                .andExpect(jsonPath("$.materias[?(@.codigo == 'TIC-3011')].porcentajeEvaluado").value(45.00))
                .andExpect(jsonPath("$.materias[?(@.codigo == 'TIC-3011')].notaAcumulada").value(4.40));
    }

    @Test
    @DisplayName("reporta la materia sin notas con acumulada nula y 0% evaluado")
    void materiaSinNotas() throws Exception {
        mockMvc.perform(get("/api/v1/estudiantes/A00377012/carga-academica"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.materias.length()").value(1))
                .andExpect(jsonPath("$.materias[0].notas.length()").value(0))
                .andExpect(jsonPath("$.materias[0].notaAcumulada").doesNotExist())
                .andExpect(jsonPath("$.materias[0].porcentajeEvaluado").value(0.00))
                .andExpect(jsonPath("$.resumen.promedioAcumulado").doesNotExist());
    }

    @Test
    @DisplayName("un estudiante sin matriculas responde 200 con lista vacia, no 404")
    void estudianteSinMatriculas() throws Exception {
        // Existir y no tener carga son cosas distintas: confundirlas obligaria al
        // consumidor a interpretar un 404 ambiguo.
        mockMvc.perform(get("/api/v1/estudiantes/A00412998/carga-academica"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.materias.length()").value(0))
                .andExpect(jsonPath("$.resumen.totalCreditos").value(0));
    }

    @Test
    @DisplayName("devuelve 404 cuando el estudiante no existe en la replica")
    void estudianteInexistente() throws Exception {
        mockMvc.perform(get("/api/v1/estudiantes/A00999999/carga-academica"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.recurso").value("estudiante"));
    }

    @Test
    @DisplayName("devuelve 404 cuando el periodo solicitado no existe")
    void periodoInexistente() throws Exception {
        mockMvc.perform(get("/api/v1/estudiantes/A00398123/carga-academica").param("periodo", "2030-01"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.recurso").value("periodo"));
    }
}
