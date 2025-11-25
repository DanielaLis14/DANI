package pe.edu.upeu.farmafx.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "farmafx_perfiles")
public class Perfil {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idPerfil;

    @NotBlank(message = "El código del perfil es obligatorio")
    @Size(max = 6, message = "El código del perfil no puede exceder 6 caracteres")
    @Column(nullable = false, unique = true, length = 6)
    private String codigoPerfil;

    @NotBlank(message = "El nombre del perfil es obligatorio")
    @Size(max = 20, message = "El nombre del perfil no puede exceder 20 caracteres")
    @Column(nullable = false, unique = true, length = 20)
    private String nombrePerfil;

    @Size(max = 200, message = "La descripción del perfil no puede exceder 200 caracteres")
    @Column(length = 200)
    private String descripcionPerfil;

    @Column(nullable = false)
    @Builder.Default
    private Boolean estadoPerfil = true;
}
