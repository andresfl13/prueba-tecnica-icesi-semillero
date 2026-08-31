-- =====================================================================================
-- Datos de ejemplo.
--
-- Simulan lo que la sincronizacion contra el ERP habria dejado en la replica.
-- El conjunto esta armado a proposito para ejercitar los casos limite del servicio:
--   * un estudiante con matricula CANCELADA -> no debe aparecer en la respuesta
--   * un estudiante con carga en un periodo anterior -> no debe mezclarse con la actual
--   * materias con evaluaciones aun sin calificar -> el porcentaje evaluado es < 100
--   * un estudiante sin ninguna matricula vigente -> respuesta valida con lista vacia
-- =====================================================================================

INSERT INTO periodo_academico (codigo, nombre, fecha_inicio, fecha_fin, vigente) VALUES
    ('2025-02', 'Segundo semestre 2025', '2025-07-21', '2025-11-29', FALSE),
    ('2026-01', 'Primer semestre 2026',  '2026-01-19', '2026-05-23', TRUE);

INSERT INTO estudiante (codigo_institucional, nombres, apellidos, programa, semestre, estado_academico, actualizado_en) VALUES
    ('A00398123', 'Andres Felipe',  'Lopez Reyes',    'Ingenieria de Sistemas',  7, 'ACTIVO',              now() - INTERVAL '4 minutes'),
    ('A00401556', 'Valentina',      'Muriel Ocampo',  'Ingenieria Telematica',   6, 'ACTIVO',              now() - INTERVAL '4 minutes'),
    ('A00377012', 'Juan Sebastian', 'Arboleda Rojas', 'Ingenieria de Sistemas',  9, 'PRUEBA_ACADEMICA',    now() - INTERVAL '4 minutes'),
    ('A00412998', 'Mariana',        'Castro Gil',     'Ingenieria de Sistemas',  1, 'ACTIVO',              now() - INTERVAL '4 minutes');

INSERT INTO materia (codigo, nombre, creditos) VALUES
    ('TIC-3011', 'Computacion en Internet II',        3),
    ('TIC-4001', 'Arquitectura de Software',          3),
    ('TIC-2022', 'Estructuras de Datos y Algoritmos', 4),
    ('TIC-3502', 'Bases de Datos Avanzadas',          3),
    ('MAT-1013', 'Calculo Diferencial',               4),
    ('TEL-3100', 'Redes de Computadores',             3);

-- Oferta del periodo vigente
INSERT INTO grupo (materia_id, periodo_id, codigo_grupo, docente)
SELECT m.id, p.id, g.codigo_grupo, g.docente
FROM (VALUES
    ('TIC-3011', '2026-01', '01', 'Carlos Andres Delgado'),
    ('TIC-4001', '2026-01', '01', 'Diana Marcela Ruiz'),
    ('TIC-2022', '2026-01', '02', 'Luis Eduardo Munera'),
    ('TIC-3502', '2026-01', '01', 'Angela Maria Villegas'),
    ('TEL-3100', '2026-01', '01', 'Oscar Ivan Bedoya'),
    ('MAT-1013', '2025-02', '03', 'Patricia Solano')
) AS g (codigo_materia, codigo_periodo, codigo_grupo, docente)
JOIN materia m ON m.codigo = g.codigo_materia
JOIN periodo_academico p ON p.codigo = g.codigo_periodo;

-- Matriculas
INSERT INTO matricula (estudiante_id, grupo_id, estado, fecha_matricula, actualizado_en)
SELECT e.id, gr.id, x.estado, x.fecha::DATE, now() - INTERVAL '4 minutes'
FROM (VALUES
    -- A00398123: caso principal. Tres materias vigentes + una cancelada que NO debe salir.
    ('A00398123', 'TIC-3011', '2026-01', 'INSCRITA',  '2026-01-15'),
    ('A00398123', 'TIC-4001', '2026-01', 'INSCRITA',  '2026-01-15'),
    ('A00398123', 'TIC-3502', '2026-01', 'INSCRITA',  '2026-01-16'),
    ('A00398123', 'TEL-3100', '2026-01', 'CANCELADA', '2026-01-15'),
    -- A00398123 tambien curso un periodo anterior: no debe mezclarse con el vigente.
    ('A00398123', 'MAT-1013', '2025-02', 'INSCRITA',  '2025-07-18'),
    -- A00401556: carga vigente propia.
    ('A00401556', 'TEL-3100', '2026-01', 'INSCRITA',  '2026-01-14'),
    ('A00401556', 'TIC-2022', '2026-01', 'INSCRITA',  '2026-01-14'),
    -- A00377012: una sola materia, sin ninguna nota registrada todavia.
    ('A00377012', 'TIC-2022', '2026-01', 'INSCRITA',  '2026-01-17')
    -- A00412998 queda a proposito SIN matriculas: primiparo que aun no matricula.
) AS x (codigo_estudiante, codigo_materia, codigo_periodo, estado, fecha)
JOIN estudiante e ON e.codigo_institucional = x.codigo_estudiante
JOIN materia m ON m.codigo = x.codigo_materia
JOIN periodo_academico p ON p.codigo = x.codigo_periodo
JOIN grupo gr ON gr.materia_id = m.id AND gr.periodo_id = p.id;

-- Esquema de evaluacion: cada grupo del periodo vigente suma 100%.
INSERT INTO evaluacion (grupo_id, nombre, porcentaje, orden)
SELECT gr.id, ev.nombre, ev.porcentaje, ev.orden
FROM grupo gr
JOIN periodo_academico p ON p.id = gr.periodo_id AND p.codigo = '2026-01'
CROSS JOIN (VALUES
    ('Parcial 1',  30.00, 1),
    ('Parcial 2',  30.00, 2),
    ('Proyecto',   25.00, 3),
    ('Talleres',   15.00, 4)
) AS ev (nombre, porcentaje, orden);

-- Notas registradas hasta la fecha. Solo Parcial 1 y Talleres estan calificados en
-- casi todos los grupos: el servicio debe reportar 45% evaluado, no inventar un 100%.
INSERT INTO nota (matricula_id, evaluacion_id, valor, fecha_registro, actualizado_en)
SELECT mat.id, ev.id, x.valor, x.fecha::TIMESTAMPTZ, now() - INTERVAL '4 minutes'
FROM (VALUES
    ('A00398123', 'TIC-3011', 'Parcial 1', 4.30, '2026-03-06 15:20:00-05'),
    ('A00398123', 'TIC-3011', 'Talleres',  4.60, '2026-03-20 09:05:00-05'),
    ('A00398123', 'TIC-4001', 'Parcial 1', 3.80, '2026-03-05 11:40:00-05'),
    ('A00398123', 'TIC-4001', 'Talleres',  4.10, '2026-03-19 16:00:00-05'),
    ('A00398123', 'TIC-3502', 'Parcial 1', 2.90, '2026-03-07 10:10:00-05'),
    ('A00401556', 'TEL-3100', 'Parcial 1', 4.05, '2026-03-06 14:00:00-05'),
    ('A00401556', 'TIC-2022', 'Parcial 1', 3.50, '2026-03-06 08:30:00-05'),
    ('A00401556', 'TIC-2022', 'Talleres',  4.80, '2026-03-21 17:45:00-05')
    -- A00377012 no tiene notas: su materia sale con 0% evaluado y sin definitiva parcial.
) AS x (codigo_estudiante, codigo_materia, nombre_evaluacion, valor, fecha)
JOIN estudiante e ON e.codigo_institucional = x.codigo_estudiante
JOIN materia m ON m.codigo = x.codigo_materia
JOIN periodo_academico p ON p.codigo = '2026-01'
JOIN grupo gr ON gr.materia_id = m.id AND gr.periodo_id = p.id
JOIN matricula mat ON mat.estudiante_id = e.id AND mat.grupo_id = gr.id
JOIN evaluacion ev ON ev.grupo_id = gr.id AND ev.nombre = x.nombre_evaluacion;

INSERT INTO sincronizacion_replica (entidad, ultima_sincronizacion, origen, registros_afectados) VALUES
    ('academico', now() - INTERVAL '4 minutes', 'ERP', 8);
