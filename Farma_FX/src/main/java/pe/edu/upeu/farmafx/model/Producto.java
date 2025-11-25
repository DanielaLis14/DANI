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
@Table(name = "farmafx_productos")
public class Producto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idProducto;

    @NotBlank(message = "El código del producto es obligatorio")
    @Size(max = 30, message = "El código del producto no puede exceder 30 caracteres")
    @Column(nullable = false, unique = true, length = 30)
    private String codigoProducto;

    @NotBlank(message = "El nombre del producto es obligatorio")
    @Size(max = 120, message = "El nombre del producto no puede exceder 120 caracteres")
    @Column(nullable = false, length = 120)
    private String nombreProducto;

    @Size(max = 200, message = "La descripción del producto no puede exceder 200 caracteres")
    @Column(length = 200)
    @Builder.Default
    private String descripcionProducto = "";

    @NotNull(message = "El precio de compra es obligatorio")
    @DecimalMin(value = "0.0", message = "El precio de compra no puede ser negativo")
    @Digits(integer = 10, fraction = 2, message = "El precio de compra debe tener como máximo 10 dígitos enteros y 2 decimales")
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal precioCompra;

    @NotNull(message = "El precio de venta es obligatorio")
    @DecimalMin(value = "0.0", message = "El precio de venta no puede ser negativo")
    @Digits(integer = 10, fraction = 2, message = "El precio de venta debe tener como máximo 10 dígitos enteros y 2 decimales")
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal precioVenta;

    @NotNull(message = "El stock actual es obligatorio")
    @Min(value = 0, message = "El stock actual no puede ser negativo")
    @Column(nullable = false)
    private Integer stockActual;

    @NotNull(message = "El stock mínimo es obligatorio")
    @Min(value = 0, message = "El stock mínimo no puede ser negativo")
    @Column(nullable = false)
    private Integer stockMinimo;

    @Column(nullable = false)
    @Builder.Default
    private Boolean estadoProducto = true;

    // Categoria puede ser NULL (productos huérfanos cuando se elimina su categoría)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_categoria")
    private Categoria categoria;

    // Marca es obligatoria (todo producto debe tener marca)
    @NotNull(message = "La marca es obligatoria")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_marca", nullable = false)
    private Marca marca;

    @NotNull(message = "La unidad de medida es obligatoria")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_unidad_medida", nullable = false)
    private UnidadMedida unidadMedida;
}
