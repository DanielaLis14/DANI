package pe.edu.upeu.farmafx.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import pe.edu.upeu.farmafx.enums.TipoComprobante;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "farmafx_ventas")
public class Venta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idVenta;

    @NotNull(message = "El tipo de comprobante es obligatorio")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TipoComprobante tipoComprobanteVenta;

    @NotBlank(message = "La serie del comprobante es obligatoria")
    @Size(max = 10, message = "La serie del comprobante no puede exceder 10 caracteres")
    @Column(nullable = false, length = 10)
    private String serieComprobanteVenta;

    @NotBlank(message = "El número del comprobante es obligatorio")
    @Size(max = 20, message = "El número del comprobante no puede exceder 20 caracteres")
    @Column(nullable = false, length = 20)
    private String numeroComprobanteVenta;

    @NotNull(message = "El subtotal de la venta es obligatorio")
    @DecimalMin(value = "0.0", inclusive = true, message = "El subtotal no puede ser negativo")
    @Digits(integer = 10, fraction = 2, message = "El subtotal debe tener como máximo 10 dígitos enteros y 2 decimales")
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal subtotalVenta;

    @NotNull(message = "El impuesto de la venta es obligatorio")
    @DecimalMin(value = "0.0", inclusive = true, message = "El impuesto no puede ser negativo")
    @Digits(integer = 10, fraction = 2, message = "El impuesto debe tener como máximo 10 dígitos enteros y 2 decimales")
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal igvVenta;

    @NotNull(message = "El total de la venta es obligatorio")
    @DecimalMin(value = "0.0", inclusive = true, message = "El total no puede ser negativo")
    @Digits(integer = 10, fraction = 2, message = "El total debe tener como máximo 10 dígitos enteros y 2 decimales")
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal totalVenta;

    @Column(nullable = false)
    @Builder.Default
    private Boolean estadoVenta = true;

    @NotNull(message = "El cliente es obligatorio")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_cliente", nullable = false)
    private Cliente clienteVenta;

    @NotNull(message = "El usuario responsable es obligatorio")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_usuario", nullable = false)
    private Usuario usuarioVenta;

    @NotNull(message = "La fecha de generación es obligatoria")
    @Column(nullable = false)
    private LocalDateTime fechaGeneracionVenta;
}
