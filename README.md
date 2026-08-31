# Prueba Técnica · Semillero — Ingeniero de Arquitectura e Innovación TI

Universidad Icesi · Oficina de Arquitectura e Innovación de TI

Caso: **Vista 360° del Estudiante**, una plataforma nueva que centraliza la información relevante de
cada estudiante apoyándose en el ecosistema institucional existente.

---

## Entregables

| Parte | Contenido | Dónde |
|---|---|---|
| **1 · Diseño de la solución** | Tres diagramas de arquitectura y el documento de decisiones y supuestos | [`parte1-arquitectura/`](parte1-arquitectura/) |
| **2 · Servicio** | Especificación, modelo de datos e implementación de `svc-academico` | [`parte2-svc-academico/`](parte2-svc-academico/) |
| **3 · Seguridad y comunicación** | Autenticación, autorización y los escenarios A y B | [`parte3-4-seguridad-operacion/`](parte3-4-seguridad-operacion/) |
| **4 · Operación y calidad** | Incidente intermitente y reclamo por acceso indebido | [`parte3-4-seguridad-operacion/`](parte3-4-seguridad-operacion/) |

## Parte 1 · Diseño

| Vista | Qué responde |
|---|---|
| [Contexto](parte1-arquitectura/diagramas/out/01-contexto.png) | Cómo encaja Vista 360° en el ecosistema sin reemplazar nada |
| [Contenedores](parte1-arquitectura/diagramas/out/02-contenedores.png) | Qué piezas la componen y cómo se comunican |
| [Eventos y data warehouse](parte1-arquitectura/diagramas/out/03-eventos-dw.png) | Cómo se propaga un cambio y cómo se alimenta el DW |

Decisiones, supuestos declarados y riesgos: [`DECISIONES.md`](parte1-arquitectura/DECISIONES.md).

Los diagramas son código (PlantUML) y se regeneran con:

```bash
plantuml -tpng -o out parte1-arquitectura/diagramas/*.puml
```

## Parte 2 · Servicio

`svc-academico` devuelve las materias matriculadas y las notas actuales de un estudiante.
Es el mismo servicio marcado en el diagrama de contenedores de la Parte 1.

```bash
cd parte2-svc-academico
docker compose up -d
./mvnw spring-boot:run
```

Detalle del contrato, el modelo de datos, las pruebas y el plan de lo pendiente:
[`parte2-svc-academico/README.md`](parte2-svc-academico/README.md).

## Partes 3 y 4 · Seguridad, comunicación, operación y calidad

Respuestas argumentadas en [`RESPUESTAS.md`](parte3-4-seguridad-operacion/RESPUESTAS.md), apoyadas en dos
diagramas de secuencia:

| Vista | Qué responde |
|---|---|
| [Autenticación y autorización](parte3-4-seguridad-operacion/diagramas/out/04-autenticacion-autorizacion.png) | Cómo se autentica el usuario y dónde se decide qué puede ver, y sobre quién |
| [Escenario financiero](parte3-4-seguridad-operacion/diagramas/out/05-escenario-financiero.png) | Cómo se resuelve la consulta en vivo y qué pasa cuando el ERP no responde |

---

## Cómo está versionado

El repositorio sigue un flujo tipo **Git Flow**:

| Rama | Propósito |
|---|---|
| `main` | Entregas. Cada parte terminada llega aquí con su etiqueta (`v1.0-parte1`, `v1.0-parte2`, …) |
| `develop` | Integración del trabajo en curso |
| `feature/*` | Una rama por entregable, integrada a `develop` con *merge* sin *fast-forward* |

Los *merges* usan `--no-ff` a propósito: conservan la agrupación de cada entregable en el historial,
que es justamente lo que se pierde con un *fast-forward*. Los mensajes siguen Conventional Commits.

## Cómo ejecutar y verificar

Guía completa en [`GUIA-DE-EJECUCION.md`](GUIA-DE-EJECUCION.md): requisitos, ejecución, casos de prueba
sugeridos y resolución de problemas.

> La Parte 2 es un **servicio backend (API REST)**, no una aplicación con interfaz gráfica. Se consume por
> HTTP; la documentación interactiva del contrato está en Swagger UI.

La ruta más corta:

```bash
cd parte2-svc-academico && export JAVA_HOME=$(/usr/libexec/java_home -v 21) && ./mvnw verify
```

> Requiere **JDK 21+** y Docker corriendo. Con un JDK anterior el *build* falla a propósito con un mensaje
> que explica qué hacer.

## Uso de herramientas de IA

Se usó **Claude (Claude Code)**. La declaración completa —qué hizo la herramienta y qué decisiones tomé
yo, parte por parte— está en [`USO-DE-IA.md`](USO-DE-IA.md).
