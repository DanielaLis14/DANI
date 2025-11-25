package pe.edu.upeu.farmafx.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "farmafx_categorias")
public class Categoria {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idCategoria;

    @NotBlank(message = "El nombre de la categoría es obligatorio")
    @Size(max = 80, message = "El nombre de la categoría no puede exceder 80 caracteres")
    @Column(nullable = false, unique = true, length = 80)
    private String nombreCategoria;

    @Size(max = 200, message = "La descripción de la categoría no puede exceder 200 caracteres")
    @Column(length = 200)
    private String descripcionCategoria;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "categoria_padre_id")
    private Categoria categoriaPadre;

    @Column(nullable = false)
    @Builder.Default
    private Boolean estadoCategoria = true;
}
