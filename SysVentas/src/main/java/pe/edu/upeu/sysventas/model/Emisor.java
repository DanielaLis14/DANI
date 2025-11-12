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
@Table(name = "upeu_emisor")
public class Emisor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_emisor")
    private Long idEmisor;
    @Column(name = "ruc", nullable = false, length = 12)
    private String ruc;
    @Column(name = "nombre_comercial", nullable = false, length = 160)
    private String nombreComercial;
    @Column(name = "ubigeo", nullable = false, length = 8)
    private String ubigeo;
    @Column(name = "domicilio_fiscal", nullable = false, length = 120)
    private String domicilioFiscal;
    @Column(name = "urbanizacion", nullable = false, length = 60)
    private String urbanizacion;
    @Column(name = "departamento", nullable = false, length = 60)
    private String departamento;
    @Column(name = "provincia", nullable = false, length = 60)
    private String provincia;
    @Column(name = "distrito", nullable = false, length = 60)
    private String distrito;
}
