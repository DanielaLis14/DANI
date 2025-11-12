package pe.edu.upeu.sysventas.service;

import pe.edu.upeu.sysventas.model.Usuario;

public interface IUsuarioService extends ICrudGenericService<Usuario,Long>{
    Usuario loginUsuario(String user, String clave);
}
