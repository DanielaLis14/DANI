package pe.edu.upeu.farmafx.servicio;

import pe.edu.upeu.farmafx.modelo.Categoria;
import java.util.List;

public interface CategoriaServicioI {
    List<Categoria> listarCategorias();
    Categoria guardarCategoria(Categoria categoria);
    void eliminarCategoria(int id);
}
