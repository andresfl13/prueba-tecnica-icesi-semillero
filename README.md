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
| **3 · Seguridad y comunicación** | Respuestas argumentadas a los escenarios A y B | *pendiente* |
| **4 · Operación y calidad** | Respuestas argumentadas a los escenarios A y B | *pendiente* |

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

La secuencia completa de comandos está en [`VERSIONADO.md`](VERSIONADO.md).

## Uso de herramientas de IA

> Ajustar antes de entregar.

Se usó **Claude (Claude Code)** como apoyo en la exploración de alternativas de arquitectura, la redacción
de la documentación y la generación del código de diagramas, migraciones, entidades y pruebas. Las
decisiones de diseño fueron tomadas y validadas por el autor. El detalle por parte está declarado al final
de cada documento.
