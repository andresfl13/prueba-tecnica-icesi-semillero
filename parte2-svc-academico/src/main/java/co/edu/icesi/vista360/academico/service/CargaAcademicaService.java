package co.edu.icesi.vista360.academico.service;

import co.edu.icesi.vista360.academico.domain.*;
import co.edu.icesi.vista360.academico.repository.*;
import co.edu.icesi.vista360.academico.web.dto.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Clock;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Resuelve la consulta de carga academica y notas de un estudiante.
 *
 * <p>El servicio es de solo lectura por diseno: la fuente de verdad es el ERP y esta
 * base es una replica (Parte 1, decisiones D2 y D5). Por eso toda respuesta incluye
 * la frescura del dato.</p>
 */
@Service
public class CargaAcademicaService {

    /** Escala 0.0 a 5.0 con dos decimales, que es como la Universidad reporta notas. */
    private static final int ESCALA_NOTA = 2;

    private static final String ENTIDAD_SINCRONIZACION = "academico";

    private final EstudianteRepository estudiantes;
    private final PeriodoAcademicoRepository periodos;
    private final MatriculaRepository matriculas;
    private final NotaRepository notas;
    private final SincronizacionReplicaRepository sincronizaciones;
    private final Clock reloj;

    public CargaAcademicaService(EstudianteRepository estudiantes,
                                 PeriodoAcademicoRepository periodos,
                                 MatriculaRepository matriculas,
                                 NotaRepository notas,
                                 SincronizacionReplicaRepository sincronizaciones,
                                 Clock reloj) {
        this.estudiantes = estudiantes;
        this.periodos = periodos;
        this.matriculas = matriculas;
        this.notas = notas;
        this.sincronizaciones = sincronizaciones;
        this.reloj = reloj;
    }

    /**
     * @param codigoEstudiante codigo institucional, p.ej. {@code A00398123}
     * @param codigoPeriodo    periodo a consultar; si es null se usa el vigente
     * @throws RecursoNoEncontradoException si el estudiante o el periodo no existen
     */
    @Transactional(readOnly = true)
    public CargaAcademicaResponse consultar(String codigoEstudiante, String codigoPeriodo) {
        Estudiante estudiante = estudiantes.findByCodigoInstitucional(codigoEstudiante)
                .orElseThrow(() -> RecursoNoEncontradoException.estudiante(codigoEstudiante));

        PeriodoAcademico periodo = resolverPeriodo(codigoPeriodo);

        // Solo la carga vigente: una matricula CANCELADA o RETIRADA no es una materia
        // que el estudiante "tenga inscrita actualmente".
        List<Matricula> carga = matriculas.buscarCarga(estudiante.getId(), periodo.getCodigo(), EstadoMatricula.INSCRITA);

        Map<Long, List<Nota>> notasPorMatricula = carga.isEmpty()
                ? Map.of()
                : notas.buscarPorMatriculas(carga.stream().map(Matricula::getId).toList())
                        .stream()
                        .collect(Collectors.groupingBy(n -> n.getMatricula().getId()));

        List<MateriaMatriculadaDto> materias = carga.stream()
                .map(matricula -> aDto(matricula, notasPorMatricula.getOrDefault(matricula.getId(), List.of())))
                .toList();

        return new CargaAcademicaResponse(
                aDto(estudiante),
                aDto(periodo),
                materias,
                resumir(materias),
                frescura());
    }

    private PeriodoAcademico resolverPeriodo(String codigoPeriodo) {
        if (codigoPeriodo == null || codigoPeriodo.isBlank()) {
            return periodos.findByVigenteIsTrue()
                    .orElseThrow(RecursoNoEncontradoException::periodoVigente);
        }
        return periodos.findByCodigo(codigoPeriodo)
                .orElseThrow(() -> RecursoNoEncontradoException.periodo(codigoPeriodo));
    }

    private MateriaMatriculadaDto aDto(Matricula matricula, List<Nota> notasDeLaMateria) {
        Grupo grupo = matricula.getGrupo();
        Materia materia = grupo.getMateria();

        List<NotaDto> notasDto = notasDeLaMateria.stream()
                .sorted(Comparator.comparing(n -> n.getEvaluacion().getOrden()))
                .map(n -> new NotaDto(
                        n.getEvaluacion().getNombre(),
                        n.getEvaluacion().getPorcentaje(),
                        n.getValor(),
                        n.getFechaRegistro()))
                .toList();

        BigDecimal porcentajeEvaluado = notasDeLaMateria.stream()
                .map(n -> n.getEvaluacion().getPorcentaje())
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(ESCALA_NOTA, RoundingMode.HALF_UP);

        return new MateriaMatriculadaDto(
                materia.getCodigo(),
                materia.getNombre(),
                materia.getCreditos(),
                grupo.getCodigoGrupo(),
                grupo.getDocente(),
                notasDto,
                calcularNotaAcumulada(notasDeLaMateria),
                porcentajeEvaluado);
    }

    /**
     * Nota acumulada sobre lo evaluado hasta ahora.
     *
     * <p>Se pondera contra el porcentaje ya evaluado y no contra 100: reportar la suma
     * cruda daria la impresion de que el estudiante va perdiendo cuando lo unico que
     * pasa es que aun faltan evaluaciones por calificar.</p>
     *
     * @return null si la materia no tiene ninguna nota registrada
     */
    private BigDecimal calcularNotaAcumulada(List<Nota> notasDeLaMateria) {
        if (notasDeLaMateria.isEmpty()) {
            return null;
        }
        BigDecimal pesoTotal = BigDecimal.ZERO;
        BigDecimal acumulado = BigDecimal.ZERO;
        for (Nota nota : notasDeLaMateria) {
            BigDecimal peso = nota.getEvaluacion().getPorcentaje();
            pesoTotal = pesoTotal.add(peso);
            acumulado = acumulado.add(nota.getValor().multiply(peso));
        }
        if (pesoTotal.compareTo(BigDecimal.ZERO) == 0) {
            return null;
        }
        return acumulado.divide(pesoTotal, ESCALA_NOTA, RoundingMode.HALF_UP);
    }

    private ResumenDto resumir(List<MateriaMatriculadaDto> materias) {
        int totalCreditos = materias.stream().mapToInt(MateriaMatriculadaDto::creditos).sum();

        List<MateriaMatriculadaDto> conNotas = materias.stream()
                .filter(m -> m.notaAcumulada() != null)
                .toList();

        BigDecimal promedio = null;
        int creditosConNota = conNotas.stream().mapToInt(MateriaMatriculadaDto::creditos).sum();
        if (creditosConNota > 0) {
            BigDecimal acumulado = conNotas.stream()
                    .map(m -> m.notaAcumulada().multiply(BigDecimal.valueOf(m.creditos())))
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            promedio = acumulado.divide(BigDecimal.valueOf(creditosConNota), ESCALA_NOTA, RoundingMode.HALF_UP);
        }

        return new ResumenDto(materias.size(), totalCreditos, promedio);
    }

    private FrescuraDto frescura() {
        Optional<SincronizacionReplica> sincronizacion = sincronizaciones.findById(ENTIDAD_SINCRONIZACION);
        if (sincronizacion.isEmpty()) {
            // La replica nunca se ha sincronizado. Se responde igual, pero el consumidor
            // debe poder distinguir "dato viejo" de "dato de origen desconocido".
            return new FrescuraDto("DESCONOCIDO", null, -1);
        }
        SincronizacionReplica registro = sincronizacion.get();
        long antiguedad = Duration.between(registro.getUltimaSincronizacion(), OffsetDateTime.now(reloj)).toSeconds();
        return new FrescuraDto(registro.getOrigen(), registro.getUltimaSincronizacion(), Math.max(antiguedad, 0));
    }

    private EstudianteDto aDto(Estudiante estudiante) {
        return new EstudianteDto(
                estudiante.getCodigoInstitucional(),
                estudiante.getNombreCompleto(),
                estudiante.getPrograma(),
                estudiante.getSemestre() == null ? null : estudiante.getSemestre().intValue(),
                estudiante.getEstadoAcademico());
    }

    private PeriodoDto aDto(PeriodoAcademico periodo) {
        return new PeriodoDto(
                periodo.getCodigo(),
                periodo.getNombre(),
                periodo.getFechaInicio(),
                periodo.getFechaFin(),
                periodo.isVigente());
    }
}
