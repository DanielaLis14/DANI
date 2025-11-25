package pe.edu.upeu.farmafx.utils;

import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Gestor centralizado del Stage principal de la aplicación.
 *
 * <p><b>Propósito único (SRP):</b> Proveer acceso al Stage primario desde cualquier componente Spring.</p>
 *
 * <p><b>Responsabilidades:</b></p>
 * <ul>
 *   <li>Almacenar referencia al Stage principal</li>
 *   <li>Proveer acceso thread-safe</li>
 *   <li>Logging para debugging</li>
 * </ul>
 *
 * <p><b>Uso típico:</b></p>
 * <ul>
 *   <li>MainGuiController configura el Stage al inicializar</li>
 *   <li>Controllers CRUD lo usan para diálogos modales</li>
 *   <li>Componentes Toast/DialogHelper lo usan para notificaciones</li>
 * </ul>
 *
 * @author FarmaFX Team
 * @version 2.0 - Spring Component
 */
@Component
public class StageManager {

    private static final Logger logger = LoggerFactory.getLogger(StageManager.class);

    private volatile Stage primaryStage;

    /**
     * Configura el Stage principal de la aplicación.
     * Debe llamarse UNA VEZ al iniciar MainGui.
     *
     * @param stage Stage principal (MainGui window)
     */
    public void setPrimaryStage(Stage stage) {
        if (stage == null) {
            logger.warn("Intento de configurar Stage null - ignorado");
            return;
        }

        if (this.primaryStage != null) {
            logger.warn("Stage ya configurado - sobreescribiendo con: {}", stage.getTitle());
        }

        this.primaryStage = stage;
        logger.info("Stage principal configurado: {}", stage.getTitle());
    }

    /**
     * Obtiene el Stage principal configurado.
     *
     * @return Stage principal o null si no se ha configurado
     */
    public Stage getPrimaryStage() {
        if (primaryStage == null) {
            logger.debug("Stage principal aún no configurado");
        }
        return primaryStage;
    }

    /**
     * Verifica si el Stage principal está configurado.
     *
     * @return true si está configurado, false en caso contrario
     */
    public boolean isConfigured() {
        return primaryStage != null;
    }

    /**
     * Limpia la referencia al Stage (útil para tests).
     */
    public void clear() {
        logger.debug("Limpiando referencia a Stage principal");
        this.primaryStage = null;
    }
}
