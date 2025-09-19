package pe.edu.upeu.farmafx.repositorio;

import pe.edu.upeu.farmafx.modelo.Categoria;
import pe.edu.upeu.farmafx.enums.Estado;
import java.util.ArrayList;
import java.util.List;

public class CategoriaRepository {

    protected List<Categoria> listaCategorias = new ArrayList<>();
    private int proximoId = 1;

    public CategoriaRepository() {
        guardar(new Categoria() {{ setNombre("Analgésicos"); setEstado(Estado.ACTIVO); }});
        guardar(new Categoria() {{ setNombre("Antibióticos"); setEstado(Estado.ACTIVO); }});
        guardar(new Categoria() {{ setNombre("Vitaminas"); setEstado(Estado.INACTIVO); }});
    }

    public List<Categoria> buscarTodos() {
        return new ArrayList<>(listaCategorias);
    }

    public Categoria guardar(Categoria categoria) {
        if (categoria.getId() == 0) {
            categoria.setId(proximoId++);
            listaCategorias.add(categoria);
        } else {
            listaCategorias.stream()
                    .filter(c -> c.getId() == categoria.getId())
                    .findFirst()
                    .ifPresent(c -> {
                        c.setNombre(categoria.getNombre());
                        c.setEstado(categoria.getEstado());
                    });
        }
        return categoria;
    }

    public void eliminar(int id) {
        listaCategorias.removeIf(c -> c.getId() == id);
    }
}