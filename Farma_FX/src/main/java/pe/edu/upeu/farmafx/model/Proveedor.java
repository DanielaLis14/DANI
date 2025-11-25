package pe.edu.upeu.farmafx.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "farmafx_proveedores")
public class Proveedor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idProveedor;

    @NotBlank(message = "El RUC o número fiscal es obligatorio")
    @Size(max = 20, message = "El RUC o número fiscal no puede exceder 20 caracteres")
    @Column(nullable = false, unique = true, length = 20)
    private String rucProveedor;

    @NotBlank(message = "La razón social es obligatoria")
    @Size(max = 120, message = "La razón social no puede exceder 120 caracteres")
    @Column(nullable = false, length = 120)
    private String razonSocialProveedor;

    @Size(max = 120, message = "El nombre del contacto no puede exceder 120 caracteres")
    @Column(length = 120)
    private String contactoProveedor;

    @Email(message = "El correo debe tener un formato válido")
    @Size(max = 120, message = "El correo no puede exceder 120 caracteres")
    @Column(length = 120)
    private String emailProveedor;

    @Size(max = 20, message = "El teléfono no puede exceder 20 caracteres")
    @Column(length = 20)
    private String telefonoProveedor;


    @Column(nullable = false)
    @Builder.Default
    private Boolean estadoProveedor = true;
}
