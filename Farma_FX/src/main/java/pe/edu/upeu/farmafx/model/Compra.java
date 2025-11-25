package pe.edu.upeu.farmafx.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import pe.edu.upeu.farmafx.enums.TipoComprobante;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "farmafx_compras")
public class Compra {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idCompra;

    @NotNull(message = "El tipo de comprobante es obligatorio")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TipoComprobante tipoComprobante;

    @NotBlank(message = "La serie del comprobante es obligatoria")
    @Size(max = 10, message = "La serie del comprobante no puede exceder 10 caracteres")
    @Column(nullable = false, length = 10)
    private String serieComprobante;

    @NotBlank(message = "El número del comprobante es obligatorio")
    @Size(max = 20, message = "El número del comprobante no puede exceder 20 caracteres")
    @Column(nullable = false, length = 20)
    private String numeroComprobante;

    @NotNull(message = "La fecha de compra es obligatoria")
    @Column(nullable = false)
    private LocalDate fechaCompra;

    @NotNull(message = "El subtotal de la compra es obligatorio")
    @DecimalMin(value = "0.0", inclusive = true, message = "El subtotal no puede ser negativo")
    @Digits(integer = 10, fraction = 2, message = "El subtotal debe tener como máximo 10 dígitos enteros y 2 decimales")
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal subtotalCompra;

    @NotNull(message = "El impuesto de la compra es obligatorio")
    @DecimalMin(value = "0.0", inclusive = true, message = "El impuesto no puede ser negativo")
    @Digits(integer = 10, fraction = 2, message = "El impuesto debe tener como máximo 10 dígitos enteros y 2 decimales")
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal igvCompra;

    @NotNull(message = "El total de la compra es obligatorio")
    @DecimalMin(value = "0.0", inclusive = true, message = "El total no puede ser negativo")
    @Digits(integer = 10, fraction = 2, message = "El total debe tener como máximo 10 dígitos enteros y 2 decimales")
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal totalCompra;

    @Column(nullable = false)
    @Builder.Default
    private Boolean estadoCompra = true;

    @NotNull(message = "El proveedor es obligatorio")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_proveedor", nullable = false)
    private Proveedor proveedor;

    @NotNull(message = "El usuario responsable es obligatorio")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_usuario", nullable = false)
    private Usuario usuarioRegistro;

    @Column(nullable = false)
    private LocalDateTime fechaRegistroCompra;

    @PrePersist
    void asignarFechaRegistro() {
        if (fechaRegistroCompra == null) {
            fechaRegistroCompra = LocalDateTime.now();
        }
    }
}
