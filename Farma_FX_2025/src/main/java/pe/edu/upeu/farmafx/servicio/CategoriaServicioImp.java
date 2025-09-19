package pe.edu.upeu.farmafx.servicio;

import org.springframework.stereotype.Service;
import pe.edu.upeu.farmafx.modelo.Categoria;
import pe.edu.upeu.farmafx.repositorio.CategoriaRepository;
import java.util.List;

@Service
public class CategoriaServicioImp extends CategoriaRepository implements CategoriaServicioI {

    @Override
    public List<Categoria> listarCategorias() {
        return super.buscarTodos();
    }

    @Override
    public Categoria guardarCategoria(Categoria categoria) {
        return super.guardar(categoria);
    }

    @Override
    public void eliminarCategoria(int id) {
        super.eliminar(id);
    }
}
