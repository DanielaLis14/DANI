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
@Table(name = "farmafx_usuarios")
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idUsuario;

    @NotBlank(message = "El nombre de usuario es obligatorio")
    @Size(max = 40, message = "El nombre de usuario no puede exceder 40 caracteres")
    @Column(nullable = false, unique = true, length = 40)
    private String username;

    @NotBlank(message = "La contraseña es obligatoria")
    @Size(max = 120, message = "La contraseña debe tener hasta 120 caracteres")
    @Column(nullable = false, length = 120)
    private String passwordHash;

    @NotBlank(message = "El nombre completo es obligatorio")
    @Size(max = 120, message = "El nombre completo no puede exceder 120 caracteres")
    @Column(nullable = false, length = 120)
    private String nombreCompleto;

    @Email(message = "El correo debe tener un formato válido")
    @Size(max = 120, message = "El correo no puede exceder 120 caracteres")
    @Column(length = 120)
    private String emailUsuario;

    @Column(nullable = false)
    @Builder.Default
    private Boolean estadoUsuario = true;

    @NotNull(message = "El perfil es obligatorio")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_perfil", nullable = false)
    private Perfil perfil;
}
