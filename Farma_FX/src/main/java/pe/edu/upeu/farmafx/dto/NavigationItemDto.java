package pe.edu.upeu.farmafx.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para items de navegación (sidebar, breadcrumbs, tabs futuros).
 * Representa elementos que cargan vistas, NO acciones CRUD.
 * Diferencias con ButtonDto:
 * - Tiene route (ruta de vista a cargar)
 * - Tiene group (para separadores inteligentes)
 * - Tiene tipo (VIEW, EXIT)
 *
 * @see pe.edu.upeu.farmafx.service.INavigationService
 * @since 2025-11-17
 */
@AllArgsConstructor
@NoArgsConstructor
@Data
public class NavigationItemDto {

    /**
     * Identificador único del item (ej: "dashboard", "productos", "marcas")
     */
    private String id;

    /**
     * Texto visible en el sidebar (localizable via i18n)
     */
    private String label;

    /**
     * Nombre del icono FontAwesome (SIN prefijo "FA_")
     * Ejemplo: "HOME", "CUBES", "TAG"
     */
    private String icon;

    /**
     * Ruta de la vista FXML a cargar (puede ser null si no tiene vista)
     * Ejemplo: "/view/marcas/marcas.fxml"
     */
    private String route;

    /**
     * Grupo al que pertenece (usado para separadores automáticos)
     * Ejemplo: "INVENTARIO", "VENTAS", "GESTION", "SISTEMA"
     * null = sin grupo (items especiales como Dashboard)
     */
    private String group;

    /**
     * Tipo de item: "VIEW" (carga vista) o "EXIT" (cerrar sesión)
     */
    private String tipo;

    /**
     * Atajo de teclado (opcional)
     * Ejemplo: "Ctrl+1", "Ctrl+2"
     */
    private String shortcut;

    /**
     * Estado del item (true = habilitado, false = deshabilitado)
     */
    private boolean enabled;
}