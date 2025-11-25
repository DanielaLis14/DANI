package pe.edu.upeu.farmafx.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import pe.edu.upeu.farmafx.enums.TipoCliente;
import pe.edu.upeu.farmafx.enums.TipoDocumento;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "farmafx_clientes")
public class Cliente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idCliente;

    @NotNull(message = "El tipo de cliente es obligatorio")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TipoCliente tipoCliente;

    @NotNull(message = "El tipo de documento es obligatorio")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private TipoDocumento tipoDocumento;

    @NotBlank(message = "El número de documento es obligatorio")
    @Size(max = 20, message = "El número de documento no puede exceder 20 caracteres")
    @Column(nullable = false, unique = true, length = 20)
    private String numeroDocumento;

    @NotBlank(message = "El nombre o razón social es obligatorio")
    @Size(max = 120, message = "El nombre del cliente no puede exceder 120 caracteres")
    @Column(nullable = false, length = 120)
    private String nombreCompleto;

    @Email(message = "El correo debe tener un formato válido")
    @Size(max = 120, message = "El correo no puede exceder 120 caracteres")
    @Column(length = 120)
    private String emailCliente;

    @Column(nullable = false)
    @Builder.Default
    private Boolean estadoCliente = true;
}
