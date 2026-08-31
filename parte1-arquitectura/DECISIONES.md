# Parte 1 · Diseño de la solución — Vista 360° del Estudiante

Documento de decisiones y supuestos. Los diagramas están en `diagramas/out/`.

| Diagrama | Qué responde |
|---|---|
| `01-contexto.png` | Cómo encaja Vista 360° en el ecosistema sin reemplazar nada |
| `02-contenedores.png` | Qué piezas la componen y cómo se comunican entre sí |
| `03-eventos-dw.png` | Cómo se propaga un cambio y cómo se alimenta el data warehouse |

---

## 1. Resumen de la solución

Vista 360° es una **capa propia** sobre el ecosistema existente. No reemplaza al ERP ni al LMS: los consume
a través de la plataforma de integración, y **solo es dueña de los datos que hoy no existen en ningún sistema**
(reportes de acompañamiento, alertas, solicitudes y la asignación asesor ↔ estudiante).

La regla que sostiene todo el diseño:

> **Síncrono para lo que el usuario está esperando en pantalla. Asíncrono para lo que debe propagarse.**

---

## 2. Supuestos declarados

El caso es deliberadamente abierto. Estos son los supuestos que asumí, por qué son razonables y **qué se cae si
resultan falsos** — que es la parte que importa.

| # | Supuesto | Por qué es razonable | Si fuera falso |
|---|---|---|---|
| S1 | ~20.000 estudiantes activos y ~300 personas de acompañamiento, con picos en matrícula y publicación de notas | Tamaño típico de una universidad privada colombiana | Con volúmenes mucho menores, la separación en microservicios dejaría de justificarse |
| S2 | **El ERP no emite eventos por sí solo.** La plataforma de integración los deriva por CDC sobre la BD o por sondeo programado | El enunciado dice "algunas APIs y acceso a la BD", no menciona eventos | Se cae todo el refresco por eventos; quedaría solo sincronización programada, con más desfase |
| S3 | La asignación asesor ↔ estudiante **no existe hoy** en ningún sistema | El enunciado dice que los registros de acompañamiento son nuevos | Si el ERP ya la tuviera, se replica igual que la matrícula y `svc-acompanamiento` deja de ser su dueño |
| S4 | La plataforma de identidad soporta OIDC para usuarios y OAuth2 *client credentials* para servicio a servicio | El enunciado dice "estándares abiertos de identidad" | Habría que introducir un emisor de tokens propio para el tráfico interno |
| S5 | El acceso directo a la BD del ERP existe, pero **queda reservado a la plataforma de integración** | Es una decisión mía, no un hecho del enunciado | — |
| S6 | El data warehouse ya tiene procesos de ingesta propios contra el ERP y el LMS | Es un repositorio analítico "del ecosistema", anterior a Vista 360° | Vista 360° tendría que asumir una ingesta que no le corresponde |

---

## 3. Decisiones de arquitectura

### D1 · Microservicios delimitados por propiedad del dato, no por capa técnica

**Decisión.** Cinco servicios con BD propia: `academico`, `financiero`, `actividad`, `acompanamiento`, `alertas`,
más un BFF agregador que no es dueño de nada.

**Justificación.** El corte no es "porque escala": cada frontera coincide con **un dueño de dato distinto y un
perfil de cambio distinto**. `svc-financiero` no persiste y depende de la disponibilidad del ERP; `svc-alertas`
reacciona a eventos y no a usuarios; `svc-acompanamiento` es el único que escribe datos de los que es fuente de
verdad. Meterlos en un mismo despliegue acoplaría ciclos de vida que no tienen razón de ir juntos.

**Alternativa descartada.** Monolito modular. Es más barato de operar y sería mi elección para un piloto, pero la
plataforma es institucional y de uso transversal: el costo operativo se justifica.

### D2 · Estrategia de datos híbrida, decidida dato por dato

**Decisión.** Lectura en vivo para el estado financiero; réplica local refrescada por eventos para lo académico y
la actividad; BD propia para lo que nace en Vista 360°.

**Justificación.** No hay una respuesta única correcta: depende del **costo de que el dato esté desactualizado**.
Un saldo obsoleto produce un reclamo formal del estudiante; una nota con dos minutos de desfase, no. Replicar
todo sería rápido y resiliente pero riesgoso en lo financiero; consultar todo en vivo acoplaría la disponibilidad
de Vista 360° a la de un ERP on-premise que no fue diseñado para esa carga de lectura.

### D3 · Toda la integración pasa por la plataforma de integración

**Decisión.** Ningún servicio de Vista 360° toca el ERP, su base de datos ni el LMS directamente.

**Justificación.** Es la pieza que la Universidad **ya tiene** para eso. Evita N acoplamientos punto a punto contra
un ERP on-premise, centraliza *throttling* y trazabilidad, y deja un único lugar donde absorber un cambio de
contrato del ERP.

**Costo asumido.** Introduce una dependencia de un equipo que no es el mío y un salto de latencia. Lo acepto: el
acoplamiento directo al ERP es más caro a mediano plazo que ese salto.

### D4 · BFF agregador con degradación parcial

**Decisión.** El BFF llama a los cinco servicios **en paralelo**, con timeout por servicio, y arma la respuesta con
lo que llegó a tiempo.

**Justificación.** La vista 360° es por definición una composición. Si el ERP está lento, la pantalla debe cargar
**sin la tarjeta financiera** y decirlo explícitamente, en vez de fallar entera. Cinco dependencias síncronas sin
degradación multiplican la probabilidad de fallo en vez de reducirla.

### D5 · Vista 360° es fuente de verdad únicamente de sus datos nuevos

**Decisión.** Lo replicado se marca como réplica y **nunca** se edita en Vista 360°; se muestra con su marca de
frescura ("actualizado hace X"). Lo propio vive en `svc-acompanamiento` y `svc-alertas`.

**Justificación.** Es lo que evita que la plataforma se convierta en un segundo ERP no oficial. Además hace honesta
la interfaz: el usuario sabe qué está viendo en vivo y qué es una copia.

### D6 · Cada sistema alimenta el data warehouse; Vista 360° aporta lo suyo

**Decisión.** El ERP y el LMS alimentan el DW por sus propios pipelines. Vista 360° publica al bus los eventos de
sus datos propios, y el DW los ingiere de ahí.

**Justificación.** Vista 360° no debe ser intermediaria de datos que no le pertenecen: la volvería cuello de botella
de la analítica institucional y le haría heredar sus huecos — lo que se lee en vivo (el estado financiero) no queda
persistido y por tanto nunca podría entregarlo al DW.

### D7 · `svc-alertas` separado de `svc-acompanamiento`

**Decisión.** Servicios distintos, pese a pertenecer al mismo dominio funcional.

**Justificación.** Tienen perfiles de carga opuestos: `acompanamiento` responde a personas y su tráfico sigue la
jornada laboral; `alertas` consume eventos y su pico llega cuando el ERP publica un lote de notas — un momento en
el que el equipo de acompañamiento no debería ver la plataforma degradada.

**Reconocimiento honesto.** Es la frontera más discutible del diseño. Si el volumen de reglas resultara bajo,
fusionarlos sería razonable.

---

## 4. De dónde sale cada dato

| Dato | Fuente de verdad | Cómo llega a Vista 360° | Por qué así |
|---|---|---|---|
| Identidad y roles | Plataforma de identidad | Claims del token OIDC | Nunca se replica |
| Datos personales y maestros | ERP | Réplica en `svc-academico`, refresco por evento + reconciliación nocturna | Cambia poco; se necesita para buscar y listar sin golpear el ERP |
| Programa, semestre, **estado académico** | ERP | Réplica + evento `EstadoAcademicoCambiado` | Debe disparar intervención temprana: tiene que ser *push*, no *pull* |
| Materias matriculadas | ERP | Réplica, refresco por `MatriculaActualizada` | Estable dentro del semestre, altísimo volumen de lectura |
| Notas actuales | ERP | Réplica + evento `NotaPublicada`; consulta de respaldo en vivo | Tolera minutos de desfase |
| **Estado financiero** | ERP | **Lectura en vivo** vía integración, caché 30–60 s, circuit breaker. **No se persiste** | Un saldo desactualizado genera un reclamo formal |
| Actividad en campus virtual | LMS | Sincronización incremental a `svc-actividad` | API en la nube con límite de tasa; dato indicativo, no transaccional |
| Reportes, alertas y solicitudes | **Vista 360°** | BD propia: *es* la fuente de verdad | Dato nuevo, no existe en el ecosistema |
| Asignación asesor ↔ estudiante | **Vista 360°** | BD propia | Supuesto S3; es el insumo de la autorización (Parte 3) |
| Información analítica | Data warehouse | Vista 360° no lo consulta: lo **alimenta** | Separación entre lo operacional y lo analítico |

---

## 5. Cómo se comunican los componentes

| Origen → Destino | Estilo | Protocolo | Por qué |
|---|---|---|---|
| Navegador → Portal | Síncrono | HTTPS | — |
| Portal → Plataforma de identidad | Redirección | OIDC + PKCE | El SPA no puede guardar secretos |
| Portal → BFF | Síncrono | REST/JSON + JWT | El usuario espera la respuesta |
| BFF → servicios | Síncrono **en paralelo** | REST | Con timeout por servicio y degradación parcial (D4) |
| Servicios → ecosistema | Síncrono | REST vía integración, OAuth2 *client credentials* | Único punto de entrada al ERP y al LMS (D3) |
| Ecosistema → Vista 360° | **Asíncrono** | Publicación / suscripción sobre el bus | Un cambio debe propagarse sin que nadie lo pregunte |
| Vista 360° → data warehouse | **Asíncrono** | Eventos sobre el bus | Desacopla la analítica de la operación (D6) |
| Entre servicios | Asíncrono por defecto | Eventos | La única excepción síncrona es `svc-alertas` preguntando a `svc-acompanamiento` por la asignación, porque es una consulta puntual que condiciona la acción inmediata |

---

## 6. Riesgos asumidos y cómo se mitigan

| Riesgo | Mitigación |
|---|---|
| El ERP se vuelve el cuello de botella del dato financiero en vivo | Caché de 30–60 s, circuit breaker y degradación parcial de la pantalla (D4) |
| Consistencia eventual visible al usuario en lo replicado | La interfaz muestra la marca de frescura del dato; lo financiero, que no la tolera, no se replica |
| Puede no autorizarse CDC sobre la BD del ERP (S2) | Repliegue a sondeo programado sobre las APIs que el ERP sí expone: mismo diseño, más latencia |
| Dependencia del equipo de la plataforma de integración | Contratos versionados y acordados por adelantado; los servicios traducen a su propio modelo para no heredar el del ERP |
| La separación en microservicios encarece la operación | Se acepta explícitamente por el alcance institucional (D1) |

---

## 7. Uso de herramientas de IA

> Completar antes de entregar, según lo que efectivamente hayas usado.

- **Herramienta:** Claude (Claude Code).
- **En qué partes:** exploración de alternativas de arquitectura, redacción de este documento y generación del código
  PlantUML de los tres diagramas.
- **Con qué propósito:** contrastar decisiones y sus alternativas, y acelerar la producción de los entregables.
  Las decisiones de arquitectura (microservicios, estrategia híbrida de datos, integración centralizada) fueron
  tomadas y validadas por mí.
