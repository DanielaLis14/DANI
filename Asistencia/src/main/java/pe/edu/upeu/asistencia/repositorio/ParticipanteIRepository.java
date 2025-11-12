package pe.edu.upeu.asistencia.repositorio;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pe.edu.upeu.asistencia.modelo.Participante;

@Repository
public interface ParticipanteIRepository extends JpaRepository<Participante,String> {
}