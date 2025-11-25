package pe.edu.upeu.farmafx.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.upeu.farmafx.model.VentaDetalle;
import pe.edu.upeu.farmafx.repository.ICrudGenericRepository;
import pe.edu.upeu.farmafx.repository.VentaDetalleRepository;
import pe.edu.upeu.farmafx.service.IVentaDetalleService;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class VentaDetalleServiceImpl extends CrudGenericServiceImpl<VentaDetalle, Long> implements IVentaDetalleService {

    private final VentaDetalleRepository ventaDetalleRepository;

    @Override
    protected ICrudGenericRepository<VentaDetalle, Long> getRepository() {
        return ventaDetalleRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<VentaDetalle> listarPorVenta(Long idVenta) {
        if (idVenta == null) {
            return new ArrayList<>();
        }
        List<VentaDetalle> detalles = ventaDetalleRepository.listarPorVenta(idVenta);
        return detalles == null ? new ArrayList<>() : detalles;
    }
}
