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
import pe.edu.upeu.farmafx.model.Categoria;
import pe.edu.upeu.farmafx.service.IButtonService;
import pe.edu.upeu.farmafx.service.ICategoriaService;
import pe.edu.upeu.farmafx.utils.SessionManager;
import pe.edu.upeu.farmafx.utils.UtilsX;

import javafx.scene.input.KeyCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.function.Consumer;


@Controller
@RequiredArgsConstructor
@Slf4j
public class CategoriasController {

    private static final int REGISTROS_POR_PAGINA = 7;
    private static final String MODULO = "CATEGORIAS";

    // Mensajes de usuario
    private static final String MSG_SELECCIONAR = "Seleccione una categoria";
    private static final String MSG_GUARDADO = "Guardado correctamente";
    private static final String MSG_NOMBRE_OBLIGATORIO = "Nombre obligatorio";
    private static final String MSG_NOMBRE_EXISTE = "Nombre ya existe";
    private static final String MSG_NO_ENCONTRADA = "Categoria no encontrada";

    // Menú contextual
    private static final String MENU_EDITAR = "Editar";
    private static final String MENU_INACTIVAR_ACTIVAR = "Inactivar/Activar";
    private static final String MENU_ELIMINAR = "Eliminar";

    // Mensajes de estado
    private static final String MSG_ACTIVADA = "Categoria activada";
    private static final String MSG_INACTIVADA = "Categoria inactivada";
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
    private final ICategoriaService categoriaService;
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
    @FXML private ComboBox<Categoria> cbxCategoriaPadre;
    @FXML private CheckBox chkEstado;
    @FXML private Button btnGuardar, btnCancelar;
    @FXML private ProgressIndicator piProgreso;
    @FXML private TableView<Categoria> tableCategorias;
    @FXML private TableColumn<Categoria, Long> colId;
    @FXML private TableColumn<Categoria, String> colNombre, colDescripcion, colPadre;
    @FXML private TableColumn<Categoria, Boolean> colEstado;
    @FXML private Label lblTotalCategorias;
    @FXML private Label lblContadorNombre, lblContadorDesc;
    // Paginación mejorada
    @FXML private TextField txtPagina;
    @FXML private Label lblTotalPaginas;
    @FXML private Button btnPrimera, btnAnterior, btnSiguiente, btnUltima;

    private final ObservableList<Categoria> categoriasData = FXCollections.observableArrayList();
    private int paginaActual = 0, totalPaginas = 0;
    private long totalRegistros = 0L;
    private String busquedaActual = "", perfilUsuario = "";
    private Long categoriaEnEdicion = null;
    private boolean operacionEnCurso = false;

    // Para detectar cambios sin guardar
    private String nombreOriginal = "", descripcionOriginal = "";
    private boolean estadoOriginal = true;
    private Long categoriaPadreOriginalId = null; // ID del padre original

    // Records para agrupar datos relacionados (Java 17+)
    private record DatosGuardado(String nombre, String descripcion, boolean estado) {}
    private record InfoCascada(DialogHelper.OpcionCascada opcion, long cantidad, boolean cancelado) {}

    @FXML
    public void initialize() {
        log.info("Inicializando CategoriasGoldController");
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
        cargarComboCategoriasPadre();
        cargarCategorias();

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
        tableHelper.configurarColumnaNumerica(colId, Categoria::getIdCategoria);
        tableHelper.configurarColumnaTexto(colNombre, Categoria::getNombreCategoria);
        tableHelper.configurarColumnaTexto(colDescripcion, Categoria::getDescripcionCategoria);
        // Columna Padre: mostrar nombre del padre o "-" si no tiene
        tableHelper.configurarColumnaTexto(colPadre, cat ->
            cat.getCategoriaPadre() != null ? cat.getCategoriaPadre().getNombreCategoria() : "-");
        tableHelper.configurarColumnaBooleana(colEstado, Categoria::getEstadoCategoria);

        // Bloquear ordenamiento y reordenamiento (usamos SortHelper)
        tableHelper.bloquearOrdenamiento(tableCategorias);
        tableHelper.bloquearReordenamiento(tableCategorias);

        // Configurar tabla (items + listener selección)
        tableHelper.configurarTabla(tableCategorias, categoriasData, this::onSeleccionCambiada);

        // Configurar menú contextual
        tableHelper.configurarMenuContextual(tableCategorias, this::crearMenuContextual);
    }

    /**
     * Crea menú contextual con opciones según permisos.
     */
    private ContextMenu crearMenuContextual(Categoria categoria) {
        ContextMenu menu = new ContextMenu();

        if (buttonService.puede(perfilUsuario, MODULO, "editar")) {
            MenuItem editar = new MenuItem(MENU_EDITAR);
            editar.setOnAction(e -> iniciarEdicion(categoria));
            menu.getItems().add(editar);

            MenuItem cambiar = new MenuItem(MENU_INACTIVAR_ACTIVAR);
            cambiar.setOnAction(e -> cambiarEstado(categoria));
            menu.getItems().add(cambiar);
        }

        if (buttonService.puede(perfilUsuario, MODULO, "eliminar")) {
            MenuItem eliminar = new MenuItem(MENU_ELIMINAR);
            eliminar.setOnAction(e -> eliminar(categoria));
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
        new AutocompleteHelper(txtBusqueda, this::sugerirCategorias).configure();

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
            cargarCategorias();
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
            cargarCategorias();
        });

        sortHelper
            .addOption("ID Ascendente", "idCategoria", Sort.Direction.ASC, false)
            .addOption("ID Descendente", "idCategoria", Sort.Direction.DESC, false)
            .addOption("Nombre A-Z", "nombreCategoria", Sort.Direction.ASC, true)
            .addOption("Nombre Z-A", "nombreCategoria", Sort.Direction.DESC, true)
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
                cargarCategorias();
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
            if (tableCategorias.getScene() == null) {
                log.warn("Scene null, no se puede configurar click fuera");
                return;
            }

            Node root = tableCategorias.getScene().getRoot();

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
                if (!tableCategorias.contains(tableCategorias.screenToLocal(event.getScreenX(), event.getScreenY()))) {
                    tableCategorias.getSelectionModel().clearSelection();
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
        if (tableCategorias == null || tableCategorias.getScene() == null) {
            log.warn("No se pueden configurar atajos: tabla o scene null");
            return;
        }

        new KeybindHelper(tableCategorias.getScene())
            .onEscapeCrud(txtPagina, tableCategorias,
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
        cargarCategorias();
    }

    private List<String> sugerirCategorias(String term) {
        String filtro = term == null ? "" : term.trim();
        if (filtro.length() < 2) {
            return Collections.emptyList();
        }

        Boolean estadoFiltro = filterStateHelper != null ? filterStateHelper.getCurrentState() : null;
        Pageable pageable = PageRequest.of(0, 8, Sort.by(Sort.Order.asc("nombreCategoria").ignoreCase()));

        try {
            Page<Categoria> page = categoriaService.buscarPaginado(filtro, estadoFiltro, pageable);
            return page.getContent().stream()
                    .map(Categoria::getNombreCategoria)
                    .filter(Objects::nonNull)
                    .filter(nombre -> AutocompleteHelper.matchesWordStart(nombre, filtro))
                    .distinct()
                    .toList();
        } catch (Exception ex) {
            log.warn("No se pudo cargar sugerencias de categorías para '{}'", filtro, ex);
            return Collections.emptyList();
        }
    }

    private void limpiar() {
        txtBusqueda.clear();
        filterStateHelper.reset();
        sortHelper.reset();
        tableCategorias.getSelectionModel().clearSelection();
        tableCategorias.requestFocus(); // Quitar focus de campos antes de reset (evita re-trigger validadores)
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
     * <p>
     * Usado después de guardar para volver a estado SIN_SELECCION limpio.
     */
    private void limpiarCompletamente() {
        // Forzar pérdida de focus antes de limpiar
        tableCategorias.requestFocus();

        // Limpiar formulario y validadores
        resetearEdicion();
        deshabilitarCamposFormulario();

        // Limpiar búsqueda
        txtBusqueda.clear();
        busquedaActual = "";

        // Resetear filtros (sin buscar, se hace después al cargarCategorias)
        filterStateHelper.reset();
        sortHelper.reset();
        paginaActual = 0;

        // Deseleccionar tabla
        tableCategorias.getSelectionModel().clearSelection();

        // Desbloquear controles de búsqueda
        desbloquearEdicion();

        log.info("Form limpiado completamente después de guardar");
    }

    /**
     * Carga categorias paginadas desde BD sin bloquear la UI.
     * Muestra ProgressIndicator mientras se carga, actualiza tabla al terminar.
     */
    private void cargarCategorias() {
        Task<Page<Categoria>> task = new Task<>() {
            @Override
            protected Page<Categoria> call() {
                return obtenerPaginaCategorias();
            }
        };

        ejecutarOperacionAsync("categorias-cargar", task,
            page -> {
                actualizarDatosPaginacion(page);
                actualizarBotones();
                log.debug("Cargadas {} categorias, página {}/{}", page.getNumberOfElements(), paginaActual + 1, totalPaginas);
            },
            ex -> {
                actualizarBotones();
                log.error("Error al cargar categorias", ex);
                notificarError("Error al cargar categorias: " + ex.getMessage());
            }
        );
    }

    private void reSeleccionarCategoria(Long categoriaId, boolean mantenerPaginacionBloqueada) {
        if (categoriaId == null) {
            return;
        }

        tableCategorias.getItems().stream()
            .filter(m -> m.getIdCategoria().equals(categoriaId))
            .findFirst()
            .ifPresent(m -> {
                tableCategorias.getSelectionModel().select(m);
                if (mantenerPaginacionBloqueada) {
                    bloquearPaginacion();
                }
            });
    }

    private Page<Categoria> obtenerPaginaCategorias() {
        Boolean estadoFiltro = filterStateHelper.getCurrentState();
        return busquedaActual.isEmpty() ?
            categoriaService.listar(estadoFiltro, crearPageable()) :
            categoriaService.buscarPaginado(busquedaActual, estadoFiltro, crearPageable());
    }

    private void actualizarDatosPaginacion(Page<Categoria> page) {
        // Si página actual quedó vacía pero hay datos en páginas anteriores, retroceder
        if (page.isEmpty() && paginaActual > 0 && page.getTotalElements() > 0) {
            paginaActual--;
            cargarCategorias(); // Recargar página anterior
            return;
        }

        categoriasData.clear();
        categoriasData.addAll(page.getContent());
        totalPaginas = page.getTotalPages();
        totalRegistros = page.getTotalElements();
        actualizarPaginacion();
    }

    private Pageable crearPageable() {
        Sort.Order order = sortHelper.createOrder();
        return PageRequest.of(paginaActual, REGISTROS_POR_PAGINA, Sort.by(order));
    }

    private void actualizarPaginacion() {
        lblTotalCategorias.setText("Total: " + totalRegistros);
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
        tableCategorias.getSelectionModel().clearSelection();
        resetearEdicion();

        // Mostrar próximo ID disponible
        Long proximoId = categoriaService.obtenerProximoId();
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
        Categoria categoria = obtenerSeleccionado();
        if (categoria == null) {
            notificarWarning(MSG_SELECCIONAR);
            return;
        }
        iniciarEdicion(categoria);
    }

    /**
     * Inicia edición desde cualquier origen (click tabla o menú contextual).
     * Centraliza la lógica para garantizar consistencia.
     */
    private void iniciarEdicion(Categoria categoria) {
        if (categoria == null) return;

        cargarCategoria(categoria);
        habilitarCamposFormulario();
        bloquearEdicion();  // Bloquea buscador, filtros, paginación
        actualizarBotones();
        txtNombre.requestFocus();
        log.info("Edición iniciada - Categoria ID: {}", categoria.getIdCategoria());
    }

    private Categoria obtenerSeleccionado() {
        return tableCategorias.getSelectionModel().getSelectedItem();
    }

    /**
     * Maneja cambio de selección en tabla.
     * Si está editando, sale del modo edición y vuelve a PREVIEW.
     * Luego actualiza formulario con datos del ítem seleccionado.
     *
     * @param categoria Categoria seleccionada, o null si se deseleccionó
     */
    private void onSeleccionCambiada(Categoria categoria) {
        if (estaFormularioEditable()) {
            resetearEdicion();
            deshabilitarCamposFormulario();
            desbloquearEdicion();  // Desbloquea paginación, búsqueda, filtros
        }
        cargarPreview(categoria);
        actualizarBotones();
    }

    /**
     * Obtiene Stage actual de la Scene.
     * NO cachear - Stage puede cambiar después de re-login.
     *
     * @return Stage owner, o null si Scene no disponible
     */
    private Stage obtenerStage() {
        if (tableCategorias != null && tableCategorias.getScene() != null) {
            return (Stage) tableCategorias.getScene().getWindow();
        }
        return null;
    }

    /**
     * Carga datos de categoria en formulario para visualización (PREVIEW).
     * Campos permanecen disabled, solo lectura.
     */
    private void cargarPreview(Categoria categoria) {
        if (categoria == null) {
            limpiarFormulario();
            return;
        }

        cargarDatosFormulario(categoria);
        seleccionarCategoriaPadreEnCombo(categoria.getCategoriaPadre());
    }

    /**
     * Carga datos de categoria para edición.
     * Prepara categoriaEnEdicion y valores originales.
     */
    private void cargarCategoria(Categoria categoria) {
        if (categoria == null) return;

        categoriaEnEdicion = categoria.getIdCategoria();
        cargarDatosFormulario(categoria);

        cargarComboCategoriasPadre();
        seleccionarCategoriaPadreEnCombo(categoria.getCategoriaPadre());
        deshabilitarItemsCirculares();

        guardarValoresOriginales();
    }

    /**
     * Carga datos básicos de categoria en los controles del formulario.
     * Método común reutilizado por cargarPreview() y cargarCategoria().
     */
    private void cargarDatosFormulario(Categoria categoria) {
        lblId.setText(String.valueOf(categoria.getIdCategoria()));
        txtNombre.setText(categoria.getNombreCategoria());
        txtDescripcion.setText(Objects.requireNonNullElse(categoria.getDescripcionCategoria(), ""));
        chkEstado.setSelected(Objects.requireNonNullElse(categoria.getEstadoCategoria(), true));
    }

    /**
     * Selecciona categoría padre en el ComboBox.
     * Método auxiliar reutilizable para PREVIEW y EDICION.
     *
     * @param categoriaPadre Categoria padre a seleccionar, o null si no tiene padre
     */
    private void seleccionarCategoriaPadreEnCombo(Categoria categoriaPadre) {
        if (cbxCategoriaPadre == null || cbxCategoriaPadre.getItems() == null) {
            return;
        }

        if (categoriaPadre != null) {
            // Buscar y seleccionar el padre en el ComboBox
            cbxCategoriaPadre.getItems().stream()
                .filter(cat -> cat != null && cat.getIdCategoria().equals(categoriaPadre.getIdCategoria()))
                .findFirst()
                .ifPresent(cat -> cbxCategoriaPadre.getSelectionModel().select(cat));
        } else {
            // Sin padre: seleccionar el primer item (null = "Sin padre")
            cbxCategoriaPadre.getSelectionModel().selectFirst();
        }
    }

    /**
     * Filtra ComboBox padre para prevenir referencias circulares.
     * - La categoría misma NO aparece como opción
     * - Las categorías hijas NO aparecen (evita ciclos indirectos)
     * <p>
     * Llamar solo en MODO EDICION después de cargar datos.
     * Restaurar con cargarComboCategoriasPadre() al salir de edición.
     */
    private void deshabilitarItemsCirculares() {
        if (cbxCategoriaPadre == null || categoriaEnEdicion == null) return;

        Long idSeleccionadoOriginal = obtenerIdCategoriaPadreSeleccionada();
        Set<Long> idsAExcluir = construirIdsAExcluir();

        cbxCategoriaPadre.setItems(filtrarCategoriasValidas(idsAExcluir));
        restaurarSeleccionPadre(idSeleccionadoOriginal);

        log.debug("Filtrados {} items circulares", idsAExcluir.size());
    }

    private Long obtenerIdCategoriaPadreSeleccionada() {
        Categoria seleccion = cbxCategoriaPadre.getSelectionModel().getSelectedItem();
        return seleccion != null ? seleccion.getIdCategoria() : null;
    }

    private Set<Long> construirIdsAExcluir() {
        Set<Long> ids = new HashSet<>();
        ids.add(categoriaEnEdicion);
        categoriaService.obtenerCategoriasHijas(categoriaEnEdicion)
            .forEach(h -> ids.add(h.getIdCategoria()));
        return ids;
    }

    private ObservableList<Categoria> filtrarCategoriasValidas(Set<Long> idsAExcluir) {
        ObservableList<Categoria> filtradas = FXCollections.observableArrayList();
        filtradas.add(null); // "(Sin padre)"

        cbxCategoriaPadre.getItems().stream()
            .filter(cat -> cat != null && !idsAExcluir.contains(cat.getIdCategoria()))
            .forEach(filtradas::add);

        return filtradas;
    }

    private void restaurarSeleccionPadre(Long idOriginal) {
        if (idOriginal == null) {
            cbxCategoriaPadre.getSelectionModel().selectFirst();
            return;
        }

        cbxCategoriaPadre.getItems().stream()
            .filter(cat -> cat != null && cat.getIdCategoria().equals(idOriginal))
            .findFirst()
            .ifPresentOrElse(
                cat -> cbxCategoriaPadre.getSelectionModel().select(cat),
                () -> cbxCategoriaPadre.getSelectionModel().selectFirst()
            );
    }

    /**
     * Configura el cellFactory normal del ComboBox padre.
     * Muestra el nombre de la categoría o "(Sin padre)" si es null.
     * Usa crearCeldaCategoriaPadre() para evitar duplicación (DRY).
     */
    private void configurarCellFactoryNormal() {
        if (cbxCategoriaPadre == null) {
            return;
        }

        cbxCategoriaPadre.setCellFactory(param -> crearCeldaCategoriaPadre());
        cbxCategoriaPadre.setButtonCell(crearCeldaCategoriaPadre());
    }

    /**
     * Crea una ListCell para el ComboBox de categoría padre.
     * Muestra "(Sin padre)" si es null, o el nombre de la categoría.
     * Método factory reutilizable (DRY).
     *
     * @return Nueva instancia de ListCell configurada
     */
    private ListCell<Categoria> crearCeldaCategoriaPadre() {
        return new ListCell<>() {
            @Override
            protected void updateItem(Categoria item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setText(null);
                } else if (item == null) {
                    setText("(Sin padre)");
                } else {
                    setText(item.getNombreCategoria());
                }
            }
        };
    }

    private void guardarValoresOriginales() {
        nombreOriginal = obtenerTexto(txtNombre);
        descripcionOriginal = obtenerTexto(txtDescripcion);
        estadoOriginal = chkEstado.isSelected();

        // Guardar ID del padre original
        Categoria categoriaPadreOriginal = cbxCategoriaPadre != null ?
            cbxCategoriaPadre.getSelectionModel().getSelectedItem() : null;
        categoriaPadreOriginalId = categoriaPadreOriginal != null ? categoriaPadreOriginal.getIdCategoria() : null;
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
        Categoria categoria = obtenerSeleccionado();
        if (categoria == null) {
            notificarWarning(MSG_SELECCIONAR);
            return;
        }
        cambiarEstado(categoria);
    }

    /**
     * Cambia el estado de una categoria (activar/inactivar).
     * Si tiene subcategorías y/o productos, muestra diálogo de cascada con información detallada.
     */
    private void cambiarEstado(Categoria categoria) {
        boolean activar = !categoria.getEstadoCategoria();
        Long categoriaId = categoria.getIdCategoria();

        // Contar subcategorías y productos TOTALES (incluyendo hijas)
        long subcategoriasActivas = categoriaService.contarCategoriasHijasActivas(categoriaId);
        long subcategoriasInactivas = categoriaService.contarCategoriasHijasInactivas(categoriaId);
        long productosActivosTotales = categoriaService.contarProductosActivosTotales(categoriaId);
        long productosInactivosTotales = categoriaService.contarProductosInactivosTotales(categoriaId);

        DialogHelper.OpcionCascada opcion;

        if (!activar && (subcategoriasActivas > 0 || productosActivosTotales > 0)) {
            // INACTIVAR con subcategorías activas y/o productos activos → Mostrar diálogo cascada
            opcion = DialogHelper.showCascadaCategoriaInactivar(obtenerStage(), subcategoriasActivas, productosActivosTotales);
        } else if (activar && (subcategoriasInactivas > 0 || productosInactivosTotales > 0)) {
            // ACTIVAR con subcategorías inactivas y/o productos inactivos → Mostrar diálogo cascada
            opcion = DialogHelper.showCascadaCategoriaActivar(obtenerStage(), subcategoriasInactivas, productosInactivosTotales);
        } else {
            // Sin subcategorías ni productos afectados → Cambio directo sin diálogo
            opcion = DialogHelper.OpcionCascada.SOLO_ENTIDAD;
        }

        // Procesar según opción elegida
        if (opcion == DialogHelper.OpcionCascada.CANCELAR) {
            return; // Usuario canceló
        }

        Task<Page<Categoria>> task = new Task<>() {
            @Override
            protected Page<Categoria> call() {
                if (opcion == DialogHelper.OpcionCascada.CASCADA_COMPLETA) {
                    // Cambiar categoria + subcategorías + productos
                    if (activar) {
                        categoriaService.activarCategoriaYProductos(categoriaId);
                    } else {
                        categoriaService.inactivarCategoriaYProductos(categoriaId);
                    }
                } else {
                    // Solo cambiar esta categoria (sin afectar hijas ni productos)
                    Categoria entidad = categoriaService.findById(categoriaId)
                        .orElseThrow(() -> new IllegalStateException(MSG_NO_ENCONTRADA));
                    entidad.setEstadoCategoria(activar);
                    categoriaService.save(entidad);
                }
                return obtenerPaginaCategorias();
            }
        };

        ejecutarOperacionAsync("categorias-cambiar-estado", task,
            page -> {
                actualizarDatosPaginacion(page);
                cargarComboCategoriasPadre();
                actualizarBotones();
                boolean estabaEditando = categoriaEnEdicion != null && categoriaEnEdicion.equals(categoriaId);
                reSeleccionarCategoria(categoriaId, estabaEditando);
                if (estabaEditando) {
                    chkEstado.setSelected(activar);
                }

                // Toast informativo con detalles de cascada
                String mensaje = construirMensajeCambioEstado(activar, opcion,
                        activar ? subcategoriasInactivas : subcategoriasActivas,
                        activar ? productosInactivosTotales : productosActivosTotales);
                notificarInfo(mensaje);
                log.info("Categoria {} - ID: {}, opción: {}", mensaje, categoriaId, opcion);
            },
            ex -> {
                actualizarBotones();
                log.error("Error al cambiar estado", ex);
                notificarError("Error: " + ex.getMessage());
            }
        );
    }

    /**
     * Construye mensaje para Toast después de cambiar estado.
     */
    private String construirMensajeCambioEstado(boolean activar, DialogHelper.OpcionCascada opcion,
                                                 long subcategorias, long productos) {
        String mensaje = activar ? MSG_ACTIVADA : MSG_INACTIVADA;

        if (opcion == DialogHelper.OpcionCascada.CASCADA_COMPLETA) {
            List<String> detalles = new ArrayList<>();
            if (subcategorias > 0) {
                detalles.add(subcategorias + " subcategoría(s)");
            }
            if (productos > 0) {
                detalles.add(productos + " producto(s)");
            }
            if (!detalles.isEmpty()) {
                mensaje += " con " + String.join(" + ", detalles) + " en cascada";
            }
        }

        return mensaje;
    }

    private void eliminarSeleccionado() {
        Categoria categoria = obtenerSeleccionado();
        if (categoria == null) {
            notificarWarning(MSG_SELECCIONAR);
            return;
        }
        eliminar(categoria);
    }

    /**
     * Elimina una categoria después de confirmar y validar dependencias.
     * Usa DialogHelper.showConfirmarEliminacionConDependencias() para mensaje genérico.
     * Recarga tabla automáticamente si la eliminación es exitosa.
     */
    private void eliminar(Categoria categoria) {
        long categoriasHijas = categoriaService.contarCategoriasHijas(categoria.getIdCategoria());
        long productosActivos = categoriaService.contarProductosActivosPorCategoria(categoria.getIdCategoria());
        long productosInactivos = categoriaService.contarProductosInactivosPorCategoria(categoria.getIdCategoria());

        // Usar DialogHelper genérico para confirmación con dependencias
        if (!DialogHelper.showConfirmarEliminacionConDependencias(
                obtenerStage(),
                "Categoría",
                categoria.getNombreCategoria(),
                categoriasHijas,
                productosActivos,
                productosInactivos)) {
            return;
        }

        Task<Void> task = new Task<>() {
            @Override
            protected Void call() {
                categoriaService.eliminarCategoriaConCascada(categoria.getIdCategoria());
                return null;
            }
        };

        ejecutarOperacionAsync("categorias-eliminar", task,
            aVoid -> {
                resetearEdicion();
                deshabilitarCamposFormulario();
                tableCategorias.getSelectionModel().clearSelection();
                cargarCategorias();
                cargarComboCategoriasPadre();
                actualizarBotones();

                String toastMsg = construirMensajeEliminacion(categoriasHijas, productosActivos, productosInactivos);
                notificarSuccess(toastMsg);

                log.info("Eliminada - ID: {}, Nombre: {}, Hijas: {}, Productos: {} ({} activos, {} inactivos)",
                        categoria.getIdCategoria(), categoria.getNombreCategoria(),
                        categoriasHijas, productosActivos + productosInactivos, productosActivos, productosInactivos);
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

    private String construirMensajeEliminacion(long hijas, long productosActivos, long productosInactivos) {
        long totalProductos = productosActivos + productosInactivos;

        if (hijas == 0 && totalProductos == 0) {
            return "Categoría eliminada";
        }

        List<String> partes = new ArrayList<>();
        if (hijas > 0) {
            partes.add(hijas + " subcategorías");
        }
        if (totalProductos > 0) {
            partes.add(String.format("%d productos (%d activos, %d inactivos)",
                totalProductos, productosActivos, productosInactivos));
        }

        return "Categoría eliminada (" + String.join(" + ", partes) + ")";
    }

    /**
     * Guarda una nueva categoria o actualiza una existente.
     * Ejecuta validación backend antes de persistir en BD.
     * Mantiene UI responsiva usando Task con callbacks.
     */
    private void guardar() {
        if (!validar()) return;

        DatosGuardado datos = extraerDatosGuardado();
        InfoCascada cascada = determinarCascada(datos);

        if (cascada.cancelado) return;

        ejecutarGuardadoAsync(datos, cascada);
    }

    private DatosGuardado extraerDatosGuardado() {
        return new DatosGuardado(
            obtenerTexto(txtNombre),
            obtenerTexto(txtDescripcion),
            chkEstado.isSelected()
        );
    }

    private InfoCascada determinarCascada(DatosGuardado datos) {
        if (categoriaEnEdicion == null || estadoOriginal == datos.estado) {
            return new InfoCascada(null, 0, false);
        }

        Long categoriaId = categoriaEnEdicion;
        long productosActivos = categoriaService.contarProductosActivosPorCategoria(categoriaId);
        long productosInactivos = categoriaService.contarProductosInactivosPorCategoria(categoriaId);

        DialogHelper.OpcionCascada opcion;
        long cantidad;

        if (!datos.estado && productosActivos > 0) {
            opcion = DialogHelper.showCascadaInactivar(obtenerStage(), "Categoria", productosActivos);
            cantidad = productosActivos;
        } else if (datos.estado && productosInactivos > 0) {
            opcion = DialogHelper.showCascadaActivar(obtenerStage(), "Categoria", productosInactivos);
            cantidad = productosInactivos;
        } else {
            opcion = DialogHelper.OpcionCascada.SOLO_ENTIDAD;
            cantidad = 0;
        }

        boolean cancelado = opcion == DialogHelper.OpcionCascada.CANCELAR;
        return new InfoCascada(opcion, cantidad, cancelado);
    }

    private void ejecutarGuardadoAsync(DatosGuardado datos, InfoCascada cascada) {
        final DialogHelper.OpcionCascada opcionCascada = cascada.opcion;
        final long cantidadProductosAfectados = cascada.cantidad;

        Task<Long> task = new Task<>() {
            @Override
            protected Long call() {
                Categoria categoria;

                if (categoriaEnEdicion != null) {
                    if (opcionCascada == DialogHelper.OpcionCascada.CASCADA_COMPLETA) {
                        if (datos.estado) {
                            categoriaService.activarCategoriaYProductos(categoriaEnEdicion);
                        } else {
                            categoriaService.inactivarCategoriaYProductos(categoriaEnEdicion);
                        }
                    }

                    categoria = categoriaService.findById(categoriaEnEdicion)
                        .orElseThrow(() -> new IllegalStateException(MSG_NO_ENCONTRADA));
                    aplicarDatosFormulario(categoria, datos.nombre, datos.descripcion, datos.estado);
                } else {
                    Categoria categoriaPadre = cbxCategoriaPadre != null ?
                        cbxCategoriaPadre.getSelectionModel().getSelectedItem() : null;

                    categoria = Categoria.builder()
                        .nombreCategoria(datos.nombre)
                        .descripcionCategoria(datos.descripcion.isEmpty() ? null : datos.descripcion)
                        .estadoCategoria(datos.estado)
                        .categoriaPadre(categoriaPadre)
                        .build();
                }

                categoriaService.validarCategoria(categoria);
                categoriaService.save(categoria);

                return categoria.getIdCategoria();
            }
        };

        ejecutarOperacionAsync("categorias-guardar", task,
            ignoredId -> onGuardadoExitoso(datos, opcionCascada, cantidadProductosAfectados),
            this::onGuardadoFallido
        );
    }

    private void onGuardadoExitoso(DatosGuardado datos, DialogHelper.OpcionCascada opcion, long cantidad) {
        notificarSuccess(MSG_GUARDADO);

        if (opcion == DialogHelper.OpcionCascada.CASCADA_COMPLETA) {
            String accion = datos.estado ? "activada" : "inactivada";
            notificarInfo("Categoria " + accion + " con " + cantidad + " producto(s) en cascada");
        }

        limpiarCompletamente();
        cargarCategorias();
        cargarComboCategoriasPadre();
        actualizarBotones();
    }

    private void onGuardadoFallido(Throwable ex) {
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

    private void aplicarDatosFormulario(Categoria categoria, String nombre, String descripcion, boolean estado) {
        categoria.setNombreCategoria(nombre);
        categoria.setDescripcionCategoria(descripcion.isEmpty() ? null : descripcion);
        categoria.setEstadoCategoria(estado);

        // Guardar categoría padre seleccionada en el ComboBox
        if (cbxCategoriaPadre != null) {
            Categoria categoriaPadre = cbxCategoriaPadre.getSelectionModel().getSelectedItem();
            categoria.setCategoriaPadre(categoriaPadre);
        }
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
        Long idParaReseleccionar = categoriaEnEdicion;

        // Forzar pérdida de focus de campos del formulario antes de limpiar
        // (para que validadores actualicen estado visual correctamente)
        tableCategorias.requestFocus();

        resetearEdicion();
        deshabilitarCamposFormulario();
        desbloquearEdicion();  // Desbloquea buscador, filtros, paginación

        if (idParaReseleccionar != null) {
            // Volver a PREVIEW: re-seleccionar el item que estábamos editando
            reSeleccionarCategoria(idParaReseleccionar, false);
            // cargarPreview se llamará automáticamente por el listener de tabla
        } else {
            // Era NUEVO: ir a SIN_SELECCION
            tableCategorias.getSelectionModel().clearSelection();
        }

        actualizarBotones();
        actualizarPaginacion();
    }

    private boolean formularioTieneCambios() {
        if (categoriaEnEdicion == null) {
            // Para nuevo registro: verificar si hay datos no guardados
            return !obtenerTexto(txtNombre).isEmpty() || !obtenerTexto(txtDescripcion).isEmpty();
        }

        // Para edición: comparar con originales (normalizados con trim)
        String nombreActual = obtenerTexto(txtNombre);
        String descripcionActual = obtenerTexto(txtDescripcion);
        boolean estadoActual = chkEstado.isSelected();

        // Comparar categoría padre seleccionada
        Categoria categoriaPadreActual = cbxCategoriaPadre != null ?
            cbxCategoriaPadre.getSelectionModel().getSelectedItem() : null;
        Long categoriaPadreActualId = categoriaPadreActual != null ? categoriaPadreActual.getIdCategoria() : null;

        return !Objects.equals(nombreActual, Objects.requireNonNullElse(nombreOriginal, "")) ||
               !Objects.equals(descripcionActual, Objects.requireNonNullElse(descripcionOriginal, "")) ||
               estadoActual != estadoOriginal ||
               !Objects.equals(categoriaPadreActualId, categoriaPadreOriginalId);
    }

    private String obtenerTexto(TextInputControl control) {
        String valor = control.getText();
        return valor == null ? "" : valor.trim();
    }

    private void resetearEdicion() {
        limpiarFormulario();
        categoriaEnEdicion = null;
    }

    private void limpiarFormulario() {
        lblId.setText(ID_SIN_SELECCION);
        txtNombre.clear();
        txtDescripcion.clear();
        cbxCategoriaPadre.getSelectionModel().selectFirst(); // Seleccionar "(Sin padre)"
        chkEstado.setSelected(true);
        lblMensajeForm.setText("");

        // El ComboBox se restaura automáticamente con cargarComboCategoriasPadre() después de guardar/cancelar

        // Resetear estado visual de validadores
        if (nombreValidator != null) nombreValidator.reset();
        if (descripcionValidator != null) descripcionValidator.reset();
    }

    private void deshabilitarCamposFormulario() {
        txtNombre.setDisable(true);
        txtDescripcion.setDisable(true);
        cbxCategoriaPadre.setDisable(true);
        chkEstado.setDisable(true);
        UIHelper.disable(btnGuardar, btnCancelar);
    }

    private void habilitarCamposFormulario() {
        txtNombre.setDisable(false);
        txtDescripcion.setDisable(false);
        cbxCategoriaPadre.setDisable(false);
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
        tableCategorias.setDisable(true);
    }

    /**
     * Re-habilita botones de operación después de completar Task.
     * actualizarBotones() ajustará estado final según contexto.
     */
    private void habilitarAccionesOperacion() {
        operacionEnCurso = false;
        UIHelper.enable(btnLimpiar);
        tableCategorias.setDisable(false);

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
            if (categoriaService.existsByNombreCategoria(nombre)) {
                mostrarErrorValidacion(txtNombre, MSG_NOMBRE_EXISTE);
                return false;
            }
        }
        // Validación para EDICIÓN (ignora el propio ID)
        else {
            if (categoriaService.existsByNombreCategoriaAndNotId(nombre, categoriaEnEdicion)) {
                mostrarErrorValidacion(txtNombre, MSG_NOMBRE_EXISTE);
                return false;
            }
        }

        return true;
    }

    private boolean esNuevoRegistro() {
        return categoriaEnEdicion == null;
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

    // ============ COMBO BOX PADRE ============

    /**
     * Carga el ComboBox de categoría padre con todas las categorías.
     * Configura rendering para mostrar nombre de categoría.
     * Justificación: Permite seleccionar categoría padre durante edición/nuevo.
     */
    private void cargarComboCategoriasPadre() {
        if (cbxCategoriaPadre == null) {
            log.warn("cbxCategoriaPadre es null, no se puede cargar");
            return;
        }

        try {
            // Cargar solo categorías ACTIVAS para el combo (evita asignar padres inactivos)
            Page<Categoria> page = categoriaService.listar(true, PageRequest.of(0, Integer.MAX_VALUE));
            ObservableList<Categoria> categorias = FXCollections.observableArrayList();
            categorias.add(null); // Opción vacía para "Sin padre"
            categorias.addAll(page.getContent());

            cbxCategoriaPadre.setItems(categorias);

            // Configurar rendering del combo (reutiliza método auxiliar)
            configurarCellFactoryNormal();

            log.debug("ComboBox de categoría padre cargado con {} categorías", categorias.size());
        } catch (Exception e) {
            log.error("Error al cargar categorías padre", e);
            notificarError("Error al cargar categorías padre: " + e.getMessage());
        }
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
