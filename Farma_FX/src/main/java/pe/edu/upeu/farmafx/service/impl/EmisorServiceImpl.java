package pe.edu.upeu.farmafx.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.upeu.farmafx.model.Emisor;
import pe.edu.upeu.farmafx.repository.EmisorRepository;
import pe.edu.upeu.farmafx.repository.ICrudGenericRepository;
import pe.edu.upeu.farmafx.service.IEmisorService;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class EmisorServiceImpl extends CrudGenericServiceImpl<Emisor, Long> implements IEmisorService {

    private final EmisorRepository emisorRepository;

    @Override
    protected ICrudGenericRepository<Emisor, Long> getRepository() {
        return emisorRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public Emisor buscarPorRuc(String ruc) {
        if (ruc == null || ruc.isBlank()) {
            return null;
        }
        return emisorRepository.buscarPorRuc(ruc.trim());
    }

    @Override
    @Transactional(readOnly = true)
    public List<Emisor> listarActivos() {
        List<Emisor> emisores = emisorRepository.listarPorEstado(Boolean.TRUE);
        return emisores == null ? new ArrayList<>() : emisores;
    }
}
