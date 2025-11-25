package pe.edu.upeu.farmafx.components;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import lombok.extern.slf4j.Slf4j;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Autocomplete para ComboBox editable con filtrado en memoria.
 * No toca la BD: filtra sobre la lista ya cargada (ej. solo activos).
 *
 * <p>Características:
 * <ul>
 *   <li>Filtrado mientras se escribe (normalizado, sin acentos)</li>
 *   <li>Dropdown se abre automáticamente al escribir</li>
 *   <li>Soporte para refrescar lista (updateSource)</li>
 *   <li>Soporte para limpiar selección (clear)</li>
 *   <li>Evita recursión entre listeners</li>
 * </ul>
 *
 * <p>Uso típico en Controller:
 * <pre>{@code
 * comboMarcaHelper = new ComboBoxAutocompleteHelper<>(cbxMarca,
 *         Marca::getNombreMarca,
 *         marca -> log.debug("Marca seleccionada: {}", marca.getNombreMarca()))
 *     .configure();
 *
 * // Refrescar lista si se agregan nuevos items
 * comboMarcaHelper.updateSource(marcaService.listarActivos());
 * }</pre>
 *
 * @param <T> tipo del ítem (entidad o DTO)
 * @version 2.0
 */
@Slf4j
public class ComboBoxAutocompleteHelper<T> {

    private final ComboBox<T> comboBox;
    private final Function<T, String> displayMapper;
    private final Consumer<T> onSelect;
    private ObservableList<T> sourceItems;

    /** Flag para evitar recursión entre valueProperty y textProperty listeners */
    private boolean updating = false;

    /**
     * Constructor principal.
     *
     * @param comboBox      ComboBox a configurar
     * @param displayMapper función que extrae el texto a mostrar (ej. Marca::getNombreMarca)
     * @param onSelect      callback opcional al seleccionar un ítem (puede ser null)
     */
    public ComboBoxAutocompleteHelper(ComboBox<T> comboBox,
                                      Function<T, String> displayMapper,
                                      Consumer<T> onSelect) {
        this.comboBox = comboBox;
        this.displayMapper = displayMapper;
        this.onSelect = onSelect;
        this.sourceItems = FXCollections.observableArrayList(comboBox.getItems());
    }

    /**
     * Constructor sin callback de selección.
     */
    public ComboBoxAutocompleteHelper(ComboBox<T> comboBox,
                                      Function<T, String> displayMapper) {
        this(comboBox, displayMapper, null);
    }

    /**
     * Configura el autocomplete. Llamar una sola vez en initialize().
     *
     * @return this (patrón builder)
     */
    public ComboBoxAutocompleteHelper<T> configure() {
        comboBox.setEditable(true);
        TextField editor = comboBox.getEditor();

        // Filtrar mientras se escribe
        editor.textProperty().addListener((obs, oldVal, newVal) -> {
            if (updating) return; // Evitar recursión
            filtrar(newVal);
        });

        // Al seleccionar un ítem del dropdown
        comboBox.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (updating) return; // Evitar recursión

            updating = true;
            try {
                if (newVal != null) {
                    editor.setText(displayMapper.apply(newVal));
                    if (onSelect != null) {
                        onSelect.accept(newVal);
                    }
                }
            } finally {
                updating = false;
            }
        });

        // Abrir dropdown al hacer focus y escribir
        editor.addEventHandler(KeyEvent.KEY_RELEASED, event -> {
            if (event.getCode() == KeyCode.DOWN || event.getCode() == KeyCode.UP) {
                return; // Dejar navegación normal
            }
            if (event.getCode() == KeyCode.ENTER || event.getCode() == KeyCode.ESCAPE) {
                return; // Dejar comportamiento normal
            }
            if (!comboBox.isShowing() && !editor.getText().isEmpty()) {
                comboBox.show();
            }
        });

        // ESC cierra dropdown y limpia si no hay selección válida
        editor.addEventHandler(KeyEvent.KEY_PRESSED, event -> {
            if (event.getCode() == KeyCode.ESCAPE && comboBox.isShowing()) {
                comboBox.hide();
                event.consume();
            }
        });

        // Inicializa con lista completa
        comboBox.setItems(FXCollections.observableArrayList(sourceItems));

        log.debug("ComboBoxAutocompleteHelper configurado para '{}'", comboBox.getId());
        return this;
    }

    /**
     * Filtra la lista según el término escrito.
     * Usa normalización (sin acentos, case-insensitive).
     */
    private void filtrar(String termRaw) {
        String term = termRaw == null ? "" : termRaw.trim();

        // Si está vacío o muy corto, mostrar lista completa
        if (term.isEmpty()) {
            comboBox.setItems(FXCollections.observableArrayList(sourceItems));
            return;
        }

        List<T> filtrados = sourceItems.stream()
                .filter(Objects::nonNull)
                .filter(item -> AutocompleteHelper.matchesWordStart(displayMapper.apply(item), term))
                .toList();

        comboBox.setItems(FXCollections.observableArrayList(filtrados));

        // Abrir dropdown si hay resultados
        if (!filtrados.isEmpty() && !comboBox.isShowing()) {
            comboBox.show();
        }

        log.debug("Filtrado ComboBox '{}': '{}' -> {} coincidencias",
                comboBox.getId(), term, filtrados.size());
    }

    /**
     * Actualiza la lista fuente (útil cuando se agregan nuevos items desde otro módulo).
     *
     * @param nuevaLista nueva lista de items
     * @return this (patrón builder)
     */
    public ComboBoxAutocompleteHelper<T> updateSource(Collection<T> nuevaLista) {
        T seleccionActual = comboBox.getValue();

        updating = true;
        try {
            sourceItems = FXCollections.observableArrayList(nuevaLista);
            comboBox.setItems(FXCollections.observableArrayList(sourceItems));

            // Restaurar selección si aún existe en la nueva lista
            if (seleccionActual != null && sourceItems.contains(seleccionActual)) {
                comboBox.setValue(seleccionActual);
            }
        } finally {
            updating = false;
        }

        log.debug("ComboBox '{}' actualizado con {} items", comboBox.getId(), sourceItems.size());
        return this;
    }

    /**
     * Limpia la selección y el texto del editor.
     *
     * @return this (patrón builder)
     */
    public ComboBoxAutocompleteHelper<T> clear() {
        updating = true;
        try {
            comboBox.setValue(null);
            comboBox.getEditor().clear();
            comboBox.setItems(FXCollections.observableArrayList(sourceItems));
        } finally {
            updating = false;
        }
        return this;
    }

    /**
     * Establece un valor programáticamente.
     *
     * @param value valor a establecer
     * @return this (patrón builder)
     */
    public ComboBoxAutocompleteHelper<T> setValue(T value) {
        updating = true;
        try {
            comboBox.setValue(value);
            if (value != null) {
                comboBox.getEditor().setText(displayMapper.apply(value));
            } else {
                comboBox.getEditor().clear();
            }
        } finally {
            updating = false;
        }
        return this;
    }

    /**
     * Obtiene el valor seleccionado actualmente.
     *
     * @return valor seleccionado o null
     */
    public T getValue() {
        return comboBox.getValue();
    }

    /**
     * Obtiene el ComboBox configurado.
     *
     * @return ComboBox
     */
    public ComboBox<T> getComboBox() {
        return comboBox;
    }

    /**
     * Deshabilita el ComboBox (modo preview/no edición).
     *
     * @return this
     */
    public ComboBoxAutocompleteHelper<T> disable() {
        comboBox.setDisable(true);
        return this;
    }

    /**
     * Habilita el ComboBox (modo edición).
     *
     * @return this
     */
    public ComboBoxAutocompleteHelper<T> enable() {
        comboBox.setDisable(false);
        return this;
    }
}