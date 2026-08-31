package co.edu.icesi.vista360.academico.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;

@Configuration
public class ConfiguracionAplicacion {

    /**
     * El reloj se inyecta en lugar de llamar a {@code OffsetDateTime.now()} directamente
     * para poder fijar el tiempo en las pruebas y verificar el calculo de antiguedad
     * del dato de forma determinista.
     */
    @Bean
    public Clock reloj() {
        return Clock.systemUTC();
    }

    @Bean
    public OpenAPI openApi() {
        return new OpenAPI().info(new Info()
                .title("svc-academico · Vista 360 del Estudiante")
                .version("1.0.0")
                .description("""
                        Servicio de consulta de materias matriculadas y notas del periodo.

                        Mantiene una replica de solo lectura de los datos academicos cuya fuente de
                        verdad es el ERP institucional. Toda respuesta declara la frescura del dato.""")
                .contact(new Contact().name("Oficina de Arquitectura e Innovacion de TI"))
                .license(new License().name("Uso academico")));
    }
}
