package pe.edu.upeu.farmafx.controller;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import pe.edu.upeu.farmafx.components.Toast;
import pe.edu.upeu.farmafx.enums.ViewRoute;
import pe.edu.upeu.farmafx.model.Usuario;
import pe.edu.upeu.farmafx.service.IUsuarioService;
import pe.edu.upeu.farmafx.utils.CredentialManager;
import pe.edu.upeu.farmafx.utils.SessionManager;
import pe.edu.upeu.farmafx.utils.ViewNavigator;

import java.util.function.Consumer;

/**
 * Controller moderno para login con funcionalidad "Recordar usuario".
 * Diseño inspirado en aplicaciones modernas (Discord/Xice).
 * Características:
 * - Diseño oscuro elegante (fondo #050810, acento azul #1f85ce)
 * - Persistencia de credenciales encriptadas (AES-128 + Preferences API)
 * - Autocompletado si "recordar" está activo
 * - Header personalizado con arrastre y efecto difuminado (opacity)
 * - Dimensiones: 700x500px, no resizable
 *
 * @author FarmaFx Team
 * @version 2.0
 */
@Controller
@RequiredArgsConstructor
@Slf4j
public class LoginController {

    // Mensajes de usuario
    private static final String MSG_CAMPOS_VACIOS = "Debe ingresar usuario y contraseña";
    private static final String MSG_CREDENCIALES_INVALIDAS = "Credenciales inválidas. Intente nuevamente";

    // Mensajes de UI
    private static final String MSG_AUTENTICANDO = "Autenticando...";
    private static final String MSG_BOTON_LOGIN = "Login";

    // Configuración de arrastre de ventana
    private static final int HEADER_HEIGHT = 40;
    private static final double DRAG_OPACITY = 0.65;

    private final IUsuarioService usuarioService;
    private final ViewNavigator viewNavigator;
    private final SessionManager sessionManager;

    @FXML
    private StackPane rootPane;

    @FXML
    private TextField txtUsername;

    @FXML
    private PasswordField txtPassword;

    @FXML
    private CheckBox chkRememberMe;

    @FXML
    private Button btnLogin;

    @FXML
    private Button btnClose;

    @FXML
    private StackPane progressOverlay;

    private double dragOffsetX = 0;
    private double dragOffsetY = 0;

    @FXML
    public void initialize() {
        Platform.runLater(() -> {
            // Habilitar arrastre de ventana desde el header personalizado
            habilitarArrastreVentana();

            // Cargar credenciales guardadas si existe "recordar usuario"
            cargarCredencialesGuardadas();

            // SIEMPRE hacer focus en username y seleccionar todo el texto
            txtUsername.requestFocus();
            txtUsername.selectAll();
        });

        // Listener para checkbox "Recordar usuario"
        chkRememberMe.selectedProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal) {
                // Si desmarca el checkbox, borrar credenciales guardadas
                CredentialManager.clearCredentials();
                log.info("Credenciales borradas por usuario");
            }
        });
    }

    /**
     * Obtiene Stage actual de la Scene.
     * Patrón consistente con MarcasController.
     *
     * @return Stage owner, o null si Scene no disponible
     */
    private Stage obtenerStage() {
        if (rootPane != null && rootPane.getScene() != null) {
            return (Stage) rootPane.getScene().getWindow();
        }
        return null;
    }

    /**
     * Habilita arrastrar la ventana desde cualquier parte del header.
     * Solo funciona si stage está en modo UNDECORATED.
     * Aplica efecto de difuminado (opacity) mientras arrastra.
     */
    private void habilitarArrastreVentana() {
        if (obtenerStage() == null) return;

        // Listener cuando presiona el mouse en el rootPane
        rootPane.setOnMousePressed(event -> {
            Stage stage = obtenerStage();
            if (stage == null) return;

            // Solo arrastrar si el click es en la parte superior (header)
            if (event.getSceneY() <= HEADER_HEIGHT) {
                stage.setOpacity(DRAG_OPACITY);
                dragOffsetX = event.getSceneX();
                dragOffsetY = event.getSceneY();
            }
        });

        // Listener cuando arrastra el mouse
        rootPane.setOnMouseDragged(event -> {
            Stage stage = obtenerStage();
            if (stage == null) return;

            if (event.getSceneY() <= HEADER_HEIGHT || dragOffsetX != 0) {
                stage.setX(event.getScreenX() - dragOffsetX);
                stage.setY(event.getScreenY() - dragOffsetY);
            }
        });

        // Reset cuando suelta el mouse
        rootPane.setOnMouseReleased(event -> {
            Stage stage = obtenerStage();
            if (stage == null) return;

            stage.setOpacity(1.0); // Restaurar opacidad completa
            dragOffsetX = 0;
            dragOffsetY = 0;
        });
    }

    /**
     * Carga credenciales guardadas si el usuario activó "Recordar usuario" previamente.
     */
    private void cargarCredencialesGuardadas() {
        if (CredentialManager.shouldRemember()) {
            String username = CredentialManager.getLastUsername();
            String password = CredentialManager.getLastPassword();

            if (!username.isEmpty() && !password.isEmpty()) {
                txtUsername.setText(username);
                txtPassword.setText(password);
                chkRememberMe.setSelected(true);
                log.info("Credenciales cargadas automáticamente para: {}", username);
            }
        }
    }

    /**
     * Maneja el evento de login (botón o Enter en password field).
     * Ejecuta autenticación de forma asíncrona sin bloquear UI.
     */
    @FXML
    private void login(ActionEvent ignoredEvent) {
        final String username = txtUsername.getText().trim();
        final String password = txtPassword.getText();

        // Validación básica
        if (username.isEmpty() || password.isEmpty()) {
            Stage stage = obtenerStage();
            if (stage != null) {
                Toast.showWarning(stage, MSG_CAMPOS_VACIOS, Toast.DURATION_NORMAL);
            }
            return;
        }

        // Cambiar texto del botón antes de iniciar Task
        btnLogin.setText(MSG_AUTENTICANDO);

        // Task async para autenticación
        Task<Usuario> task = new Task<>() {
            @Override
            protected Usuario call() {
                log.info("Iniciando autenticación para usuario: {}", username);
                return usuarioService.loginUsuario(username, password);
            }
        };

        // Ejecutar Task con callbacks
        ejecutarLoginAsync(task,
            usuario -> {
                // ✅ Login exitoso
                log.info("Usuario {} autenticado correctamente", username);

                // Guardar credenciales si checkbox está marcado
                if (chkRememberMe.isSelected()) {
                    CredentialManager.saveCredentials(username, password, true);
                    log.info("Credenciales guardadas para: {}", username);
                } else {
                    CredentialManager.clearCredentials();
                }

                // Iniciar sesión
                iniciarSesion(usuario);

                // Marcar flag para que MainGui muestre Toast de bienvenida
                sessionManager.setLoginExitoso();

                // Navegar INMEDIATAMENTE a MainGui (sin delay)
                navegarAMainGui();
            },
            ex -> {
                // ❌ Login fallido
                log.warn("Login fallido para usuario: {} - {}", username, ex.getMessage());
                Stage stageActual = obtenerStage();
                if (stageActual != null) {
                    Toast.showError(stageActual, MSG_CREDENCIALES_INVALIDAS, Toast.DURATION_NORMAL);
                }

                // Limpiar password para reintentar
                txtPassword.clear();
                txtPassword.requestFocus();
            }
        );
    }

    /**
     * Cierra la aplicación (botón X del header).
     */
    @FXML
    private void cerrar(ActionEvent ignoredEvent) {
        log.info("Cerrando aplicación desde login moderno");
        Platform.exit();
        System.exit(0);
    }

    /**
     * Inicia la sesión del usuario en SessionManager.
     */
    private void iniciarSesion(Usuario usuario) {
        sessionManager.setUserId(usuario.getIdUsuario());
        sessionManager.setUserName(usuario.getNombreCompleto());
        sessionManager.setUserPerfil(usuario.getPerfil().getNombrePerfil());
    }

    /**
     * Navega a la ventana principal (MainGui Moderno Xice-style).
     * Usa header DEFAULT de Windows (DECORATED) + sidebar oscuro.
     */
    private void navegarAMainGui() {
        viewNavigator.clearCache();

        // Cargar MAIN_GUI_MODERN (Xice-style con sidebar + header Windows)
        Parent view = viewNavigator.loadView(ViewRoute.MAIN_GUI_MODERN.getPath());

        // Crear nuevo Stage para MainGui
        Stage mainStage = new Stage();
        mainStage.initStyle(javafx.stage.StageStyle.DECORATED);  // Header Windows nativo
        mainStage.setScene(new Scene(view));
        mainStage.setTitle("FarmaFx - Sistema de Gestión");
        mainStage.setMaximized(true);   // Abrir maximizada

        // Cerrar Stage del login
        Stage loginStage = (Stage) rootPane.getScene().getWindow();
        loginStage.close();

        // Mostrar MainGui
        mainStage.show();

        log.info("Navegado a MainGui con header Windows nativo");
    }

    // ============ OPERACIONES ASYNC ============

    /**
     * Ejecuta la operación de login de forma asíncrona en hilo secundario sin bloquear UI.
     *
     * @param task Task con la operación de autenticación
     * @param onSuccess Callback cuando login es exitoso
     * @param onFailed Callback cuando login falla
     */
    private void ejecutarLoginAsync(Task<Usuario> task,
                                    Consumer<Usuario> onSuccess,
                                    Consumer<Throwable> onFailed) {
        // Mostrar overlay de progreso (fondo oscuro + spinner central)
        if (progressOverlay != null) {
            progressOverlay.setVisible(true);
        }
        btnLogin.setDisable(true);
        btnClose.setDisable(true);

        task.setOnSucceeded(e -> {
            finalizarLoginAsync();
            onSuccess.accept(task.getValue());
        });

        task.setOnFailed(e -> {
            finalizarLoginAsync();
            onFailed.accept(task.getException());
        });

        // Ejecutar Task en hilo daemon
        Thread hilo = new Thread(task, "login-auth");
        hilo.setDaemon(true);
        hilo.start();

        log.info("Operación async iniciada - Hilo: login-auth");
    }

    /**
     * Finaliza la operación de login async restaurando el estado de los controles.
     */
    private void finalizarLoginAsync() {
        if (progressOverlay != null) {
            progressOverlay.setVisible(false);
        }
        btnLogin.setDisable(false);
        btnClose.setDisable(false);
        btnLogin.setText(MSG_BOTON_LOGIN);
    }

}