# FarmaFx - Resumen Técnico para IAs

## Stack
Spring Boot 3 + JPA/Hibernate + SQLite | JavaFX 21 | Maven | Lombok

## Arquitectura
`Model → Repository → Service → Controller + FXML`

## Estado Actual (2025-11-24)

### Controllers CRUD
| Controller | Versión | Estado | Líneas | Score |
|------------|---------|--------|--------|-------|
| **MarcasController** | v4.5 | GOLD STANDARD | ~1294 | 10/10 |
| **CategoriasGoldController** | v1.0 | GOLD STANDARD | ~1549 | 10/10 |
| **ProductosGoldController** | v1.0 | GOLD STANDARD | ~1217 | 10/10 |

### Diferencias entre Controllers GOLD

| Característica | MarcasController | CategoriasGoldController | ProductosGoldController |
|----------------|------------------|--------------------------|-------------------------|
| Jerarquía padre/hijo | No | Sí (ComboBox padre) | No |
| Records Java 17 | No | Sí (`DatosGuardado`, `InfoCascada`) | No |
| ComboBox FK | 0 | 1 (Categoría padre) | 3 (Marca, Categoría, UnidadMedida) |
| Código auto-generado | No | No | Sí (PROD-XXXX) |
| Validaciones numéricas | Texto simple | Texto simple | BigDecimal + Integer |
| Alertas visuales tabla | No | No | Sí (stock rojo/amarillo) |
| Cascada | Solo productos | Subcategorías + productos | No (entidad hoja) |

---

## 10 Helpers Reutilizables

| Helper | Propósito | Carga CSS |
|--------|-----------|-----------|
| **KeybindHelper** | Atajos teclado (F5, ESC, Ctrl+N/S/E, Delete) | - |
| **SortHelper** | ComboBox ordenamiento | FXML |
| **FilterStateHelper** | Filtro Todos/Activo/Inactivo | FXML |
| **SearchHelper** | Debounce 300ms | FXML |
| **PaginationHelper** | Paginación + disable/enable | FXML |
| **FocusHelper** | Click fuera quita focus | - |
| **ValidationHelper** | Validación tiempo real | FXML |
| **CharCounterHelper** | Contador "X/600" | FXML |
| **TableViewHelper** | Configurar tabla + menú contextual | FXML |
| **UIHelper** | disable/enable para Buttons | - |

### Uso Típico en Controller
```java
// En inicializarDatos()
sortHelper = new SortHelper(cmbOrdenamiento, () -> { paginaActual = 0; cargarDatos(); })
    .addOption("Nombre A-Z", "nombreCampo", Sort.Direction.ASC, true)
    .configure();

filterStateHelper = new FilterStateHelper(cmbEstadoFiltro, () -> { paginaActual = 0; cargarDatos(); })
    .configure();

searchHelper = new SearchHelper(txtBusqueda, this::buscar).configure();

paginationHelper = new PaginationHelper(txtPagina, lblTotal, btnPrimera, btnAnterior, btnSiguiente, btnUltima,
    pagina -> { paginaActual = pagina; cargarDatos(); }).configure();

new KeybindHelper(tabla.getScene())
    .onEscapeCrud(txtPagina, tabla, this::estaFormularioEditable, this::cancelar, this::limpiarFormulario, this::actualizarBotones)
    .on(KeyCode.F5, this::limpiar)
    .onCtrl(KeyCode.N, this::nuevo, () -> KeybindHelper.isEnabled(btnNuevo))
    .configure();
```

---

## Preview Mode (4 Estados)

| Estado | Campos | Botones TOP | lblId |
|--------|--------|-------------|-------|
| SIN_SELECCION | Vacíos, disabled | Nuevo ✓ | "—" |
| PREVIEW | Llenos, disabled | Nuevo/Editar/Inactivar/Eliminar ✓ | ID actual |
| EDICION | Llenos, enabled | Todos ✗ | ID actual |
| NUEVO | Vacíos, enabled | Todos ✗ | Próximo ID |

---

## Patrones Obligatorios

```java
// 1. Constructor Injection
@RequiredArgsConstructor
private final IServicio servicio;

// 2. Stage fresco (NO cachear)
private Stage obtenerStage() {
    return (Stage) tabla.getScene().getWindow();
}

// 3. Focus antes de reset
tablaMarcas.requestFocus();
limpiarFormulario();

// 4. Bloquear edición durante operación
private void bloquearEdicion() {
    paginationHelper.disable();
    searchHelper.disable();
    filterStateHelper.disable();
    sortHelper.disable();
}
```

---

## CSS Modular (10 archivos)

```
css/
├── toast.css, dialog.css       ← Cargados en Java (componentes dinámicos)
├── search.css, buttons.css, forms.css, tables.css
├── filters.css, pagination.css, components.css
└── marcas-modern.css           ← Específico por módulo
```

**En FXML:**
```xml
<BorderPane stylesheets="@../../css/components.css, @../../css/filters.css, ...">
```

---

## DialogHelper - Métodos Principales

```java
// Confirmación simple
DialogHelper.showConfirmar(stage, "Título", "Mensaje")

// Eliminación con dependencias (genérico)
DialogHelper.showConfirmarEliminacionConDependencias(stage, "Categoría", nombre, hijas, prodActivos, prodInactivos)

// Cascada para Marcas (solo productos)
DialogHelper.showCascadaInactivar(stage, "Marca", cantidadProductos)
DialogHelper.showCascadaActivar(stage, "Marca", cantidadProductos)

// Cascada para Categorías (subcategorías + productos)
DialogHelper.showCascadaCategoriaInactivar(stage, subcategorias, productos)
DialogHelper.showCascadaCategoriaActivar(stage, subcategorias, productos)
```

---

## Toast Premium

```java
Toast.showSuccess(stage, "Mensaje", Toast.DURATION_NORMAL);  // Verde
Toast.showWarning(stage, "Mensaje", Toast.DURATION_NORMAL);  // Amarillo
Toast.showError(stage, "Mensaje", Toast.DURATION_LONG);      // Rojo
Toast.showInfo(stage, "Mensaje", Toast.DURATION_NORMAL);     // Azul
```

---

## Notas Importantes

1. **Records en Categorías**: Justificados por complejidad de cascada (subcategorías + productos). NO aplicar en Marcas/Productos.

2. **Categoria.categoria puede ser NULL**: Productos huérfanos cuando se elimina categoría.

3. **ComboBox en Productos**: Cargar en `inicializarDatos()`:
   ```java
   cbxMarca.setItems(FXCollections.observableArrayList(marcaService.listarCombobox()));
   cbxCategoria.setItems(FXCollections.observableArrayList(categoriaService.listarCombobox()));
   cbxUnidadMedida.setItems(FXCollections.observableArrayList(unidadMedidaService.listarCombobox()));
   ```

4. **Validación BigDecimal**: Usar `@DecimalMin`, `@Digits` en modelo, validar en controller con try-catch para NumberFormatException.

5. **ScrollPane FXML (Estándar para todos los CRUDs)**: ✅ **ESTANDARIZADO**
   ```xml
   <center>
       <ScrollPane fitToWidth="true" hbarPolicy="NEVER" styleClass="content-scroll" vbarPolicy="AS_NEEDED">
           <VBox spacing="16" styleClass="content-area">
               <!-- Formulario -->
               <!-- Tabla con VBox.vgrow="ALWAYS" -->
           </VBox>
       </ScrollPane>
   </center>
   ```
   - ✅ Aplicado en: `marcas.fxml`, `categorias.fxml`, `productos_gold.fxml`
   - ✅ Permite scroll vertical cuando formulario + tabla exceden altura ventana
   - ✅ Tabla se expande automáticamente (VBox.vgrow="ALWAYS")

---

## 🎯 PRÓXIMO: Ventas + Reportes JasperReports

### **Estado:** PLANIFICADO ⏳
Ver documento completo: **`PLAN_VENTAS_REPORTES.md`**

**Resumen:**
- **Backend:** ✅ Completo (Venta, VentaDetalle, VentaCarrito, Cliente)
- **Frontend:** ❌ Pendiente (VentasController + ventas.fxml)
- **Reportes:** ❌ Pendiente (JasperReports para PDF)

**Prioridad:** URGENTE
**Tiempo estimado:** 3.5-6 horas

**Características clave:**
- Pantalla TODO-EN-UNO (productos | carrito)
- Cliente genérico "PÚBLICO GENERAL" (temporal)
- Cálculo automático IGV (18%)
- Generación PDF comprobante (JasperReports)
- Ruta PDFs: `C:\Users\{usuario}\FarmaFX\Comprobantes\`

---

**Última actualización**: 2025-11-24 01:12

**Versiones actuales**:
- MarcasController v4.5 (GOLD 10/10)
- CategoriasGoldController v1.0 (GOLD 10/10)
- **ProductosGoldController v1.0 (GOLD 10/10)** ✅ COMPLETADO
- 10 Helpers reutilizables
- EntityComboBoxHelper v2.0 (soporte nullable)
- TableViewHelper (alertas visuales stock)
- CSS Modular (10 archivos) + alertas stock (`.table-cell-danger`, `.table-cell-warning`)
- ScrollPane FXML (estandarizado en todos los CRUDs)
- Toast Premium v3.4
- DialogHelper con cascadas genéricas y específicas