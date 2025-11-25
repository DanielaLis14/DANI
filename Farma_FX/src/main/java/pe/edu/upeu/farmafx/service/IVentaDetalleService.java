package pe.edu.upeu.farmafx.service;

import pe.edu.upeu.farmafx.model.VentaDetalle;

import java.util.List;

public interface IVentaDetalleService extends ICrudGenericService<VentaDetalle, Long> {

    List<VentaDetalle> listarPorVenta(Long idVenta);
}
