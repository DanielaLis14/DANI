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
@Table(name = "farmafx_compra_detalles")
public class CompraDetalle {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idCompraDetalle;

    @NotNull(message = "La cantidad es obligatoria")
    @Min(value = 1, message = "La cantidad debe ser al menos 1")
    @Column(nullable = false)
    private Integer cantidadCompra;

    @NotNull(message = "El precio unitario es obligatorio")
    @DecimalMin(value = "0.0", inclusive = true, message = "El precio unitario no puede ser negativo")
    @Digits(integer = 10, fraction = 2, message = "El precio unitario debe tener como máximo 10 dígitos enteros y 2 decimales")
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal precioUnitarioCompra;

    @NotNull(message = "El subtotal es obligatorio")
    @DecimalMin(value = "0.0", inclusive = true, message = "El subtotal no puede ser negativo")
    @Digits(integer = 10, fraction = 2, message = "El subtotal debe tener como máximo 10 dígitos enteros y 2 decimales")
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal subtotalDetalleCompra;

    @NotNull(message = "La compra relacionada es obligatoria")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_compra", nullable = false)
    private Compra compra;

    @NotNull(message = "El producto es obligatorio")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_producto", nullable = false)
    private Producto productoCompra;
}
