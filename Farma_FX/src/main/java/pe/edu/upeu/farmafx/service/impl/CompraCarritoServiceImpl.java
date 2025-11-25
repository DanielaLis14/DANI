package pe.edu.upeu.farmafx.service.impl;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.upeu.farmafx.model.CompraCarrito;
import pe.edu.upeu.farmafx.repository.CompraCarritoRepository;
import pe.edu.upeu.farmafx.repository.ICrudGenericRepository;
import pe.edu.upeu.farmafx.service.ICompraCarritoService;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CompraCarritoServiceImpl extends CrudGenericServiceImpl<CompraCarrito, Long> implements ICompraCarritoService {

    private static final Logger logger = LoggerFactory.getLogger(CompraCarritoServiceImpl.class);
    private final CompraCarritoRepository compraCarritoRepository;

    @Override
    protected ICrudGenericRepository<CompraCarrito, Long> getRepository() {
        return compraCarritoRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<CompraCarrito> listarPorUsuario(Long idUsuario) {
        if (idUsuario == null) {
            logger.debug("listarPorUsuario sin idUsuario, retorna lista vacía");
            return new ArrayList<>();
        }
        List<CompraCarrito> carrito = compraCarritoRepository.listarPorUsuario(idUsuario);
        return carrito == null ? new ArrayList<>() : carrito;
    }

    @Override
    @Transactional
    public void eliminarPorUsuario(Long idUsuario) {
        if (idUsuario == null) {
            logger.debug("eliminarPorUsuario sin idUsuario, nada que eliminar");
            return;
        }
        compraCarritoRepository.eliminarPorUsuario(idUsuario);
    }
}
