# Uso de herramientas de inteligencia artificial

La prueba indica que el uso de IA está permitido y pide declarar **cuál se usó, en qué partes y con qué
propósito**. Esta es la declaración completa.

**Herramienta:** Claude (Anthropic), a través de Claude Code.
**Modalidad de trabajo:** conversacional. El patrón fue consistente en las cuatro partes: la herramienta
planteaba dos o tres alternativas con sus contrapartidas, yo decidía y justificaba, y sobre esa decisión se
producía el entregable.

---

## Resumen por parte

| Parte | Qué decidí yo | Qué aportó la herramienta |
|---|---|---|
| **1 · Diseño** | Las cuatro decisiones estructurales de la arquitectura | Las alternativas y sus contrapartidas, la redacción del documento y el código PlantUML de los diagramas |
| **2 · Servicio** | Criterio del stack, motor de base de datos y alcance de la implementación | Verificación de versiones vigentes, escritura del código, resolución de incompatibilidades y ejecución de las pruebas |
| **3 · Seguridad y comunicación** | Manejo del token, identidad entre servicios y dónde se evalúa la autorización | Redacción, diagramas de secuencia y el detalle técnico de respaldo |
| **4 · Operación y calidad** | Prioridad de diagnóstico, alcance de la auditoría y modelado de la asignación | Redacción y el desarrollo de cada decisión en sus implicaciones |
| **Versionado** | Todos los *commits* y la publicación del repositorio | Propuesta de la estrategia de ramas |

---

## Detalle

### Parte 1 · Diseño de la solución

Tomé las cuatro decisiones estructurales sobre alternativas planteadas con sus contrapartidas:

1. **Estrategia de datos híbrida**, decidida dato por dato según el costo de que esté desactualizado.
2. **Microservicios.** Aquí la herramienta recomendaba un monolito modular; opté por microservicios porque
   se trata de una plataforma de uso institucional transversal, no de un piloto, e indiqué que el resto del
   diseño se ajustara a esa escala.
3. **Toda la integración a través de la plataforma de integración**, sin acceso directo al ERP.
4. **Alimentación distribuida del data warehouse**, con Vista 360° aportando únicamente sus datos propios.

La herramienta redactó `DECISIONES.md` y generó el PlantUML de los tres diagramas.

### Parte 2 · Servicio

Definí el criterio del stack —que fuera vigente en la industria y no heredado de la carrera— y aporté el
perfil del cargo, que fijó Java con Spring Boot. Elegí PostgreSQL sustentándolo arquitectónicamente
(el microservicio tiene base propia; el PL/SQL del ERP vive en la capa de integración) y acoté el alcance a
un endpoint terminado de punta a punta en lugar de varios a medias.

La herramienta verificó contra Maven Central las versiones vigentes en lugar de asumirlas —lo que evitó
cuatro desactualizaciones reales, incluida la autoconfiguración de Flyway, que en Spring Boot 4 cambió de
módulo— escribió el código, resolvió las incompatibilidades que fueron apareciendo y ejecutó las pruebas
hasta dejarlas en verde.

### Parte 3 · Seguridad y comunicación

Los dos escenarios de comunicación (3.2) son la aplicación directa de decisiones que ya había tomado en la
Parte 1: la lectura en vivo del estado financiero se deriva de la estrategia híbrida de datos, y la
propagación asíncrona del cambio de condición académica, de la alimentación distribuida del data warehouse.

En seguridad (3.1) decidí, sobre alternativas contrastadas una por una:

- **El token no llega al navegador.** Lo custodia el BFF y al cliente solo le llega una cookie `HttpOnly`.
  Descarté guardarlo en memoria del SPA: quedaría al alcance de cualquier XSS. Acepto a cambio tener que
  protegerme de CSRF.
- **Se propaga el token del usuario** hacia los microservicios, y no credenciales de servicio. Con estas
  últimas el servicio pierde la identidad de quien pregunta y la autorización quedaría solo en el BFF.
  Acepto la mayor superficie de exposición que eso implica.
- **La autorización se evalúa en cada microservicio**, no solo en el borde. Un único punto de decisión es
  más limpio, pero deja sin control cualquier acceso que no pase por él.

La herramienta redactó las respuestas, generó los dos diagramas de secuencia y aportó el respaldo técnico
de cada decisión: los estándares aplicables (OIDC con PKCE, RFC 9457, RFC 8693), la referencia a
*Broken Object Level Authorization* del OWASP API Security Top 10 y las garantías que exige un bus de
eventos para no fallar en silencio.

### Parte 4 · Operación y calidad

Decidí, también sobre alternativas contrastadas:

- **Trazabilidad distribuida punta a punta** como la previsión de diseño prioritaria frente al incidente
  intermitente. Los logs centralizados y las métricas por dependencia son valiosos, pero ninguno permite
  seguir el recorrido de *una* petición y aislar el salto que falla.
- **Auditoría de lecturas y escrituras** en un registro inmutable. Auditar solo escrituras sería más barato,
  pero dejaría sin respuesta la mitad del reclamo: el estudiante sospecha que su información fue
  *consultada*.
- **La asignación asesor ↔ estudiante se guarda con vigencia histórica**, no solo su estado actual. Sin
  fechas de inicio y fin, un acceso legítimo del pasado se vuelve indistinguible de uno indebido en cuanto
  la asignación cambia.

La herramienta desarrolló cada decisión en sus implicaciones y aportó el marco normativo aplicable
(Ley 1581 de 2012 sobre protección de datos personales).

### Versionado

Todos los *commits* y la creación del repositorio los ejecuté yo. La herramienta propuso la estrategia de
ramas y la convención de mensajes.

---

## Sobre el propósito

Usé la herramienta para **contrastar decisiones y acelerar la producción**, no para reemplazar el criterio.
En cada punto de diseño pedí las alternativas con sus contrapartidas antes de decidir, y la redacción y el
código se produjeron sobre esas decisiones.

El caso más claro es el de los microservicios: la recomendación era otra y la descarté con un argumento
sobre el alcance del sistema. Ese tipo de intercambio —y no la generación de texto— es donde la herramienta
aportó más valor.
