package pe.edu.upeu.farmafx.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import pe.edu.upeu.farmafx.model.Perfil;
import pe.edu.upeu.farmafx.repository.ICrudGenericRepository;
import pe.edu.upeu.farmafx.repository.PerfilRepository;
import pe.edu.upeu.farmafx.service.IPerfilService;

@Service
@RequiredArgsConstructor
public class PerfilServiceImpl extends CrudGenericServiceImpl<Perfil, Long> implements IPerfilService {

    private final PerfilRepository perfilRepository;

    @Override
    protected ICrudGenericRepository<Perfil, Long> getRepository() {
        return perfilRepository;
    }
}
