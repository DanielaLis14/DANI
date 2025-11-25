package pe.edu.upeu.farmafx.controller;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import pe.edu.upeu.farmafx.components.AutocompleteHelper;
import pe.edu.upeu.farmafx.components.CharCounterHelper;
import pe.edu.upeu.farmafx.components.DialogHelper;
import pe.edu.upeu.farmafx.components.FilterStateHelper;
import pe.edu.upeu.farmafx.components.FocusHelper;
import pe.edu.upeu.farmafx.components.KeybindHelper;
import pe.edu.upeu.farmafx.components.PaginationHelper;
import pe.edu.upeu.farmafx.components.SearchHelper;
import pe.edu.upeu.farmafx.components.SortHelper;
import pe.edu.upeu.farmafx.components.TableViewHelper;
import pe.edu.upeu.farmafx.components.Toast;
import pe.edu.upeu.farmafx.components.UIHelper;
import pe.edu.upeu.farmafx.components.ValidationHelper;
import pe.edu.upeu.farmafx.dto.ButtonDto;
import pe.edu.upeu.farmafx.model.Marca;
import pe.edu.upeu.farmafx.service.IButtonService;
import pe.edu.upeu.farmafx.service.IMarcaService;
import pe.edu.upeu.farmafx.utils.SessionManager;
import pe.edu.upeu.farmafx.utils.UtilsX;

import javafx.scene.input.KeyCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import java.util.function.Consumer;


@Controller
@RequiredArgsConstructor
@Slf4j
public class MarcasController {

    private static final int REGISTROS_POR_PAGINA = 7;
    private static final String MODULO = "MARCAS";

    // Mensajes de usuario
    private static final String MSG_SELECCIONAR = "Seleccione una marca";
    private static final String MSG_GUARDADO = "Guardado correctamente";
    private static final String MSG_ELIMINADA = "Marca eliminada";
    private static final String MSG_NOMBRE_OBLIGATORIO = "Nombre obligatorio";
    private static final String MSG_NOMBRE_EXISTE = "Nombre ya existe";
    private static final String MSG_NO_ENCONTRADA = "Marca no encontrada";
    private static final String MSG_TIENE_PRODUCTOS = "No se puede eliminar: existen productos con esta marca";

    // Menú contextual
    private static final String MENU_EDITAR = "Editar";
    private static final String MENU_INACTIVAR_ACTIVAR = "Inactivar/Activar";
    private static final String MENU_ELIMINAR = "Eliminar";

    // Mensajes de estado
    private static final String MSG_ACTIVADA = "Marca activada";
    private static final String MSG_INACTIVADA = "Marca inactivada";
    private static final String MSG_MINIMO_CARACTERES = "Mínimo 3 caracteres";
    private static final String MSG_DESCRIPCION_LARGA = "Descripción no puede exceder 600 caracteres";
    private static final String MSG_NOMBRE_LARGA = "Nombre no puede exceder 80 caracteres";

    // Límites de validación
    private static final int MAX_NOMBRE_CARACTERES = 80;
    private static final int MAX_DESCRIPCION_CARACTERES = 600;

    // Diálogos
    private static final String DIALOG_CAMBIOS_TITULO = "Cambios sin guardar";
    private static final String DIALOG_CAMBIOS_MENSAJE = "¿Descartar los cambios realizados?";

    // Configuración de ordenamiento (dinámico)
    // SORT_FIELD eliminado - ahora es dinámico via ComboBox

    // UI
    private static final String ID_SIN_SELECCION = "—";

    // ========== DEPENDENCIAS (Inyectadas por Constructor) ==========
    private final IMarcaService marcaService;
    private final SessionManager sessionManager;
    private final IButtonService buttonService;
    private final UtilsX utilsX;

    private final TableViewHelper tableHelper = new TableViewHelper();

    // Helpers de filtros, ordenamiento, paginación y búsqueda
    private FilterStateHelper filterStateHelper;
    private SortHelper sortHelper;
    private PaginationHelper paginationHelper;
    private SearchHelper searchHelper;

    // Validadores de formulario
    private ValidationHelper nombreValidator;
    private ValidationHelper descripcionValidator;

    @FXML private HBox searchContainer;
    @FXML private TextField txtBusqueda;
    @FXML private ComboBox<String> cmbEstadoFiltro, cmbOrdenamiento;
    @FXML private Button btnLimpiar, btnLimpiarBusqueda, btnNuevo, btnEditar, btnInactivar, btnEliminar;
    @FXML private Label lblMensajeForm;
    @FXML private Label lblId;
    @FXML private TextField txtNombre;
    @FXML private TextArea txtDescripcion;
    @FXML private CheckBox chkEstado;
    @FXML private Button btnGuardar, btnCancelar;
    @FXML private ProgressIndicator piProgreso;
    @FXML private TableView<Marca> tableMarcas;
    @FXML private TableColumn<Marca, Long> colId;
    @FXML private TableColumn<Marca, String> colNombre, colDescripcion;
    @FXML private TableColumn<Marca, Boolean> colEstado;
    @FXML private Label lblTotalMarcas;
    @FXML private Label lblContadorNombre, lblContadorDesc;
    // Paginación mejorada
    @FXML private TextField txtPagina;
    @FXML private Label lblTotalPaginas;
    @FXML private Button btnPrimera, btnAnterior, btnSiguiente, btnUltima;

    private final ObservableList<Marca> marcasData = FXCollections.observableArrayList();
    private int paginaActual = 0, totalPaginas = 0;
    private long totalRegistros = 0L;
    private String busquedaActual = "", perfilUsuario = "";
    private Long marcaEnEdicion = null;
    private boolean operacionEnCurso = false;

    // Para detectar cambios sin guardar
    private String nombreOriginal = "", descripcionOriginal = "";
    private boolean estadoOriginal = true;

    @FXML
    public void initialize() {
        log.info("Inicializando MarcasController");
        Platform.runLater(this::inicializarDatos);
    }

    private void inicializarDatos() {
        limpiarFormulario();
        deshabilitarCamposFormulario();

        // Lazy initialization del stage
        obtenerStage();

        perfilUsuario = Objects.requireNonNullElse(sessionManager.getUserPerfil(), "");
        log.info("Perfil: {}", perfilUsuario.isEmpty() ? "desconocido" : perfilUsuario);

        configurarVisibilidad();
        configurarTabla();
        configurarEventos();
        configurarBuscadorConX();
        configurarFiltroEstado();
        configurarFiltroOrdenamiento();
        configurarPaginacion();
        configurarTextAreaDescripcion();
        configurarValidadores();
        configurarClickFueraTabla();
        configurarAtajosTeclado();
        cargarMarcas();

        // Estado inicial: Nuevo habilitado, resto disabled
        btnNuevo.setDisable(false);
        btnEditar.setDisable(true);
        btnInactivar.setDisable(true);
        btnEliminar.setDisable(true);
    }

    /**
     * Configura visibilidad y tooltips de botones según permisos del perfil.
     */
    private void configurarVisibilidad() {
        if (perfilUsuario.isEmpty()) return;

        // Cargar idioma para tooltips localizados
        String idiomaActual = utilsX.cargarIdiomaActual();
        Properties idioma = utilsX.detectLanguage(idiomaActual);

        // Obtener botones autorizados con configuración completa
        Map<String, ButtonDto> botones = buttonService.obtenerBotones(perfilUsuario, MODULO, idioma);

        // Configurar cada botón
        configurarBoton(btnNuevo, botones.get("nuevo"));
        configurarBoton(btnEditar, botones.get("editar"));
        configurarBoton(btnInactivar, botones.get("inactivar"));
        configurarBoton(btnEliminar, botones.get("eliminar"));

        // Configurar botones de formulario (siempre visibles si hay permisos)
        configurarBoton(btnGuardar, botones.get("guardar"));
        configurarBoton(btnCancelar, botones.get("cancelar"));
    }

    /**
     * Configura un botón según su DTO: visibilidad y tooltip.
     */
    private void configurarBoton(Button btn, ButtonDto dto) {
        if (btn == null) return;

        if (dto == null) {
            // No autorizado: ocultar
            btn.setVisible(false);
            btn.setManaged(false);
            btn.setDisable(true);
        } else {
            // Autorizado: mostrar con tooltip
            btn.setVisible(true);
            btn.setManaged(true);
            btn.setDisable(false);

            // Tooltip descriptivo
            btn.setTooltip(new Tooltip(dto.getTooltip()));
        }
    }

    private void configurarTabla() {
        // Configurar columnas
        tableHelper.configurarColumnaNumerica(colId, Marca::getIdMarca);
        tableHelper.configurarColumnaTexto(colNombre, Marca::getNombreMarca);
        tableHelper.configurarColumnaTexto(colDescripcion, Marca::getDescripcionMarca);
        tableHelper.configurarColumnaBooleana(colEstado, Marca::getEstadoMarca);

        // Bloquear ordenamiento y reordenamiento (usamos SortHelper)
        tableHelper.bloquearOrdenamiento(tableMarcas);
        tableHelper.bloquearReordenamiento(tableMarcas);

        // Configurar tabla (items + listener selección)
        tableHelper.configurarTabla(tableMarcas, marcasData, this::onSeleccionCambiada);

        // Configurar menú contextual
        tableHelper.configurarMenuContextual(tableMarcas, this::crearMenuContextual);
    }

    /**
     * Crea menú contextual con opciones según permisos.
     */
    private ContextMenu crearMenuContextual(Marca marca) {
        ContextMenu menu = new ContextMenu();

        if (buttonService.puede(perfilUsuario, MODULO, "editar")) {
            MenuItem editar = new MenuItem(MENU_EDITAR);
            editar.setOnAction(e -> iniciarEdicion(marca));
            menu.getItems().add(editar);

            MenuItem cambiar = new MenuItem(MENU_INACTIVAR_ACTIVAR);
            cambiar.setOnAction(e -> cambiarEstado(marca));
            menu.getItems().add(cambiar);
        }

        if (buttonService.puede(perfilUsuario, MODULO, "eliminar")) {
            MenuItem eliminar = new MenuItem(MENU_ELIMINAR);
            eliminar.setOnAction(e -> eliminar(marca));
            menu.getItems().add(eliminar);
        }

        return menu;
    }

    private void configurarEventos() {
        registrarAccion(btnLimpiar, this::limpiar);
        // Búsqueda se configura en configurarBuscadorConX() via SearchHelper

        registrarAccion(btnNuevo, this::nuevo);
        registrarAccion(btnEditar, this::editar);
        registrarAccion(btnInactivar, this::cambiarEstadoSeleccionado);
        registrarAccion(btnEliminar, this::eliminarSeleccionado);

        registrarAccion(btnGuardar, this::guardar);
        registrarAccion(btnCancelar, this::cancelar);
        // Paginación se configura en configurarPaginacion() via PaginationHelper
    }

    private void registrarAccion(Button boton, Runnable accion) {
        if (boton != null) {
            boton.setOnAction(e -> accion.run());
        }
    }

    // ============ UX FEATURES ============

    /**
     * Configura búsqueda con debounce usando SearchHelper.
     * También configura botón "X" para limpiar texto.
     */
    private void configurarBuscadorConX() {
        // Configurar SearchHelper con debounce 300ms
        searchHelper = new SearchHelper(txtBusqueda, this::buscar).configure();

        // Autocomplete dinámico (no interfiere con SearchHelper)
        new AutocompleteHelper(txtBusqueda, this::sugerirMarcas).configure();

        // Mostrar/ocultar botón "X" según contenido
        if (btnLimpiarBusqueda != null) {
            txtBusqueda.textProperty().addListener((obs, oldVal, newVal) -> {
                boolean tieneTexto = newVal != null && !newVal.trim().isEmpty();
                btnLimpiarBusqueda.setVisible(tieneTexto);
                btnLimpiarBusqueda.setManaged(tieneTexto);
            });

            // Acción: limpiar usando SearchHelper (cancela debounce + busca)
            btnLimpiarBusqueda.setOnAction(e -> searchHelper.clear());
        }

        // Efecto focus: agregar clase CSS cuando el TextField tiene foco
        if (searchContainer != null) {
            txtBusqueda.focusedProperty().addListener((obs, wasFocused, isFocused) -> {
                if (isFocused) {
                    searchContainer.getStyleClass().add("focused");
                } else {
                    searchContainer.getStyleClass().remove("focused");
                }
            });
        }
    }

    /**
     * Configura ComboBox de filtro por estado usando FilterStateHelper.
     * Opciones: Todos (null), Activo (true), Inactivo (false).
     * Ejecuta búsqueda automáticamente al cambiar.
     */
    private void configurarFiltroEstado() {
        filterStateHelper = new FilterStateHelper(cmbEstadoFiltro, () -> {
            paginaActual = 0;
            cargarMarcas();
        }).configure();
    }

    /**
     * Configura ComboBox de ordenamiento usando SortHelper.
     * Opciones: ID Asc/Desc, Nombre A-Z/Z-A.
     * Ejecuta búsqueda automáticamente al cambiar.
     */
    private void configurarFiltroOrdenamiento() {
        sortHelper = new SortHelper(cmbOrdenamiento, () -> {
            paginaActual = 0;
            cargarMarcas();
        });

        sortHelper
                .addOption("ID Ascendente", "idMarca", Sort.Direction.ASC, false)
                .addOption("ID Descendente", "idMarca", Sort.Direction.DESC, false)
                .addOption("Nombre A-Z", "nombreMarca", Sort.Direction.ASC, true)
                .addOption("Nombre Z-A", "nombreMarca", Sort.Direction.DESC, true)
                .configure();
    }

    /**
     * Configura paginación mejorada usando PaginationHelper.
     * Layout: [Primera] [Anterior] Página [_3_] de 10 [Siguiente] [Última]
     */
    private void configurarPaginacion() {
        paginationHelper = new PaginationHelper(
                txtPagina, lblTotalPaginas,
                btnPrimera, btnAnterior, btnSiguiente, btnUltima,
                pagina -> {
                    paginaActual = pagina;
                    cargarMarcas();
                }
        ).configure();
    }

    /**
     * Configura contadores de caracteres para nombre y descripción.
     * Muestra "X/80" y "X/600" con cambio de color según uso.
     */
    private void configurarTextAreaDescripcion() {
        // Contador para nombre (80 caracteres)
        if (lblContadorNombre != null) {
            CharCounterHelper.configure(txtNombre, lblContadorNombre, MAX_NOMBRE_CARACTERES);
        }

        // Contador para descripción (600 caracteres)
        if (lblContadorDesc != null) {
            CharCounterHelper.configure(txtDescripcion, lblContadorDesc, MAX_DESCRIPCION_CARACTERES);
        }
    }

    /**
     * Configura validadores de formulario con ValidationHelper.
     * Feedback visual en tiempo real mientras el usuario escribe.
     */
    private void configurarValidadores() {
        // Validador de nombre: requerido, mínimo 3, máximo 80 caracteres
        nombreValidator = new ValidationHelper(txtNombre)
                .required(MSG_NOMBRE_OBLIGATORIO)
                .minLength(3, MSG_MINIMO_CARACTERES)
                .maxLength(MAX_NOMBRE_CARACTERES, MSG_NOMBRE_LARGA)
                .configure();

        // Validador de descripción: máximo 600 caracteres (opcional)
        descripcionValidator = new ValidationHelper(txtDescripcion)
                .maxLength(MAX_DESCRIPCION_CARACTERES, MSG_DESCRIPCION_LARGA)
                .configure();
    }

    /**
     * Configura comportamiento de click fuera de controles.
     * 1. FocusHelper: quita focus de inputs al click fuera
     * 2. Deselección tabla: limpia selección al click fuera (solo si no está editando)
     */
    private void configurarClickFueraTabla() {
        Platform.runLater(() -> {
            if (tableMarcas.getScene() == null) {
                log.warn("Scene null, no se puede configurar click fuera");
                return;
            }

            Node root = tableMarcas.getScene().getRoot();

            // 1. FocusHelper: quitar focus de inputs al click fuera
            FocusHelper.configurarClickFuera(root, txtPagina, txtBusqueda, txtNombre, txtDescripcion);

            // 2. Deselección de tabla al click fuera (usa addEventFilter para coexistir)
            root.addEventFilter(MouseEvent.MOUSE_CLICKED, event -> {
                // Solo procesar si NO está editando
                if (estaFormularioEditable()) {
                    return;
                }

                // Solo si hay selección actual
                if (obtenerSeleccionado() == null) {
                    return;
                }

                // Verificar si el click fue fuera de la tabla
                if (!tableMarcas.contains(tableMarcas.screenToLocal(event.getScreenX(), event.getScreenY()))) {
                    tableMarcas.getSelectionModel().clearSelection();
                    limpiarFormulario();
                    actualizarBotones();
                }
            });
        });
    }

    // ============ ATAJOS DE TECLADO ============

    /**
     * Configura atajos de teclado usando KeybindHelper.
     * ESC: 3 prioridades (paginación → cancelar → deseleccionar)
     * F5: Reiniciar vista
     * Delete: Eliminar seleccionado
     * Ctrl+N/S/E: Nuevo/Guardar/Editar
     */
    private void configurarAtajosTeclado() {
        if (tableMarcas == null || tableMarcas.getScene() == null) {
            log.warn("No se pueden configurar atajos: tabla o scene null");
            return;
        }

        new KeybindHelper(tableMarcas.getScene())
                .onEscapeCrud(txtPagina, tableMarcas,
                        this::estaFormularioEditable,
                        this::cancelar,
                        this::limpiarFormulario,
                        this::actualizarBotones)
                .on(KeyCode.F5, this::limpiar)
                .on(KeyCode.DELETE, this::eliminarSeleccionado, () -> KeybindHelper.isEnabled(btnEliminar))
                .onCtrl(KeyCode.N, this::nuevo, () -> KeybindHelper.isEnabled(btnNuevo))
                .onCtrl(KeyCode.S, this::guardar, () -> KeybindHelper.isEnabled(btnGuardar))
                .onCtrl(KeyCode.E, this::editar, () -> KeybindHelper.isEnabled(btnEditar))
                .configure();
    }

    private void actualizarBotones() {
        if (operacionEnCurso) {
            return;
        }

        boolean haySeleccion = obtenerSeleccionado() != null;
        boolean enEdicion = estaFormularioEditable();

        if (enEdicion) {
            // EDICION/NUEVO: todos los botones TOP disabled
            UIHelper.disable(btnNuevo, btnEditar, btnInactivar, btnEliminar);
        } else {
            // PREVIEW o SIN_SELECCION
            UIHelper.enable(btnNuevo);
            btnEditar.setDisable(!haySeleccion);
            btnInactivar.setDisable(!haySeleccion);
            btnEliminar.setDisable(!haySeleccion);
        }
    }

    // ============ BÚSQUEDA Y PAGINACIÓN ============

    private void buscar() {
        busquedaActual = txtBusqueda.getText().trim();
        paginaActual = 0;
        cargarMarcas();
    }

    private List<String> sugerirMarcas(String term) {
        String filtro = term == null ? "" : term.trim();
        if (filtro.length() < 2) {
            return Collections.emptyList();
        }

        Boolean estadoFiltro = filterStateHelper != null ? filterStateHelper.getCurrentState() : null;
        Pageable pageable = PageRequest.of(0, 8, Sort.by(Sort.Order.asc("nombreMarca").ignoreCase()));

        try {
            Page<Marca> page = marcaService.buscarPaginado(filtro, estadoFiltro, pageable);
            return page.getContent().stream()
                    .map(Marca::getNombreMarca)
                    .filter(Objects::nonNull)
                    .filter(nombre -> AutocompleteHelper.matchesWordStart(nombre, filtro))
                    .distinct()
                    .toList();
        } catch (Exception ex) {
            log.warn("No se pudo cargar sugerencias de marcas para '{}'", filtro, ex);
            return Collections.emptyList();
        }
    }

    private void limpiar() {
        txtBusqueda.clear();
        filterStateHelper.reset();
        sortHelper.reset();
        tableMarcas.getSelectionModel().clearSelection();
        tableMarcas.requestFocus(); // Quitar focus de campos antes de reset (evita re-trigger validadores)
        resetearEdicion();
        deshabilitarCamposFormulario();
        desbloquearEdicion();  // Desbloquea paginación, búsqueda, filtros
        buscar();
        actualizarBotones();
        log.info("Vista reiniciada - Filtros, ordenamiento y búsqueda limpiados");
    }

    /**
     * Limpia completamente después de guardar/eliminar:
     * - Formulario + validadores
     * - Búsqueda y filtros
     * - Tabla (deselecciona)
     * - Restaura estado deshabilitado
     * Usado después de guardar para volver a estado SIN_SELECCION limpio.
     */
    private void limpiarCompletamente() {
        // Forzar pérdida de focus antes de limpiar
        tableMarcas.requestFocus();

        // Limpiar formulario y validadores
        resetearEdicion();
        deshabilitarCamposFormulario();

        // Limpiar búsqueda
        txtBusqueda.clear();
        busquedaActual = "";

        // Resetear filtros (sin buscar, se hace después al cargarMarcas)
        filterStateHelper.reset();
        sortHelper.reset();
        paginaActual = 0;

        // Deseleccionar tabla
        tableMarcas.getSelectionModel().clearSelection();

        // Desbloquear controles de búsqueda
        desbloquearEdicion();

        log.info("Form limpiado completamente después de guardar");
    }

    /**
     * Carga marcas paginadas desde BD sin bloquear la UI.
     * Muestra ProgressIndicator mientras se carga, actualiza tabla al terminar.
     */
    private void cargarMarcas() {
        Task<Page<Marca>> task = new Task<>() {
            @Override
            protected Page<Marca> call() {
                return obtenerPaginaMarcas();
            }
        };

        ejecutarOperacionAsync("marcas-cargar", task,
                page -> {
                    actualizarDatosPaginacion(page);
                    actualizarBotones();
                    log.debug("Cargadas {} marcas, página {}/{}", page.getNumberOfElements(), paginaActual + 1, totalPaginas);
                },
                ex -> {
                    actualizarBotones();
                    log.error("Error al cargar marcas", ex);
                    notificarError("Error al cargar marcas: " + ex.getMessage());
                }
        );
    }

    private void reSeleccionarMarca(Long marcaId, boolean mantenerPaginacionBloqueada) {
        if (marcaId == null) {
            return;
        }

        tableMarcas.getItems().stream()
                .filter(m -> m.getIdMarca().equals(marcaId))
                .findFirst()
                .ifPresent(m -> {
                    tableMarcas.getSelectionModel().select(m);
                    if (mantenerPaginacionBloqueada) {
                        bloquearPaginacion();
                    }
                });
    }

    private Page<Marca> obtenerPaginaMarcas() {
        Boolean estadoFiltro = filterStateHelper.getCurrentState();
        return busquedaActual.isEmpty() ?
                marcaService.listar(estadoFiltro, crearPageable()) :
                marcaService.buscarPaginado(busquedaActual, estadoFiltro, crearPageable());
    }

    private void actualizarDatosPaginacion(Page<Marca> page) {
        // Si página actual quedó vacía pero hay datos en páginas anteriores, retroceder
        if (page.isEmpty() && paginaActual > 0 && page.getTotalElements() > 0) {
            paginaActual--;
            cargarMarcas(); // Recargar página anterior
            return;
        }

        marcasData.clear();
        marcasData.addAll(page.getContent());
        totalPaginas = page.getTotalPages();
        totalRegistros = page.getTotalElements();
        actualizarPaginacion();
    }

    private Pageable crearPageable() {
        Sort.Order order = sortHelper.createOrder();
        return PageRequest.of(paginaActual, REGISTROS_POR_PAGINA, Sort.by(order));
    }

    private void actualizarPaginacion() {
        lblTotalMarcas.setText("Total: " + totalRegistros);
        paginationHelper.sincronizar(paginaActual, totalPaginas);
    }

    // ============ BLOQUEO DE EDICIÓN (Helpers) ============

    /**
     * Bloquea búsqueda, filtros y paginación cuando entra en EDICION/NUEVO.
     * Evita que el usuario busque mientras está editando un registro.
     * Consistente con JavaFX: controls disabled ignoran hover/focus automáticamente.
     */
    private void bloquearEdicion() {
        paginationHelper.disable();          // Deshabilita paginación
        searchHelper.disable();              // Deshabilita buscador
        filterStateHelper.disable();         // Deshabilita filtro estado
        sortHelper.disable();                // Deshabilita ordenamiento
        searchContainer.setDisable(true);    // Deshabilita HBox (oscurece visualmente)
    }

    /**
     * Desbloquea búsqueda, filtros y paginación cuando sale de EDICION/NUEVO.
     * Permite que el usuario busque nuevamente después de guardar/cancelar.
     */
    private void desbloquearEdicion() {
        paginationHelper.enable();           // Habilita paginación
        searchHelper.enable();               // Habilita buscador
        filterStateHelper.enable();          // Habilita filtro estado
        sortHelper.enable();                 // Habilita ordenamiento
        searchContainer.setDisable(false);   // Habilita HBox
    }

    private void bloquearPaginacion() {
        paginationHelper.disable();
    }

    private void habilitarPaginacion() {
        paginationHelper.enable();
    }

    private void nuevo() {
        tableMarcas.getSelectionModel().clearSelection();
        resetearEdicion();

        // Mostrar próximo ID disponible
        Long proximoId = marcaService.obtenerProximoId();
        lblId.setText(String.valueOf(proximoId));

        habilitarCamposFormulario();
        bloquearEdicion();  // Bloquea buscador, filtros, paginación
        actualizarBotones();

        // Forzar focus en Nombre y trigger visual del validador
        Platform.runLater(() -> {
            txtNombre.requestFocus();
            // Forzar validación visual inmediata (simular pérdida y recuperación de focus)
            if (nombreValidator != null) {
                nombreValidator.validate();
            }
        });

        log.info("Modo NUEVO activado - Próximo ID: {}", proximoId);
    }

    private void editar() {
        Marca marca = obtenerSeleccionado();
        if (marca == null) {
            notificarWarning(MSG_SELECCIONAR);
            return;
        }
        iniciarEdicion(marca);
    }

    /**
     * Inicia edición desde cualquier origen (click tabla o menú contextual).
     * Centraliza la lógica para garantizar consistencia.
     */
    private void iniciarEdicion(Marca marca) {
        if (marca == null) return;

        cargarMarca(marca);
        habilitarCamposFormulario();
        bloquearEdicion();  // Bloquea buscador, filtros, paginación
        actualizarBotones();
        txtNombre.requestFocus();
        log.info("Edición iniciada - Marca ID: {}", marca.getIdMarca());
    }

    private Marca obtenerSeleccionado() {
        return tableMarcas.getSelectionModel().getSelectedItem();
    }

    /**
     * Maneja cambio de selección en tabla.
     * Si está editando, sale del modo edición y vuelve a PREVIEW.
     * Luego actualiza formulario con datos del ítem seleccionado.
     *
     * @param marca Marca seleccionada, o null si se deseleccionó
     */
    private void onSeleccionCambiada(Marca marca) {
        if (estaFormularioEditable()) {
            resetearEdicion();
            deshabilitarCamposFormulario();
            desbloquearEdicion();  // Desbloquea paginación, búsqueda, filtros
        }
        cargarPreview(marca);
        actualizarBotones();
    }

    /**
     * Obtiene Stage actual de la Scene.
     * NO cachear - Stage puede cambiar después de re-login.
     *
     * @return Stage owner, o null si Scene no disponible
     */
    private Stage obtenerStage() {
        if (tableMarcas != null && tableMarcas.getScene() != null) {
            return (Stage) tableMarcas.getScene().getWindow();
        }
        return null;
    }

    /**
     * Carga datos de marca en formulario para visualización (PREVIEW).
     * Campos permanecen disabled, solo lectura.
     */
    private void cargarPreview(Marca marca) {
        if (marca == null) {
            limpiarFormulario();
            return;
        }

        lblId.setText(String.valueOf(marca.getIdMarca()));
        txtNombre.setText(marca.getNombreMarca());
        txtDescripcion.setText(Objects.requireNonNullElse(marca.getDescripcionMarca(), ""));
        chkEstado.setSelected(Objects.requireNonNullElse(marca.getEstadoMarca(), true));

        // No guardar originales aquí - se hace al entrar en EDICION
    }

    /**
     * Carga datos de marca para edición.
     * Prepara marcaEnEdicion y valores originales.
     */
    private void cargarMarca(Marca marca) {
        if (marca == null) return;

        marcaEnEdicion = marca.getIdMarca();
        lblId.setText(String.valueOf(marca.getIdMarca()));
        txtNombre.setText(marca.getNombreMarca());
        txtDescripcion.setText(Objects.requireNonNullElse(marca.getDescripcionMarca(), ""));
        chkEstado.setSelected(Objects.requireNonNullElse(marca.getEstadoMarca(), true));

        // Guardar valores originales para detectar cambios
        guardarValoresOriginales();
    }

    private void guardarValoresOriginales() {
        nombreOriginal = obtenerTexto(txtNombre);
        descripcionOriginal = obtenerTexto(txtDescripcion);
        estadoOriginal = chkEstado.isSelected();
    }

    // ============ OPERACIONES ASYNC ============

    private <T> void ejecutarOperacionAsync(String nombreHilo,
                                            Task<T> task,
                                            Consumer<T> onSuccess,
                                            Consumer<Throwable> onFailed) {
        piProgreso.setVisible(true);
        deshabilitarAccionesOperacion();

        task.setOnSucceeded(e -> {
            piProgreso.setVisible(false);
            habilitarAccionesOperacion();
            onSuccess.accept(task.getValue());
        });

        task.setOnFailed(e -> {
            piProgreso.setVisible(false);
            habilitarAccionesOperacion();
            Throwable ex = task.getException();
            onFailed.accept(ex);
        });

        Thread hilo = new Thread(task, nombreHilo);
        hilo.setDaemon(true);
        hilo.start();
    }

    // ============ OPERACIONES CRUD ============

    private void cambiarEstadoSeleccionado() {
        Marca marca = obtenerSeleccionado();
        if (marca == null) {
            notificarWarning(MSG_SELECCIONAR);
            return;
        }
        cambiarEstado(marca);
    }

    /**
     * Cambia el estado de una marca (activar/inactivar).
     * Si tiene productos, muestra diálogo de cascada.
     */
    private void cambiarEstado(Marca marca) {
        boolean activar = !marca.getEstadoMarca();
        Long marcaId = marca.getIdMarca();

        // Contar productos afectados
        long productosActivos = marcaService.contarProductosActivosPorMarca(marcaId);
        long productosInactivos = marcaService.contarProductosInactivosPorMarca(marcaId);

        DialogHelper.OpcionCascada opcion;

        if (!activar && productosActivos > 0) {
            // INACTIVAR marca con productos activos → Mostrar diálogo cascada
            opcion = DialogHelper.showCascadaInactivar(obtenerStage(), "Marca", productosActivos);
        } else if (activar && productosInactivos > 0) {
            // ACTIVAR marca con productos inactivos → Mostrar diálogo cascada
            opcion = DialogHelper.showCascadaActivar(obtenerStage(), "Marca", productosInactivos);
        } else {
            // Sin productos afectados → Cambio directo sin diálogo
            opcion = DialogHelper.OpcionCascada.SOLO_ENTIDAD;
        }

        // Procesar según opción elegida
        if (opcion == DialogHelper.OpcionCascada.CANCELAR) {
            return; // Usuario canceló
        }

        Task<Page<Marca>> task = new Task<>() {
            @Override
            protected Page<Marca> call() {
                if (opcion == DialogHelper.OpcionCascada.CASCADA_COMPLETA) {
                    // Cambiar marca + productos
                    if (activar) {
                        marcaService.activarMarcaYProductos(marcaId);
                    } else {
                        marcaService.inactivarMarcaYProductos(marcaId);
                    }
                } else {
                    // Solo cambiar marca
                    Marca entidad = marcaService.findById(marcaId)
                            .orElseThrow(() -> new IllegalStateException(MSG_NO_ENCONTRADA));
                    entidad.setEstadoMarca(activar);
                    marcaService.save(entidad);
                }
                return obtenerPaginaMarcas();
            }
        };

        ejecutarOperacionAsync("marcas-cambiar-estado", task,
                page -> {
                    actualizarDatosPaginacion(page);
                    actualizarBotones();
                    boolean estabaEditando = marcaEnEdicion != null && marcaEnEdicion.equals(marcaId);
                    reSeleccionarMarca(marcaId, estabaEditando);
                    if (estabaEditando) {
                        chkEstado.setSelected(activar);
                    }

                    // Toast informativo
                    String mensaje = activar ? MSG_ACTIVADA : MSG_INACTIVADA;
                    if (opcion == DialogHelper.OpcionCascada.CASCADA_COMPLETA) {
                        long cantidad = activar ? productosInactivos : productosActivos;
                        mensaje += " con " + cantidad + " producto(s) en cascada";
                    }
                    notificarInfo(mensaje);
                    log.info("Marca {} - ID: {}, opción: {}", mensaje, marcaId, opcion);
                },
                ex -> {
                    actualizarBotones();
                    log.error("Error al cambiar estado", ex);
                    notificarError("Error: " + ex.getMessage());
                }
        );
    }

    private void eliminarSeleccionado() {
        Marca marca = obtenerSeleccionado();
        if (marca == null) {
            notificarWarning(MSG_SELECCIONAR);
            return;
        }
        eliminar(marca);
    }

    /**
     * Elimina una marca después de confirmar y validar dependencias.
     * Recarga tabla automáticamente si la eliminación es exitosa.
     */
    private void eliminar(Marca marca) {
        if (marcaService.tieneProductosAsociados(marca.getIdMarca())) {
            notificarWarning(MSG_TIENE_PRODUCTOS);
            return;
        }

        if (!DialogHelper.showConfirmarEliminacion(obtenerStage(), "Marca: " + marca.getNombreMarca())) return;

        Task<Void> task = new Task<>() {
            @Override
            protected Void call() {
                marcaService.delete(marca.getIdMarca());
                return null;
            }
        };

        ejecutarOperacionAsync("marcas-eliminar", task,
                aVoid -> {
                    resetearEdicion();
                    deshabilitarCamposFormulario();
                    tableMarcas.getSelectionModel().clearSelection();
                    cargarMarcas();
                    actualizarBotones();
                    notificarSuccess(MSG_ELIMINADA);
                    log.info("Eliminada - ID: {}", marca.getIdMarca());
                },
                ex -> {
                    actualizarBotones();
                    if (ex instanceof IllegalArgumentException) {
                        log.warn("Validación fallida al eliminar: {}", ex.getMessage());
                        notificarWarning("Validación: " + ex.getMessage());
                    } else {
                        log.error("Error interno al eliminar", ex);
                        notificarError("Error al eliminar. Contacte con soporte.");
                    }
                }
        );
    }

    /**
     * Guarda una nueva marca o actualiza una existente.
     * Ejecuta validación backend antes de persistir en BD.
     * Mantiene UI responsiva usando Task con callbacks.
     */
    private void guardar() {
        if (!validar()) return;

        final String nombre = obtenerTexto(txtNombre);
        final String descripcion = obtenerTexto(txtDescripcion);
        final boolean estado = chkEstado.isSelected();

        // Detectar cambio de estado en EDICIÓN
        final DialogHelper.OpcionCascada opcionCascada;
        final long cantidadProductosAfectados; // ← Capturar ANTES del Task

        if (marcaEnEdicion != null && estadoOriginal != estado) {
            // Estado cambió → Verificar productos y mostrar diálogo cascada
            Long marcaId = marcaEnEdicion;
            long productosActivos = marcaService.contarProductosActivosPorMarca(marcaId);
            long productosInactivos = marcaService.contarProductosInactivosPorMarca(marcaId);

            if (!estado && productosActivos > 0) {
                // Cambió a INACTIVO con productos activos → Diálogo cascada
                opcionCascada = DialogHelper.showCascadaInactivar(obtenerStage(), "Marca", productosActivos);
                cantidadProductosAfectados = productosActivos;
            } else if (estado && productosInactivos > 0) {
                // Cambió a ACTIVO con productos inactivos → Diálogo cascada
                opcionCascada = DialogHelper.showCascadaActivar(obtenerStage(), "Marca", productosInactivos);
                cantidadProductosAfectados = productosInactivos;
            } else {
                // Sin productos afectados → Solo entidad
                opcionCascada = DialogHelper.OpcionCascada.SOLO_ENTIDAD;
                cantidadProductosAfectados = 0;
            }

            // Usuario canceló → No guardar
            if (opcionCascada == DialogHelper.OpcionCascada.CANCELAR) {
                return;
            }
        } else {
            // Nuevo registro o estado no cambió → Sin cascada
            opcionCascada = null;
            cantidadProductosAfectados = 0;
        }

        Task<Long> task = new Task<>() {
            @Override
            protected Long call() {
                Marca marca;

                if (marcaEnEdicion != null) {
                    // EDICIÓN: Aplicar cascada si corresponde
                    if (opcionCascada == DialogHelper.OpcionCascada.CASCADA_COMPLETA) {
                        // Aplicar cambios con cascada (usa métodos del service)
                        if (estado) {
                            marcaService.activarMarcaYProductos(marcaEnEdicion);
                        } else {
                            marcaService.inactivarMarcaYProductos(marcaEnEdicion);
                        }
                    }

                    // Cargar marca actualizada y aplicar datos del formulario
                    marca = marcaService.findById(marcaEnEdicion)
                            .orElseThrow(() -> new IllegalStateException(MSG_NO_ENCONTRADA));
                    aplicarDatosFormulario(marca, nombre, descripcion, estado);
                } else {
                    // NUEVO: Usar builder pattern (Marca tiene @Builder)
                    marca = Marca.builder()
                            .nombreMarca(nombre)
                            .descripcionMarca(descripcion.isEmpty() ? null : descripcion)
                            .estadoMarca(estado)
                            .build();
                }

                // Validar y guardar (común para NUEVO y EDICIÓN)
                marcaService.validarMarca(marca);
                marcaService.save(marca);

                return marca.getIdMarca(); // Retorna ID guardado
            }
        };

        ejecutarOperacionAsync("marcas-guardar", task,
                ignoredId -> {
                    // Toast 1: Guardado exitoso
                    notificarSuccess(MSG_GUARDADO);

                    // Toast 2: Info de cascada (si aplica)
                    if (opcionCascada == DialogHelper.OpcionCascada.CASCADA_COMPLETA) {
                        String mensajeCascada = "Marca " + (estado ? "activada" : "inactivada") +
                                " con " + cantidadProductosAfectados + " producto(s) en cascada";
                        notificarInfo(mensajeCascada);
                    }

                    // Limpiar completamente: form + búsqueda + validadores + tabla
                    limpiarCompletamente();
                    cargarMarcas();
                    actualizarBotones();
                },
                ex -> {
                    actualizarBotones();
                    habilitarCamposFormulario();
                    bloquearPaginacion();
                    if (ex instanceof IllegalArgumentException) {
                        log.warn("Validación fallida: {}", ex.getMessage());
                        notificarWarning("Validación: " + ex.getMessage());
                    } else {
                        log.error("Error al guardar", ex);
                        notificarError("Error al guardar: " + ex.getMessage());
                    }
                }
        );
    }

    private void aplicarDatosFormulario(Marca marca, String nombre, String descripcion, boolean estado) {
        marca.setNombreMarca(nombre);
        marca.setDescripcionMarca(descripcion.isEmpty() ? null : descripcion);
        marca.setEstadoMarca(estado);
    }

    private void cancelar() {
        // Advertir si hay cambios sin guardar
        if (formularioTieneCambios()) {
            if (!DialogHelper.showConfirmar(
                    obtenerStage(),
                    DIALOG_CAMBIOS_TITULO,
                    DIALOG_CAMBIOS_MENSAJE
            )) return;
        }

        // Capturar ID antes de resetear (para volver a PREVIEW)
        Long idParaReseleccionar = marcaEnEdicion;

        // Forzar pérdida de focus de campos del formulario antes de limpiar
        // (para que validadores actualicen estado visual correctamente)
        tableMarcas.requestFocus();

        resetearEdicion();
        deshabilitarCamposFormulario();
        desbloquearEdicion();  // Desbloquea buscador, filtros, paginación

        if (idParaReseleccionar != null) {
            // Volver a PREVIEW: re-seleccionar el item que estábamos editando
            reSeleccionarMarca(idParaReseleccionar, false);
            // cargarPreview se llamará automáticamente por el listener de tabla
        } else {
            // Era NUEVO: ir a SIN_SELECCION
            tableMarcas.getSelectionModel().clearSelection();
        }

        actualizarBotones();
        actualizarPaginacion();
    }

    private boolean formularioTieneCambios() {
        if (marcaEnEdicion == null) {
            // Para nuevo registro: verificar si hay datos no guardados
            return !obtenerTexto(txtNombre).isEmpty() || !obtenerTexto(txtDescripcion).isEmpty();
        }

        // Para edición: comparar con originales (normalizados con trim)
        String nombreActual = obtenerTexto(txtNombre);
        String descripcionActual = obtenerTexto(txtDescripcion);
        boolean estadoActual = chkEstado.isSelected();

        return !Objects.equals(nombreActual, Objects.requireNonNullElse(nombreOriginal, "")) ||
                !Objects.equals(descripcionActual, Objects.requireNonNullElse(descripcionOriginal, "")) ||
                estadoActual != estadoOriginal;
    }

    private String obtenerTexto(TextInputControl control) {
        String valor = control.getText();
        return valor == null ? "" : valor.trim();
    }

    private void resetearEdicion() {
        limpiarFormulario();
        marcaEnEdicion = null;
    }

    private void limpiarFormulario() {
        lblId.setText(ID_SIN_SELECCION);
        txtNombre.clear();
        txtDescripcion.clear();
        chkEstado.setSelected(true);
        lblMensajeForm.setText("");

        // Resetear estado visual de validadores
        if (nombreValidator != null) nombreValidator.reset();
        if (descripcionValidator != null) descripcionValidator.reset();
    }

    private void deshabilitarCamposFormulario() {
        txtNombre.setDisable(true);
        txtDescripcion.setDisable(true);
        chkEstado.setDisable(true);
        UIHelper.disable(btnGuardar, btnCancelar);
    }

    private void habilitarCamposFormulario() {
        txtNombre.setDisable(false);
        txtDescripcion.setDisable(false);
        chkEstado.setDisable(false);
        UIHelper.enable(btnGuardar, btnCancelar);
    }

    private boolean estaFormularioEditable() {
        return !txtNombre.isDisabled();
    }

    /**
     * Deshabilita todos los botones de operación durante Tasks async.
     * Previene doble-submit y acciones concurrentes.
     */
    private void deshabilitarAccionesOperacion() {
        operacionEnCurso = true;
        UIHelper.disable(btnNuevo, btnEditar, btnInactivar, btnEliminar,
                btnGuardar, btnCancelar, btnLimpiar);
        paginationHelper.disable();
        tableMarcas.setDisable(true);
    }

    /**
     * Re-habilita botones de operación después de completar Task.
     * actualizarBotones() ajustará estado final según contexto.
     */
    private void habilitarAccionesOperacion() {
        operacionEnCurso = false;
        UIHelper.enable(btnLimpiar);
        tableMarcas.setDisable(false);

        if (estaFormularioEditable()) {
            bloquearPaginacion();
        } else {
            habilitarPaginacion();
        }
    }

    // ============ VALIDACIÓN ============

    private boolean validar() {
        String nombre = obtenerTexto(txtNombre);
        String descripcion = obtenerTexto(txtDescripcion);

        if (!validarNombreObligatorio(nombre)) {
            return false;
        }

        // Validación de longitud máxima del nombre
        if (nombre.length() > MAX_NOMBRE_CARACTERES) {
            mostrarErrorValidacion(txtNombre, MSG_NOMBRE_LARGA + " (" + nombre.length() + "/" + MAX_NOMBRE_CARACTERES + ")");
            return false;
        }

        // Validación de longitud de descripción
        if (descripcion.length() > MAX_DESCRIPCION_CARACTERES) {
            mostrarErrorValidacion(txtDescripcion, MSG_DESCRIPCION_LARGA + " (" + descripcion.length() + "/" + MAX_DESCRIPCION_CARACTERES + ")");
            return false;
        }

        // Validación para NUEVO registro
        if (esNuevoRegistro()) {
            if (marcaService.existsByNombreMarca(nombre)) {
                mostrarErrorValidacion(txtNombre, MSG_NOMBRE_EXISTE);
                return false;
            }
        }
        // Validación para EDICIÓN (ignora el propio ID)
        else {
            if (marcaService.existsByNombreMarcaAndNotId(nombre, marcaEnEdicion)) {
                mostrarErrorValidacion(txtNombre, MSG_NOMBRE_EXISTE);
                return false;
            }
        }

        return true;
    }

    private boolean esNuevoRegistro() {
        return marcaEnEdicion == null;
    }

    /**
     * Valida que el nombre sea obligatorio, no esté vacío y tenga mínimo 3 caracteres.
     */
    private boolean validarNombreObligatorio(String nombre) {
        if (nombre == null || nombre.trim().isEmpty()) {
            mostrarErrorValidacion(txtNombre, MSG_NOMBRE_OBLIGATORIO);
            return false;
        }
        if (nombre.trim().length() < 3) {
            mostrarErrorValidacion(txtNombre, MSG_MINIMO_CARACTERES);
            return false;
        }
        return true;
    }

    private void mostrarErrorValidacion(TextInputControl campo, String mensaje) {
        lblMensajeForm.setText(mensaje);
        campo.requestFocus();
    }

    // ============ NOTIFICACIONES (Toast Premium) ============

    /**
     * Muestra Toast de éxito (verde) - Duración normal.
     */
    private void notificarSuccess(String mensaje) {
        mostrarToast(mensaje, Toast.Type.SUCCESS, Toast.DURATION_NORMAL);
    }

    /**
     * Muestra Toast de advertencia (amarillo) - Duración normal.
     */
    private void notificarWarning(String mensaje) {
        mostrarToast(mensaje, Toast.Type.WARNING, Toast.DURATION_NORMAL);
    }

    /**
     * Muestra Toast de error (rojo) - Duración larga para errores críticos.
     */
    private void notificarError(String mensaje) {
        mostrarToast(mensaje, Toast.Type.ERROR, Toast.DURATION_LONG);
    }

    /**
     * Muestra Toast de información (azul) - Duración normal.
     */
    private void notificarInfo(String mensaje) {
        mostrarToast(mensaje, Toast.Type.INFO, Toast.DURATION_NORMAL);
    }

    /**
     * Método interno para mostrar Toast con Stage fresco.
     * Previene bug multi-login obteniendo Stage actual en cada llamada.
     * A prueba de doble login y navegaciones múltiples.
     */
    private void mostrarToast(String mensaje, Toast.Type tipo, int duracion) {
        Stage stageActual = obtenerStage();

        if (stageActual == null) {
            log.info("Notificación sin Scene - {}: {}", tipo, mensaje);
            return;
        }

        Toast.showToast(stageActual, mensaje, tipo, duracion);
    }


}
