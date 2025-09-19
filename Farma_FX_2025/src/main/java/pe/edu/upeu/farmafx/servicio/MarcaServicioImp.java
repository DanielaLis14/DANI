package pe.edu.upeu.farmafx.servicio;

import org.springframework.stereotype.Service;
import pe.edu.upeu.farmafx.modelo.Marca;
import pe.edu.upeu.farmafx.repositorio.MarcaRepository;
import java.util.List;

@Service
public class MarcaServicioImp extends MarcaRepository implements MarcaServicioI {

    @Override
    public List<Marca> listarMarcas() {
        return super.buscarTodos();
    }

    @Override
    public Marca guardarMarca(Marca marca) {
        return super.guardar(marca);
    }

    @Override
    public void eliminarMarca(int id) {
        super.eliminar(id);
    }
}
