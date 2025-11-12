package pe.edu.upeu.sysventas.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import pe.edu.upeu.sysventas.model.CompraDetalle;
import pe.edu.upeu.sysventas.model.Perfil;
import pe.edu.upeu.sysventas.repository.ICrudGenericRepository;
import pe.edu.upeu.sysventas.repository.PerfilRepository;
import pe.edu.upeu.sysventas.service.IPerfilService;

@RequiredArgsConstructor
@Service
public class PerfilServiceImp extends CrudGenericServiceImp<Perfil, Long> implements IPerfilService {

    private final PerfilRepository perfilRepository;

    @Override
    protected ICrudGenericRepository<Perfil, Long> getRepo() {
        return perfilRepository;
    }
}
