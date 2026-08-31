package co.edu.icesi.vista360.academico.repository;

import co.edu.icesi.vista360.academico.domain.Nota;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;

public interface NotaRepository extends JpaRepository<Nota, Long> {

    /**
     * Trae de una sola vez las notas de todas las matriculas de la carga.
     * Se resuelve en una segunda consulta y no en un join con matricula para evitar
     * el producto cartesiano entre materias y notas.
     */
    @Query("""
            select n from Nota n
              join fetch n.evaluacion e
            where n.matricula.id in :matriculaIds
            order by e.orden
            """)
    List<Nota> buscarPorMatriculas(@Param("matriculaIds") Collection<Long> matriculaIds);
}
