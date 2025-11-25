package pe.edu.upeu.farmafx.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para configuración completa de botones CRUD.
 * Incluye permisos, UI (label, icon, tooltip) y shortcuts.
 * Usado por ButtonService para retornar configuración completa.
 *
 * @see pe.edu.upeu.farmafx.service.IButtonService
 * @since 2025-11-17
 */
@AllArgsConstructor
@NoArgsConstructor
@Data
public class ButtonDto {

    /**
     * ID único del botón (ej: "nuevo", "editar", "inactivar", "eliminar")
     */
    private String id;

    /**
     * Texto visible del botón (localizable via i18n)
     */
    private String label;

    /**
     * Nombre del icono FontAwesome (ej: "PLUS", "EDIT", "TRASH")
     */
    private String icon;

    /**
     * Texto del tooltip (localizable via i18n)
     */
    private String tooltip;

    /**
     * Atajo de teclado (ej: "Ctrl+N", "Ctrl+G")
     */
    private String shortcut;

    /**
     * true = botón habilitado/visible, false = deshabilitado/oculto
     */
    private boolean enabled;
}
