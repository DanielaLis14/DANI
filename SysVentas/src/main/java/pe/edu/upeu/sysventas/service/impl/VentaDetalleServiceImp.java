package pe.edu.upeu.sysventas.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import pe.edu.upeu.sysventas.model.VentaDetalle;
import pe.edu.upeu.sysventas.repository.ICrudGenericRepository;
import pe.edu.upeu.sysventas.repository.VentaDetalleRepository;
import pe.edu.upeu.sysventas.service.IVentaDetalleService;

@RequiredArgsConstructor
@Service
public class VentaDetalleServiceImp extends CrudGenericServiceImp<VentaDetalle, Long> implements IVentaDetalleService {
    private final VentaDetalleRepository ventaDetalleRepository;
    @Override
    protected ICrudGenericRepository<VentaDetalle, Long> getRepo() {
        return ventaDetalleRepository;
    }
}
