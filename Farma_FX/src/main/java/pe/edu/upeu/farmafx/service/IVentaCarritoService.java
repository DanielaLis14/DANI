package pe.edu.upeu.farmafx.service;

import pe.edu.upeu.farmafx.model.VentaCarrito;

import java.util.List;

public interface IVentaCarritoService extends ICrudGenericService<VentaCarrito, Long> {

    List<VentaCarrito> listarPorUsuario(Long idUsuario);

    void eliminarPorUsuario(Long idUsuario);
}
