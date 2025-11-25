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
@Table(name = "farmafx_unidades_medida")
public class UnidadMedida {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idUmedida;

    @NotBlank(message = "El nombre de la unidad de medida es obligatorio")
    @Size(max = 80, message = "El nombre de la unidad de medida no puede exceder 80 caracteres")
    @Column(nullable = false, unique = true, length = 80)
    private String nombreUmedida;

    @NotBlank(message = "La abreviatura es obligatoria")
    @Size(max = 10, message = "La abreviatura no puede exceder 10 caracteres")
    @Column(nullable = false, unique = true, length = 10)
    private String abreviatura;

    @Column
    private Integer factorConversion;

    @Size(max = 200, message = "La descripción de la unidad de medida no puede exceder 200 caracteres")
    @Column(length = 200)
    private String descripcionUmedida;

    @Column(nullable = false)
    @Builder.Default
    private Boolean estadoUmedida = true;
}
