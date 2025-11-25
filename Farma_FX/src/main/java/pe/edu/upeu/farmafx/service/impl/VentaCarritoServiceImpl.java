package pe.edu.upeu.farmafx.service.impl;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.upeu.farmafx.model.VentaCarrito;
import pe.edu.upeu.farmafx.repository.ICrudGenericRepository;
import pe.edu.upeu.farmafx.repository.VentaCarritoRepository;
import pe.edu.upeu.farmafx.service.IVentaCarritoService;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class VentaCarritoServiceImpl extends CrudGenericServiceImpl<VentaCarrito, Long> implements IVentaCarritoService {

    private static final Logger logger = LoggerFactory.getLogger(CompraCarritoServiceImpl.class);
    private final VentaCarritoRepository ventaCarritoRepository;

    @Override
    protected ICrudGenericRepository<VentaCarrito, Long> getRepository() {
        return ventaCarritoRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<VentaCarrito> listarPorUsuario(Long idUsuario) {
        if (idUsuario == null) {
            logger.debug("listarPorUsuario sin idUsuario, retorna lista vacía");
            return new ArrayList<>();
        }
        List<VentaCarrito> carrito = ventaCarritoRepository.listarPorUsuario(idUsuario);
        return carrito == null ? new ArrayList<>() : carrito;
    }

    @Override
    @Transactional
    public void eliminarPorUsuario(Long idUsuario) {
        if (idUsuario == null) {
            logger.debug("eliminarPorUsuario sin idUsuario, nada que eliminar");
            return;
        }
        ventaCarritoRepository.eliminarPorUsuario(idUsuario);
    }
}
