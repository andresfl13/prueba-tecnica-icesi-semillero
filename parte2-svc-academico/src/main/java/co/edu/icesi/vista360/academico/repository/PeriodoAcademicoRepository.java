package co.edu.icesi.vista360.academico.repository;

import co.edu.icesi.vista360.academico.domain.PeriodoAcademico;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PeriodoAcademicoRepository extends JpaRepository<PeriodoAcademico, Long> {

    Optional<PeriodoAcademico> findByCodigo(String codigo);

    Optional<PeriodoAcademico> findByVigenteIsTrue();
}
