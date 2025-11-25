package pe.edu.upeu.farmafx.components;

import javafx.scene.control.TextField;
import lombok.extern.slf4j.Slf4j;
import org.controlsfx.control.textfield.AutoCompletionBinding;
import org.controlsfx.control.textfield.TextFields;

import java.util.Collections;
import java.util.List;
import java.text.Normalizer;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Helper reutilizable para agregar autocomplete con ControlsFX a un TextField.
 * Usa debounce interno para no saturar el proveedor de sugerencias mientras se escribe.
 *
 * Flujo:
 * - El usuario escribe → debounce de 200 ms → se piden sugerencias (provider)
 * - Si hay selección en el dropdown, se setea el texto y se ejecuta onSelect opcional
 */
@Slf4j
public class AutocompleteHelper {

    private static final int MIN_CHARS = 2;

    private final TextField searchField;
    private final Function<String, List<String>> suggestionProvider;
    private final Consumer<String> onSelect;

    private AutoCompletionBinding<String> binding;

    public AutocompleteHelper(TextField searchField,
                              Function<String, List<String>> suggestionProvider) {
        this(searchField, suggestionProvider, null);
    }

    public AutocompleteHelper(TextField searchField,
                              Function<String, List<String>> suggestionProvider,
                              Consumer<String> onSelect) {
        this.searchField = searchField;
        this.suggestionProvider = suggestionProvider;
        this.onSelect = onSelect;
    }

    /**
     * Configura el binding y el debounce. Llamar una sola vez en initialize().
     */
    public AutocompleteHelper configure() {
        binding = TextFields.bindAutoCompletion(searchField, request -> {
            String term = request.getUserText() == null ? "" : request.getUserText().trim();
            if (term.length() < MIN_CHARS) {
                return Collections.emptyList();
            }

            try {
                List<String> result = suggestionProvider.apply(term);
                log.debug("Sugerencias cargadas ({}): {}", result == null ? 0 : result.size(), term);
                return result == null ? Collections.emptyList() : result;
            } catch (Exception ex) {
                log.warn("No se pudieron obtener sugerencias para '{}'", term, ex);
                return Collections.emptyList();
            }
        });
        binding.setVisibleRowCount(8);
        binding.setOnAutoCompleted(event -> {
            String completion = event.getCompletion();
            searchField.setText(completion);
            if (onSelect != null) {
                onSelect.accept(completion);
            }
        });

        return this;
    }

    /**
     * Libera recursos (timeline y binding). Útil al cerrar la vista.
     */
    public void dispose() {
        if (binding != null) {
            binding.dispose();
        }
    }

    /**
     * Normaliza texto para comparación (minúsculas y sin acentos).
     */
    public static String normalizeText(String valor) {
        String base = valor == null ? "" : valor.toLowerCase();
        return Normalizer.normalize(base, Normalizer.Form.NFD).replaceAll("\\p{M}+", "");
    }

    /**
     * Retorna true si el texto empieza con el término o lo contiene al inicio de una palabra.
     */
    public static boolean matchesWordStart(String texto, String rawTerm) {
        if (texto == null || rawTerm == null) {
            return false;
        }
        String filtroNorm = normalizeText(rawTerm.trim());
        if (filtroNorm.isEmpty()) {
            return false;
        }
        String normalizado = normalizeText(texto);
        return normalizado.startsWith(filtroNorm) || normalizado.contains(" " + filtroNorm);
    }
}
