package pe.edu.upeu.farmafx.repositorio;

import pe.edu.upeu.farmafx.modelo.Usuario;
import pe.edu.upeu.farmafx.enums.Estado;
import pe.edu.upeu.farmafx.enums.RolUsuario;
import java.util.ArrayList;
import java.util.List;

public class UsuarioRepository {

    protected List<Usuario> listaUsuarios;

    public UsuarioRepository() {
        this.listaUsuarios = new ArrayList<>();
        cargarDatosIniciales();
    }

    public Usuario buscarPorDni(String dni) {
        for (Usuario usuario : listaUsuarios) {
            if (usuario.getDni().equals(dni)) {
                return usuario;
            }
        }
        return null;
    }

    public void guardar(Usuario usuario) {
        listaUsuarios.add(usuario);
    }

    private void cargarDatosIniciales() {
        Usuario admin = new Usuario();
        admin.setDni("12345678");
        admin.setClave("admin1234");
        admin.setRol(RolUsuario.ADMINISTRADOR);
        admin.setEstado(Estado.ACTIVO);
        listaUsuarios.add(admin);

        Usuario cliente = new Usuario();
        cliente.setDni("87654321");
        cliente.setClave("cliente5678");
        cliente.setRol(RolUsuario.CLIENTE);
        cliente.setEstado(Estado.ACTIVO);
        listaUsuarios.add(cliente);
    }
}