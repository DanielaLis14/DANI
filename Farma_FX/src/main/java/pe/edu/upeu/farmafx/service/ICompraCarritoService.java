package pe.edu.upeu.farmafx.service;

import pe.edu.upeu.farmafx.model.CompraCarrito;

import java.util.List;

public interface ICompraCarritoService extends ICrudGenericService<CompraCarrito, Long> {

    List<CompraCarrito> listarPorUsuario(Long idUsuario);

    void eliminarPorUsuario(Long idUsuario);
}
