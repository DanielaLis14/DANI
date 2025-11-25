package pe.edu.upeu.farmafx.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "farmafx_venta_carrito")
public class VentaCarrito {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idVentaCarrito;

    @NotNull(message = "La cantidad es obligatoria")
    @Min(value = 1, message = "La cantidad debe ser al menos 1")
    @Column(nullable = false)
    private Integer cantidadCarrito;

    @NotNull(message = "El precio unitario es obligatorio")
    @DecimalMin(value = "0.0", inclusive = true, message = "El precio unitario no puede ser negativo")
    @Digits(integer = 10, fraction = 2, message = "El precio unitario debe tener como máximo 10 dígitos enteros y 2 decimales")
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal precioUnitarioCarrito;

    @NotNull(message = "El total es obligatorio")
    @DecimalMin(value = "0.0", inclusive = true, message = "El total no puede ser negativo")
    @Digits(integer = 10, fraction = 2, message = "El total debe tener como máximo 10 dígitos enteros y 2 decimales")
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal totalCarrito;

    @Column(nullable = false)
    @Builder.Default
    private Boolean estadoCarrito = true;

    @NotNull(message = "El producto es obligatorio")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_producto", nullable = false)
    private Producto productoCarrito;

    @NotNull(message = "El usuario es obligatorio")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_usuario", nullable = false)
    private Usuario usuarioCarrito;
}
