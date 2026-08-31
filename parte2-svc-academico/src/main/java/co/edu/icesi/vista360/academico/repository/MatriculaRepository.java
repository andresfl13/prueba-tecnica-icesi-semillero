package co.edu.icesi.vista360.academico.repository;

import co.edu.icesi.vista360.academico.domain.EstadoMatricula;
import co.edu.icesi.vista360.academico.domain.Matricula;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface MatriculaRepository extends JpaRepository<Matricula, Long> {

    /**
     * Trae la carga de un estudiante en un periodo con un unico SELECT.
     * Los {@code join fetch} son deliberados: sin ellos, recorrer las materias en el
     * servicio dispararia una consulta por matricula (problema N+1).
     */
    @Query("""
            select m from Matricula m
              join fetch m.grupo g
              join fetch g.materia mat
              join fetch g.periodo p
            where m.estudiante.id = :estudianteId
              and p.codigo = :codigoPeriodo
              and m.estado = :estado
            order by mat.codigo
            """)
    List<Matricula> buscarCarga(@Param("estudianteId") Long estudianteId,
                                @Param("codigoPeriodo") String codigoPeriodo,
                                @Param("estado") EstadoMatricula estado);
}
