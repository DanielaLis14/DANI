package pe.edu.upeu.farmafx.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO minimalista para configuración de menús.
 * Contiene solo lo esencial: identificador, ruta, título y tipo de acción.
 * Responsabilidad del controller: agrupar por menú, construir labels localizados.
 */
@AllArgsConstructor
@NoArgsConstructor
@Data
public class MenuItemDto {
    private String id;       // "miproductos", "micategorias", etc.
    private String ruta;     // ViewRoute.PRODUCTOS_LISTA.getPath()
    private String titulo;   // "Productos", "Categorías" (se localiza en controller)
    private String tipo;     // "X" (Exit), "S" (Session), "T" (Tab)
}
