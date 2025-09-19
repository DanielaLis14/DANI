package pe.edu.upeu.farmafx.servicio;

import pe.edu.upeu.farmafx.modelo.Marca;
import java.util.List;

public interface MarcaServicioI {
    List<Marca> listarMarcas();
    Marca guardarMarca(Marca marca);
    void eliminarMarca(int id);
}
