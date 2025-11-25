package pe.edu.upeu.farmafx.components;

import javafx.scene.control.Label;
import javafx.scene.control.TextInputControl;
import lombok.extern.slf4j.Slf4j;

/**
 * Helper para mostrar contador de caracteres en campos de texto.
 * Muestra "125/600" y cambia de color según el porcentaje usado.
 *
 * <p>Uso típico:</p>
 * <pre>{@code
 * // Básico - solo contador
 * CharCounterHelper.configure(txtDescripcion, lblContador, 600);
 *
 * // Con umbrales personalizados
 * new CharCounterHelper(txtDescripcion, lblContador, 600)
 *     .warningAt(80)   // Amarillo al 80%
 *     .errorAt(100)    // Rojo al 100%
 *     .configure();
 * }</pre>
 *
 * <p>CSS classes aplicadas automáticamente:</p>
 * <ul>
 *   <li>.counter-normal - Gris (por defecto)</li>
 *   <li>.counter-warning - Amarillo (al alcanzar warningAt%)</li>
 *   <li>.counter-error - Rojo (al alcanzar errorAt%)</li>
 * </ul>
 *
 * @author Claude Code
 * @version 1.0
 */
@Slf4j
public class CharCounterHelper {

    private static final String CSS_NORMAL = "counter-normal";
    private static final String CSS_WARNING = "counter-warning";
    private static final String CSS_ERROR = "counter-error";

    private final TextInputControl field;
    private final Label counterLabel;
    private final int maxLength;

    private int warningPercent = 80;
    private int errorPercent = 100;

    /**
     * Constructor principal.
     *
     * @param field        Campo de texto (TextField o TextArea)
     * @param counterLabel Label donde mostrar el contador
     * @param maxLength    Límite máximo de caracteres
     */
    public CharCounterHelper(TextInputControl field, Label counterLabel, int maxLength) {
        this.field = field;
        this.counterLabel = counterLabel;
        this.maxLength = maxLength;
    }

    /**
     * Configura el umbral de warning (amarillo).
     * Por defecto es 80%.
     *
     * @param percent Porcentaje (0-100) donde mostrar warning
     * @return this para encadenamiento
     */
    public CharCounterHelper warningAt(int percent) {
        this.warningPercent = Math.max(0, Math.min(100, percent));
        return this;
    }

    /**
     * Configura el umbral de error (rojo).
     * Por defecto es 100%.
     *
     * @param percent Porcentaje (0-100) donde mostrar error
     * @return this para encadenamiento
     */
    public CharCounterHelper errorAt(int percent) {
        this.errorPercent = Math.max(0, Math.min(100, percent));
        return this;
    }

    /**
     * Configura el listener y muestra el contador inicial.
     *
     * @return this para encadenamiento
     */
    public CharCounterHelper configure() {
        // Listener para actualizar en cada cambio
        field.textProperty().addListener((obs, oldVal, newVal) -> updateCounter(newVal));

        // Mostrar estado inicial
        updateCounter(field.getText());

        log.debug("CharCounterHelper configurado - máximo: {} caracteres", maxLength);
        return this;
    }

    /**
     * Actualiza el texto y estilo del contador.
     */
    private void updateCounter(String text) {
        int currentLength = text == null ? 0 : text.length();
        int percent = (currentLength * 100) / maxLength;

        // Actualizar texto
        counterLabel.setText(currentLength + "/" + maxLength);

        // Actualizar estilo según porcentaje
        updateStyle(percent);
    }

    /**
     * Aplica la clase CSS según el porcentaje usado.
     */
    private void updateStyle(int percent) {
        counterLabel.getStyleClass().removeAll(CSS_NORMAL, CSS_WARNING, CSS_ERROR);

        if (percent >= errorPercent) {
            counterLabel.getStyleClass().add(CSS_ERROR);
        } else if (percent >= warningPercent) {
            counterLabel.getStyleClass().add(CSS_WARNING);
        } else {
            counterLabel.getStyleClass().add(CSS_NORMAL);
        }
    }

    /**
     * Obtiene la cantidad actual de caracteres.
     *
     * @return cantidad de caracteres en el campo
     */
    public int getCurrentLength() {
        String text = field.getText();
        return text == null ? 0 : text.length();
    }

    /**
     * Verifica si el campo excede el límite.
     *
     * @return true si excede maxLength
     */
    public boolean isOverLimit() {
        return getCurrentLength() > maxLength;
    }

    /**
     * Verifica si el campo está en zona de warning.
     *
     * @return true si está en o sobre el umbral de warning
     */
    public boolean isInWarningZone() {
        int percent = (getCurrentLength() * 100) / maxLength;
        return percent >= warningPercent;
    }

    // ========== MÉTODO ESTÁTICO CONVENIENTE ==========

    /**
     * Método estático para configuración rápida con valores por defecto.
     * Warning al 80%, Error al 100%.
     *
     * @param field        Campo de texto
     * @param counterLabel Label del contador
     * @param maxLength    Límite máximo
     * @return instancia configurada
     */
    public static CharCounterHelper configure(TextInputControl field, Label counterLabel, int maxLength) {
        return new CharCounterHelper(field, counterLabel, maxLength).configure();
    }
}