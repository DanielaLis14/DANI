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
@Table(name = "farmafx_marcas")
public class Marca {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idMarca;

    @NotBlank(message = "El nombre de la marca es obligatorio")
    @Size(max = 80, message = "El nombre de la marca no puede exceder 80 caracteres")
    @Column(nullable = false, unique = true, length = 80)
    private String nombreMarca;

    @Size(max = 600, message = "La descripción de la marca no puede exceder 200 caracteres")
    @Column(length = 600)
    private String descripcionMarca;

    @Column(nullable = false)
    @Builder.Default
    private Boolean estadoMarca = true;
}
