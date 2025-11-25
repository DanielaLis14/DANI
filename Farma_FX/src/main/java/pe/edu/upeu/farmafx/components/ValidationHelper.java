package pe.edu.upeu.farmafx.components;

import javafx.scene.control.Label;
import javafx.scene.control.TextInputControl;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Helper para validación de TextField en tiempo real.
 * Usa CSS classes para feedback visual y Label opcional para mensajes.
 *
 * <p>Uso típico:</p>
 * <pre>{@code
 * // Con Label de error
 * ValidationHelper nombreVal = new ValidationHelper(txtNombre, lblNombreError)
 *     .required("El nombre es requerido")
 *     .minLength(2, "Mínimo 2 caracteres")
 *     .maxLength(100, "Máximo 100 caracteres")
 *     .configure();
 *
 * // Sin Label (solo CSS)
 * ValidationHelper descVal = new ValidationHelper(txtDescripcion)
 *     .maxLength(500, "Máximo 500 caracteres")
 *     .configure();
 *
 * // En guardar()
 * if (!nombreVal.validate() || !descVal.validate()) {
 *     Toast.showWarning(stage, "Corrija los campos", Toast.DURATION_NORMAL);
 *     return;
 * }
 * }</pre>
 *
 * <p>CSS classes aplicadas:</p>
 * <ul>
 *   <li>.field-error - Borde rojo cuando hay error</li>
 *   <li>.field-success - Borde verde cuando es válido</li>
 * </ul>
 *
 * @author Claude Code
 * @version 1.0
 */
@Slf4j
public class ValidationHelper {

    private static final String CSS_ERROR = "field-error";
    private static final String CSS_SUCCESS = "field-success";

    private final TextInputControl field;
    private final Label errorLabel;
    private final List<Validator> validators = new ArrayList<>();
    private boolean validateOnType = true;
    private boolean showSuccessState = false;

    /**
     * Constructor con Label de error.
     *
     * @param field      TextField o TextArea a validar
     * @param errorLabel Label donde mostrar mensajes de error (puede ser null)
     */
    public ValidationHelper(TextInputControl field, Label errorLabel) {
        this.field = field;
        this.errorLabel = errorLabel;
    }

    /**
     * Constructor sin Label (solo feedback visual CSS).
     *
     * @param field TextField o TextArea a validar
     */
    public ValidationHelper(TextInputControl field) {
        this(field, null);
    }

    // ========== VALIDATORS ==========

    /**
     * Campo requerido (no vacío).
     *
     * @param mensaje Mensaje de error si está vacío
     * @return this para encadenamiento
     */
    public ValidationHelper required(String mensaje) {
        validators.add(text -> text.isEmpty() ? mensaje : null);
        return this;
    }

    /**
     * Longitud mínima.
     *
     * @param min     Cantidad mínima de caracteres
     * @param mensaje Mensaje de error
     * @return this para encadenamiento
     */
    public ValidationHelper minLength(int min, String mensaje) {
        validators.add(text -> text.length() < min ? mensaje : null);
        return this;
    }

    /**
     * Longitud máxima.
     *
     * @param max     Cantidad máxima de caracteres
     * @param mensaje Mensaje de error
     * @return this para encadenamiento
     */
    public ValidationHelper maxLength(int max, String mensaje) {
        validators.add(text -> text.length() > max ? mensaje : null);
        return this;
    }

    /**
     * Debe coincidir con patrón regex.
     *
     * @param regex   Patrón a validar
     * @param mensaje Mensaje de error
     * @return this para encadenamiento
     */
    public ValidationHelper pattern(Pattern regex, String mensaje) {
        validators.add(text -> !text.isEmpty() && !regex.matcher(text).matches() ? mensaje : null);
        return this;
    }

    /**
     * Debe coincidir con patrón regex (String).
     *
     * @param regex   Patrón como String
     * @param mensaje Mensaje de error
     * @return this para encadenamiento
     */
    public ValidationHelper pattern(String regex, String mensaje) {
        return pattern(Pattern.compile(regex), mensaje);
    }

    /**
     * Validador personalizado.
     *
     * @param validator Función que retorna mensaje de error o null si válido
     * @return this para encadenamiento
     */
    public ValidationHelper custom(Validator validator) {
        validators.add(validator);
        return this;
    }

    // ========== CONFIGURATION ==========

    /**
     * Desactiva validación mientras escribe (solo al perder foco).
     *
     * @return this para encadenamiento
     */
    public ValidationHelper validateOnBlurOnly() {
        this.validateOnType = false;
        return this;
    }

    /**
     * Muestra borde verde cuando el campo es válido.
     * Por defecto está desactivado (solo muestra error).
     *
     * @return this para encadenamiento
     */
    public ValidationHelper showSuccess() {
        this.showSuccessState = true;
        return this;
    }

    /**
     * Configura los listeners de validación.
     * Debe llamarse después de agregar todos los validators.
     *
     * @return this para encadenamiento
     */
    public ValidationHelper configure() {
        // Validar al perder foco
        field.focusedProperty().addListener((obs, wasFocused, isFocused) -> {
            if (!isFocused) {
                validate();
            }
        });

        // Validar mientras escribe (si está habilitado)
        if (validateOnType) {
            field.textProperty().addListener((obs, oldVal, newVal) -> {
                // Solo validar si ya tuvo foco antes (evitar validar al cargar)
                if (field.isFocused()) {
                    validate();
                }
            });
        }

        log.debug("ValidationHelper configurado para campo con {} validadores", validators.size());
        return this;
    }

    // ========== VALIDATION ==========

    /**
     * Ejecuta todas las validaciones.
     *
     * @return true si todas las validaciones pasan
     */
    public boolean validate() {
        String text = getText();

        for (Validator v : validators) {
            String error = v.validate(text);
            if (error != null) {
                showError(error);
                return false;
            }
        }

        clearError();
        return true;
    }

    /**
     * Valida sin modificar estado visual.
     * Útil para verificar sin mostrar errores.
     *
     * @return true si todas las validaciones pasan
     */
    public boolean isValid() {
        String text = getText();
        return validators.stream()
            .allMatch(v -> v.validate(text) == null);
    }

    /**
     * Obtiene el texto actual del campo (trimmed).
     *
     * @return texto sin espacios al inicio/fin
     */
    public String getText() {
        String text = field.getText();
        return text == null ? "" : text.trim();
    }

    // ========== VISUAL STATE ==========

    private void showError(String mensaje) {
        field.getStyleClass().removeAll(CSS_ERROR, CSS_SUCCESS);
        field.getStyleClass().add(CSS_ERROR);

        if (errorLabel != null) {
            errorLabel.setText(mensaje);
            errorLabel.setVisible(true);
            errorLabel.setManaged(true);
        }

        log.debug("Validación fallida: {}", mensaje);
    }

    private void clearError() {
        field.getStyleClass().removeAll(CSS_ERROR, CSS_SUCCESS);

        if (showSuccessState && !getText().isEmpty()) {
            field.getStyleClass().add(CSS_SUCCESS);
        }

        if (errorLabel != null) {
            errorLabel.setText("");
            errorLabel.setVisible(false);
            errorLabel.setManaged(false);
        }
    }

    /**
     * Limpia estado visual sin validar.
     * Útil al limpiar formulario.
     */
    public void reset() {
        field.getStyleClass().removeAll(CSS_ERROR, CSS_SUCCESS);
        if (errorLabel != null) {
            errorLabel.setText("");
            errorLabel.setVisible(false);
            errorLabel.setManaged(false);
        }
    }

    // ========== INTERFACE ==========

    /**
     * Interface funcional para validadores custom.
     */
    @FunctionalInterface
    public interface Validator {
        /**
         * Valida el texto.
         *
         * @param text Texto a validar (ya trimmed)
         * @return mensaje de error o null si válido
         */
        String validate(String text);
    }
}