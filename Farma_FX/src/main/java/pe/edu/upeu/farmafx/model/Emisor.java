package pe.edu.upeu.farmafx.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "farmafx_emisores")
public class Emisor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idEmisor;

    @NotBlank(message = "El RUC es obligatorio")
    @Size(max = 12, message = "El RUC no puede exceder 12 caracteres")
    @Column(nullable = false, unique = true, length = 12)
    private String rucEmisor;

    @NotBlank(message = "El nombre comercial es obligatorio")
    @Size(max = 160, message = "El nombre comercial no puede exceder 160 caracteres")
    @Column(nullable = false, length = 160)
    private String nombreComercialEmisor;

    @NotBlank(message = "El ubigeo es obligatorio")
    @Pattern(regexp = "\\d{6}", message = "El ubigeo debe tener 6 dígitos")
    @Column(nullable = false, length = 6)
    private String ubigeoEmisor;

    @NotBlank(message = "El domicilio fiscal es obligatorio")
    @Size(max = 120, message = "El domicilio fiscal no puede exceder 120 caracteres")
    @Column(nullable = false, length = 120)
    private String domicilioFiscalEmisor;

    @NotBlank(message = "La urbanización es obligatoria")
    @Size(max = 60, message = "La urbanización no puede exceder 60 caracteres")
    @Column(nullable = false, length = 60)
    private String urbanizacionEmisor;

    @NotBlank(message = "El departamento es obligatorio")
    @Size(max = 60, message = "El departamento no puede exceder 60 caracteres")
    @Column(nullable = false, length = 60)
    private String departamentoEmisor;

    @NotBlank(message = "La provincia es obligatoria")
    @Size(max = 60, message = "La provincia no puede exceder 60 caracteres")
    @Column(nullable = false, length = 60)
    private String provinciaEmisor;

    @NotBlank(message = "El distrito es obligatorio")
    @Size(max = 60, message = "El distrito no puede exceder 60 caracteres")
    @Column(nullable = false, length = 60)
    private String distritoEmisor;

    @Column(nullable = false)
    @Builder.Default
    private Boolean estadoEmisor = true;
}
