package co.edu.icesi.vista360.academico.repository;

import co.edu.icesi.vista360.academico.domain.Estudiante;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface EstudianteRepository extends JpaRepository<Estudiante, Long> {

    Optional<Estudiante> findByCodigoInstitucional(String codigoInstitucional);
}
