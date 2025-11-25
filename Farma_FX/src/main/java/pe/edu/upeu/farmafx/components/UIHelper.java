package pe.edu.upeu.farmafx.components;

import javafx.scene.control.Button;

import java.util.Objects;
import java.util.stream.Stream;

/**
 * Helper estático para operaciones comunes con botones en JavaFX.
 * Centraliza patrones repetitivos de disable/enable para reducir boilerplate.
 *
 * <p>Uso típico:</p>
 * <pre>{@code
 * // Deshabilitar múltiples botones
 * UIHelper.disable(btnGuardar, btnCancelar, btnEliminar);
 *
 * // Habilitar múltiples botones
 * UIHelper.enable(btnNuevo, btnEditar);
 * }</pre>
 *
 * @author Claude Code
 * @version 2.0 - Limpieza YAGNI (eliminados métodos sin uso)
 */
public final class UIHelper {

    private UIHelper() {
        // No instanciable - solo métodos estáticos
    }

    // ========== BUTTONS: DISABLE/ENABLE ==========

    /**
     * Deshabilita o habilita múltiples botones.
     * Filtra nulls automáticamente.
     *
     * @param disabled true para deshabilitar, false para habilitar
     * @param buttons  botones a modificar
     */
    public static void setDisabled(boolean disabled, Button... buttons) {
        Stream.of(buttons)
            .filter(Objects::nonNull)
            .forEach(btn -> btn.setDisable(disabled));
    }

    /**
     * Deshabilita múltiples botones.
     * Alias de {@code setDisabled(true, buttons)}.
     *
     * @param buttons botones a deshabilitar
     */
    public static void disable(Button... buttons) {
        setDisabled(true, buttons);
    }

    /**
     * Habilita múltiples botones.
     * Alias de {@code setDisabled(false, buttons)}.
     *
     * @param buttons botones a habilitar
     */
    public static void enable(Button... buttons) {
        setDisabled(false, buttons);
    }
}