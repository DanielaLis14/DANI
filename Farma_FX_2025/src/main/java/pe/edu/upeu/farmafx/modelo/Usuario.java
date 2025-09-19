package pe.edu.upeu.farmafx.modelo;

import lombok.Data;
import pe.edu.upeu.farmafx.enums.Estado;
import pe.edu.upeu.farmafx.enums.RolUsuario;

@Data
public class Usuario {

    private String dni;
    private String clave;
    private Estado estado;
    private RolUsuario rol;

}