package pe.edu.upeu.farmafx.utils;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Cargador de vistas FXML con caché integrado con Spring.
 * Responsabilidad única: Cargar y cachear vistas FXML.
 *
 * NO maneja:
 * - Creación de Stages/Scenes (responsabilidad del controller)
 * - Creación de Tabs/ScrollPane (responsabilidad del controller)
 *
 * Inyectable como @Autowired en cualquier bean Spring.
 */
@Component
public class ViewNavigator {

    private static final Logger logger = LoggerFactory.getLogger(ViewNavigator.class);

    private final Map<String, Parent> cache = new ConcurrentHashMap<>();
    private ApplicationContext applicationContext;

    /**
     * Cargar vista FXML desde recurso, con caché.
     * Usado para vistas que no cambian (login, main screen).
     * @param fxmlPath Ruta del archivo FXML (ej: "/view/login.fxml")
     * @return Parent cargado del FXML
     */
    public Parent loadView(String fxmlPath) {
        Objects.requireNonNull(fxmlPath, "fxmlPath");
        return cache.computeIfAbsent(fxmlPath, this::loadFromResource);
    }

    /**
     * Cargar vista FXML desde recurso, SIN caché.
     * Usado para tabs que necesitan refrescar datos en cada apertura.
     * Cada llamada recrea el controller e invoca initialize().
     * @param fxmlPath Ruta del archivo FXML
     * @return Parent nuevo cargado del FXML
     */
    public Parent loadViewNoCache(String fxmlPath) {
        Objects.requireNonNull(fxmlPath, "fxmlPath");
        return loadFromResource(fxmlPath);
    }

    /**
     * Limpiar caché de vistas cargadas.
     */
    public void clearCache() {
        cache.clear();
    }

    /**
     * Inyectar contexto Spring para resolver controladores.
     * Se llama UNA SOLA VEZ en FarmaFxApplication.init()
     */
    public void setApplicationContext(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    /**
     * Cargar FXML desde archivo de recurso.
     * Integra Spring para inyectar dependencias en controladores.
     */
    private Parent loadFromResource(String fxmlPath) {
        // Validación preventiva del path
        if (!fxmlPath.endsWith(".fxml")) {
            throw new IllegalArgumentException("Path inválido (debe terminar en .fxml): " + fxmlPath);
        }

        try {
            var resource = getClass().getResource(fxmlPath);
            if (resource == null) {
                logger.error("❌ Recurso FXML no encontrado: {}", fxmlPath);
                throw new IllegalArgumentException("Recurso no encontrado: " + fxmlPath);
            }

            FXMLLoader loader = new FXMLLoader(resource);

            if (applicationContext != null) {
                loader.setControllerFactory(applicationContext::getBean);
                logger.trace("✅ Spring context disponible para {}", fxmlPath);
            } else {
                logger.warn("⚠️ Spring context NULL - controllers sin @Autowired para {}", fxmlPath);
            }

            Parent parent = loader.load();
            logger.debug("✅ Vista cargada exitosamente: {}", fxmlPath);
            return parent;

        } catch (IOException ex) {
            // Diferenciar tipos de error para mejor debugging
            if (ex.getCause() instanceof IllegalArgumentException) {
                logger.error("❌ Controller no inyectable o tipo incompatible para {}: {}",
                    fxmlPath, ex.getCause().getMessage(), ex);
            } else if (ex.getMessage().contains("Location is not set")) {
                logger.error("❌ Recurso FXML no encontrado en classpath: {}", fxmlPath, ex);
            } else {
                logger.error("❌ Archivo FXML corrupto o inválido: {}", fxmlPath, ex);
            }
            throw new IllegalStateException("No se pudo cargar vista: " + fxmlPath, ex);
        }
    }
}
