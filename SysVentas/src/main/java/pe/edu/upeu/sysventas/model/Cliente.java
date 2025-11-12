package pe.edu.upeu.sysventas.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import pe.edu.upeu.sysventas.enums.TipoDocumento;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
@Entity
@Table(name = "upeu_cliente")

public class Cliente {
    @Id
    @Column(name = "dniruc", nullable = false, length = 12)
    private String dniruc;
    @Column(name = "nombres", nullable = false, length = 160)
    private String nombres;
    @Column(name = "rep_legal", length = 160)
    private String repLegal;
    @Column(name = "tipo_documento", nullable = false, length = 12)
    @Enumerated(EnumType.STRING)
    private TipoDocumento tipoDocumento;
}
