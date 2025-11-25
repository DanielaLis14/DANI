package pe.edu.upeu.farmafx.components;

import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.ObservableList;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.TableRow;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Helper para configurar TableView de forma declarativa y reutilizable.
 * Elimina código repetitivo en controllers de CRUD.
 * Responsabilidad única: Configurar columnas y comportamiento de tablas.
 * Sin dependencias de Spring, instancia reutilizable.
 * 
 * @version 1.1 - Carga automática de components.css para estilos de estado
 */
@Slf4j
public class TableViewHelper {

    /**
     * Configurar columna de texto simple.
     */
    public <T> void configurarColumnaTexto(TableColumn<T, String> columna, Function<T, String> extractor) {
        columna.setCellValueFactory(c -> {
            String valor = extractor.apply(c.getValue());
            return new SimpleStringProperty(valor != null ? valor : "-");
        });
    }

    /**
     * Configurar columna numérica (Long, Integer, etc).
     */
    public <T, N extends Number> void configurarColumnaNumerica(TableColumn<T, N> columna, Function<T, N> extractor) {
        columna.setCellValueFactory(c -> new SimpleObjectProperty<>(extractor.apply(c.getValue())));
    }

    /**
     * Configurar columna numérica con estilo condicional.
     * Útil para alertas visuales (ej: stock bajo, valores críticos).
     * 
     * @param columna Columna a configurar
     * @param extractor Función que obtiene el valor numérico
     * @param styleClassProvider Función que retorna clase CSS según entidad (puede retornar null/"")
     */
    public <T, N extends Number> void configurarColumnaNumerica(
        TableColumn<T, N> columna, 
        Function<T, N> extractor,
        Function<T, String> styleClassProvider
    ) {
        columna.setCellValueFactory(c -> new SimpleObjectProperty<>(extractor.apply(c.getValue())));
        
        columna.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(N item, boolean empty) {
                super.updateItem(item, empty);
                
                getStyleClass().removeAll("table-cell-warning", "table-cell-danger");
                
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.toString());
                    
                    @SuppressWarnings("unchecked")
                    T entidad = (T) getTableRow().getItem();
                    if (entidad != null) {
                        String styleClass = styleClassProvider.apply(entidad);
                        if (styleClass != null && !styleClass.isEmpty()) {
                            getStyleClass().add(styleClass);
                        }
                    }
                }
            }
        });
    }

    /**
     * Configurar columna de moneda (BigDecimal) con formato "S/ X.XX".
     */
    public <T> void configurarColumnaMoneda(TableColumn<T, BigDecimal> columna, Function<T, BigDecimal> extractor) {
        columna.setCellValueFactory(c -> new SimpleObjectProperty<>(extractor.apply(c.getValue())));
        columna.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(BigDecimal item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : String.format("S/ %.2f", item));
            }
        });
    }

    /**
     * Configurar columna booleana (Activo/Inactivo) con colores.
     * Verde para activo, rojo para inactivo.
     * Usa CSS classes (.table-cell-activo, .table-cell-inactivo).
     * NOTA: tables.css debe estar cargado en el FXML del módulo.
     */
    public <T> void configurarColumnaBooleana(TableColumn<T, Boolean> columna, Function<T, Boolean> extractor) {
        columna.setCellValueFactory(c -> new SimpleObjectProperty<>(extractor.apply(c.getValue())));

        columna.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Boolean item, boolean empty) {
                super.updateItem(item, empty);
                
                // Limpiar estilos previos
                getStyleClass().removeAll("table-cell-activo", "table-cell-inactivo");
                
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item ? "Activo" : "Inactivo");
                    getStyleClass().add(item ? "table-cell-activo" : "table-cell-inactivo");
                }
            }
        });
    }

    /**
     * Configurar tabla: asignar items y listener de selección.
     */
    public <T> void configurarTabla(
        TableView<T> tabla,
        ObservableList<T> items,
        Consumer<T> onSeleccionado
    ) {
        tabla.setItems(items);
        tabla.getSelectionModel().selectedItemProperty().addListener(
            (obs, old, nuevo) -> {
                if (nuevo != null) {
                    onSeleccionado.accept(nuevo);
                }
            }
        );
    }

    /**
     * Configurar menú contextual en filas de tabla.
     * El controller decide qué items mostrar según permisos del usuario.
     * Si el menú está vacío, no se muestra nada.
     */
    public <T> void configurarMenuContextual(
        TableView<T> tabla,
        Function<T, ContextMenu> crearMenu
    ) {
        tabla.setRowFactory(tv -> new TableRow<>() {
            @Override
            protected void updateItem(T item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setContextMenu(null);
                } else {
                    ContextMenu menu = crearMenu.apply(item);
                    // Solo mostrar menú si tiene items
                    setContextMenu(menu != null && !menu.getItems().isEmpty() ? menu : null);
                }
            }
        });
    }

    /**
     * Bloquea el reordenamiento de columnas arrastrando headers.
     * Útil cuando se usa ordenamiento con ComboBox para evitar confusión al usuario.
     *
     * @param tabla TableView a configurar
     */
    public <T> void bloquearReordenamiento(TableView<T> tabla) {
        if (tabla == null) return;
        tabla.getColumns().forEach(col -> col.setReorderable(false));
    }

    /**
     * Bloquea el ordenamiento por click en headers.
     * Útil cuando se usa ordenamiento con ComboBox (SortHelper).
     *
     * @param tabla TableView a configurar
     */
    public <T> void bloquearOrdenamiento(TableView<T> tabla) {
        if (tabla == null) return;
        tabla.getColumns().forEach(col -> col.setSortable(false));
    }
    

}
