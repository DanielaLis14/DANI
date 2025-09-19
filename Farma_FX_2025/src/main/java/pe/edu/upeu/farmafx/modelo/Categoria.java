package pe.edu.upeu.farmafx.modelo;

import lombok.Data;
import pe.edu.upeu.farmafx.enums.Estado;

@Data
public class Categoria {
    private int id;
    private String nombre;
    private Estado estado;
}