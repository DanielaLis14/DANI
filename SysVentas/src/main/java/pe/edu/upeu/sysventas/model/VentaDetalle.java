/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package pe.edu.upeu.sysventas.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
@Entity
@Table(name = "upeu_venta_detalle")
public class VentaDetalle {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_venta_detalle")
    private Long idVentaDetalle;
    @Column(name = "pu", nullable = false)
    private Double pu;
    @Column(name = "cantidad", nullable = false)
    private Double cantidad;
    @Column(name = "descuento", nullable = false)
    private Double descuento;
    @Column(name = "subtotal", nullable = false)
    private Double subtotal;
    @ManyToOne
    @JoinColumn(name = "id_venta", referencedColumnName = "id_venta",
            nullable = false, foreignKey = @ForeignKey(name
                    = "FK_VENTA_VENTADETALLE"))
    private Venta venta;
    @ManyToOne
    @JoinColumn(name = "id_producto", referencedColumnName = "id_producto",
            nullable = false, foreignKey = @ForeignKey(name
                    = "FK_PRODUCTO_VENTADETALLE"))
    private Producto producto;
}
