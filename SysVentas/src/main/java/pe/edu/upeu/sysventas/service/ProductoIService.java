package pe.edu.upeu.sysventas.service;

import pe.edu.upeu.sysventas.dto.ModeloDataAutocomplet;
import pe.edu.upeu.sysventas.model.Producto;

import java.util.List;

public interface ProductoIService {
    Producto save(Producto producto);
    List<Producto> findAll();
    Producto update(Producto producto);
    void delete(Long id);
    Producto findById(Long id);
    List<ModeloDataAutocomplet> listAutoCompletProducto(String nombre);
    public List<ModeloDataAutocomplet> listAutoCompletProducto();
}
