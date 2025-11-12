package pe.edu.upeu.sysventas.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import pe.edu.upeu.sysventas.model.CompraDetalle;
import pe.edu.upeu.sysventas.repository.CompraDetalleRepository;
import pe.edu.upeu.sysventas.repository.ICrudGenericRepository;
import pe.edu.upeu.sysventas.service.ICompraDetalleService;

@RequiredArgsConstructor
@Service
public class CompraDetalleServiceImp extends CrudGenericServiceImp<CompraDetalle, Long> implements ICompraDetalleService {

    private final CompraDetalleRepository compraDetalleRepository;

    @Override
    protected ICrudGenericRepository<CompraDetalle, Long> getRepo() {
        return compraDetalleRepository;
    }
}
