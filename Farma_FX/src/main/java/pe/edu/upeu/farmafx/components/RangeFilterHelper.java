package pe.edu.upeu.farmafx.components;

import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.util.StringConverter;

import java.math.BigDecimal;
import java.util.function.Consumer;
import java.util.function.UnaryOperator;
import java.util.regex.Pattern;

/**
 * Helper para filtro de rango numérico (precio, stock, cantidad, etc.).
 * Usa 2 TextFields (min y max) + botón "Aplicar" + botón "Limpiar".
 *
 * <p><b>Estado:</b> Código preparatorio para ProductosController (pendiente de implementar).
 * Actualmente sin uso en el proyecto, pero diseñado y listo para filtros de rango en CRUDs.</p>
 *
 * <p><b>Características principales:</b></p>
 * <ul>
 *   <li>Record {@code Range} con helpers (isEmpty, isComplete, etc.)</li>
 *   <li>BigDecimal para precisión numérica en precios</li>
 *   <li>Validación automática: solo permite números decimales positivos</li>
 *   <li>Swap automático si min {@literal >} max</li>
 *   <li>Enter en TextFields aplica filtro inmediatamente</li>
 *   <li>Botón "Limpiar" resetea y re-ejecuta callback con rango vacío</li>
 * </ul>
 *
 * <p><b>Uso típico en ProductosController (ejemplo futuro):</b></p>
 * <pre>{@code
 * @FXML private TextField txtPrecioMin;
 * @FXML private TextField txtPrecioMax;
 * @FXML private Button btnAplicarPrecio;
 * @FXML private Button btnLimpiarPrecio;
 *
 * private RangeFilterHelper precioFilterHelper;
 *
 * private void configurarFiltroPrecio() {
 *     precioFilterHelper = new RangeFilterHelper(
 *         txtPrecioMin, txtPrecioMax,
 *         btnAplicarPrecio, btnLimpiarPrecio,
 *         (range) -> {
 *             logger.info("Filtro precio: {} - {}", range.min(), range.max());
 *             paginaActual = 0;
 *             cargarProductos();
 *         }
 *     ).configure();
 * }
 *
 * // Obtener rango para query
 * RangeFilterHelper.Range rango = precioFilterHelper.getRange();
 * if (rango != null) {
 *     // Aplicar WHERE precio >= rango.min() AND precio <= rango.max()
 *     Page<Producto> page = productoService.findByPrecioRange(
 *         rango.min(), rango.max(), pageable
 *     );
 * }
 * }</pre>
 *
 * @version 1.0
 * @since 2025-11-21
 */
public class RangeFilterHelper {

    /**
     * Rango de valores numéricos.
     *
     * @param min Valor mínimo (puede ser null si no se especificó)
     * @param max Valor máximo (puede ser null si no se especificó)
     */
    public record Range(BigDecimal min, BigDecimal max) {
        /**
         * Verifica si el rango está completamente vacío.
         */
        public boolean isEmpty() {
            return min == null && max == null;
        }

        /**
         * Verifica si solo hay valor mínimo.
         */
        public boolean hasOnlyMin() {
            return min != null && max == null;
        }

        /**
         * Verifica si solo hay valor máximo.
         */
        public boolean hasOnlyMax() {
            return min == null && max != null;
        }

        /**
         * Verifica si el rango está completo (min y max).
         */
        public boolean isComplete() {
            return min != null && max != null;
        }
    }

    private final TextField txtMin;
    private final TextField txtMax;
    private final Button btnApply;
    private final Button btnClear;
    private final Consumer<Range> onRangeApplied;

    private Range currentRange = null;

    /**
     * Constructor del helper.
     *
     * @param txtMin TextField para valor mínimo
     * @param txtMax TextField para valor máximo
     * @param btnApply Botón "Aplicar" que ejecuta el filtro
     * @param btnClear Botón "Limpiar" que resetea el filtro
     * @param onRangeApplied Callback que se ejecuta al aplicar el filtro
     */
    public RangeFilterHelper(TextField txtMin, TextField txtMax,
                             Button btnApply, Button btnClear,
                             Consumer<Range> onRangeApplied) {
        this.txtMin = txtMin;
        this.txtMax = txtMax;
        this.btnApply = btnApply;
        this.btnClear = btnClear;
        this.onRangeApplied = onRangeApplied;
    }

    /**
     * Configura los TextFields con validación numérica y listeners de botones.
     * Debe llamarse una sola vez durante initialize().
     *
     * @return this (para encadenamiento fluido)
     */
    public RangeFilterHelper configure() {
        validateTextFields();
        configureNumericInput(txtMin);
        configureNumericInput(txtMax);
        configureButtons();
        return this;
    }

    /**
     * Valida que los componentes no sean null.
     */
    private void validateTextFields() {
        if (txtMin == null || txtMax == null) {
            throw new IllegalStateException("TextFields son null - verificar @FXML binding");
        }
        if (btnApply == null || btnClear == null) {
            throw new IllegalStateException("Buttons son null - verificar @FXML binding");
        }
    }

    /**
     * Configura TextField para aceptar solo números decimales positivos.
     *
     * @param textField TextField a configurar
     */
    private void configureNumericInput(TextField textField) {
        // Patrón: números enteros o decimales positivos (ej: 10, 10.5, 0.99)
        Pattern pattern = Pattern.compile("\\d*\\.?\\d*");

        TextFormatter<String> formatter = new TextFormatter<>(new StringConverter<>() {
            @Override
            public String toString(String object) {
                return object;
            }

            @Override
            public String fromString(String string) {
                return string;
            }
        }, "", change -> {
            String newText = change.getControlNewText();
            if (pattern.matcher(newText).matches()) {
                return change;
            }
            return null; // Rechazar cambio
        });

        textField.setTextFormatter(formatter);
        textField.setPromptText("0.00");
    }

    /**
     * Configura los botones "Aplicar" y "Limpiar".
     */
    private void configureButtons() {
        // Botón Aplicar
        btnApply.setOnAction(e -> applyRange());

        // Enter en cualquier TextField también aplica
        txtMin.setOnAction(e -> applyRange());
        txtMax.setOnAction(e -> applyRange());

        // Botón Limpiar
        btnClear.setOnAction(e -> clear());
    }

    /**
     * Aplica el filtro de rango actual.
     * Valida que min <= max si ambos están presentes.
     */
    private void applyRange() {
        try {
            BigDecimal min = parseValue(txtMin.getText());
            BigDecimal max = parseValue(txtMax.getText());

            // Validación: min debe ser <= max
            if (min != null && max != null && min.compareTo(max) > 0) {
                // Swap automático si están invertidos
                BigDecimal temp = min;
                min = max;
                max = temp;
                txtMin.setText(min.toString());
                txtMax.setText(max.toString());
            }

            currentRange = new Range(min, max);

            if (onRangeApplied != null) {
                onRangeApplied.accept(currentRange);
            }

        } catch (NumberFormatException ex) {
            // Ignorar valores inválidos
            currentRange = new Range(null, null);
        }
    }

    /**
     * Parsea un valor String a BigDecimal.
     *
     * @param text Texto del TextField
     * @return BigDecimal o null si está vacío/inválido
     */
    private BigDecimal parseValue(String text) {
        if (text == null || text.trim().isEmpty()) {
            return null;
        }
        try {
            return new BigDecimal(text.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /**
     * Limpia el filtro de rango (resetea a valores vacíos).
     */
    public void clear() {
        txtMin.clear();
        txtMax.clear();
        currentRange = new Range(null, null);

        if (onRangeApplied != null) {
            onRangeApplied.accept(currentRange);
        }
    }

    /**
     * Obtiene el rango actualmente aplicado.
     *
     * @return Range con min y max (pueden ser null), o null si nunca se aplicó
     */
    public Range getRange() {
        return currentRange;
    }

    /**
     * Verifica si hay un rango activo (al menos min o max especificado).
     *
     * @return true si hay filtro activo, false si está vacío
     */
    public boolean hasActiveRange() {
        return currentRange != null && !currentRange.isEmpty();
    }

    /**
     * Establece un rango específico programáticamente.
     *
     * @param min Valor mínimo (puede ser null)
     * @param max Valor máximo (puede ser null)
     */
    public void setRange(BigDecimal min, BigDecimal max) {
        if (min != null) {
            txtMin.setText(min.toString());
        } else {
            txtMin.clear();
        }

        if (max != null) {
            txtMax.setText(max.toString());
        } else {
            txtMax.clear();
        }

        currentRange = new Range(min, max);
    }

    /**
     * Deshabilita los controles del filtro.
     */
    public void disable() {
        txtMin.setDisable(true);
        txtMax.setDisable(true);
        btnApply.setDisable(true);
        btnClear.setDisable(true);
    }

    /**
     * Habilita los controles del filtro.
     */
    public void enable() {
        txtMin.setDisable(false);
        txtMax.setDisable(false);
        btnApply.setDisable(false);
        btnClear.setDisable(false);
    }
}