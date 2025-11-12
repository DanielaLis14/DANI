package pe.edu.upeu.sysventas.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import pe.edu.upeu.sysventas.model.Emisor;
import pe.edu.upeu.sysventas.repository.EmisorRepository;
import pe.edu.upeu.sysventas.repository.ICrudGenericRepository;
import pe.edu.upeu.sysventas.service.IEmisorService;
@RequiredArgsConstructor
@Service
public class EmisorServiceImp extends CrudGenericServiceImp<Emisor, Long> implements IEmisorService {
    private final EmisorRepository emisorRepository;

    @Override
    protected ICrudGenericRepository<Emisor, Long> getRepo() {
        return null;
    }
}
