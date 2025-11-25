package pe.edu.upeu.farmafx.service;

import pe.edu.upeu.farmafx.model.CompraDetalle;

import java.util.List;

public interface ICompraDetalleService extends ICrudGenericService<CompraDetalle, Long> {

    List<CompraDetalle> listarPorCompra(Long idCompra);
}
