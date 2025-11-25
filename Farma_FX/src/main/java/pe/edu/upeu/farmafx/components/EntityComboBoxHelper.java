package pe.edu.upeu.farmafx.components;

import javafx.collections.FXCollections;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Helper genérico para ComboBox de selección de entidades (FK).
 * Usado típicamente en formularios para seleccionar Marca, Categoría, Cliente, etc.
 *
 * <p><b>Estado:</b> ✅ ACTIVO - Listo para ProductosController y otros CRUDs.</p>
 *
 * <p><b>Características principales:</b></p>
 * <ul>
 *   <li>Genérico {@code <T>} - reutilizable para cualquier entidad JPA</li>
 *   <li>Map interno para vincular labels del ComboBox con entidades reales</li>
 *   <li>Placeholder estándar ("-- Seleccione --") para indicar sin selección</li>
 *   <li>✅ <b>Soporte para nullable</b>: permite FKs opcionales con texto personalizado</li>
 *   <li>✅ <b>Soporte para autocomplete</b>: filtrado mientras se escribe</li>
 *   <li>API fluida para configuración encadenada</li>
 *   <li>Listener opcional para detectar cambios de selección</li>
 *   <li>Auto-agregado de entidades faltantes en modo edición</li>
 *   <li>Validaciones robustas contra NPE</li>
 * </ul>
 *
 * <p><b>Uso típico en ProductosController:</b></p>
 * <pre>{@code
 * // ========== DECLARACIÓN ==========
 * @FXML private ComboBox<String> cbxMarca;      // NOT NULL
 * @FXML private ComboBox<String> cbxCategoria;  // NULLABLE
 *
 * private EntityComboBoxHelper<Marca> marcaHelper;
 * private EntityComboBoxHelper<Categoria> categoriaHelper;
 *
 * // ========== CONFIGURACIÓN (en initialize) ==========
 *
 * // Marca (NOT NULL - sin opción null, CON autocomplete)
 * marcaHelper = new EntityComboBoxHelper<>(cbxMarca)
 *     .withAutocomplete()  // ← Habilita filtrado
 *     .loadEntities(marcaService.listarActivos(), Marca::getNombreMarca);
 *
 * // Categoría (NULLABLE - con opción null, CON autocomplete)
 * categoriaHelper = new EntityComboBoxHelper<>(cbxCategoria)
 *     .withNullOption("(Sin categoría)")
 *     .withAutocomplete()
 *     .loadEntities(categoriaService.listarActivos(), Categoria::getNombreCategoria);
 *
 * // ========== CARGAR EN EDICIÓN ==========
 * marcaHelper.selectEntity(producto.getMarca());
 * categoriaHelper.selectEntity(producto.getCategoria());  // null OK
 *
 * // ========== OBTENER AL GUARDAR ==========
 * Marca marca = marcaHelper.getSelectedEntity();
 * Categoria categoria = categoriaHelper.getSelectedEntity();  // null es válido
 * }</pre>
 *
 * @param <T> Tipo de la entidad JPA (Marca, Categoria, Cliente, etc.)
 * @version 3.0
 * @since 2025-11-23
 */
@Slf4j
public class EntityComboBoxHelper<T> {

    private static final String PLACEHOLDER = "-- Seleccione --";

    private final ComboBox<String> combo;
    private final Map<String, T> entityMap = new LinkedHashMap<>();
    private List<String> allLabels = new ArrayList<>();  // Lista completa para filtrado
    private T selectedEntity = null;
    private Consumer<T> selectionChangeListener;
    private Function<T, String> storedLabelExtractor;  // Guardado para selectEntity

    // ========== SOPORTE NULLABLE ==========
    private boolean allowNull = false;
    private String nullLabel = null;

    // ========== SOPORTE AUTOCOMPLETE ==========
    private boolean autocompleteEnabled = false;
    private boolean updating = false;  // Flag para evitar recursión

    /**
     * Constructor del helper.
     *
     * @param combo ComboBox del FXML para selección de entidad
     * @throws IllegalArgumentException si combo es null
     */
    public EntityComboBoxHelper(ComboBox<String> combo) {
        if (combo == null) {
            throw new IllegalArgumentException("ComboBox no puede ser null");
        }
        this.combo = combo;
    }

    /**
     * Habilita soporte para selección null (FK opcional).
     * Agrega una opción especial al ComboBox que representa "sin valor".
     *
     * <p><b>Ejemplo:</b> Categoría puede ser null en Producto (productos sin categoría).</p>
     *
     * @param nullLabel Texto a mostrar para la opción null (ej: "(Sin categoría)", "(Ninguno)")
     * @return this (para encadenamiento fluido)
     * @throws IllegalArgumentException si nullLabel es null o vacío
     */
    public EntityComboBoxHelper<T> withNullOption(String nullLabel) {
        if (nullLabel == null || nullLabel.trim().isEmpty()) {
            throw new IllegalArgumentException("nullLabel no puede ser null o vacío");
        }
        this.allowNull = true;
        this.nullLabel = nullLabel.trim();
        return this;
    }

    /**
     * Habilita autocomplete/filtrado en el ComboBox.
     * El ComboBox se vuelve editable y filtra mientras se escribe.
     *
     * <p><b>Debe llamarse ANTES de loadEntities().</b></p>
     *
     * @return this (para encadenamiento fluido)
     */
    public EntityComboBoxHelper<T> withAutocomplete() {
        this.autocompleteEnabled = true;
        return this;
    }

    /**
     * Carga entidades desde BD y llena el ComboBox.
     * Se debe llamar cada vez que se necesite refrescar la lista (ej: después de agregar nueva marca).
     *
     * <p><b>Nota:</b> Si se habilitó {@code withNullOption()}, la opción null se agrega automáticamente.</p>
     *
     * @param entities Lista de entidades desde service.findAll()
     * @param labelExtractor Función para extraer el texto a mostrar (ej: Marca::getNombreMarca)
     * @return this (para encadenamiento fluido)
     * @throws IllegalStateException si combo es null (binding FXML incorrecto)
     * @throws IllegalArgumentException si entities o labelExtractor son null
     */
    public EntityComboBoxHelper<T> loadEntities(List<T> entities, Function<T, String> labelExtractor) {
        if (combo == null) {
            throw new IllegalStateException("ComboBox es null - verificar @FXML binding");
        }
        if (entities == null) {
            throw new IllegalArgumentException("Lista de entidades no puede ser null");
        }
        if (labelExtractor == null) {
            throw new IllegalArgumentException("labelExtractor no puede ser null");
        }

        this.storedLabelExtractor = labelExtractor;

        combo.getItems().clear();
        entityMap.clear();
        allLabels.clear();

        // 1. Agregar placeholder inicial
        combo.getItems().add(PLACEHOLDER);
        allLabels.add(PLACEHOLDER);

        // 2. Agregar opción null si está habilitado
        if (allowNull && nullLabel != null) {
            combo.getItems().add(nullLabel);
            allLabels.add(nullLabel);
            entityMap.put(nullLabel, null);  // Mapea texto → null
        }

        // 3. Agregar entidades
        for (T entity : entities) {
            if (entity == null) {
                continue;  // Ignorar nulls en la lista
            }
            String label = labelExtractor.apply(entity);
            if (label != null && !label.trim().isEmpty()) {
                combo.getItems().add(label);
                allLabels.add(label);
                entityMap.put(label, entity);
            }
        }

        combo.setValue(PLACEHOLDER);
        selectedEntity = null;

        // Configurar autocomplete si está habilitado
        if (autocompleteEnabled) {
            configurarAutocomplete();
        }

        log.debug("EntityComboBoxHelper cargado: {} entidades, autocomplete={}",
                entities.size(), autocompleteEnabled);

        return this;
    }

    /**
     * Configura el comportamiento de autocomplete.
     */
    private void configurarAutocomplete() {
        combo.setEditable(true);
        TextField editor = combo.getEditor();

        // Filtrar mientras se escribe
        editor.textProperty().addListener((obs, oldVal, newVal) -> {
            if (updating) return;
            filtrar(newVal);
        });

        // Al seleccionar un ítem del dropdown
        combo.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (updating) return;

            updating = true;
            try {
                if (newVal != null && !newVal.equals(PLACEHOLDER)) {
                    selectedEntity = entityMap.get(newVal);
                    if (selectionChangeListener != null) {
                        selectionChangeListener.accept(selectedEntity);
                    }
                }
            } finally {
                updating = false;
            }
        });

        // Abrir dropdown al escribir
        editor.addEventHandler(KeyEvent.KEY_RELEASED, event -> {
            if (event.getCode() == KeyCode.DOWN || event.getCode() == KeyCode.UP) {
                return;
            }
            if (event.getCode() == KeyCode.ENTER || event.getCode() == KeyCode.ESCAPE) {
                return;
            }
            if (!combo.isShowing() && !editor.getText().isEmpty()
                    && !editor.getText().equals(PLACEHOLDER)) {
                combo.show();
            }
        });

        // ESC cierra dropdown
        editor.addEventHandler(KeyEvent.KEY_PRESSED, event -> {
            if (event.getCode() == KeyCode.ESCAPE && combo.isShowing()) {
                combo.hide();
                event.consume();
            }
        });
    }

    /**
     * Filtra la lista según el término escrito.
     */
    private void filtrar(String termRaw) {
        String term = termRaw == null ? "" : termRaw.trim();

        // Si está vacío, mostrar lista completa
        if (term.isEmpty() || term.equals(PLACEHOLDER)) {
            updating = true;
            try {
                combo.getItems().setAll(allLabels);
            } finally {
                updating = false;
            }
            return;
        }

        List<String> filtrados = allLabels.stream()
                .filter(label -> !label.equals(PLACEHOLDER))  // No filtrar placeholder
                .filter(label -> AutocompleteHelper.matchesWordStart(label, term))
                .toList();

        // Siempre incluir placeholder al inicio
        List<String> resultado = new ArrayList<>();
        resultado.add(PLACEHOLDER);
        if (allowNull && nullLabel != null && AutocompleteHelper.matchesWordStart(nullLabel, term)) {
            resultado.add(nullLabel);
        }
        resultado.addAll(filtrados.stream()
                .filter(l -> !l.equals(nullLabel))  // Evitar duplicar nullLabel
                .toList());

        updating = true;
        try {
            combo.getItems().setAll(resultado);
        } finally {
            updating = false;
        }

        // Abrir dropdown si hay resultados
        if (!filtrados.isEmpty() && !combo.isShowing()) {
            combo.show();
        }

        log.debug("Filtrado ComboBox '{}': '{}' -> {} coincidencias",
                combo.getId(), term, filtrados.size());
    }

    /**
     * Selecciona una entidad específica en el ComboBox.
     * Útil al cargar un registro para edición.
     *
     * <p><b>Comportamiento con null:</b></p>
     * <ul>
     *   <li>Si {@code allowNull=true} y {@code entity=null}: selecciona la opción null</li>
     *   <li>Si {@code allowNull=false} y {@code entity=null}: vuelve al placeholder</li>
     * </ul>
     *
     * @param entity Entidad a seleccionar (puede ser null)
     */
    public void selectEntity(T entity) {
        if (storedLabelExtractor == null) {
            throw new IllegalStateException("Debe llamar loadEntities() antes de selectEntity()");
        }
        selectEntity(entity, storedLabelExtractor);
    }

    /**
     * Selecciona una entidad específica en el ComboBox con labelExtractor explícito.
     * Útil al cargar un registro para edición.
     *
     * @param entity Entidad a seleccionar (puede ser null)
     * @param labelExtractor Función para extraer el texto
     */
    public void selectEntity(T entity, Function<T, String> labelExtractor) {
        updating = true;
        try {
            if (entity == null) {
                if (allowNull && nullLabel != null) {
                    combo.setValue(nullLabel);
                    if (autocompleteEnabled) {
                        combo.getEditor().setText(nullLabel);
                    }
                    selectedEntity = null;
                } else {
                    combo.setValue(PLACEHOLDER);
                    if (autocompleteEnabled) {
                        combo.getEditor().setText(PLACEHOLDER);
                    }
                    selectedEntity = null;
                }
                return;
            }

            if (labelExtractor == null) {
                throw new IllegalArgumentException("labelExtractor no puede ser null cuando entity no es null");
            }

            String label = labelExtractor.apply(entity);
            if (label == null || label.trim().isEmpty()) {
                throw new IllegalArgumentException("El label extraído no puede ser null o vacío");
            }

            if (!entityMap.containsKey(label)) {
                // Entidad no está en la lista → agregarla (caso: entidad inactiva en edición)
                combo.getItems().add(label);
                allLabels.add(label);
                entityMap.put(label, entity);
            }

            combo.setValue(label);
            if (autocompleteEnabled) {
                combo.getEditor().setText(label);
            }
            selectedEntity = entity;

        } finally {
            updating = false;
        }
    }

    /**
     * Obtiene la entidad actualmente seleccionada.
     *
     * @return Entidad seleccionada, o null
     */
    public T getSelectedEntity() {
        String selected = combo.getValue();
        if (selected == null || selected.equals(PLACEHOLDER)) {
            return null;
        }
        return entityMap.get(selected);
    }

    /**
     * Verifica si el usuario no ha seleccionado nada (está en placeholder).
     *
     * @return true si está en placeholder "-- Seleccione --"
     */
    public boolean isPlaceholderSelected() {
        String selected = combo.getValue();
        return selected == null || selected.equals(PLACEHOLDER);
    }

    /**
     * Verifica si se seleccionó la opción null (solo cuando allowNull=true).
     *
     * @return true si se seleccionó la opción null
     */
    public boolean isNullSelected() {
        if (!allowNull || nullLabel == null) {
            return false;
        }
        String selected = combo.getValue();
        return nullLabel.equals(selected);
    }

    /**
     * Limpia la selección actual (vuelve a placeholder).
     */
    public void clear() {
        updating = true;
        try {
            combo.setValue(PLACEHOLDER);
            if (autocompleteEnabled) {
                combo.getEditor().setText("");
                combo.getItems().setAll(allLabels);  // Restaurar lista completa
            }
            selectedEntity = null;
        } finally {
            updating = false;
        }
    }

    /**
     * Configura listener que se ejecuta al cambiar la selección.
     *
     * @param onChange Callback con la entidad seleccionada (puede ser null)
     * @return this (para encadenamiento fluido)
     */
    public EntityComboBoxHelper<T> onSelectionChange(Consumer<T> onChange) {
        this.selectionChangeListener = onChange;

        if (!autocompleteEnabled) {
            // Solo agregar listener si no hay autocomplete (el autocomplete ya tiene su propio listener)
            combo.setOnAction(e -> {
                selectedEntity = getSelectedEntity();
                if (selectionChangeListener != null) {
                    selectionChangeListener.accept(selectedEntity);
                }
            });
        }

        return this;
    }

    /**
     * Verifica si hay una selección válida (no está en placeholder).
     *
     * @return true si hay selección válida
     */
    public boolean hasSelection() {
        return !isPlaceholderSelected();
    }

    /**
     * Obtiene el label actualmente seleccionado.
     *
     * @return Label del ComboBox
     */
    public String getSelectedLabel() {
        String value = combo.getValue();
        return value != null ? value : PLACEHOLDER;
    }

    /**
     * Deshabilita el ComboBox.
     */
    public void disable() {
        combo.setDisable(true);
    }

    /**
     * Habilita el ComboBox.
     */
    public void enable() {
        combo.setDisable(false);
    }

    /**
     * Actualiza la lista de entidades (útil cuando se agregan nuevos items desde otro módulo).
     *
     * @param entities Nueva lista de entidades
     * @return this
     */
    public EntityComboBoxHelper<T> updateEntities(List<T> entities) {
        if (storedLabelExtractor == null) {
            throw new IllegalStateException("Debe llamar loadEntities() antes de updateEntities()");
        }

        T seleccionActual = getSelectedEntity();
        loadEntities(entities, storedLabelExtractor);

        // Restaurar selección si aún existe
        if (seleccionActual != null) {
            String label = storedLabelExtractor.apply(seleccionActual);
            if (entityMap.containsKey(label)) {
                selectEntity(seleccionActual);
            }
        }

        return this;
    }
}