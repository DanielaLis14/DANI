package pe.edu.upeu.encapsulamiento;

import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
public class Jugador {
    private String nombre;
    private String apellido;
    private int edad;
    private String posicion;
    private int numeroCam;

    @Override
    public String toString() {
        return "El jugador tienes estas caracteristicas:\n" +
                "\nnombre "+nombre +
                "\napellido " + apellido +
                "\nedad " + edad +
                "\nposicion " + posicion +
                "\nnumeroCam " + numeroCam;

    }

}
