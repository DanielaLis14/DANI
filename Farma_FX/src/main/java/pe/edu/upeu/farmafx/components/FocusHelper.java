package pe.edu.upeu.farmafx.components;

import javafx.scene.Node;
import javafx.scene.control.TextInputControl;
import javafx.scene.input.MouseEvent;
import lombok.extern.slf4j.Slf4j;

/**
 * Helper para gestionar focus de campos de entrada.
 * Permite quitar focus al hacer click fuera de los inputs.
 *
 * <p>Uso típico:</p>
 * <pre>{@code
 * // En initialize() del controller, después de que Scene esté disponible
 * Platform.runLater(() -> {
 *     Node root = tableMarcas.getScene().getRoot();
 *     FocusHelper.configurarClickFuera(root, txtPagina, txtBusqueda, txtNombre, txtDescripcion);
 * });
 * }</pre>
 *
 * @author Claude Code
 * @version 1.0
 */
@Slf4j
public class FocusHelper {

    private FocusHelper() {
        // Utility class - no instanciar
    }

    /**
     * Configura el root para quitar focus de inputs al hacer click fuera.
     *
     * @param root      Nodo raíz donde se registra el listener (típicamente BorderPane)
     * @param inputs    TextFields/TextAreas que deben perder focus al click fuera
     */
    public static void configurarClickFuera(Node root, TextInputControl... inputs) {
        if (root == null || inputs == null || inputs.length == 0) {
            log.warn("FocusHelper: root o inputs nulos/vacíos, no se configuró");
            return;
        }

        root.addEventFilter(MouseEvent.MOUSE_CLICKED, event -> {
            for (TextInputControl input : inputs) {
                if (debeQuitarFocus(input, event)) {
                    root.requestFocus();
                    log.debug("Focus removido de: {}", input.getId());
                    return; // Solo uno puede tener focus a la vez
                }
            }
        });

        log.debug("FocusHelper configurado con {} inputs", inputs.length);
    }

    /**
     * Verifica si el input debe perder focus.
     * Retorna true si:
     * 1. El input no es null
     * 2. El input tiene focus actualmente
     * 3. El click fue FUERA del input
     */
    private static boolean debeQuitarFocus(TextInputControl input, MouseEvent event) {
        if (input == null || !input.isFocused()) {
            return false;
        }

        // Verificar si el click fue dentro del input
        double x = event.getScreenX();
        double y = event.getScreenY();

        return !input.contains(input.screenToLocal(x, y));
    }
}
