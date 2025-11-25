package pe.edu.upeu.farmafx.components;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.scene.control.TextField;
import javafx.util.Duration;
import lombok.extern.slf4j.Slf4j;

/**
 * Helper para búsqueda con debounce usando JavaFX Timeline.
 * Evita ejecutar búsquedas en cada keypress, esperando que el usuario
 * termine de escribir antes de ejecutar la búsqueda.
 *
 * <p>Uso típico:</p>
 * <pre>{@code
 * private SearchHelper searchHelper;
 *
 * private void configurarBuscador() {
 *     searchHelper = new SearchHelper(txtBusqueda, this::buscar).configure();
 * }
 * }</pre>
 *
 * <p>Características:</p>
 * <ul>
 *   <li>Debounce configurable (default 300ms)</li>
 *   <li>Cancela búsqueda pendiente si usuario sigue escribiendo</li>
 *   <li>Búsqueda inmediata al presionar Enter (cancela debounce)</li>
 *   <li>100% JavaFX nativo - sin threads adicionales</li>
 * </ul>
 *
 * @author Claude Code
 * @version 1.0
 */
@Slf4j
public class SearchHelper {

    private static final Duration DEFAULT_DEBOUNCE = Duration.millis(300);

    private final TextField searchField;
    private final Runnable onSearch;
    private final Duration debounce;
    private Timeline timeline;

    /**
     * Constructor con debounce por defecto (300ms).
     *
     * @param searchField TextField de búsqueda
     * @param onSearch    Callback a ejecutar cuando se debe buscar
     */
    public SearchHelper(TextField searchField, Runnable onSearch) {
        this(searchField, onSearch, DEFAULT_DEBOUNCE);
    }

    /**
     * Constructor con debounce personalizado.
     *
     * @param searchField TextField de búsqueda
     * @param onSearch    Callback a ejecutar cuando se debe buscar
     * @param debounce    Tiempo de espera antes de ejecutar búsqueda
     */
    public SearchHelper(TextField searchField, Runnable onSearch, Duration debounce) {
        this.searchField = searchField;
        this.onSearch = onSearch;
        this.debounce = debounce;
    }

    /**
     * Configura los listeners del TextField.
     * Debe llamarse después del constructor.
     *
     * @return this para encadenamiento fluido
     */
    public SearchHelper configure() {
        timeline = new Timeline();
        timeline.setCycleCount(1);

        // Listener de texto: inicia debounce en cada cambio
        searchField.textProperty().addListener((obs, oldVal, newVal) -> {
            timeline.stop();
            timeline.getKeyFrames().setAll(
                new KeyFrame(debounce, e -> {
                    log.debug("Búsqueda por debounce: '{}'", newVal);
                    onSearch.run();
                })
            );
            timeline.playFromStart();
        });

        // Enter: búsqueda inmediata (cancela debounce pendiente)
        searchField.setOnAction(e -> {
            timeline.stop();
            log.debug("Búsqueda por Enter: '{}'", searchField.getText());
            onSearch.run();
        });

        log.debug("SearchHelper configurado - debounce: {}ms", debounce.toMillis());
        return this;
    }

    /**
     * Limpia el campo de búsqueda y ejecuta búsqueda vacía.
     * Útil para botones "Limpiar" o "X".
     */
    public void clear() {
        timeline.stop();
        searchField.clear();
        onSearch.run();
    }

    /**
     * Detiene cualquier búsqueda pendiente sin ejecutarla.
     * Útil al cambiar de vista o cerrar ventana.
     */
    public void stop() {
        if (timeline != null) {
            timeline.stop();
        }
    }

    /**
     * Obtiene el texto actual del campo de búsqueda (trimmed).
     *
     * @return texto de búsqueda sin espacios al inicio/fin
     */
    public String getText() {
        String text = searchField.getText();
        return text == null ? "" : text.trim();
    }

    /**
     * Deshabilita el campo de búsqueda.
     * Útil cuando el usuario entra en modo edición.
     */
    public void disable() {
        searchField.setDisable(true);
    }

    /**
     * Habilita el campo de búsqueda.
     * Útil cuando el usuario sale del modo edición.
     */
    public void enable() {
        searchField.setDisable(false);
    }
}