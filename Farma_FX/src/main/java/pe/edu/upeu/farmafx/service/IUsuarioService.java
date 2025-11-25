package pe.edu.upeu.farmafx.service;

import pe.edu.upeu.farmafx.model.Usuario;

public interface IUsuarioService extends ICrudGenericService<Usuario, Long> {

    Usuario loginUsuario(String username, String password);

    boolean existeUsername(String username);

    Usuario registrarCliente(String username, String password,
                            String nombreCompleto, String email, String telefono);
}
