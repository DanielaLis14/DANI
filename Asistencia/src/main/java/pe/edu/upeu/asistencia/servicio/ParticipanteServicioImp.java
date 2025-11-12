package pe.edu.upeu.asistencia.servicio;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pe.edu.upeu.asistencia.modelo.Participante;
import pe.edu.upeu.asistencia.repositorio.ParticipanteIRepository;

import java.util.List;

@Service
public class ParticipanteServicioImp implements ParticipanteServicioI {
    @Autowired
    ParticipanteIRepository participanteRepository;
    @Override
    public void save(Participante participante) {
        participanteRepository.save(participante);
    }
    @Override
    public Participante update(Participante participante) {
        return participanteRepository.save(participante);
    }
    @Override
    public void delete(String dni) {
        participanteRepository.deleteById(dni);
    }
    @Override
    public Participante findById(String dni) {
        return participanteRepository.findById(dni).orElse(null);
    }
    @Override
    public List<Participante> findAll(){
        return participanteRepository.findAll();
    }
}