package pe.edu.upeu.farmafx.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class PersonaDto {

    private String dni;
    private String nombre;
    private String apellidoPaterno;
    private String apellidoMaterno;

    @Override
    public String toString() {
        return dni + " " + nombre + " " + apellidoPaterno + " " + apellidoMaterno;
    }
}
