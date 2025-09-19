package pe.edu.upeu.farmafx.modelo;

import lombok.Data;
import pe.edu.upeu.farmafx.enums.Estado;

@Data
public class Marca {
    private int id;
    private String nombre;
    private Estado estado;
}