package pe.edu.upeu.farmafx.components;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Popup;
import javafx.stage.Stage;
import javafx.util.Duration;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

/**
 * Toast Premium con diseño oscuro, animaciones fluidas y gestión de múltiples notificaciones.
 * Características:
 * - 4 tipos: SUCCESS (verde), ERROR (rojo), INFO (azul), WARNING (amarillo)
 * - Animación slide-in desde abajo + fade-in
 * - Barra de progreso visual del tiempo restante
 * - Botón X para cerrar manualmente
 * - Múltiples Toasts apilados automáticamente
 * - Se oculta al cambiar de ventana (Alt+Tab)
 * - Reposicionamiento automático al redimensionar/mover ventana
 * - Estilos en toast.css (acorde al diseño oscuro del proyecto)
 *
 * @author FarmaFx Team
 * @version 3.4 (Stage API + auto-cleanup on Stage close)
 */
public class Toast {

    // Duraciones estándar (ms)
    public static final int DURATION_SHORT = 2000;
    public static final int DURATION_NORMAL = 2500;
    public static final int DURATION_LONG = 3500;

    // Configuración de apilado
    private static final int TOAST_SPACING = 12; // Espaciado entre Toasts
    private static final int MARGIN_RIGHT = 20;
    private static final int MARGIN_BOTTOM = 20;
    private static final int INITIAL_SLIDE_OFFSET = 50; // Offset inicial para animación slide-in desde abajo

    // Gestión de Toasts activos
    private static final List<ToastPopup> activeToasts = new ArrayList<>();

    /**
     * Tipos de Toast con iconos y estilos CSS.
     */
    @Getter
    public enum Type {
        SUCCESS("✓", "toast-success"),
        ERROR("✕", "toast-error"),
        INFO("ℹ", "toast-info"),
        WARNING("⚠", "toast-warning");

        private final String icon;
        private final String styleClass;

        Type(String icon, String styleClass) {
            this.icon = icon;
            this.styleClass = styleClass;
        }
    }

    private Toast() {
        // Utility class - no instanciar
    }

    /**
     * Muestra un Toast con tipo específico.
     *
     * @param ownerStage Stage padre
     * @param message Mensaje a mostrar
     * @param type Tipo de Toast
     * @param durationInMillis Duración en milisegundos
     */
    public static void showToast(Stage ownerStage, String message, Type type, int durationInMillis) {
        Platform.runLater(() -> {
            ToastPopup toastPopup = new ToastPopup(ownerStage, message, type, durationInMillis);
            toastPopup.show();
        });
    }

    /**
     * Muestra Toast de éxito (verde).
     */
    public static void showSuccess(Stage ownerStage, String message, int duration) {
        showToast(ownerStage, message, Type.SUCCESS, duration);
    }

    /**
     * Muestra Toast de error (rojo).
     */
    public static void showError(Stage ownerStage, String message, int duration) {
        showToast(ownerStage, message, Type.ERROR, duration);
    }

    /**
     * Muestra Toast de información (azul).
     */
    public static void showInfo(Stage ownerStage, String message, int duration) {
        showToast(ownerStage, message, Type.INFO, duration);
    }

    /**
     * Muestra Toast de advertencia (amarillo).
     */
    public static void showWarning(Stage ownerStage, String message, int duration) {
        showToast(ownerStage, message, Type.WARNING, duration);
    }

    /**
     * Clase interna que representa un Toast individual con Popup y animaciones.
     */
    private static class ToastPopup {
        private final Stage ownerStage;
        private final String message;
        private final Type type;
        private final int duration;
        private final Popup popup;
        private final VBox container;
        private final ProgressBar progressBar;
        private Timeline progressTimeline;
        private Timeline delayTimeline;
        private Timeline fadeOutTimeline;
        private ChangeListener<Boolean> focusListener;
        private ChangeListener<Boolean> showingListener;
        private ChangeListener<Number> widthListener;
        private ChangeListener<Number> heightListener;
        private ChangeListener<Number> xListener;
        private ChangeListener<Number> yListener;

        public ToastPopup(Stage ownerStage, String message, Type type, int duration) {
            this.ownerStage = ownerStage;
            this.message = message;
            this.type = type;
            this.duration = duration;
            this.popup = new Popup();
            this.container = createContainer();
            this.progressBar = createProgressBar();

            buildToast();
        }

        /**
         * Crea el contenedor principal del Toast.
         */
        private VBox createContainer() {
            VBox vbox = new VBox(8); // 8px spacing entre header y progress bar
            vbox.getStyleClass().addAll("toast-container", type.getStyleClass());
            vbox.setOpacity(0);
            vbox.setTranslateY(20); // Posición inicial para slide-in

            // Cargar CSS si está disponible
            java.net.URL cssUrl = Toast.class.getResource("/css/toast.css");
            if (cssUrl != null) {
                vbox.getStylesheets().add(cssUrl.toExternalForm());
            }

            return vbox;
        }

        /**
         * Crea la barra de progreso.
         */
        private ProgressBar createProgressBar() {
            ProgressBar bar = new ProgressBar(1.0);
            bar.getStyleClass().add("toast-progress");
            bar.setMaxWidth(Double.MAX_VALUE);
            return bar;
        }

        /**
         * Construye la estructura completa del Toast.
         */
        private void buildToast() {
            // Header: [Icono] [Mensaje] [Spacer] [Botón X]
            HBox header = new HBox(10);
            header.setAlignment(Pos.CENTER_LEFT);
            header.getStyleClass().add("toast-header");

            // Icono
            Label iconLabel = new Label(type.getIcon());
            iconLabel.getStyleClass().add("toast-icon");

            // Mensaje
            Label messageLabel = new Label(message);
            messageLabel.getStyleClass().add("toast-message");
            messageLabel.setWrapText(true);
            messageLabel.setMaxWidth(380); // Máximo ancho para wrap

            // Spacer (empuja botón cerrar a la derecha)
            Region spacer = new Region();
            HBox.setHgrow(spacer, Priority.ALWAYS);

            // Botón cerrar
            Button closeBtn = new Button("✕");
            closeBtn.getStyleClass().add("toast-close-btn");
            closeBtn.setOnAction(e -> closeManually());

            header.getChildren().addAll(iconLabel, messageLabel, spacer, closeBtn);

            // Agregar header y progress bar al container
            container.getChildren().addAll(header, progressBar);

            // Configurar Popup
            popup.setAutoHide(false);
            popup.setAutoFix(true);
            popup.getContent().add(container);

            // Ocultar y pausar Toast cuando ventana pierde foco (Alt+Tab, minimizar, etc.)
            focusListener = (obs, wasFocused, isNowFocused) -> {
                if (!isNowFocused) {
                    // Ventana perdió foco → ocultar Toast y pausar timers
                    popup.hide();
                    if (progressTimeline != null) progressTimeline.pause();
                    if (delayTimeline != null) delayTimeline.pause();
                } else {
                    // Ventana recuperó foco → reposicionar, mostrar y reanudar timers
                    if (activeToasts.contains(this) && !popup.isShowing()) {
                        repositionAllToasts();
                        popup.show(ownerStage, popup.getX(), popup.getY());
                        if (progressTimeline != null) progressTimeline.play();
                        if (delayTimeline != null) delayTimeline.play();
                    }
                }
            };
            ownerStage.focusedProperty().addListener(focusListener);

            // Configurar listeners de geometría (width, height, x, y)
            setupGeometryListeners();

            // Limpiar Toast cuando Stage se cierra/oculta
            showingListener = (obs, wasShowing, isNowShowing) -> {
                if (!isNowShowing && activeToasts.contains(this)) {
                    // Stage se cerró → limpiar Toast inmediatamente sin animación
                    close();
                }
            };
            ownerStage.showingProperty().addListener(showingListener);
        }

        /**
         * Configura listeners de geometría para reposicionamiento automático.
         */
        private void setupGeometryListeners() {
            ChangeListener<Number> geometryListener = (obs, oldVal, newVal) -> {
                if (popup.isShowing()) {
                    repositionAllToasts();
                }
            };

            widthListener = geometryListener;
            heightListener = geometryListener;
            xListener = geometryListener;
            yListener = geometryListener;

            ownerStage.widthProperty().addListener(widthListener);
            ownerStage.heightProperty().addListener(heightListener);
            ownerStage.xProperty().addListener(xListener);
            ownerStage.yProperty().addListener(yListener);
        }

        /**
         * Muestra el Toast con animaciones.
         */
        public void show() {
            // Agregar a lista de activos
            activeToasts.add(this);

            // Calcular posición inicial (fuera de pantalla, abajo)
            double x = ownerStage.getX() + ownerStage.getWidth() - container.getPrefWidth() - MARGIN_RIGHT;
            double y = ownerStage.getY() + ownerStage.getHeight() + INITIAL_SLIDE_OFFSET;

            popup.show(ownerStage, x, y);

            // Después de mostrar, obtener dimensiones reales y reposicionar
            Platform.runLater(() -> {
                repositionAllToasts();
                animateIn();
            });
        }

        /**
         * Anima la entrada del Toast (slide-in + fade-in).
         */
        private void animateIn() {
            Timeline slideIn = new Timeline(
                new KeyFrame(Duration.ZERO,
                    new KeyValue(container.opacityProperty(), 0),
                    new KeyValue(container.translateYProperty(), 20)
                ),
                new KeyFrame(Duration.millis(300),
                    new KeyValue(container.opacityProperty(), 1),
                    new KeyValue(container.translateYProperty(), 0)
                )
            );

            slideIn.setOnFinished(e -> startAutoClose());
            slideIn.play();
        }

        /**
         * Inicia el auto-cierre con barra de progreso.
         */
        private void startAutoClose() {
            // Timeline para barra de progreso
            progressTimeline = new Timeline(
                new KeyFrame(Duration.ZERO, new KeyValue(progressBar.progressProperty(), 1.0)),
                new KeyFrame(Duration.millis(duration), new KeyValue(progressBar.progressProperty(), 0.0))
            );

            // Timeline de delay antes de fade-out
            delayTimeline = new Timeline(new KeyFrame(Duration.millis(duration)));
            delayTimeline.setOnFinished(e -> animateOut());

            progressTimeline.play();
            delayTimeline.play();
        }

        /**
         * Anima la salida del Toast (fade-out + slide-out).
         */
        private void animateOut() {
            fadeOutTimeline = new Timeline(
                new KeyFrame(Duration.ZERO,
                    new KeyValue(container.opacityProperty(), 1),
                    new KeyValue(container.translateYProperty(), 0)
                ),
                new KeyFrame(Duration.millis(250),
                    new KeyValue(container.opacityProperty(), 0),
                    new KeyValue(container.translateYProperty(), 20)
                )
            );

            fadeOutTimeline.setOnFinished(e -> close());
            fadeOutTimeline.play();
        }

        /**
         * Cierra el Toast y limpia recursos.
         */
        private void close() {
            popup.hide();
            activeToasts.remove(this);
            repositionAllToasts();
            cleanupResources();
        }

        /**
         * Limpia timelines y listeners.
         */
        private void cleanupResources() {
            // Limpiar timelines
            if (progressTimeline != null) progressTimeline.stop();
            if (delayTimeline != null) delayTimeline.stop();
            if (fadeOutTimeline != null) fadeOutTimeline.stop();

            // Limpiar listeners
            if (focusListener != null) {
                ownerStage.focusedProperty().removeListener(focusListener);
                focusListener = null;
            }
            if (showingListener != null) {
                ownerStage.showingProperty().removeListener(showingListener);
                showingListener = null;
            }
            if (widthListener != null) {
                ownerStage.widthProperty().removeListener(widthListener);
                widthListener = null;
            }
            if (heightListener != null) {
                ownerStage.heightProperty().removeListener(heightListener);
                heightListener = null;
            }
            if (xListener != null) {
                ownerStage.xProperty().removeListener(xListener);
                xListener = null;
            }
            if (yListener != null) {
                ownerStage.yProperty().removeListener(yListener);
                yListener = null;
            }
        }

        /**
         * Cierra el Toast manualmente (botón X).
         */
        private void closeManually() {
            if (progressTimeline != null) progressTimeline.stop();
            if (delayTimeline != null) delayTimeline.stop();
            animateOut();
        }

        /**
         * Reposiciona todos los Toasts activos (apilado) - Optimizado O(n).
         */
        private static void repositionAllToasts() {
            double accumulatedHeight = 0;

            for (ToastPopup toast : activeToasts) {
                // Acumular altura antes de calcular posición
                accumulatedHeight += toast.container.getHeight() + TOAST_SPACING;

                // Calcular posición
                double baseY = toast.ownerStage.getY() + toast.ownerStage.getHeight() - MARGIN_BOTTOM;
                double targetY = baseY - accumulatedHeight;
                double targetX = toast.ownerStage.getX() + toast.ownerStage.getWidth()
                                - toast.container.getWidth() - MARGIN_RIGHT;

                // Reposicionar directamente (sin animación para evitar ReadOnlyProperty)
                toast.popup.setX(targetX);
                toast.popup.setY(targetY);
            }
        }
    }
}