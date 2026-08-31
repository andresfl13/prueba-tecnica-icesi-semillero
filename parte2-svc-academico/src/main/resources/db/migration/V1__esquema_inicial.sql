-- =====================================================================================
-- svc-academico · esquema inicial
--
-- Este servicio mantiene una REPLICA DE SOLO LECTURA de los datos academicos cuya
-- fuente de verdad es el ERP institucional (ver Parte 1, decisiones D2 y D5).
-- Por eso cada tabla replicada lleva 'origen' y 'actualizado_en': el servicio debe
-- poder responder no solo "cual es el dato" sino "que tan fresco es".
-- =====================================================================================

CREATE TABLE periodo_academico (
    id           BIGSERIAL PRIMARY KEY,
    codigo       VARCHAR(10)  NOT NULL UNIQUE,
    nombre       VARCHAR(60)  NOT NULL,
    fecha_inicio DATE         NOT NULL,
    fecha_fin    DATE         NOT NULL,
    vigente      BOOLEAN      NOT NULL DEFAULT FALSE,
    CONSTRAINT ck_periodo_fechas CHECK (fecha_fin > fecha_inicio)
);

COMMENT ON TABLE periodo_academico IS 'Periodos academicos. El vigente define que significa "actualmente" en el contrato del servicio.';

-- Invariante: a lo sumo un periodo vigente a la vez. Un indice unico parcial lo
-- garantiza en la base y no en el codigo, que es donde debe estar.
CREATE UNIQUE INDEX ux_periodo_unico_vigente ON periodo_academico (vigente) WHERE vigente;


CREATE TABLE estudiante (
    id                   BIGSERIAL PRIMARY KEY,
    codigo_institucional VARCHAR(20)  NOT NULL UNIQUE,
    nombres              VARCHAR(120) NOT NULL,
    apellidos            VARCHAR(120) NOT NULL,
    programa             VARCHAR(120) NOT NULL,
    semestre             SMALLINT     CHECK (semestre BETWEEN 1 AND 20),
    estado_academico     VARCHAR(30)  NOT NULL,
    origen               VARCHAR(20)  NOT NULL DEFAULT 'ERP',
    actualizado_en       TIMESTAMPTZ  NOT NULL DEFAULT now()
);

COMMENT ON COLUMN estudiante.codigo_institucional IS 'Identificador publico del estudiante (p.ej. A00123456). Es el que viaja en la URL; la PK interna nunca se expone.';


CREATE TABLE materia (
    id       BIGSERIAL PRIMARY KEY,
    codigo   VARCHAR(20)  NOT NULL UNIQUE,
    nombre   VARCHAR(160) NOT NULL,
    creditos SMALLINT     NOT NULL CHECK (creditos > 0)
);

COMMENT ON TABLE materia IS 'Catalogo de materias, independiente del periodo. Separarlo de "grupo" evita duplicar la materia en cada semestre.';


CREATE TABLE grupo (
    id           BIGSERIAL PRIMARY KEY,
    materia_id   BIGINT      NOT NULL REFERENCES materia (id),
    periodo_id   BIGINT      NOT NULL REFERENCES periodo_academico (id),
    codigo_grupo VARCHAR(10) NOT NULL,
    docente      VARCHAR(160),
    CONSTRAINT ux_grupo UNIQUE (materia_id, periodo_id, codigo_grupo)
);

COMMENT ON TABLE grupo IS 'Oferta concreta de una materia en un periodo. Es lo que el estudiante matricula, no la materia abstracta.';


CREATE TABLE matricula (
    id              BIGSERIAL PRIMARY KEY,
    estudiante_id   BIGINT      NOT NULL REFERENCES estudiante (id),
    grupo_id        BIGINT      NOT NULL REFERENCES grupo (id),
    estado          VARCHAR(20) NOT NULL,
    fecha_matricula DATE        NOT NULL,
    origen          VARCHAR(20) NOT NULL DEFAULT 'ERP',
    actualizado_en  TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT ux_matricula UNIQUE (estudiante_id, grupo_id),
    CONSTRAINT ck_matricula_estado CHECK (estado IN ('INSCRITA', 'CANCELADA', 'RETIRADA'))
);

-- El servicio siempre filtra por estudiante y estado: ese es el indice que importa.
CREATE INDEX ix_matricula_estudiante_estado ON matricula (estudiante_id, estado);


CREATE TABLE evaluacion (
    id         BIGSERIAL PRIMARY KEY,
    grupo_id   BIGINT       NOT NULL REFERENCES grupo (id),
    nombre     VARCHAR(80)  NOT NULL,
    porcentaje NUMERIC(5, 2) NOT NULL CHECK (porcentaje > 0 AND porcentaje <= 100),
    orden      SMALLINT     NOT NULL,
    CONSTRAINT ux_evaluacion UNIQUE (grupo_id, nombre)
);

COMMENT ON TABLE evaluacion IS 'Esquema de evaluacion del grupo (parciales, talleres, final) con su peso. Vive en el grupo porque cada docente define el suyo.';


CREATE TABLE nota (
    id             BIGSERIAL PRIMARY KEY,
    matricula_id   BIGINT       NOT NULL REFERENCES matricula (id),
    evaluacion_id  BIGINT       NOT NULL REFERENCES evaluacion (id),
    valor          NUMERIC(3, 2) NOT NULL CHECK (valor >= 0 AND valor <= 5),
    fecha_registro TIMESTAMPTZ  NOT NULL,
    origen         VARCHAR(20)  NOT NULL DEFAULT 'ERP',
    actualizado_en TIMESTAMPTZ  NOT NULL DEFAULT now(),
    CONSTRAINT ux_nota UNIQUE (matricula_id, evaluacion_id)
);

-- DECISION DE MODELADO: la nota cuelga de (matricula, evaluacion) y NO de
-- (estudiante, materia). Una nota solo existe en el contexto de una inscripcion
-- concreta: si el estudiante repite la materia en otro periodo, son dos matriculas
-- distintas con sus propias notas y el modelo no queda ambiguo.
CREATE INDEX ix_nota_matricula ON nota (matricula_id);


CREATE TABLE sincronizacion_replica (
    entidad               VARCHAR(40) PRIMARY KEY,
    ultima_sincronizacion TIMESTAMPTZ NOT NULL,
    origen                VARCHAR(20) NOT NULL,
    registros_afectados   INTEGER
);

COMMENT ON TABLE sincronizacion_replica IS 'Marca de frescura de la replica. Alimenta el bloque "frescura" de la respuesta: el consumidor debe saber que esta viendo una copia y de cuando.';
