package pe.edu.upeu.farmafx.service.impl;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.upeu.farmafx.model.CompraDetalle;
import pe.edu.upeu.farmafx.repository.CompraDetalleRepository;
import pe.edu.upeu.farmafx.repository.ICrudGenericRepository;
import pe.edu.upeu.farmafx.service.ICompraDetalleService;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CompraDetalleServiceImpl extends CrudGenericServiceImpl<CompraDetalle, Long> implements ICompraDetalleService {

    private static final Logger logger = LoggerFactory.getLogger(CompraDetalleServiceImpl.class);

    private final CompraDetalleRepository compraDetalleRepository;

    @Override
    protected ICrudGenericRepository<CompraDetalle, Long> getRepository() {
        return compraDetalleRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<CompraDetalle> listarPorCompra(Long idCompra) {
        if (idCompra == null) {
            logger.debug("listarPorCompra sin idCompra, retorna lista vacía");
            return new ArrayList<>();
        }
        List<CompraDetalle> detalles = compraDetalleRepository.listarPorCompra(idCompra);
        return detalles == null ? new ArrayList<>() : detalles;
    }
}
