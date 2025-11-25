package pe.edu.upeu.farmafx.components;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.Window;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.atomic.AtomicReference;

/**
 * DialogHelper v2.0 - Sistema de Diálogos Premium
 * <p>
 * Características:
 * - Diseño oscuro consistente con FarmaFx
 * - CSS modular en dialog.css
 * - API tipo Toast (métodos estáticos)
 * - Soporte para cascadas (Marca/Categoría → Productos)
 * - Tipos: Cascadas, Confirmaciones, Informativos
 * - Botones estilizados con colores semánticos
 * - Modal con bloqueo de ventana padre
 * <p>
 * Uso:
 * <pre>
 * OpcionCascada opcion = DialogHelper.showCascadaInactivar(stage, "Marca", 15);
 * boolean confirmado = DialogHelper.showConfirmarEliminacion(stage, "Marca: XYZ");
 * DialogHelper.showError(stage, "Error", "No se pudo conectar");
 * </pre>
 *
 * @author FarmaFx Team
 * @version 2.0
 */
public class DialogHelper {

    private static final Logger logger = LoggerFactory.getLogger(DialogHelper.class);
    private static final String CSS_PATH = "/css/dialog.css";

    private DialogHelper() {
        // Utility class - no instanciar
    }

    /**
     * Opciones de respuesta para diálogos de cascada.
     */
    @Getter
    public enum OpcionCascada {
        CASCADA_COMPLETA("Cascada completa"),  // Inactivar/Activar entidad + productos
        SOLO_ENTIDAD("Solo entidad"),          // Solo entidad, productos sin cambios
        CANCELAR("Cancelar");                  // Cancelar operación

        private final String descripcion;

        OpcionCascada(String descripcion) {
            this.descripcion = descripcion;
        }
    }

    // ============ DIÁLOGOS DE CASCADA ============

    /**
     * Muestra diálogo para INACTIVAR entidad con productos activos.
     * Opciones: Inactivar todo / Solo entidad / Cancelar
     *
     * @param owner             Ventana padre (Stage)
     * @param nombreEntidad     Nombre de la entidad (ej: "Marca", "Categoría")
     * @param cantidadProductos Cantidad de productos activos
     * @return Opción elegida por el usuario
     */
    public static OpcionCascada showCascadaInactivar(Window owner, String nombreEntidad, long cantidadProductos) {
        String titulo = "Inactivar " + nombreEntidad;
        String header = "Esta " + nombreEntidad.toLowerCase() + " tiene " + cantidadProductos + " producto(s) activo(s)";
        String content = "¿Qué desea hacer?";

        return showDialogoCascada(
                owner,
                titulo,
                header,
                content,
                null, // Sin warning
                "Inactivar todo",
                "Solo " + nombreEntidad.toLowerCase(),
                "dialog-cascada-inactivar"
        );
    }

    /**
     * Muestra diálogo para ACTIVAR entidad con productos inactivos.
     * Opciones: Activar todo / Solo entidad / Cancelar
     * Incluye advertencia sobre productos inactivos por otras razones.
     *
     * @param owner             Ventana padre (Stage)
     * @param nombreEntidad     Nombre de la entidad (ej: "Marca", "Categoría")
     * @param cantidadProductos Cantidad de productos inactivos
     * @return Opción elegida por el usuario
     */
    public static OpcionCascada showCascadaActivar(Window owner, String nombreEntidad, long cantidadProductos) {
        String titulo = "Activar " + nombreEntidad;
        String header = "Esta " + nombreEntidad.toLowerCase() + " tiene " + cantidadProductos + " producto(s) inactivo(s)";
        String content = "¿Qué desea hacer?";
        String warning = "⚠ Advertencia: Algunos productos pueden estar inactivos por stock 0, vencimiento u otras razones.";

        return showDialogoCascada(
                owner,
                titulo,
                header,
                content,
                warning,
                "Activar todo",
                "Solo " + nombreEntidad.toLowerCase(),
                "dialog-cascada-activar"
        );
    }

    // ============ DIÁLOGOS DE CASCADA CON SUBCATEGORÍAS ============

    /**
     * Muestra diálogo para INACTIVAR categoría con subcategorías y/o productos.
     * Incluye información detallada de subcategorías afectadas.
     *
     * @param owner          Ventana padre (Stage)
     * @param subcategorias  Cantidad de subcategorías activas que serán inactivadas
     * @param productos      Cantidad de productos activos (propios + de subcategorías)
     * @return Opción elegida por el usuario
     */
    public static OpcionCascada showCascadaCategoriaInactivar(Window owner, long subcategorias, long productos) {
        String titulo = "Inactivar Categoría";
        StringBuilder header = new StringBuilder("Esta categoría tiene:");

        if (subcategorias > 0) {
            header.append("\n• ").append(subcategorias).append(" subcategoría(s) activa(s)");
        }
        if (productos > 0) {
            header.append("\n• ").append(productos).append(" producto(s) activo(s)");
        }

        String content = "¿Qué desea hacer?";

        return showDialogoCascada(
                owner,
                titulo,
                header.toString(),
                content,
                null,
                "Inactivar todo",
                "Solo esta categoría",
                "dialog-cascada-inactivar"
        );
    }

    /**
     * Muestra diálogo para ACTIVAR categoría con subcategorías y/o productos.
     * Incluye información detallada de subcategorías afectadas.
     *
     * @param owner          Ventana padre (Stage)
     * @param subcategorias  Cantidad de subcategorías inactivas que serán activadas
     * @param productos      Cantidad de productos inactivos (propios + de subcategorías)
     * @return Opción elegida por el usuario
     */
    public static OpcionCascada showCascadaCategoriaActivar(Window owner, long subcategorias, long productos) {
        String titulo = "Activar Categoría";
        StringBuilder header = new StringBuilder("Esta categoría tiene:");

        if (subcategorias > 0) {
            header.append("\n• ").append(subcategorias).append(" subcategoría(s) inactiva(s)");
        }
        if (productos > 0) {
            header.append("\n• ").append(productos).append(" producto(s) inactivo(s)");
        }

        String content = "¿Qué desea hacer?";
        String warning = "⚠ Advertencia: Algunos productos pueden estar inactivos por stock 0, vencimiento u otras razones.";

        return showDialogoCascada(
                owner,
                titulo,
                header.toString(),
                content,
                warning,
                "Activar todo",
                "Solo esta categoría",
                "dialog-cascada-activar"
        );
    }

    /**
     * Método interno para crear diálogos de cascada con estructura común.
     */
    private static OpcionCascada showDialogoCascada(
            Window owner,
            String titulo,
            String headerText,
            String contentText,
            String warningText,
            String textoCascadaCompleta,
            String textoSoloEntidad,
            String styleClass) {

        AtomicReference<OpcionCascada> resultado = new AtomicReference<>(OpcionCascada.CANCELAR);
        Stage dialog = crearDialogStage(owner, titulo);

        // Content
        VBox contentBox = new VBox(12);
        contentBox.setAlignment(Pos.TOP_LEFT);
        contentBox.setPadding(new Insets(24));

        // Header (mensaje principal)
        Label lblHeader = new Label(headerText);
        lblHeader.getStyleClass().addAll("dialog-message-header");
        lblHeader.setWrapText(true);
        lblHeader.setMaxWidth(450);

        // Warning (si existe)
        if (warningText != null && !warningText.isEmpty()) {
            Label lblWarning = new Label(warningText);
            lblWarning.getStyleClass().addAll("dialog-warning-text");
            lblWarning.setWrapText(true);
            lblWarning.setMaxWidth(450);
            contentBox.getChildren().addAll(lblHeader, lblWarning);
        } else {
            contentBox.getChildren().add(lblHeader);
        }

        // Content (pregunta)
        Label lblContent = new Label(contentText);
        lblContent.getStyleClass().addAll("dialog-message-content");
        lblContent.setWrapText(true);
        lblContent.setMaxWidth(450);
        contentBox.getChildren().add(lblContent);

        // Botones
        HBox buttonBox = new HBox(10);
        buttonBox.setAlignment(Pos.CENTER_RIGHT);
        buttonBox.setPadding(new Insets(16, 24, 20, 24));

        Button btnCascadaCompleta = crearBoton(textoCascadaCompleta, "dialog-button", "dialog-button-primary");
        Button btnSoloEntidad = crearBoton(textoSoloEntidad, "dialog-button", "dialog-button-secondary");
        Button btnCancelar = crearBoton("Cancelar", "dialog-button", "dialog-button-cancel");

        btnCascadaCompleta.setOnAction(e -> {
            resultado.set(OpcionCascada.CASCADA_COMPLETA);
            dialog.close();
        });

        btnSoloEntidad.setOnAction(e -> {
            resultado.set(OpcionCascada.SOLO_ENTIDAD);
            dialog.close();
        });

        btnCancelar.setOnAction(e -> {
            resultado.set(OpcionCascada.CANCELAR);
            dialog.close();
        });

        buttonBox.getChildren().addAll(btnCancelar, btnSoloEntidad, btnCascadaCompleta);

        // Layout principal
        VBox root = new VBox();
        root.getChildren().addAll(contentBox, buttonBox);
        root.getStyleClass().addAll("dialog-pane", styleClass); // ← FIX: Aplicar styleClass

        Scene scene = new Scene(root);
        aplicarCSS(scene);
        dialog.setScene(scene);

        dialog.showAndWait();
        return resultado.get();
    }

    // ============ CONFIRMACIONES ESTÁNDAR ============

    /**
     * Muestra diálogo de confirmación genérico.
     *
     * @param owner   Ventana padre
     * @param titulo  Título del diálogo
     * @param mensaje Mensaje a mostrar
     * @return true si confirmó, false si canceló
     */
    public static boolean showConfirmar(Window owner, String titulo, String mensaje) {
        return showConfirmarConBotones(owner, titulo, mensaje, "Aceptar", "dialog-confirmar");
    }

    /**
     * Muestra diálogo de confirmación para eliminar.
     * Botón "Eliminar" en rojo (danger).
     *
     * @param owner          Ventana padre
     * @param nombreElemento Nombre del elemento a eliminar
     * @return true si confirmó eliminación, false si canceló
     */
    public static boolean showConfirmarEliminacion(Window owner, String nombreElemento) {
        String titulo = "Confirmar eliminación";
        String mensaje = "¿Está seguro de eliminar este elemento?\n\nEsta acción no se puede deshacer.\n\n" + nombreElemento;
        return showConfirmarConBotones(owner, titulo, mensaje, "Eliminar", "dialog-eliminar", true);
    }

    /**
     * Muestra diálogo de confirmación para eliminar entidad con dependencias.
     * Genérico para cualquier entidad jerárquica (Categoría, etc.) con hijas y/o productos.
     * Construye mensaje descriptivo con contadores de dependencias afectadas.
     *
     * @param owner              Ventana padre
     * @param tipoEntidad        Tipo de entidad ("Categoría", "Marca", etc.)
     * @param nombreEntidad      Nombre del elemento a eliminar
     * @param subcategorias      Cantidad de hijas/dependencias (0 si no aplica)
     * @param productosActivos   Cantidad de productos activos afectados
     * @param productosInactivos Cantidad de productos inactivos afectados
     * @return true si confirmó eliminación, false si canceló
     */
    public static boolean showConfirmarEliminacionConDependencias(
            Window owner,
            String tipoEntidad,
            String nombreEntidad,
            long subcategorias,
            long productosActivos,
            long productosInactivos) {

        String titulo = "Eliminar " + tipoEntidad + ": " + nombreEntidad;
        String mensaje = construirMensajeEliminacionConDependencias(
                tipoEntidad, subcategorias, productosActivos, productosInactivos);

        return showConfirmarConBotones(owner, titulo, mensaje, "Eliminar", "dialog-eliminar", true);
    }

    /**
     * Construye mensaje descriptivo para eliminación con dependencias.
     * Adapta el mensaje según las dependencias existentes.
     *
     * @param tipoEntidad        Tipo ("categoría", "marca")
     * @param subcategorias      Cantidad de hijas (0 si no tiene)
     * @param productosActivos   Productos activos
     * @param productosInactivos Productos inactivos
     * @return Mensaje formateado para el diálogo
     */
    private static String construirMensajeEliminacionConDependencias(
            String tipoEntidad,
            long subcategorias,
            long productosActivos,
            long productosInactivos) {

        long totalProductos = productosActivos + productosInactivos;
        String tipoLower = tipoEntidad.toLowerCase();
        StringBuilder sb = new StringBuilder();

        if (subcategorias > 0 && totalProductos > 0) {
            // Tiene hijas Y productos
            sb.append("Esta ").append(tipoLower).append(" tiene:\n");
            sb.append("  • ").append(subcategorias).append(" subcategoría(s) hija(s)\n");
            sb.append("  • ").append(totalProductos).append(" producto(s) asociado(s) (");
            sb.append(productosActivos).append(" activo(s), ");
            sb.append(productosInactivos).append(" inactivo(s))\n\n");
            sb.append("⚠️ ADVERTENCIA:\n");
            sb.append("  • Las ").append(subcategorias).append(" subcategoría(s) TAMBIÉN serán eliminadas\n");
            sb.append("  • Los ").append(totalProductos).append(" producto(s) quedarán SIN categoría\n");
            sb.append("    (deberá reasignarlos a otra categoría)\n\n");
            sb.append("Esta acción NO se puede deshacer.");

        } else if (subcategorias > 0) {
            // Solo hijas, sin productos
            sb.append("Esta ").append(tipoLower).append(" tiene:\n");
            sb.append("  • ").append(subcategorias).append(" subcategoría(s) hija(s)\n\n");
            sb.append("⚠️ ADVERTENCIA:\n");
            sb.append("  • Las ").append(subcategorias).append(" subcategoría(s) TAMBIÉN serán eliminadas\n\n");
            sb.append("Esta acción NO se puede deshacer.");

        } else if (totalProductos > 0) {
            // Solo productos, sin hijas
            sb.append("Esta ").append(tipoLower).append(" tiene:\n");
            sb.append("  • ").append(totalProductos).append(" producto(s) asociado(s) (");
            sb.append(productosActivos).append(" activo(s), ");
            sb.append(productosInactivos).append(" inactivo(s))\n\n");
            sb.append("⚠️ ADVERTENCIA:\n");
            sb.append("  • Los ").append(totalProductos).append(" producto(s) quedarán SIN categoría\n");
            sb.append("    (deberá reasignarlos a otra categoría)\n\n");
            sb.append("Esta acción NO se puede deshacer.");

        } else {
            // Sin dependencias
            sb.append("¿Está seguro de eliminar esta ").append(tipoLower).append("?\n\n");
            sb.append("Esta acción NO se puede deshacer.");
        }

        return sb.toString();
    }

    /**
     * Muestra diálogo de confirmación para cambiar estado (activar/inactivar).
     * Método preparatorio para futuro uso en otros controllers.
     *
     * @param owner          Ventana padre
     * @param nombreElemento Nombre del elemento
     * @param activar        true si va a activar, false si va a inactivar
     * @return true si confirmó, false si canceló
     */
    @SuppressWarnings("unused")
    public static boolean showConfirmarCambioEstado(Window owner, String nombreElemento, boolean activar) {
        String accion = activar ? "activar" : "inactivar";
        String titulo = "Confirmar acción";
        String mensaje = "¿Desea " + accion + " este elemento?\n\n" + nombreElemento;
        return showConfirmar(owner, titulo, mensaje);
    }

    /**
     * Muestra diálogo de advertencia con confirmación.
     * Método preparatorio para futuro uso en otros controllers.
     *
     * @param owner   Ventana padre
     * @param titulo  Título
     * @param mensaje Mensaje de advertencia
     * @return true si confirmó, false si canceló
     */
    @SuppressWarnings("unused")
    public static boolean showAdvertencia(Window owner, String titulo, String mensaje) {
        return showConfirmarConBotones(owner, titulo, mensaje, "Continuar", "dialog-advertencia");
    }

    /**
     * Método interno para confirmaciones con 2 botones.
     */
    private static boolean showConfirmarConBotones(
            Window owner,
            String titulo,
            String mensaje,
            String textoConfirmar,
            String styleClass) {
        return showConfirmarConBotones(owner, titulo, mensaje, textoConfirmar, styleClass, false);
    }

    /**
     * Método interno para confirmaciones con 2 botones (con opción danger).
     */
    private static boolean showConfirmarConBotones(
            Window owner,
            String titulo,
            String mensaje,
            String textoConfirmar,
            String styleClass,
            boolean confirmarEsDanger) {

        AtomicReference<Boolean> resultado = new AtomicReference<>(false);
        Stage dialog = crearDialogStage(owner, titulo);

        // Content
        VBox contentBox = new VBox(12);
        contentBox.setAlignment(Pos.TOP_LEFT);
        contentBox.setPadding(new Insets(24));

        Label lblMensaje = new Label(mensaje);
        lblMensaje.getStyleClass().addAll("dialog-message-content");
        lblMensaje.setWrapText(true);
        lblMensaje.setMaxWidth(450);
        contentBox.getChildren().add(lblMensaje);

        // Botones
        HBox buttonBox = new HBox(10);
        buttonBox.setAlignment(Pos.CENTER_RIGHT);
        buttonBox.setPadding(new Insets(16, 24, 20, 24));

        String confirmarStyle = confirmarEsDanger ? "dialog-button-danger" : "dialog-button-primary";
        Button btnConfirmar = crearBoton(textoConfirmar, "dialog-button", confirmarStyle);
        Button btnCancelar = crearBoton("Cancelar", "dialog-button", "dialog-button-cancel");

        btnConfirmar.setOnAction(e -> {
            resultado.set(true);
            dialog.close();
        });

        btnCancelar.setOnAction(e -> {
            resultado.set(false);
            dialog.close();
        });

        buttonBox.getChildren().addAll(btnCancelar, btnConfirmar);

        // Layout principal
        VBox root = new VBox();
        root.getChildren().addAll(contentBox, buttonBox);
        root.getStyleClass().addAll("dialog-pane", styleClass); // ← FIX: Aplicar styleClass

        Scene scene = new Scene(root);
        aplicarCSS(scene);
        dialog.setScene(scene);

        dialog.showAndWait();
        return resultado.get();
    }

    // ============ INFORMATIVOS (1 botón) ============

    /**
     * Muestra diálogo de error.
     * Método preparatorio para futuro uso en otros controllers.
     *
     * @param owner   Ventana padre
     * @param titulo  Título
     * @param mensaje Mensaje de error
     */
    @SuppressWarnings("unused")
    public static void showError(Window owner, String titulo, String mensaje) {
        showInformativo(owner, titulo, mensaje, "dialog-error");
    }

    /**
     * Muestra diálogo de información.
     * Método preparatorio para futuro uso en otros controllers.
     *
     * @param owner   Ventana padre
     * @param titulo  Título
     * @param mensaje Mensaje informativo
     */
    @SuppressWarnings("unused")
    public static void showInfo(Window owner, String titulo, String mensaje) {
        showInformativo(owner, titulo, mensaje, "dialog-info");
    }

    /**
     * Muestra diálogo de éxito.
     * Método preparatorio para futuro uso en otros controllers.
     *
     * @param owner   Ventana padre
     * @param titulo  Título
     * @param mensaje Mensaje de éxito
     */
    @SuppressWarnings("unused")
    public static void showExito(Window owner, String titulo, String mensaje) {
        showInformativo(owner, titulo, mensaje, "dialog-exito");
    }

    /**
     * Método interno para informativos con 1 botón.
     */
    private static void showInformativo(Window owner, String titulo, String mensaje, String styleClass) {
        Stage dialog = crearDialogStage(owner, titulo);

        // Content
        VBox contentBox = new VBox(12);
        contentBox.setAlignment(Pos.TOP_LEFT);
        contentBox.setPadding(new Insets(24));

        Label lblMensaje = new Label(mensaje);
        lblMensaje.getStyleClass().addAll("dialog-message-content");
        lblMensaje.setWrapText(true);
        lblMensaje.setMaxWidth(450);
        contentBox.getChildren().add(lblMensaje);

        // Botón
        HBox buttonBox = new HBox(10);
        buttonBox.setAlignment(Pos.CENTER_RIGHT);
        buttonBox.setPadding(new Insets(16, 24, 20, 24));

        Button btnAceptar = crearBoton("Aceptar", "dialog-button", "dialog-button-primary");
        btnAceptar.setOnAction(e -> dialog.close());

        buttonBox.getChildren().add(btnAceptar);

        // Layout principal
        VBox root = new VBox();
        root.getChildren().addAll(contentBox, buttonBox);
        root.getStyleClass().addAll("dialog-pane", styleClass); // ← FIX: Aplicar styleClass

        Scene scene = new Scene(root);
        aplicarCSS(scene);
        dialog.setScene(scene);

        dialog.showAndWait();
    }

    // ============ UTILIDADES ============

    /**
     * Crea Stage de diálogo con configuración estándar.
     */
    private static Stage crearDialogStage(Window owner, String titulo) {
        Stage dialog = new Stage();
        dialog.initModality(Modality.WINDOW_MODAL);
        dialog.initOwner(owner);
        dialog.initStyle(StageStyle.UNDECORATED);
        dialog.setTitle(titulo);
        dialog.setResizable(false);

        return dialog;
    }

    /**
     * Crea botón con estilos CSS aplicados.
     */
    private static Button crearBoton(String texto, String... styleClasses) {
        Button btn = new Button(texto);
        btn.getStyleClass().addAll(styleClasses);
        return btn;
    }

    /**
     * Aplica CSS de dialog.css a la Scene.
     */
    private static void aplicarCSS(Scene scene) {
        try {
            var cssResource = DialogHelper.class.getResource(CSS_PATH);
            if (cssResource != null) {
                String cssUrl = cssResource.toExternalForm();
                scene.getStylesheets().add(cssUrl);
                logger.debug("CSS aplicado correctamente: {}", CSS_PATH);
            } else {
                logger.warn("No se encontró el archivo CSS: {}", CSS_PATH);
            }
        } catch (Exception e) {
            logger.warn("No se pudo cargar CSS del diálogo: {}", e.getMessage());
        }
    }

}