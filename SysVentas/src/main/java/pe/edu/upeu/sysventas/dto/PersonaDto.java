package pe.edu.upeu.sysventas.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class PersonaDto {
    String dni, nombre, apellidoPaterno, apellidoMaterno;
    @Override
    public String toString(){
        return dni+" "+nombre+" "+apellidoPaterno+" "+apellidoMaterno;
    }
}
