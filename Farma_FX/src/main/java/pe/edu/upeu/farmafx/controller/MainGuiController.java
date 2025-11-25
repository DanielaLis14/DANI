package pe.edu.upeu.farmafx.controller;

import javafx.animation.*;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;

import java.util.*;

import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView;
import pe.edu.upeu.farmafx.components.Toast;
import pe.edu.upeu.farmafx.dto.NavigationItemDto;
import pe.edu.upeu.farmafx.enums.ViewRoute;
import pe.edu.upeu.farmafx.service.INavigationService;
import pe.edu.upeu.farmafx.utils.SessionManager;
import pe.edu.upeu.farmafx.utils.StageManager;
import pe.edu.upeu.farmafx.utils.UtilsX;
import pe.edu.upeu.farmafx.utils.ViewNavigator;

/**
 * Controller MainGui v14.2 - Sidebar Dinámico con NavigationService.
 * Mejoras v14.0:
 * - Usa NavigationItemDto con campos tipo/shortcut
 * - Usa Map para acceso O(1) a items
 * - fx:id sidebarItemsContainer (sin búsqueda dinámica)
 * - UtilsX inyectado por Spring
 * - Manejo de tipo EXIT para cerrar sesión
 *
 * @version 14.2
 * @since 2025-11-17
 */
@Controller
@RequiredArgsConstructor
@Slf4j
public class MainGuiController {

    // ========== CONSTANTES ==========
    private static final int SIDEBAR_WIDTH_EXPANDED = 220;
    private static final int SIDEBAR_WIDTH_COLLAPSED = 60;
    private static final int ANIMATION_DURATION_MS = 150;
    private static final int HOVER_DISPLACEMENT_PX = 4;
    private static final int HOVER_ANIMATION_DURATION_MS = 150;
    private static final int CARET_ANIMATION_DURATION_MS = 180;
    private static final int CARET_ROTATION_ANGLE = 90;
    private static final String CSS_CLASS_ACTIVE = "sidebar-item-active";
    private static final String CSS_CLASS_USER_CHIP_OPEN = "user-chip-open";

    // Defaults
    private static final String DEFAULT_USER_NAME = "Usuario";
    private static final String DEFAULT_PERFIL = "Cliente";
    private static final String DEFAULT_GROUP = "ESPECIAL";

    // Dimensiones ventana
    private static final int MIN_WINDOW_WIDTH = 1225;
    private static final int MIN_WINDOW_HEIGHT = 700;

    // Menú usuario
    private static final String MENU_EDITAR_PERFIL = "Editar perfil";
    private static final String MENU_CONFIGURACION = "Configuración";
    private static final String MENU_CERRAR_SESION = "Cerrar sesión";
    private static final int USER_MENU_OFFSET_Y = 8;

    // ========== COMPONENTES FXML ==========
    @FXML private VBox sidebar;
    @FXML private VBox sidebarItemsContainer;
    @FXML private StackPane contentArea;
    @FXML private Label lblPageTitle;
    @FXML private Label lblUserName;
    @FXML private HBox userChip;
    @FXML private FontAwesomeIconView iconCaret;

    // ========== ESTADO ==========
    private boolean sidebarExpandido = true;
    private Timeline animacionSidebar;
    private HBox itemActivo = null;
    private final Map<HBox, TranslateTransition> hoverAnimations = new HashMap<>();
    private final ContextMenu userMenu = new ContextMenu();
    private RotateTransition caretAnimation;

    // Mapa de shortcuts: "Ctrl+D" -> datos del item
    private final Map<String, ShortcutData> shortcuts = new HashMap<>();

    // Record para almacenar datos de shortcut
    private record ShortcutData(HBox itemBox, NavigationItemDto item) {}

    // ========== DEPENDENCIAS (Inyectadas por Spring) ==========
    private final ViewNavigator viewNavigator;
    private final SessionManager sessionManager;
    private final StageManager stageManager;
    private final INavigationService navigationService;
    private final UtilsX utilsX;

    // ========================================
    // INICIALIZACIÓN
    // ========================================

    @FXML
    private void initialize() {
        log.info("Inicializando MainGuiModernController v14.2");
        Platform.runLater(this::inicializarDatos);
    }

    private void inicializarDatos() {
        configurarStageManager();
        construirSidebarDinamico();
        configurarAtajosTeclado();
        actualizarNombreUsuario();
        configurarAnimacionCaret();
        configurarUserChipMenu();

        // Mostrar Toast de bienvenida si acaba de hacer login
        verificarYMostrarToastBienvenida();

        log.info("MainGui v14.2 inicializado con {} shortcuts", shortcuts.size());
    }

    private void configurarStageManager() {
        if (sidebar == null || sidebar.getScene() == null) {
            log.warn("No se pudo configurar Stage principal: sidebar o scene null");
            return;
        }

        if (!(sidebar.getScene().getWindow() instanceof Stage stage)) {
            log.warn("Window no es Stage: {}", sidebar.getScene().getWindow());
            return;
        }

        stageManager.setPrimaryStage(stage);

        // Configurar dimensiones mínimas de la ventana
        stage.setMinWidth(MIN_WINDOW_WIDTH);   // Garantiza contenido completo con sidebar expandido
        stage.setMinHeight(MIN_WINDOW_HEIGHT); // Alto mínimo cómodo para CRUD

        log.debug("Stage principal configurado - Min: {}x{}", MIN_WINDOW_WIDTH, MIN_WINDOW_HEIGHT);
    }

    // ========================================
    // SIDEBAR DINÁMICO
    // ========================================

    private void construirSidebarDinamico() {
        String perfil = obtenerPerfilActual();
        Properties idioma = obtenerIdiomaActual();

        log.info("Construyendo sidebar para perfil: {}", perfil);

        Map<String, NavigationItemDto> items = navigationService.obtenerNavegacion(perfil, idioma);

        if (items.isEmpty()) {
            log.warn("Sin items para perfil '{}' - sidebar vacío", perfil);
            return;
        }

        Map<String, List<NavigationItemDto>> itemsPorGrupo = agruparItemsPorGrupo(items);
        construirSidebarConGrupos(itemsPorGrupo);

        log.info("Sidebar construido: {} items, {} grupos", items.size(), itemsPorGrupo.size());
    }

    private String obtenerPerfilActual() {
        return Optional.ofNullable(sessionManager)
            .map(SessionManager::getUserPerfil)
            .orElse(DEFAULT_PERFIL);
    }

    private Properties obtenerIdiomaActual() {
        String idiomaActual = utilsX.cargarIdiomaActual();
        return utilsX.detectLanguage(idiomaActual);
    }

    private Map<String, List<NavigationItemDto>> agruparItemsPorGrupo(Map<String, NavigationItemDto> items) {
        Map<String, List<NavigationItemDto>> porGrupo = new LinkedHashMap<>();
        items.values().forEach(item -> {
            String grupo = item.getGroup() != null ? item.getGroup() : DEFAULT_GROUP;
            porGrupo.computeIfAbsent(grupo, k -> new ArrayList<>()).add(item);
        });
        return porGrupo;
    }

    private void construirSidebarConGrupos(Map<String, List<NavigationItemDto>> porGrupo) {
        sidebarItemsContainer.getChildren().clear();
        sidebarItemsContainer.getChildren().add(crearEspaciador(12));

        HBox primerItemBox = null;
        NavigationItemDto primerItemDto = null;
        boolean primerGrupo = true;

        for (Map.Entry<String, List<NavigationItemDto>> entry : porGrupo.entrySet()) {
            if (!primerGrupo) {
                sidebarItemsContainer.getChildren().add(crearSeparador());
            }
            primerGrupo = false;

            for (NavigationItemDto item : entry.getValue()) {
                HBox itemBox = crearSidebarItem(item);
                sidebarItemsContainer.getChildren().add(itemBox);

                if (primerItemBox == null) {
                    primerItemBox = itemBox;
                    primerItemDto = item;
                }
            }
        }

        sidebarItemsContainer.getChildren().add(crearEspaciador(20));

        if (primerItemBox != null) {
            HBox itemToSelect = primerItemBox;
            String label = primerItemDto.getLabel();
            Platform.runLater(() -> seleccionarItem(itemToSelect, label));
        }
    }

    private HBox crearSidebarItem(NavigationItemDto item) {
        HBox box = new HBox(0);
        box.setAlignment(Pos.CENTER_LEFT);
        box.setPrefHeight(48);
        box.getStyleClass().add("sidebar-item");

        // StackPane para icono (40px fijo)
        StackPane iconContainer = new StackPane();
        iconContainer.setAlignment(Pos.CENTER);
        iconContainer.setMinWidth(40);
        iconContainer.setPrefWidth(40);
        iconContainer.setMaxWidth(40);

        FontAwesomeIconView icon = new FontAwesomeIconView(
            FontAwesomeIcon.valueOf(item.getIcon())
        );
        icon.setSize("20");
        icon.getStyleClass().add("sidebar-icon");
        iconContainer.getChildren().add(icon);

        // Label
        Label label = new Label(item.getLabel());
        label.getStyleClass().add("sidebar-label");
        label.setPadding(new Insets(0, 0, 0, 12));
        label.managedProperty().bind(label.visibleProperty());

        box.getChildren().addAll(iconContainer, label);

        // Click handler
        box.setOnMouseClicked(e -> ejecutarNavegacion(box, item));

        // Hover handlers
        box.setOnMouseEntered(e -> animarHover(box, HOVER_DISPLACEMENT_PX));
        box.setOnMouseExited(e -> animarHover(box, 0));

        // Registrar shortcut si existe
        if (item.getShortcut() != null && !item.getShortcut().isEmpty()) {
            shortcuts.put(item.getShortcut(), new ShortcutData(box, item));
        }

        return box;
    }

    private Region crearEspaciador(double altura) {
        Region region = new Region();
        region.setPrefHeight(altura);
        return region;
    }

    private VBox crearSeparador() {
        Separator linea = new Separator();
        linea.getStyleClass().add("sidebar-separator");

        VBox separadorBox = new VBox(0);
        separadorBox.getChildren().addAll(
            crearEspaciador(8),
            linea,
            crearEspaciador(8)
        );
        return separadorBox;
    }

    // ========================================
    // TOGGLE SIDEBAR
    // ========================================

    @FXML
    private void toggleSidebar() {
        if (sidebarExpandido) {
            actualizarVisibilidadLabels(false);
            animarAncho(SIDEBAR_WIDTH_COLLAPSED, null);
        } else {
            animarAncho(SIDEBAR_WIDTH_EXPANDED, () -> actualizarVisibilidadLabels(true));
        }
        sidebarExpandido = !sidebarExpandido;
        log.debug("Sidebar {}", sidebarExpandido ? "expandido" : "colapsado");
    }

    private void animarAncho(int nuevoAncho, Runnable accionFinal) {
        if (animacionSidebar != null) {
            animacionSidebar.stop();
        }
        animacionSidebar = new Timeline(
            new KeyFrame(
                Duration.millis(ANIMATION_DURATION_MS),
                new KeyValue(sidebar.prefWidthProperty(), nuevoAncho),
                new KeyValue(sidebar.minWidthProperty(), nuevoAncho),
                new KeyValue(sidebar.maxWidthProperty(), nuevoAncho)
            )
        );
        animacionSidebar.setOnFinished(e -> {
            if (accionFinal != null) {
                accionFinal.run();
            }
        });
        animacionSidebar.play();
    }

    private void actualizarVisibilidadLabels(boolean visible) {
        sidebarItemsContainer.getChildren().stream()
            .filter(HBox.class::isInstance)
            .map(HBox.class::cast)
            .flatMap(hbox -> hbox.getChildren().stream())
            .filter(Label.class::isInstance)
            .map(Label.class::cast)
            .forEach(label -> label.setVisible(visible));
    }

    private void animarHover(HBox item, double desplazamiento) {
        TranslateTransition transition = hoverAnimations.computeIfAbsent(item, k -> {
            TranslateTransition t = new TranslateTransition(
                Duration.millis(HOVER_ANIMATION_DURATION_MS), item
            );
            t.setInterpolator(Interpolator.EASE_OUT);
            return t;
        });

        transition.stop();
        transition.setToX(desplazamiento);
        transition.play();
    }

    // ========================================
    // SELECCIÓN Y NAVEGACIÓN
    // ========================================

    private void seleccionarItem(HBox item, String titulo) {
        marcarItemActivo(item);
        lblPageTitle.setText(titulo);
        log.debug("{} seleccionado", titulo);
    }

    private void cargarVistaEnCentro(String rutaFxml, String titulo) {
        if (viewNavigator == null || contentArea == null) {
            log.warn("No se puede cargar vista: ViewNavigator o contentArea no disponible");
            return;
        }
        try {
            Parent vista = viewNavigator.loadViewNoCache(rutaFxml);
            contentArea.getChildren().clear();
            contentArea.getChildren().add(vista);
            lblPageTitle.setText(titulo);
            log.debug("Vista cargada: {}", titulo);
        } catch (Exception ex) {
            log.error("Error cargando vista {}", rutaFxml, ex);
        }
    }

    private void marcarItemActivo(HBox nuevoItem) {
        if (itemActivo != null) {
            itemActivo.getStyleClass().remove(CSS_CLASS_ACTIVE);
        }
        if (nuevoItem != null) {
            nuevoItem.getStyleClass().add(CSS_CLASS_ACTIVE);
        }
        itemActivo = nuevoItem;
    }

    /**
     * Ejecuta la navegación de un item (click o shortcut).
     * Centraliza lógica para evitar duplicación.
     */
    private void ejecutarNavegacion(HBox itemBox, NavigationItemDto item) {
        if ("EXIT".equals(item.getTipo())) {
            cerrarSesion();
        } else {
            seleccionarItem(itemBox, item.getLabel());
            if (item.getRoute() != null && !item.getRoute().isEmpty()) {
                cargarVistaEnCentro(item.getRoute(), item.getLabel());
            }
        }
    }

    // ========================================
    // USER CHIP Y MENÚ
    // ========================================

    private void actualizarNombreUsuario() {
        if (lblUserName == null) return;

        String nombreUsuario = Optional.ofNullable(sessionManager)
            .filter(SessionManager::isAutenticado)
            .map(SessionManager::getUserName)
            .orElse(DEFAULT_USER_NAME);

        lblUserName.setText(nombreUsuario);
    }

    private void configurarUserChipMenu() {
        if (userChip == null) return;

        MenuItem editarPerfil = new MenuItem(MENU_EDITAR_PERFIL);
        editarPerfil.setOnAction(e -> log.info("Editar perfil seleccionado"));

        MenuItem configuracion = new MenuItem(MENU_CONFIGURACION);
        configuracion.setOnAction(e -> log.info("Configuración seleccionada"));

        MenuItem cerrarSesionItem = new MenuItem(MENU_CERRAR_SESION);
        cerrarSesionItem.setOnAction(e -> cerrarSesion());

        userMenu.getItems().setAll(editarPerfil, configuracion, cerrarSesionItem);
        userMenu.getStyleClass().add("user-menu");

        userChip.setOnMouseClicked(event -> toggleUserMenu());
        userMenu.setOnHidden(e -> cerrarUserMenuUI());
    }

    private void toggleUserMenu() {
        if (userMenu.isShowing()) {
            userMenu.hide();
        } else {
            abrirUserMenu();
        }
    }

    private void abrirUserMenu() {
        userMenu.show(userChip, javafx.geometry.Side.BOTTOM, 0, USER_MENU_OFFSET_Y);
        userChip.getStyleClass().add(CSS_CLASS_USER_CHIP_OPEN);
        animarCaret(true);
    }

    private void cerrarUserMenuUI() {
        userChip.getStyleClass().remove(CSS_CLASS_USER_CHIP_OPEN);
        animarCaret(false);
    }

    private void configurarAnimacionCaret() {
        if (iconCaret == null) return;
        caretAnimation = new RotateTransition(
            Duration.millis(CARET_ANIMATION_DURATION_MS), iconCaret
        );
        caretAnimation.setInterpolator(Interpolator.EASE_BOTH);
        iconCaret.setRotate(0);
    }

    private void animarCaret(boolean abierto) {
        if (caretAnimation == null) return;
        caretAnimation.stop();
        caretAnimation.setToAngle(abierto ? CARET_ROTATION_ANGLE : 0);
        caretAnimation.playFromStart();
    }

    // ========================================
    // ATAJOS DE TECLADO
    // ========================================

    /**
     * Configura el listener de atajos de teclado en la Scene.
     */
    private void configurarAtajosTeclado() {
        if (sidebar == null) {
            log.warn("No se pueden configurar atajos: sidebar null");
            return;
        }

        if (sidebar.getScene() != null) {
            registrarListenerAtajos(sidebar.getScene());
        } else {
            // Scene aún no disponible, esperar
            sidebar.sceneProperty().addListener((obs, oldScene, newScene) -> {
                if (newScene != null) {
                    registrarListenerAtajos(newScene);
                }
            });
        }
    }

    private void registrarListenerAtajos(Scene scene) {
        scene.addEventFilter(KeyEvent.KEY_PRESSED, this::manejarAtajo);
        log.debug("Atajos de teclado configurados ({} shortcuts)", shortcuts.size());
    }

    /**
     * Maneja eventos de teclado y ejecuta navegación si coincide con un shortcut.
     * Formato esperado: "Ctrl+D", "Ctrl+1", "Ctrl+Q", etc.
     */
    private void manejarAtajo(KeyEvent event) {
        if (!event.isControlDown()) {
            return;
        }

        // Construir string del shortcut presionado
        String tecla = convertirKeyCode(event.getCode());
        String shortcutPresionado = "Ctrl+" + tecla;

        // Buscar en mapa de shortcuts
        ShortcutData data = shortcuts.get(shortcutPresionado);
        if (data == null) {
            return;
        }

        // Consumir evento para evitar propagación
        event.consume();

        // Ejecutar navegación
        ejecutarNavegacion(data.itemBox(), data.item());

        log.debug("Shortcut ejecutado: {} -> {}", shortcutPresionado, data.item().getLabel());
    }

    /**
     * Convierte KeyCode a string legible para comparar con shortcuts.
     * Soporta teclas numéricas (DIGIT y NUMPAD) y letras.
     */
    private String convertirKeyCode(KeyCode code) {
        return switch (code) {
            case DIGIT0, NUMPAD0 -> "0";
            case DIGIT1, NUMPAD1 -> "1";
            case DIGIT2, NUMPAD2 -> "2";
            case DIGIT3, NUMPAD3 -> "3";
            case DIGIT4, NUMPAD4 -> "4";
            case DIGIT5, NUMPAD5 -> "5";
            case DIGIT6, NUMPAD6 -> "6";
            case DIGIT7, NUMPAD7 -> "7";
            case DIGIT8, NUMPAD8 -> "8";
            case DIGIT9, NUMPAD9 -> "9";
            default -> code.getName().toUpperCase();
        };
    }

    // ========================================
    // TOAST BIENVENIDA (Login Exitoso)
    // ========================================

    /**
     * Muestra Toast de bienvenida si acaba de hacer login exitoso.
     * Consume el flag de SessionManager para mostrarlo solo UNA vez.
     * Usa Toast Premium (Popup con Stage owner).
     */
    private void verificarYMostrarToastBienvenida() {
        // Verificar flag en SessionManager
        if (sessionManager != null && sessionManager.consumirLoginExitoso()) {
            // Verificar que Scene esté disponible
            if (sidebar == null || sidebar.getScene() == null) {
                log.warn("No se puede mostrar Toast: sidebar o scene null");
                return;
            }

            // Obtener nombre del usuario
            String nombreUsuario = sessionManager.getUserName();

            // Mostrar Toast SUCCESS con mensaje personalizado
            String mensaje = "Bienvenido, " + nombreUsuario;
            Stage stage = (Stage) sidebar.getScene().getWindow();
            Toast.showSuccess(stage, mensaje, Toast.DURATION_SHORT);

            // Log confirmando inicio de sesión
            log.info("✅ Login exitoso - Usuario: {} | Perfil: {}",
                nombreUsuario, sessionManager.getUserPerfil());
            log.info("Toast de bienvenida mostrado para: {}", nombreUsuario);
        }
    }

    // ========================================
    // CERRAR SESIÓN
    // ========================================

    private void cerrarSesion() {
        log.info("Cerrando sesión");
        userMenu.hide();

        if (sessionManager != null) {
            sessionManager.clear();
        }

        if (viewNavigator == null) {
            log.error("ViewNavigator no disponible para cerrar sesión");
            return;
        }

        viewNavigator.clearCache();

        try {
            Parent loginView = viewNavigator.loadView(ViewRoute.TEST_LOGIN.getPath());
            if (loginView == null) {
                log.error("No se pudo cargar vista de login");
                return;
            }

            Stage loginStage = new Stage();
            loginStage.initStyle(StageStyle.UNDECORATED);
            loginStage.setScene(new Scene(loginView));
            loginStage.setTitle("FarmaFx");
            loginStage.show();

            Stage mainStage = (Stage) sidebar.getScene().getWindow();
            mainStage.close();
        } catch (Exception ex) {
            log.error("Error al cerrar sesión", ex);
        }
    }
}