package pe.edu.upeu.farmafx.components;

import javafx.scene.control.ComboBox;
import org.springframework.data.domain.Sort;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Helper para ComboBox de ordenamiento dinámico en tablas.
 * Soporta ordenamiento ascendente/descendente con case-insensitive para campos String.
 *
 * <p>Uso típico:</p>
 * <pre>{@code
 * SortHelper sortHelper = new SortHelper(cmbOrdenamiento, () -> {
 *     paginaActual = 0;
 *     cargarDatos();
 * });
 *
 * sortHelper
 *     .addOption("ID Ascendente", "idMarca", Sort.Direction.ASC, false)
 *     .addOption("ID Descendente", "idMarca", Sort.Direction.DESC, false)
 *     .addOption("Nombre A-Z", "nombreMarca", Sort.Direction.ASC, true)
 *     .addOption("Nombre Z-A", "nombreMarca", Sort.Direction.DESC, true)
 *     .configure();
 *
 * // Crear Sort.Order para Pageable
 * Sort.Order order = sortHelper.createOrder();
 * Pageable pageable = PageRequest.of(page, size, Sort.by(order));
 * }</pre>
 *
 * @version 1.0
 * @since 2025-11-21
 */
public class SortHelper {

    /**
     * Opciones de ordenamiento con metadata.
     *
     * @param label Texto mostrado al usuario (ej: "Nombre A-Z")
     * @param field Campo de la entidad (ej: "nombreMarca")
     * @param direction Dirección ASC o DESC
     * @param isString true si el campo es String (aplica ignoreCase), false para numéricos
     */
    public record SortOption(String label, String field, Sort.Direction direction, boolean isString) {}

    private final ComboBox<String> combo;
    private final Map<String, SortOption> options = new LinkedHashMap<>();
    private SortOption currentOption;
    private final Runnable onSortChange;

    /**
     * Constructor del helper.
     *
     * @param combo ComboBox del FXML para ordenamiento
     * @param onSortChange Callback que se ejecuta al cambiar ordenamiento (ej: recargar tabla)
     */
    public SortHelper(ComboBox<String> combo, Runnable onSortChange) {
        this.combo = combo;
        this.onSortChange = onSortChange;
    }

    /**
     * Agrega una opción de ordenamiento.
     * Se pueden agregar múltiples opciones con encadenamiento fluido.
     *
     * @param label Texto mostrado al usuario
     * @param field Nombre del campo en la entidad (debe coincidir con @Column o atributo JPA)
     * @param direction ASC o DESC
     * @param isString true para String (case-insensitive), false para numéricos
     * @return this (para encadenamiento)
     */
    public SortHelper addOption(String label, String field, Sort.Direction direction, boolean isString) {
        SortOption option = new SortOption(label, field, direction, isString);
        options.put(label, option);
        combo.getItems().add(label);

        // Primera opción agregada es la default
        if (currentOption == null) {
            currentOption = option;
            combo.setValue(label);
        }

        return this;
    }

    /**
     * Configura el listener del ComboBox.
     * Debe llamarse después de agregar todas las opciones con addOption().
     *
     * @return this (para encadenamiento)
     */
    public SortHelper configure() {
        if (combo == null) {
            throw new IllegalStateException("ComboBox es null - verificar @FXML binding");
        }

        if (options.isEmpty()) {
            throw new IllegalStateException("No se agregaron opciones - llamar addOption() antes de configure()");
        }

        combo.setOnAction(e -> {
            String selected = combo.getValue();
            currentOption = options.get(selected);

            if (onSortChange != null) {
                onSortChange.run();
            }
        });

        return this;
    }

    /**
     * Crea el Sort.Order correspondiente a la opción actual.
     * Aplica ignoreCase() automáticamente si el campo es String.
     *
     * @return Sort.Order listo para usar en PageRequest.of(..., Sort.by(order))
     */
    public Sort.Order createOrder() {
        if (currentOption == null) {
            throw new IllegalStateException("No hay opción seleccionada - verificar configure()");
        }

        // Crear orden base según dirección (ASC o DESC)
        Sort.Order order = currentOption.direction() == Sort.Direction.ASC
            ? Sort.Order.asc(currentOption.field())
            : Sort.Order.desc(currentOption.field());

        // Aplicar ignoreCase solo para campos String
        return currentOption.isString() ? order.ignoreCase() : order;
    }

    /**
     * Reinicia el ordenamiento a la primera opción agregada.
     * Útil para botón "Limpiar filtros".
     */
    public void reset() {
        String firstLabel = options.keySet().iterator().next();
        combo.setValue(firstLabel);
        currentOption = options.get(firstLabel);
    }

    /**
     * Deshabilita el ComboBox de ordenamiento.
     * Útil cuando el usuario entra en modo edición.
     */
    public void disable() {
        combo.setDisable(true);
    }

    /**
     * Habilita el ComboBox de ordenamiento.
     * Útil cuando el usuario sale del modo edición.
     */
    public void enable() {
        combo.setDisable(false);
    }
}