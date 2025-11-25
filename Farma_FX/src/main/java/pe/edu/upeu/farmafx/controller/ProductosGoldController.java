package pe.edu.upeu.farmafx.controller;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import pe.edu.upeu.farmafx.components.*;
import pe.edu.upeu.farmafx.dto.ButtonDto;
import pe.edu.upeu.farmafx.model.Categoria;
import pe.edu.upeu.farmafx.model.Marca;
import pe.edu.upeu.farmafx.model.Producto;
import pe.edu.upeu.farmafx.model.UnidadMedida;
import pe.edu.upeu.farmafx.service.*;
import pe.edu.upeu.farmafx.utils.SessionManager;
import pe.edu.upeu.farmafx.utils.UtilsX;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import java.util.function.Consumer;

/**
 * Controller GOLD para gestión de Productos.
 * Implementa CRUD completo con:
 * - Código autogenerado (PROD-XXXX)
 * - 3 ComboBox FK (Marca, Categoría, UnidadMedida)
 * - Validaciones numéricas (BigDecimal, Integer)
 * - Sin cascada (entidad hoja)
 *
 * @version 1.0
 * @since 2025-11-23
 */
@Controller
@RequiredArgsConstructor
@Slf4j
public class ProductosGoldController {

    // ========== CONSTANTES ==========
    private static final int REGISTROS_POR_PAGINA = 10;
    private static final String MODULO = "PRODUCTOS";

    // Mensajes de usuario
    private static final String MSG_SELECCIONAR = "Seleccione un producto";
    private static final String MSG_GUARDADO = "Guardado correctamente";
    private static final String MSG_ELIMINADO = "Producto eliminado";
    private static final String MSG_NO_ENCONTRADO = "Producto no encontrado";

    // Validaciones
    private static final String MSG_CODIGO_OBLIGATORIO = "Código obligatorio";
    private static final String MSG_NOMBRE_OBLIGATORIO = "Nombre obligatorio";
    private static final String MSG_MINIMO_CARACTERES = "Mínimo 3 caracteres";
    private static final String MSG_MARCA_OBLIGATORIA = "Debe seleccionar una marca";
    private static final String MSG_UNIDAD_OBLIGATORIA = "Debe seleccionar una unidad de medida";
    private static final String MSG_PRECIO_INVALIDO = "Precio inválido (debe ser número >= 0)";
    private static final String MSG_STOCK_INVALIDO = "Stock inválido (debe ser número entero >= 0)";

    // Menú contextual
    private static final String MENU_EDITAR = "Editar";
    private static final String MENU_INACTIVAR_ACTIVAR = "Inactivar/Activar";
    private static final String MENU_ELIMINAR = "Eliminar";

    // Mensajes de estado
    private static final String MSG_ACTIVADO = "Producto activado";
    private static final String MSG_INACTIVADO = "Producto inactivado";

    // Límites de validación
    private static final int MAX_NOMBRE_CARACTERES = 120;
    private static final int MAX_DESCRIPCION_CARACTERES = 200;

    // Diálogos
    private static final String DIALOG_CAMBIOS_TITULO = "Cambios sin guardar";
    private static final String DIALOG_CAMBIOS_MENSAJE = "¿Descartar los cambios realizados?";

    // UI
    private static final String ID_SIN_SELECCION = "—";

    // ========== DEPENDENCIAS (Inyectadas por Constructor) ==========
    private final IProductoService productoService;
    private final IMarcaService marcaService;
    private final ICategoriaService categoriaService;
    private final IUnidadMedidaService unidadMedidaService;
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

    // Helpers para ComboBox de entidades FK
    private EntityComboBoxHelper<Marca> marcaHelper;
    private EntityComboBoxHelper<Categoria> categoriaHelper;
    private EntityComboBoxHelper<UnidadMedida> unidadMedidaHelper;

    // ========== COMPONENTES FXML ==========
    @FXML private HBox searchContainer;
    @FXML private TextField txtBusqueda;
    @FXML private ComboBox<String> cmbEstadoFiltro, cmbOrdenamiento;
    @FXML private Button btnLimpiar, btnLimpiarBusqueda, btnNuevo, btnEditar, btnInactivar, btnEliminar;
    @FXML private Label lblMensajeForm;
    @FXML private Label lblId;
    @FXML private Label lblCodigo;  // Label inmutable (autogenerado)
    @FXML private TextField txtNombre;
    @FXML private TextField txtPrecioCompra, txtPrecioVenta;
    @FXML private TextField txtStockActual, txtStockMinimo;
    @FXML private TextArea txtDescripcion;
    @FXML private ComboBox<String> cbxMarca, cbxCategoria, cbxUnidadMedida;
    @FXML private CheckBox chkEstado;
    @FXML private Button btnGuardar, btnCancelar;
    @FXML private ProgressIndicator piProgreso;
    @FXML private TableView<Producto> tableProductos;
    @FXML private TableColumn<Producto, Long> colId;
    @FXML private TableColumn<Producto, String> colCodigo, colNombre, colMarca, colCategoria;
    @FXML private TableColumn<Producto, BigDecimal> colPrecioCompra, colPrecioVenta;
    @FXML private TableColumn<Producto, Integer> colStock;
    @FXML private TableColumn<Producto, Boolean> colEstado;
    @FXML private Label lblTotalProductos;
    @FXML private Label lblContadorNombre, lblContadorDesc;
    // Paginación
    @FXML private TextField txtPagina;
    @FXML private Label lblTotalPaginas;
    @FXML private Button btnPrimera, btnAnterior, btnSiguiente, btnUltima;

    // ========== ESTADO ==========
    private final ObservableList<Producto> productosData = FXCollections.observableArrayList();
    private int paginaActual = 0, totalPaginas = 0;
    private long totalRegistros = 0L;
    private String busquedaActual = "", perfilUsuario = "";
    private Long productoEnEdicion = null;
    private boolean operacionEnCurso = false;

    // Para detectar cambios sin guardar (código es inmutable, no se trackea)
    private String nombreOriginal = "", descripcionOriginal = "";
    private BigDecimal precioCompraOriginal = BigDecimal.ZERO, precioVentaOriginal = BigDecimal.ZERO;
    private Integer stockActualOriginal = 0, stockMinimoOriginal = 0;
    private boolean estadoOriginal = true;
    private Marca marcaOriginal = null;
    private Categoria categoriaOriginal = null;
    private UnidadMedida unidadMedidaOriginal = null;

    // ========== INICIALIZACIÓN ==========

    @FXML
    public void initialize() {
        log.info("Inicializando ProductosGoldController");
        Platform.runLater(this::inicializarDatos);
    }

    private void inicializarDatos() {
        obtenerStage();

        perfilUsuario = Objects.requireNonNullElse(sessionManager.getUserPerfil(), "");
        log.info("Perfil: {}", perfilUsuario.isEmpty() ? "desconocido" : perfilUsuario);

        // 1. Configurar componentes primero
        configurarVisibilidad();
        configurarTabla();
        configurarComboBoxes();  // Debe ir antes de limpiarFormulario
        configurarEventos();
        configurarBuscadorConX();
        configurarFiltroEstado();
        configurarFiltroOrdenamiento();
        configurarPaginacion();
        configurarContadoresCaracteres();
        configurarValidadores();
        configurarClickFueraTabla();
        configurarAtajosTeclado();

        // 2. Limpiar y deshabilitar después de configurar
        limpiarFormulario();
        deshabilitarCamposFormulario();

        // 3. Cargar datos
        cargarProductos();

        // Estado inicial
        btnNuevo.setDisable(false);
        btnEditar.setDisable(true);
        btnInactivar.setDisable(true);
        btnEliminar.setDisable(true);
    }

    // ========== CONFIGURACIÓN DE VISIBILIDAD ==========

    private void configurarVisibilidad() {
        if (perfilUsuario.isEmpty()) return;

        String idiomaActual = utilsX.cargarIdiomaActual();
        Properties idioma = utilsX.detectLanguage(idiomaActual);
        Map<String, ButtonDto> botones = buttonService.obtenerBotones(perfilUsuario, MODULO, idioma);

        configurarBoton(btnNuevo, botones.get("nuevo"));
        configurarBoton(btnEditar, botones.get("editar"));
        configurarBoton(btnInactivar, botones.get("inactivar"));
        configurarBoton(btnEliminar, botones.get("eliminar"));
        configurarBoton(btnGuardar, botones.get("guardar"));
        configurarBoton(btnCancelar, botones.get("cancelar"));
    }

    private void configurarBoton(Button btn, ButtonDto dto) {
        if (btn == null) return;

        if (dto == null) {
            btn.setVisible(false);
            btn.setManaged(false);
            btn.setDisable(true);
        } else {
            btn.setVisible(true);
            btn.setManaged(true);
            btn.setDisable(false);
            btn.setTooltip(new Tooltip(dto.getTooltip()));
        }
    }

    // ========== CONFIGURACIÓN DE TABLA ==========

    private void configurarTabla() {
        // Columnas básicas
        tableHelper.configurarColumnaNumerica(colId, Producto::getIdProducto);
        tableHelper.configurarColumnaTexto(colCodigo, Producto::getCodigoProducto);
        tableHelper.configurarColumnaTexto(colNombre, Producto::getNombreProducto);

        // Columnas FK (mostrar nombre de la relación)
        tableHelper.configurarColumnaTexto(colMarca, prod ->
                prod.getMarca() != null ? prod.getMarca().getNombreMarca() : "-");
        tableHelper.configurarColumnaTexto(colCategoria, prod ->
                prod.getCategoria() != null ? prod.getCategoria().getNombreCategoria() : "-");

        // Columnas numéricas (precios y stock)
        tableHelper.configurarColumnaNumerica(colPrecioCompra, Producto::getPrecioCompra);
        tableHelper.configurarColumnaNumerica(colPrecioVenta, Producto::getPrecioVenta);

        // Columna stock con alertas visuales (rojo si 0, amarillo si <= mínimo)
        tableHelper.configurarColumnaNumerica(colStock,
            Producto::getStockActual,
            producto -> {
                int actual = producto.getStockActual();
                int minimo = producto.getStockMinimo();
                if (actual == 0) return "table-cell-danger";      // 🔴 ROJO: Sin stock
                else if (actual <= minimo) return "table-cell-warning"; // 🟡 AMARILLO: Stock bajo
                return ""; // Normal
            }
        );

        // Columna estado
        tableHelper.configurarColumnaBooleana(colEstado, Producto::getEstadoProducto);

        // Bloquear ordenamiento (usamos SortHelper)
        tableHelper.bloquearOrdenamiento(tableProductos);
        tableHelper.bloquearReordenamiento(tableProductos);

        // Configurar tabla
        tableHelper.configurarTabla(tableProductos, productosData, this::onSeleccionCambiada);

        // Menú contextual
        tableHelper.configurarMenuContextual(tableProductos, this::crearMenuContextual);
    }

    private ContextMenu crearMenuContextual(Producto producto) {
        ContextMenu menu = new ContextMenu();

        if (buttonService.puede(perfilUsuario, MODULO, "editar")) {
            MenuItem editar = new MenuItem(MENU_EDITAR);
            editar.setOnAction(e -> iniciarEdicion(producto));
            menu.getItems().add(editar);

            MenuItem cambiar = new MenuItem(MENU_INACTIVAR_ACTIVAR);
            cambiar.setOnAction(e -> cambiarEstado(producto));
            menu.getItems().add(cambiar);
        }

        if (buttonService.puede(perfilUsuario, MODULO, "eliminar")) {
            MenuItem eliminar = new MenuItem(MENU_ELIMINAR);
            eliminar.setOnAction(e -> eliminar(producto));
            menu.getItems().add(eliminar);
        }

        return menu;
    }

    // ========== CONFIGURACIÓN DE COMBOBOX FK ==========

    /**
     * Configura los 3 ComboBox de entidades FK.
     * - Marca: NOT NULL (obligatoria) - Solo activas + Autocomplete
     * - Categoría: NULLABLE (opcional) - Solo activas + "(Sin categoría)" + Autocomplete
     * - UnidadMedida: NOT NULL (obligatoria) - Solo activas + Autocomplete
     */
    private void configurarComboBoxes() {
        try {
            // Marca (NOT NULL - obligatoria, solo activas, CON autocomplete)
            marcaHelper = new EntityComboBoxHelper<Marca>(cbxMarca).withAutocomplete();
            Page<Marca> marcasPage = marcaService.listar(true, PageRequest.of(0, Integer.MAX_VALUE));
            marcaHelper.loadEntities(marcasPage.getContent(), Marca::getNombreMarca);

            // Categoría (NULLABLE - opcional, solo activas + opción null, CON autocomplete)
            categoriaHelper = new EntityComboBoxHelper<Categoria>(cbxCategoria)
                    .withNullOption("(Sin categoría)")
                    .withAutocomplete();
            Page<Categoria> categoriasPage = categoriaService.listar(true, PageRequest.of(0, Integer.MAX_VALUE));
            categoriaHelper.loadEntities(categoriasPage.getContent(), Categoria::getNombreCategoria);

            // UnidadMedida (NOT NULL - obligatoria, solo activas, CON autocomplete)
            unidadMedidaHelper = new EntityComboBoxHelper<UnidadMedida>(cbxUnidadMedida).withAutocomplete();
            List<UnidadMedida> unidadesActivas = unidadMedidaService.findAll().stream()
                    .filter(UnidadMedida::getEstadoUmedida)
                    .toList();
            unidadMedidaHelper.loadEntities(unidadesActivas, UnidadMedida::getNombreUmedida);

            log.debug("ComboBoxes configurados con autocomplete: {} marcas, {} categorías, {} unidades",
                    marcasPage.getTotalElements(), categoriasPage.getTotalElements(), unidadesActivas.size());

        } catch (Exception e) {
            log.error("Error al cargar ComboBoxes", e);
            notificarError("Error al cargar opciones: " + e.getMessage());
        }
    }

    // ========== CONFIGURACIÓN DE EVENTOS ==========

    private void configurarEventos() {
        registrarAccion(btnLimpiar, this::limpiar);
        registrarAccion(btnNuevo, this::nuevo);
        registrarAccion(btnEditar, this::editar);
        registrarAccion(btnInactivar, this::cambiarEstadoSeleccionado);
        registrarAccion(btnEliminar, this::eliminarSeleccionado);
        registrarAccion(btnGuardar, this::guardar);
        registrarAccion(btnCancelar, this::cancelar);
    }

    private void registrarAccion(Button boton, Runnable accion) {
        if (boton != null) {
            boton.setOnAction(e -> accion.run());
        }
    }

    // ========== UX FEATURES ==========

    private void configurarBuscadorConX() {
        searchHelper = new SearchHelper(txtBusqueda, this::buscar).configure();

        new AutocompleteHelper(txtBusqueda, this::sugerirProductos).configure();

        if (btnLimpiarBusqueda != null) {
            txtBusqueda.textProperty().addListener((obs, oldVal, newVal) -> {
                boolean tieneTexto = newVal != null && !newVal.trim().isEmpty();
                btnLimpiarBusqueda.setVisible(tieneTexto);
                btnLimpiarBusqueda.setManaged(tieneTexto);
            });
            btnLimpiarBusqueda.setOnAction(e -> searchHelper.clear());
        }

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

    private void configurarFiltroEstado() {
        filterStateHelper = new FilterStateHelper(cmbEstadoFiltro, () -> {
            paginaActual = 0;
            cargarProductos();
        }).configure();
    }

    private void configurarFiltroOrdenamiento() {
        sortHelper = new SortHelper(cmbOrdenamiento, () -> {
            paginaActual = 0;
            cargarProductos();
        });

        sortHelper
                .addOption("Código A-Z", "codigoProducto", Sort.Direction.ASC, true)
                .addOption("Código Z-A", "codigoProducto", Sort.Direction.DESC, false)
                .addOption("Nombre A-Z", "nombreProducto", Sort.Direction.ASC, false)
                .addOption("Nombre Z-A", "nombreProducto", Sort.Direction.DESC, false)
                .addOption("Precio Compra ↑", "precioCompra", Sort.Direction.ASC, false)
                .addOption("Precio Compra ↓", "precioCompra", Sort.Direction.DESC, false)
                .addOption("Stock ↑", "stockActual", Sort.Direction.ASC, false)
                .addOption("Stock ↓", "stockActual", Sort.Direction.DESC, false)
                .configure();
    }

    private void configurarPaginacion() {
        paginationHelper = new PaginationHelper(
                txtPagina, lblTotalPaginas,
                btnPrimera, btnAnterior, btnSiguiente, btnUltima,
                pagina -> {
                    paginaActual = pagina;
                    cargarProductos();
                }
        ).configure();
    }

    private void configurarContadoresCaracteres() {
        // lblCodigo es Label (inmutable), no necesita contador
        if (lblContadorNombre != null) {
            CharCounterHelper.configure(txtNombre, lblContadorNombre, MAX_NOMBRE_CARACTERES);
        }
        if (lblContadorDesc != null) {
            CharCounterHelper.configure(txtDescripcion, lblContadorDesc, MAX_DESCRIPCION_CARACTERES);
        }
    }

    private void configurarValidadores() {
        nombreValidator = new ValidationHelper(txtNombre)
                .required(MSG_NOMBRE_OBLIGATORIO)
                .minLength(3, MSG_MINIMO_CARACTERES)
                .maxLength(MAX_NOMBRE_CARACTERES, "Nombre no puede exceder " + MAX_NOMBRE_CARACTERES + " caracteres")
                .configure();

        descripcionValidator = new ValidationHelper(txtDescripcion)
                .maxLength(MAX_DESCRIPCION_CARACTERES, "Descripción no puede exceder " + MAX_DESCRIPCION_CARACTERES + " caracteres")
                .configure();
    }

    private void configurarClickFueraTabla() {
        Platform.runLater(() -> {
            if (tableProductos.getScene() == null) {
                log.warn("Scene null, no se puede configurar click fuera");
                return;
            }

            Node root = tableProductos.getScene().getRoot();

            // lblCodigo es Label, no necesita focus handling
            FocusHelper.configurarClickFuera(root, txtPagina, txtBusqueda, txtNombre,
                    txtPrecioCompra, txtPrecioVenta, txtStockActual, txtStockMinimo, txtDescripcion);

            root.addEventFilter(MouseEvent.MOUSE_CLICKED, event -> {
                if (estaFormularioEditable()) return;
                if (obtenerSeleccionado() == null) return;

                if (!tableProductos.contains(tableProductos.screenToLocal(event.getScreenX(), event.getScreenY()))) {
                    tableProductos.getSelectionModel().clearSelection();
                    limpiarFormulario();
                    actualizarBotones();
                }
            });
        });
    }

    private void configurarAtajosTeclado() {
        if (tableProductos == null || tableProductos.getScene() == null) {
            log.warn("No se pueden configurar atajos: tabla o scene null");
            return;
        }

        new KeybindHelper(tableProductos.getScene())
                .onEscapeCrud(txtPagina, tableProductos,
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
        if (operacionEnCurso) return;

        boolean haySeleccion = obtenerSeleccionado() != null;
        boolean enEdicion = estaFormularioEditable();

        if (enEdicion) {
            UIHelper.disable(btnNuevo, btnEditar, btnInactivar, btnEliminar);
        } else {
            UIHelper.enable(btnNuevo);
            btnEditar.setDisable(!haySeleccion);
            btnInactivar.setDisable(!haySeleccion);
            btnEliminar.setDisable(!haySeleccion);
        }
    }

    // ========== BÚSQUEDA Y PAGINACIÓN ==========

    private void buscar() {
        busquedaActual = txtBusqueda.getText().trim();
        paginaActual = 0;
        cargarProductos();
    }

    private List<String> sugerirProductos(String term) {
        String filtro = term == null ? "" : term.trim();
        if (filtro.length() < 2) {
            return List.of();
        }

        Boolean estadoFiltro = filterStateHelper != null ? filterStateHelper.getCurrentState() : null;
        Pageable pageable = PageRequest.of(0, 8, Sort.by(
                Sort.Order.asc("nombreProducto").ignoreCase(),
                Sort.Order.asc("codigoProducto").ignoreCase()
        ));

        try {
            Page<Producto> page = productoService.buscarPaginado(filtro, estadoFiltro, pageable);
            return page.getContent().stream()
                    .map(p -> {
                        String codigo = Objects.requireNonNullElse(p.getCodigoProducto(), "");
                        String nombre = Objects.requireNonNullElse(p.getNombreProducto(), "");
                        return (codigo.isBlank() ? nombre : codigo + " - " + nombre).trim();
                    })
                    .filter(s -> !s.isBlank())
                    .filter(nombre -> AutocompleteHelper.matchesWordStart(nombre, filtro))
                    .distinct()
                    .toList();
        } catch (Exception ex) {
            log.warn("No se pudo cargar sugerencias de productos para '{}'", filtro, ex);
            return List.of();
        }
    }

    private void limpiar() {
        txtBusqueda.clear();
        filterStateHelper.reset();
        sortHelper.reset();
        tableProductos.getSelectionModel().clearSelection();
        tableProductos.requestFocus();
        resetearEdicion();
        deshabilitarCamposFormulario();
        desbloquearEdicion();
        buscar();
        actualizarBotones();
        log.info("Vista reiniciada");
    }

    private void limpiarCompletamente() {
        tableProductos.requestFocus();
        resetearEdicion();
        deshabilitarCamposFormulario();
        txtBusqueda.clear();
        busquedaActual = "";
        filterStateHelper.reset();
        sortHelper.reset();
        paginaActual = 0;
        tableProductos.getSelectionModel().clearSelection();
        desbloquearEdicion();
        log.info("Form limpiado completamente después de guardar");
    }

    private void cargarProductos() {
        Task<Page<Producto>> task = new Task<>() {
            @Override
            protected Page<Producto> call() {
                return obtenerPaginaProductos();
            }
        };

        ejecutarOperacionAsync("productos-cargar", task,
                page -> {
                    actualizarDatosPaginacion(page);
                    actualizarBotones();
                    log.debug("Cargados {} productos, página {}/{}", page.getNumberOfElements(), paginaActual + 1, totalPaginas);
                },
                ex -> {
                    actualizarBotones();
                    log.error("Error al cargar productos", ex);
                    notificarError("Error al cargar productos: " + ex.getMessage());
                }
        );
    }

    private void reSeleccionarProducto(Long productoId, boolean mantenerPaginacionBloqueada) {
        if (productoId == null) return;

        tableProductos.getItems().stream()
                .filter(p -> p.getIdProducto().equals(productoId))
                .findFirst()
                .ifPresent(p -> {
                    tableProductos.getSelectionModel().select(p);
                    if (mantenerPaginacionBloqueada) {
                        bloquearPaginacion();
                    }
                });
    }

    private Page<Producto> obtenerPaginaProductos() {
        Boolean estadoFiltro = filterStateHelper.getCurrentState();
        return busquedaActual.isEmpty() ?
                productoService.listar(estadoFiltro, crearPageable()) :
                productoService.buscarPaginado(busquedaActual, estadoFiltro, crearPageable());
    }

    private void actualizarDatosPaginacion(Page<Producto> page) {
        if (page.isEmpty() && paginaActual > 0 && page.getTotalElements() > 0) {
            paginaActual--;
            cargarProductos();
            return;
        }

        productosData.clear();
        productosData.addAll(page.getContent());
        totalPaginas = page.getTotalPages();
        totalRegistros = page.getTotalElements();
        actualizarPaginacion();
    }

    private Pageable crearPageable() {
        Sort.Order order = sortHelper.createOrder();
        return PageRequest.of(paginaActual, REGISTROS_POR_PAGINA, Sort.by(order));
    }

    private void actualizarPaginacion() {
        lblTotalProductos.setText("Total: " + totalRegistros);
        paginationHelper.sincronizar(paginaActual, totalPaginas);
    }

    // ========== BLOQUEO DE EDICIÓN ==========

    private void bloquearEdicion() {
        paginationHelper.disable();
        searchHelper.disable();
        filterStateHelper.disable();
        sortHelper.disable();
        searchContainer.setDisable(true);
    }

    private void desbloquearEdicion() {
        paginationHelper.enable();
        searchHelper.enable();
        filterStateHelper.enable();
        sortHelper.enable();
        searchContainer.setDisable(false);
    }

    private void bloquearPaginacion() {
        paginationHelper.disable();
    }

    private void habilitarPaginacion() {
        paginationHelper.enable();
    }

    // ========== OPERACIONES CRUD ==========

    private void nuevo() {
        tableProductos.getSelectionModel().clearSelection();
        resetearEdicion();

        // Generar código automático
        String codigoGenerado = productoService.generarSiguienteCodigo();
        lblCodigo.setText(codigoGenerado);
        lblCodigo.setDisable(true); // Código inmutable

        // Valores por defecto
        txtPrecioCompra.setText("0.00");
        txtPrecioVenta.setText("0.00");
        txtStockActual.setText("0");
        txtStockMinimo.setText("0");

        habilitarCamposFormulario();
        bloquearEdicion();
        actualizarBotones();

        Platform.runLater(() -> {
            txtNombre.requestFocus();
            if (nombreValidator != null) {
                nombreValidator.validate();
            }
        });

        log.info("Modo NUEVO activado - Código: {}", codigoGenerado);
    }

    private void editar() {
        Producto producto = obtenerSeleccionado();
        if (producto == null) {
            notificarWarning(MSG_SELECCIONAR);
            return;
        }
        iniciarEdicion(producto);
    }

    private void iniciarEdicion(Producto producto) {
        if (producto == null) return;

        cargarProducto(producto);
        habilitarCamposFormulario();
        lblCodigo.setDisable(true); // Código inmutable en edición
        bloquearEdicion();
        actualizarBotones();
        txtNombre.requestFocus();
        log.info("Edición iniciada - Producto ID: {}", producto.getIdProducto());
    }

    private Producto obtenerSeleccionado() {
        return tableProductos.getSelectionModel().getSelectedItem();
    }

    private void onSeleccionCambiada(Producto producto) {
        if (estaFormularioEditable()) {
            resetearEdicion();
            deshabilitarCamposFormulario();
            desbloquearEdicion();
        }
        cargarPreview(producto);
        actualizarBotones();
    }

    private Stage obtenerStage() {
        if (tableProductos != null && tableProductos.getScene() != null) {
            return (Stage) tableProductos.getScene().getWindow();
        }
        return null;
    }

    private void cargarPreview(Producto producto) {
        if (producto == null) {
            limpiarFormulario();
            return;
        }

        lblId.setText(String.valueOf(producto.getIdProducto()));
        lblCodigo.setText(producto.getCodigoProducto());
        txtNombre.setText(producto.getNombreProducto());
        txtDescripcion.setText(Objects.requireNonNullElse(producto.getDescripcionProducto(), ""));
        txtPrecioCompra.setText(producto.getPrecioCompra().toString());
        txtPrecioVenta.setText(producto.getPrecioVenta().toString());
        txtStockActual.setText(String.valueOf(producto.getStockActual()));
        txtStockMinimo.setText(String.valueOf(producto.getStockMinimo()));
        chkEstado.setSelected(Objects.requireNonNullElse(producto.getEstadoProducto(), true));

        // Cargar ComboBoxes
        marcaHelper.selectEntity(producto.getMarca(), Marca::getNombreMarca);
        categoriaHelper.selectEntity(producto.getCategoria(), Categoria::getNombreCategoria);
        unidadMedidaHelper.selectEntity(producto.getUnidadMedida(), UnidadMedida::getNombreUmedida);
    }

    private void cargarProducto(Producto producto) {
        if (producto == null) return;

        productoEnEdicion = producto.getIdProducto();
        cargarPreview(producto);
        guardarValoresOriginales();
    }

    private void guardarValoresOriginales() {
        // Código es inmutable, no se trackea
        nombreOriginal = obtenerTexto(txtNombre);
        descripcionOriginal = obtenerTexto(txtDescripcion);
        precioCompraOriginal = parsearBigDecimal(txtPrecioCompra.getText());
        precioVentaOriginal = parsearBigDecimal(txtPrecioVenta.getText());
        stockActualOriginal = parsearInteger(txtStockActual.getText());
        stockMinimoOriginal = parsearInteger(txtStockMinimo.getText());
        estadoOriginal = chkEstado.isSelected();
        marcaOriginal = marcaHelper.getSelectedEntity();
        categoriaOriginal = categoriaHelper.getSelectedEntity();
        unidadMedidaOriginal = unidadMedidaHelper.getSelectedEntity();
    }

    // ========== OPERACIONES ASYNC ==========

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

    // ========== CAMBIAR ESTADO (Sin cascada - Productos es hoja) ==========

    private void cambiarEstadoSeleccionado() {
        Producto producto = obtenerSeleccionado();
        if (producto == null) {
            notificarWarning(MSG_SELECCIONAR);
            return;
        }
        cambiarEstado(producto);
    }

    private void cambiarEstado(Producto producto) {
        boolean activar = !producto.getEstadoProducto();
        Long productoId = producto.getIdProducto();

        // Confirmación simple (sin cascada - productos no tienen hijos)
        if (!DialogHelper.showConfirmar(obtenerStage(),
                activar ? "Activar producto" : "Inactivar producto",
                "¿Confirmar cambio de estado?")) {
            return;
        }

        Task<Page<Producto>> task = new Task<>() {
            @Override
            protected Page<Producto> call() {
                Producto entidad = productoService.findById(productoId)
                        .orElseThrow(() -> new IllegalStateException(MSG_NO_ENCONTRADO));
                entidad.setEstadoProducto(activar);
                productoService.save(entidad);
                return obtenerPaginaProductos();
            }
        };

        ejecutarOperacionAsync("productos-cambiar-estado", task,
                page -> {
                    actualizarDatosPaginacion(page);
                    actualizarBotones();
                    boolean estabaEditando = productoEnEdicion != null && productoEnEdicion.equals(productoId);
                    reSeleccionarProducto(productoId, estabaEditando);
                    if (estabaEditando) {
                        chkEstado.setSelected(activar);
                    }
                    notificarInfo(activar ? MSG_ACTIVADO : MSG_INACTIVADO);
                    log.info("Producto {} - ID: {}", activar ? "activado" : "inactivado", productoId);
                },
                ex -> {
                    actualizarBotones();
                    log.error("Error al cambiar estado", ex);
                    notificarError("Error: " + ex.getMessage());
                }
        );
    }

    // ========== ELIMINAR (Sin validación de dependencias - Productos es hoja) ==========

    private void eliminarSeleccionado() {
        Producto producto = obtenerSeleccionado();
        if (producto == null) {
            notificarWarning(MSG_SELECCIONAR);
            return;
        }
        eliminar(producto);
    }

    private void eliminar(Producto producto) {
        if (!DialogHelper.showConfirmarEliminacion(obtenerStage(), "Producto: " + producto.getNombreProducto())) {
            return;
        }

        Task<Void> task = new Task<>() {
            @Override
            protected Void call() {
                productoService.deleteById(producto.getIdProducto());
                return null;
            }
        };

        ejecutarOperacionAsync("productos-eliminar", task,
                aVoid -> {
                    resetearEdicion();
                    deshabilitarCamposFormulario();
                    tableProductos.getSelectionModel().clearSelection();
                    cargarProductos();
                    actualizarBotones();
                    notificarSuccess(MSG_ELIMINADO);
                    log.info("Eliminado - ID: {}", producto.getIdProducto());
                },
                ex -> {
                    actualizarBotones();
                    log.error("Error al eliminar", ex);
                    notificarError("Error al eliminar: " + ex.getMessage());
                }
        );
    }

    // ========== GUARDAR ==========

    private void guardar() {
        if (!validar()) return;

        // Capturar valores del formulario
        final String codigo = lblCodigo.getText();  // Label.getText()
        final String nombre = obtenerTexto(txtNombre);
        final String descripcion = obtenerTexto(txtDescripcion);
        final BigDecimal precioCompra = parsearBigDecimal(txtPrecioCompra.getText());
        final BigDecimal precioVenta = parsearBigDecimal(txtPrecioVenta.getText());
        final Integer stockActual = parsearInteger(txtStockActual.getText());
        final Integer stockMinimo = parsearInteger(txtStockMinimo.getText());
        final boolean estado = chkEstado.isSelected();

        // Obtener entidades de ComboBox
        final Marca marca = marcaHelper.getSelectedEntity();
        final Categoria categoria = categoriaHelper.getSelectedEntity(); // Puede ser null
        final UnidadMedida unidadMedida = unidadMedidaHelper.getSelectedEntity();

        Task<Long> task = new Task<>() {
            @Override
            protected Long call() {
                Producto producto;

                if (productoEnEdicion != null) {
                    // EDICIÓN
                    producto = productoService.findById(productoEnEdicion)
                            .orElseThrow(() -> new IllegalStateException(MSG_NO_ENCONTRADO));
                    aplicarDatosFormulario(producto, codigo, nombre, descripcion,
                            precioCompra, precioVenta, stockActual, stockMinimo,
                            marca, categoria, unidadMedida, estado);
                } else {
                    // NUEVO
                    producto = Producto.builder()
                            .codigoProducto(codigo)
                            .nombreProducto(nombre)
                            .descripcionProducto(descripcion.isEmpty() ? null : descripcion)
                            .precioCompra(precioCompra)
                            .precioVenta(precioVenta)
                            .stockActual(stockActual)
                            .stockMinimo(stockMinimo)
                            .marca(marca)
                            .categoria(categoria)
                            .unidadMedida(unidadMedida)
                            .estadoProducto(estado)
                            .build();
                }

                productoService.save(producto);
                return producto.getIdProducto();
            }
        };

        ejecutarOperacionAsync("productos-guardar", task,
                ignoredId -> {
                    notificarSuccess(MSG_GUARDADO);
                    limpiarCompletamente();
                    cargarProductos();
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

    private void aplicarDatosFormulario(Producto producto, String codigo, String nombre,
                                        String descripcion, BigDecimal precioCompra,
                                        BigDecimal precioVenta, Integer stockActual,
                                        Integer stockMinimo, Marca marca, Categoria categoria,
                                        UnidadMedida unidadMedida, boolean estado) {
        producto.setCodigoProducto(codigo);
        producto.setNombreProducto(nombre);
        producto.setDescripcionProducto(descripcion.isEmpty() ? null : descripcion);
        producto.setPrecioCompra(precioCompra);
        producto.setPrecioVenta(precioVenta);
        producto.setStockActual(stockActual);
        producto.setStockMinimo(stockMinimo);
        producto.setMarca(marca);
        producto.setCategoria(categoria);
        producto.setUnidadMedida(unidadMedida);
        producto.setEstadoProducto(estado);
    }

    private void cancelar() {
        if (formularioTieneCambios()) {
            if (!DialogHelper.showConfirmar(
                    obtenerStage(),
                    DIALOG_CAMBIOS_TITULO,
                    DIALOG_CAMBIOS_MENSAJE
            )) return;
        }

        Long idParaReseleccionar = productoEnEdicion;
        tableProductos.requestFocus();
        resetearEdicion();
        deshabilitarCamposFormulario();
        desbloquearEdicion();

        if (idParaReseleccionar != null) {
            reSeleccionarProducto(idParaReseleccionar, false);
        } else {
            tableProductos.getSelectionModel().clearSelection();
        }

        actualizarBotones();
        actualizarPaginacion();
    }

    private boolean formularioTieneCambios() {
        if (productoEnEdicion == null) {
            return !obtenerTexto(txtNombre).isEmpty() ||
                    !obtenerTexto(txtDescripcion).isEmpty() ||
                    marcaHelper.hasSelection() ||
                    categoriaHelper.hasSelection() ||
                    unidadMedidaHelper.hasSelection();
        }

        return !Objects.equals(obtenerTexto(txtNombre), Objects.requireNonNullElse(nombreOriginal, "")) ||
                !Objects.equals(obtenerTexto(txtDescripcion), Objects.requireNonNullElse(descripcionOriginal, "")) ||
                !Objects.equals(parsearBigDecimal(txtPrecioCompra.getText()), precioCompraOriginal) ||
                !Objects.equals(parsearBigDecimal(txtPrecioVenta.getText()), precioVentaOriginal) ||
                !Objects.equals(parsearInteger(txtStockActual.getText()), stockActualOriginal) ||
                !Objects.equals(parsearInteger(txtStockMinimo.getText()), stockMinimoOriginal) ||
                chkEstado.isSelected() != estadoOriginal ||
                !Objects.equals(marcaHelper.getSelectedEntity(), marcaOriginal) ||
                !Objects.equals(categoriaHelper.getSelectedEntity(), categoriaOriginal) ||
                !Objects.equals(unidadMedidaHelper.getSelectedEntity(), unidadMedidaOriginal);
    }

    private String obtenerTexto(TextInputControl control) {
        String valor = control.getText();
        return valor == null ? "" : valor.trim();
    }

    private void resetearEdicion() {
        limpiarFormulario();
        productoEnEdicion = null;
    }

    private void limpiarFormulario() {
        lblId.setText(ID_SIN_SELECCION);
        lblCodigo.setText(ID_SIN_SELECCION);  // Label usa setText
        txtNombre.clear();
        txtDescripcion.clear();
        txtPrecioCompra.clear();
        txtPrecioVenta.clear();
        txtStockActual.clear();
        txtStockMinimo.clear();
        chkEstado.setSelected(true);
        lblMensajeForm.setText("");

        // Limpiar ComboBoxes (null-safe para primera llamada)
        if (marcaHelper != null) marcaHelper.clear();
        if (categoriaHelper != null) categoriaHelper.clear();
        if (unidadMedidaHelper != null) unidadMedidaHelper.clear();

        // Resetear validadores
        if (nombreValidator != null) nombreValidator.reset();
        if (descripcionValidator != null) descripcionValidator.reset();
    }

    private void deshabilitarCamposFormulario() {
        lblCodigo.setDisable(true);
        txtNombre.setDisable(true);
        txtDescripcion.setDisable(true);
        txtPrecioCompra.setDisable(true);
        txtPrecioVenta.setDisable(true);
        txtStockActual.setDisable(true);
        txtStockMinimo.setDisable(true);
        cbxMarca.setDisable(true);
        cbxCategoria.setDisable(true);
        cbxUnidadMedida.setDisable(true);
        chkEstado.setDisable(true);
        UIHelper.disable(btnGuardar, btnCancelar);
    }

    private void habilitarCamposFormulario() {
        // lblCodigo permanece disabled (inmutable)
        txtNombre.setDisable(false);
        txtDescripcion.setDisable(false);
        txtPrecioCompra.setDisable(false);
        txtPrecioVenta.setDisable(false);
        txtStockActual.setDisable(false);
        txtStockMinimo.setDisable(false);
        cbxMarca.setDisable(false);
        cbxCategoria.setDisable(false);
        cbxUnidadMedida.setDisable(false);
        chkEstado.setDisable(false);
        UIHelper.enable(btnGuardar, btnCancelar);
    }

    private boolean estaFormularioEditable() {
        return !txtNombre.isDisabled();
    }

    private void deshabilitarAccionesOperacion() {
        operacionEnCurso = true;
        UIHelper.disable(btnNuevo, btnEditar, btnInactivar, btnEliminar,
                btnGuardar, btnCancelar, btnLimpiar);
        paginationHelper.disable();
        tableProductos.setDisable(true);
    }

    private void habilitarAccionesOperacion() {
        operacionEnCurso = false;
        UIHelper.enable(btnLimpiar);
        tableProductos.setDisable(false);

        if (estaFormularioEditable()) {
            bloquearPaginacion();
        } else {
            habilitarPaginacion();
        }
    }

    // ========== VALIDACIÓN ==========

    private boolean validar() {
        String codigo = lblCodigo.getText();  // Label.getText()
        String nombre = obtenerTexto(txtNombre);

        // Validar código (autogenerado, solo verificar que existe)
        if (codigo.isEmpty() || codigo.equals(ID_SIN_SELECCION)) {
            lblMensajeForm.setText(MSG_CODIGO_OBLIGATORIO);
            return false;
        }

        // Validar nombre
        if (nombre.isEmpty()) {
            mostrarErrorValidacion(txtNombre, MSG_NOMBRE_OBLIGATORIO);
            return false;
        }
        if (nombre.length() < 3) {
            mostrarErrorValidacion(txtNombre, MSG_MINIMO_CARACTERES);
            return false;
        }

        // Validar precios
        BigDecimal precioCompra = parsearBigDecimal(txtPrecioCompra.getText());
        if (precioCompra == null || precioCompra.compareTo(BigDecimal.ZERO) < 0) {
            mostrarErrorValidacion(txtPrecioCompra, MSG_PRECIO_INVALIDO);
            return false;
        }

        BigDecimal precioVenta = parsearBigDecimal(txtPrecioVenta.getText());
        if (precioVenta == null || precioVenta.compareTo(BigDecimal.ZERO) < 0) {
            mostrarErrorValidacion(txtPrecioVenta, MSG_PRECIO_INVALIDO);
            return false;
        }

        // Validar stocks
        Integer stockActual = parsearInteger(txtStockActual.getText());
        if (stockActual == null || stockActual < 0) {
            mostrarErrorValidacion(txtStockActual, MSG_STOCK_INVALIDO);
            return false;
        }

        Integer stockMinimo = parsearInteger(txtStockMinimo.getText());
        if (stockMinimo == null || stockMinimo < 0) {
            mostrarErrorValidacion(txtStockMinimo, MSG_STOCK_INVALIDO);
            return false;
        }

        // Validar ComboBox obligatorios
        if (marcaHelper.isPlaceholderSelected()) {
            notificarWarning(MSG_MARCA_OBLIGATORIA);
            return false;
        }
        if (unidadMedidaHelper.isPlaceholderSelected()) {
            notificarWarning(MSG_UNIDAD_OBLIGATORIA);
            return false;
        }

        // Warning soft: stock actual < mínimo (permite guardar)
        if (stockActual < stockMinimo) {
            Toast.showWarning(obtenerStage(),
                    "Nota: Stock actual (" + stockActual + ") es menor que mínimo (" + stockMinimo + ")",
                    Toast.DURATION_NORMAL);
            // NO return false - permite guardar
        }

        return true;
    }

    private void mostrarErrorValidacion(TextInputControl campo, String mensaje) {
        lblMensajeForm.setText(mensaje);
        campo.requestFocus();
    }

    // ========== MÉTODOS AUXILIARES DE PARSEO ==========

    private BigDecimal parsearBigDecimal(String texto) {
        try {
            return new BigDecimal(texto.trim());
        } catch (NumberFormatException | NullPointerException e) {
            return null;
        }
    }

    private Integer parsearInteger(String texto) {
        try {
            return Integer.parseInt(texto.trim());
        } catch (NumberFormatException | NullPointerException e) {
            return null;
        }
    }

    // ========== NOTIFICACIONES ==========

    private void notificarSuccess(String mensaje) {
        mostrarToast(mensaje, Toast.Type.SUCCESS, Toast.DURATION_NORMAL);
    }

    private void notificarWarning(String mensaje) {
        mostrarToast(mensaje, Toast.Type.WARNING, Toast.DURATION_NORMAL);
    }

    private void notificarError(String mensaje) {
        mostrarToast(mensaje, Toast.Type.ERROR, Toast.DURATION_LONG);
    }

    private void notificarInfo(String mensaje) {
        mostrarToast(mensaje, Toast.Type.INFO, Toast.DURATION_NORMAL);
    }

    private void mostrarToast(String mensaje, Toast.Type tipo, int duracion) {
        Stage stageActual = obtenerStage();
        if (stageActual == null) {
            log.info("Notificación sin Scene - {}: {}", tipo, mensaje);
            return;
        }
        Toast.showToast(stageActual, mensaje, tipo, duracion);
    }
}
