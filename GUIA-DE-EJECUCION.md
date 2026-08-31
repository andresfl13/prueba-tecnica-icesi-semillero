# Guía de ejecución y verificación

Este documento indica cómo ejecutar y comprobar la entrega. Los comandos aquí descritos fueron ejecutados
y verificados sobre macOS con JDK 21 y Docker.

---

## 1. Qué se entrega

| Parte | Naturaleza del entregable | Cómo se revisa |
|---|---|---|
| **1 · Diseño de la solución** | Tres diagramas de arquitectura y un documento de decisiones | Lectura |
| **2 · Servicio** | **Servicio backend: una API REST.** Código ejecutable | Ejecución y consumo por HTTP |
| **3 · Seguridad y comunicación** | Respuestas argumentadas y dos diagramas de secuencia | Lectura |
| **4 · Operación y calidad** | Respuestas argumentadas | Lectura |

> ### Sobre la ausencia de interfaz gráfica
>
> La Parte 2 es **un servicio, no una aplicación con pantallas**. El enunciado pide construir un servicio
> que, dado el identificador de un estudiante, devuelva sus materias matriculadas y sus notas; el
> entregable es por tanto una API que se consume por HTTP y se verifica con un cliente REST, un navegador
> o las pruebas automatizadas.
>
> La interfaz que sí se puede abrir en el navegador es **Swagger UI**, que documenta el contrato y permite
> ejecutar el endpoint desde la propia página. No es una interfaz de la aplicación: es la documentación
> viva del servicio.
>
> El portal web para estudiantes y personal de acompañamiento forma parte del **diseño** de la Parte 1
> —aparece en el diagrama de contenedores como una SPA— y está declarado explícitamente fuera del alcance
> implementado, junto con el resto de lo pendiente, en la sección 5 del README del servicio.

---

## 2. Requisitos

| Requisito | Verificación | Si no está disponible |
|---|---|---|
| **JDK 21 o superior** | `java -version` | Ver la nota siguiente |
| **Docker en ejecución** | `docker info` | Iniciar Docker Desktop |
| Maven | No se requiere: el proyecto incluye `./mvnw` | — |

> **Nota sobre la versión de Java.** El proyecto compila con Java 21. Si el JDK activo es anterior, la
> compilación se detiene con un mensaje que lo indica de forma explícita. En macOS, para fijar la versión
> en la terminal actual:
>
> ```bash
> export JAVA_HOME=$(/usr/libexec/java_home -v 21)
> ```

---

## 3. Verificación rápida · pruebas automatizadas

Es la vía más corta para comprobar que todo funciona. No requiere levantar la base de datos manualmente:
Testcontainers crea y destruye su propia instancia de PostgreSQL.

```bash
cd parte2-svc-academico
export JAVA_HOME=$(/usr/libexec/java_home -v 21)
./mvnw verify
```

Resultado esperado:

```
Tests run: 4, Failures: 0, Errors: 0, Skipped: 0   <- contrato HTTP
Tests run: 8, Failures: 0, Errors: 0, Skipped: 0   <- integración contra PostgreSQL real
BUILD SUCCESS
```

La primera ejecución descarga la imagen de PostgreSQL y tarda más; las siguientes toman alrededor de
quince segundos.

### Qué cubre cada prueba

| Prueba | Comportamiento verificado |
|---|---|
| `devuelveLaCargaAcademica` | La respuesta incluye estudiante, periodo, materias, resumen y frescura |
| `rechazaCodigoInvalido` | Un código con formato inválido produce `400`, no `500` |
| `devuelve404SiNoExiste` | El error se emite en formato RFC 9457 |
| `excluyeMatriculasCanceladas` | Una materia cancelada no aparece en la carga vigente |
| `noMezclaPeriodos` | La carga de un periodo anterior no se mezcla con la del vigente |
| `calculaLaNotaAcumuladaSobreLoEvaluado` | La nota se pondera contra lo evaluado: 4.40, no 1.98 |
| `materiaSinNotas` | Una materia sin calificaciones reporta acumulada nula y 0 % evaluado |
| `estudianteSinMatriculas` | Un estudiante existente sin matrículas responde `200` con lista vacía, no `404` |

---

## 4. Ejecución del servicio

### 4.1 · Base de datos

```bash
cd parte2-svc-academico
docker compose up -d
```

Verificación:

```bash
docker compose ps
```

> **Si el puerto 5432 ya está ocupado** por otra instancia de PostgreSQL, se obtiene
> `address already in use`. Basta con exportar otro puerto **antes de ambos comandos**, en la misma
> terminal:
>
> ```bash
> export DB_PORT=5433
> ```
>
> Esta variable gobierna a la vez el puerto que publica el contenedor y el que utiliza la aplicación.

### 4.2 · Arranque

```bash
export JAVA_HOME=$(/usr/libexec/java_home -v 21)
./mvnw spring-boot:run
```

El servicio está disponible cuando aparece `Started SvcAcademicoApplication`. El esquema y los datos de
ejemplo los aplica **Flyway durante el arranque**, no el `docker-compose`: de este modo el mismo camino de
migración se ejecuta en local y en un despliegue real.

### 4.3 · Consumo

**Desde el navegador:**

| Recurso | URL |
|---|---|
| Swagger UI, con ejecución interactiva del endpoint | http://localhost:8081/swagger-ui.html |
| El endpoint directo | http://localhost:8081/api/v1/estudiantes/A00398123/carga-academica |
| Estado del servicio | http://localhost:8081/actuator/health |

**Desde la terminal:**

```bash
# Caso principal
curl -s http://localhost:8081/api/v1/estudiantes/A00398123/carga-academica | python3 -m json.tool

# Periodo anterior
curl -s "http://localhost:8081/api/v1/estudiantes/A00398123/carga-academica?periodo=2025-02" | python3 -m json.tool

# Manejo de errores
curl -s -w "\nHTTP %{http_code}\n" http://localhost:8081/api/v1/estudiantes/A00999999/carga-academica
curl -s -w "\nHTTP %{http_code}\n" http://localhost:8081/api/v1/estudiantes/abc/carga-academica
```

**Desde Postman:**

1. *Import* → `parte2-svc-academico/postman/svc-academico.postman_collection.json`
2. *Run collection*

Las siete peticiones incluyen sus propias aserciones, de modo que el resultado se lee directamente sin
inspeccionar el JSON. También puede ejecutarse sin abrir Postman:

```bash
npx newman run parte2-svc-academico/postman/svc-academico.postman_collection.json
```

### 4.4 · Detener

Con `Ctrl+C` se detiene el servicio. Para bajar la base de datos, **desde `parte2-svc-academico`**:

```bash
docker compose down -v
```

El indicador `-v` elimina también el volumen, de modo que el siguiente arranque parte de datos limpios.
Para conservarlos, `docker compose stop`.

---

## 5. Casos de prueba sugeridos

Los datos de ejemplo están construidos para ejercitar los límites del contrato, no solo el camino feliz:

| Consulta | Resultado esperado | Qué demuestra |
|---|---|---|
| `A00398123` | 3 materias | `TEL-3100` no aparece: su matrícula está cancelada |
| `A00398123?periodo=2025-02` | Solo `MAT-1013` | La carga de periodos distintos no se mezcla |
| `A00377012` | 1 materia, sin `notaAcumulada` | Una materia sin notas no reporta un promedio inventado |
| `A00412998` | `200` con `materias: []` | Existir y no tener carga son situaciones distintas |
| `A00999999` | `404` con `"recurso": "estudiante"` | El error es interpretable por una máquina, no solo legible |
| `abc` | `400` describiendo el formato | La validación actúa antes de consultar la base |
| Cualquiera | Bloque `frescura` presente | El servicio declara que sirve una réplica y de cuándo data |

**Cálculo verificable manualmente.** En `TIC-3011` hay dos notas: Parcial 1 = 4.30 (30 %) y
Talleres = 4.60 (15 %).

```
(4.30 × 30 + 4.60 × 15) / 45 = 4.40   ← valor devuelto
 4.30 × 0.30 + 4.60 × 0.15  = 1.98    ← valor que devolvería una suma sin ponderar
```

La respuesta incluye además `porcentajeEvaluado: 45.0`, porque una nota de 4.40 sobre el 45 % del curso no
tiene el mismo significado que sobre el 95 %.

---

## 6. Recorrido por los entregables documentales

| Parte | Documento | Diagramas |
|---|---|---|
| **1** | [`parte1-arquitectura/DECISIONES.md`](parte1-arquitectura/DECISIONES.md) — decisiones, supuestos y riesgos | Contexto, contenedores y flujo de eventos, en `parte1-arquitectura/diagramas/out/` |
| **2** | [`parte2-svc-academico/README.md`](parte2-svc-academico/README.md) — contrato, modelo de datos y alcance pendiente | Modelo de datos, en `parte2-svc-academico/docs/` |
| **3 y 4** | [`parte3-4-seguridad-operacion/RESPUESTAS.md`](parte3-4-seguridad-operacion/RESPUESTAS.md) | Autenticación y escenario financiero, en `parte3-4-seguridad-operacion/diagramas/out/` |

Los diagramas están versionados como código PlantUML, no como imágenes sueltas. Para regenerarlos:

```bash
plantuml -tpng -o out parte1-arquitectura/diagramas/*.puml
```

La declaración de uso de herramientas de IA está en [`USO-DE-IA.md`](USO-DE-IA.md).

---

## 7. Resolución de problemas

| Síntoma | Causa | Solución |
|---|---|---|
| `release version 21 not supported` | El JDK activo es anterior al 21 | `export JAVA_HOME=$(/usr/libexec/java_home -v 21)` |
| `Este proyecto requiere JDK 21 o superior` | La misma causa, detectada con un mensaje explícito | Igual que la anterior |
| `Ports are not available: 5432` | Existe otro PostgreSQL en ese puerto | `export DB_PORT=5433` antes de levantar el contenedor y el servicio |
| `password authentication failed for user "vista360"` | La aplicación está conectando a otro PostgreSQL: se cambió el puerto solo para el contenedor | Exportar `DB_PORT` en la misma terminal, antes de `./mvnw spring-boot:run` |
| `Could not find a valid Docker environment` | Docker no está en ejecución | Iniciar Docker Desktop |
| `no configuration file provided: not found` | El comando se ejecutó fuera de `parte2-svc-academico`, donde reside el `docker-compose.yml` | Situarse en esa carpeta. El mensaje indica que no se encontró el archivo, no que se haya detenido algo; el estado real se consulta con `docker ps` |
| `Schema validation: missing table` | Flyway no alcanzó a aplicar las migraciones | `docker compose down -v` y arrancar de nuevo |
| `Port 8081 was already in use` | Hay otra instancia del servicio activa | `lsof -nP -iTCP:8081 -sTCP:LISTEN` para identificarla, o arrancar con `SERVER_PORT=8082 ./mvnw spring-boot:run` |
| Respuestas `400` inesperadas en Swagger | El servicio está iniciando o deteniéndose | Comprobar `http://localhost:8081/actuator/health`: debe responder `200` antes de usar la interfaz |
