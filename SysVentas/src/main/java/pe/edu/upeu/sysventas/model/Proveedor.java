/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package pe.edu.upeu.sysventas.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
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
@Table(name = "upeu_proveedor")
public class Proveedor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_proveedor")
    private Long idProveedor;
    @Column(name = "dniruc", nullable = false, length = 12)
    private String dniRuc;
    @Column(name = "nombres_raso", nullable = false, length = 120)
    private String nombresRaso;
    @Column(name = "tipo_doc", nullable = false, length = 12)
    private String tipoDoc;
    @Column(name = "celular", nullable = false, length = 10)
    private String celular;
    @Column(name = "email", nullable = false, length = 100)
    private String email;
    @Column(name = "direccion", nullable = false, length = 120)
    private String direccion;
}
