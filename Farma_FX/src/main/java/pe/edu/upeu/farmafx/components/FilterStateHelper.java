package pe.edu.upeu.farmafx.components;

import javafx.scene.control.ComboBox;

/**
 * Helper para ComboBox de filtro por estado (Todos/Activo/Inactivo).
 * Reutilizable en todos los CRUDs que tengan campo estado booleano.
 *
 * <p>Uso típico:</p>
 * <pre>{@code
 * FilterStateHelper filterHelper = new FilterStateHelper(cmbEstadoFiltro, () -> {
 *     paginaActual = 0;
 *     cargarDatos();
 * }).configure();
 *
 * // Obtener estado actual para query
 * Boolean estado = filterHelper.getCurrentState(); // null=Todos, true=Activo, false=Inactivo
 * }</pre>
 *
 * @version 2.0 - Limpieza YAGNI + constantes para strings
 * @since 2025-11-21
 */
public class FilterStateHelper {

    // Constantes para opciones del ComboBox
    private static final String OPTION_TODOS = "Todos";
    private static final String OPTION_ACTIVO = "Activo";
    private static final String OPTION_INACTIVO = "Inactivo";

    private final ComboBox<String> combo;
    private Boolean currentState = null; // null=Todos, true=Activo, false=Inactivo
    private final Runnable onStateChange;

    /**
     * Constructor del helper.
     *
     * @param combo ComboBox del FXML para filtro de estado
     * @param onStateChange Callback que se ejecuta al cambiar estado (ej.: recargar tabla)
     */
    public FilterStateHelper(ComboBox<String> combo, Runnable onStateChange) {
        this.combo = combo;
        this.onStateChange = onStateChange;
    }

    /**
     * Configura el ComboBox con las 3 opciones estándar.
     * Debe llamarse una sola vez durante initialize().
     *
     * @return this (para encadenamiento fluido)
     */
    public FilterStateHelper configure() {
        if (combo == null) {
            throw new IllegalStateException("ComboBox es null - verificar @FXML binding");
        }

        // Poblar opciones usando constantes
        combo.getItems().addAll(OPTION_TODOS, OPTION_ACTIVO, OPTION_INACTIVO);
        combo.setValue(OPTION_TODOS);

        // Configurar listener de cambio
        combo.setOnAction(e -> {
            String selection = combo.getValue();
            currentState = switch (selection) {
                case OPTION_ACTIVO -> true;
                case OPTION_INACTIVO -> false;
                default -> null; // OPTION_TODOS
            };

            if (onStateChange != null) {
                onStateChange.run();
            }
        });

        return this;
    }

    /**
     * Obtiene el estado actual seleccionado.
     *
     * @return null si "Todos", true si "Activo", false si "Inactivo"
     */
    public Boolean getCurrentState() {
        return currentState;
    }

    /**
     * Reinicia el filtro a "Todos" (estado inicial).
     * Útil para botón "Limpiar filtros".
     */
    public void reset() {
        combo.setValue(OPTION_TODOS);
        currentState = null;
    }

    /**
     * Deshabilita el ComboBox de filtro.
     * Útil cuando el usuario entra en modo edición.
     */
    public void disable() {
        combo.setDisable(true);
    }

    /**
     * Habilita el ComboBox de filtro.
     * Útil cuando el usuario sale del modo edición.
     */
    public void enable() {
        combo.setDisable(false);
    }
}
