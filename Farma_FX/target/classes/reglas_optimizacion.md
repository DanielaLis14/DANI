# 📋 Reglas de Optimización - FarmaFx

**Objetivo:** Código limpio, mantenible y eficiente. Análisis comparativo obligatorio antes de cambios.

---

## 🎯 TERMINOLOGÍA

### **FIX (Corrección de Bug)**
- **Qué:** Corregir comportamiento incorrecto del código
- **Cuándo:** Algo NO funciona como debería (error lógico, crash, data corruption)
- **Ejemplo:** NullPointerException, cálculo incorrecto, race condition

### **WARNING (Corrección de Advertencia)**
- **Qué:** Eliminar warnings del IDE/compilador
- **Cuándo:** Código funciona PERO genera advertencias
- **Ejemplo:** Unused variables, unchecked casts, deprecated methods

### **MEJORA (Improvement)**
- **Qué:** Optimizar código existente SIN cambiar funcionalidad
- **Cuándo:** Código funciona PERO puede ser más legible/eficiente/mantenible
- **Ejemplo:** Extraer método duplicado, renombrar variable ambigua, aplicar DRY

### **REFACTOR (Refactorización)**
- **Qué:** Reestructurar arquitectura/diseño manteniendo funcionalidad
- **Cuándo:** Código funciona PERO estructura es subóptima
- **Ejemplo:** Extraer clase, cambiar patrón de diseño, simplificar jerarquía

### **IMPLEMENTACIÓN (Nueva Feature)**
- **Qué:** Agregar funcionalidad nueva
- **Cuándo:** Requerimiento nuevo, no existe en código actual
- **Ejemplo:** Agregar ordenamiento, paginación, validación en tiempo real

---

## 🔴 PRIORIDAD DE TAREAS

```
1. BUGS         (funcionalidad rota)
2. WARNINGS     (advertencias IDE)
3. OPTIMIZACIÓN (mejoras/refactors)
4. FEATURES     (implementaciones nuevas)
```

---

## 📐 METODOLOGÍA OBLIGATORIA

### **PARA OPTIMIZACIÓN (clase existente):**

**1. Evaluar puntos observados**
   - Listar warnings, code smells, problemas detectados
   - Ubicación exacta (líneas)

**2. Proponer alternativas (mínimo 2)**
   - Solo líneas relevantes del código (no todo el archivo)
   - Si hay repetición, resumir con comentario `// ... (repetido X veces)`

**3. Comparar ACTUAL vs MEJORAS:**
   ```markdown
   **❌ CÓDIGO ACTUAL (Líneas X-Y):**
   ```java
   // código actual relevante
   ```

   **✅ PROS:**
   - Pro 1

   **❌ CONTRAS:**
   - Contra 1

   ---

   **✅ PROPUESTA A: [Nombre descriptivo]**
   ```java
   // código propuesto
   ```

   **✅ PROS:**
   - Pro 1 (razón técnica)

   **❌ CONTRAS:**
   - Contra 1 (razón técnica)

   ---

   **✅ PROPUESTA B: [Nombre descriptivo]**
   (mismo formato)
   ```

**4. Tabla comparativa final:**
   ```markdown
   | Criterio | Actual | Propuesta A | Propuesta B |
   |----------|--------|-------------|-------------|
   | Simpleza | X/10 - razón | Y/10 - razón | Z/10 - razón |
   | Legibilidad | X/10 - razón | Y/10 - razón | Z/10 - razón |
   | Funcionalidad | X/10 - razón | Y/10 - razón | Z/10 - razón |
   | Eficiencia | X/10 - razón | Y/10 - razón | Z/10 - razón |
   | Mantenibilidad | X/10 - razón | Y/10 - razón | Z/10 - razón |
   | **Score Total** | **X/10** | **Y/10** | **Z/10** |
   | Líneas código | N | ±X | ±Y |
   | Warnings | N | N | N |
   ```

**5. Recomendación justificada:**
   ```markdown
   **🎯 RECOMENDACIÓN:** Propuesta X

   **Razones:**
   1. Razón técnica 1 (con datos)
   2. Razón técnica 2 (con datos)

   **Costo/Beneficio:** [Bajo/Medio/Alto]
   ```

**6. Usuario decide** cuáles aplicar

---

### **PARA IMPLEMENTACIÓN (feature nueva):**

**1. Evaluar formas de implementar**
   - Proponer al menos 2 alternativas viables

**2. Pros y contras de cada alternativa**

**3. Tabla comparativa** (mismos criterios)

**4. Recomendación justificada**

**5. Usuario decide** con cuál ir

---

## 📊 CRITERIOS DE EVALUACIÓN (Score 1-10)

### **1. Simpleza**
- **10/10** - Código directo, sin over-engineering
- **6/10** - Funcional pero con complejidad innecesaria
- **2/10** - Over-engineering severo

### **2. Legibilidad**
- **10/10** - Se entiende a primera vista, nombres claros
- **6/10** - Requiere análisis, nombres genéricos
- **2/10** - Código críptico

### **3. Funcionalidad**
- **10/10** - Funciona perfectamente, todos los casos cubiertos
- **6/10** - Funciona en casos normales
- **2/10** - No funciona correctamente

### **4. Eficiencia**
- **10/10** - Algoritmo óptimo (ej: O(n) vs O(n²))
- **6/10** - Performance aceptable
- **2/10** - Muy ineficiente

### **5. Mantenibilidad**
- **10/10** - Fácil cambiar, modular, DRY aplicado
- **6/10** - Modificable con esfuerzo medio
- **2/10** - Spaghetti code

**Nota:** Los criterios NO tienen peso fijo. Contexto determina importancia (ej: 20 líneas legibles vs 10 líneas densas).

---

## ✅ VALIDACIÓN PREVIA OBLIGATORIA

Antes de proponer cambios:
- [ ] **Compilar** código para verificar sintaxis
- [ ] **Verificar lógica** durante edición (no romper funcionalidad)
- [ ] **Consultar consola** para errores/warnings
- [ ] **Logs puntuales**: Agregar logs útiles (INFO/DEBUG según necesidad)
- [ ] **Logs temporales**: Si necesario para testing, marcar como `// TODO: Remove after testing`

---

## ⚠️ CAMBIOS MULTI-ARCHIVO

Si una mejora/refactor toca **2+ archivos:**
- **AVISAR:** Listar todas las clases involucradas ANTES de aplicar
- **Usuario hará respaldo** antes de confirmar
- **Aplicar paso a paso** (no todo de golpe)

---

## 🔢 LÍMITE DE PROPUESTAS

- **Paso a paso, por partes**
- No saturar con 10+ cambios simultáneos
- Agrupar por tipo (ej: "Fase 1: Warnings", "Fase 2: DRY", etc.)
- Máximo **3-5 problemas por análisis**

---

## 🚫 REGLAS DE ORO

### **YAGNI (You Aren't Gonna Need It)**
```java
// ❌ MAL - Genérico sin uso
private <T> void ejecutarAsync(String nombre, Task<T> task) { ... }

// ✅ BIEN - Específico
private void ejecutarLoginAsync(Task<Usuario> task) { ... }
```

### **DRY (Don't Repeat Yourself)**
```java
// ❌ MAL - Duplicado 3+ veces
progressOverlay.setVisible(false);
btnLogin.setDisable(false);
btnClose.setDisable(false);

// ✅ BIEN - Extraído a método
private void finalizarLogin() {
    progressOverlay.setVisible(false);
    btnLogin.setDisable(false);
    btnClose.setDisable(false);
}
```

### **Eliminar Magic Numbers/Strings**
```java
// ❌ MAL
if (event.getSceneY() <= 40) { stage.setOpacity(0.65); }

// ✅ BIEN
private static final int HEADER_HEIGHT = 40;
private static final double DRAG_OPACITY = 0.65;

if (event.getSceneY() <= HEADER_HEIGHT) {
    stage.setOpacity(DRAG_OPACITY);
}
```

### **Corrección de Warnings**
**Jerarquía de soluciones:**
1. ✅ Refactorizar código (mejor solución)
2. ⚠️ Renombrar parámetros (ej: `event` → `ignoredEvent`)
3. ❌ `@SuppressWarnings` (último recurso, con justificación)

---

## 📝 TEMPLATE DE ANÁLISIS

```markdown
## 🔍 [FIX/WARNING/MEJORA/REFACTOR/IMPLEMENTACIÓN] #N: [Título]

**📍 Ubicación:** Clase.java - Líneas X-Y
**⚠️ Clases involucradas:** [Si multi-archivo]

---

### ❌ CÓDIGO ACTUAL
```java
// Solo líneas relevantes
```

**✅ PROS:**
- Pro 1

**❌ CONTRAS:**
- Contra 1 (el problema)

---

### ✅ PROPUESTA A: [Nombre]
```java
// código propuesto
```

**✅ PROS:**
- Pro 1 (razón técnica)

**❌ CONTRAS:**
- Contra 1 (trade-off)

---

### ✅ PROPUESTA B: [Nombre]
(mismo formato)

---

### 📊 COMPARATIVA

| Criterio | Actual | Propuesta A | Propuesta B |
|----------|--------|-------------|-------------|
| Simpleza | X/10 - razón | Y/10 - razón | Z/10 - razón |
| Legibilidad | X/10 - razón | Y/10 - razón | Z/10 - razón |
| Funcionalidad | X/10 - razón | Y/10 - razón | Z/10 - razón |
| Eficiencia | X/10 - razón | Y/10 - razón | Z/10 - razón |
| Mantenibilidad | X/10 - razón | Y/10 - razón | Z/10 - razón |
| **Score Total** | **X/10** | **Y/10** | **Z/10** |
| Líneas código | N | ±X | ±Y |
| Warnings | N | -N | -N |

---

### 🎯 RECOMENDACIÓN: Propuesta X

**Razones:**
1. Razón técnica 1 (con datos)
2. Razón técnica 2 (con datos)

**Costo/Beneficio:** [Bajo/Medio/Alto]
```

---

## 🚀 MÉTRICAS DE ÉXITO (Post-aplicación)

| Métrica | Antes | Después | Mejora |
|---------|-------|---------|--------|
| Líneas totales | N | M | ±X% |
| Warnings | N | M | -X |
| Métodos | N | M | ±X |
| Constantes | N | M | +X |
| Magic numbers | N | 0 | -100% |
| Score general | X/10 | Y/10 | +Z% |

---

## ⚠️ NO HACER

- ❌ Aplicar cambios sin análisis comparativo
- ❌ Optimizar prematuramente sin medir
- ❌ Refactorizar código sin razón clara
- ❌ Introducir complejidad innecesaria
- ❌ Romper funcionalidad existente
- ❌ Proponer 10+ cambios simultáneos

## ✅ SÍ HACER

- ✅ Analizar antes de actuar
- ✅ Proponer mínimo 2 alternativas
- ✅ Medir con scores 1-10
- ✅ Justificar recomendación
- ✅ Avisar si multi-archivo
- ✅ Paso a paso, por partes
- ✅ Compilar y verificar

---

## 📘 MEJORES PRÁCTICAS Y CONVENCIONES

### Uso de Anotaciones Lombok (Obligatorio para nuevas implementaciones)

#### **@Slf4j - Logging**
```java
// ✅ CORRECTO (desde 22/11/2025)
@Slf4j
public class MiHelper {
    public void metodo() {
        log.info("Mensaje");
        log.warn("Advertencia");
        log.error("Error: {}", e.getMessage());
    }
}

// ❌ INCORRECTO (antiguo)
public class MiHelper {
    private static final Logger log = LoggerFactory.getLogger(MiHelper.class);
}
```

**Regla:** 
- ✅ **Nuevas implementaciones**: Usar `@Slf4j` SIEMPRE
- ✅ **Al editar código existente**: Migrar a `@Slf4j` si se toca el logging
- ❌ **NO crear** nuevos `Logger` manualmente

**Aplicado en:**
- ✅ TableViewHelper (22/11/2025)
- ✅ Toast (ya existía)
- ✅ DialogHelper (ya existía)
- 🔄 Pendiente: Controllers antiguos (cuando se editen)

---

#### **@RequiredArgsConstructor - Dependency Injection**
```java
// ✅ CORRECTO
@RequiredArgsConstructor
public class MarcasController {
    private final MarcaService marcaService;  // ← Inyección automática
    private final SessionManager sessionManager;
}

// ❌ INCORRECTO
public class MarcasController {
    @Autowired
    private MarcaService marcaService;  // ← Field injection (evitar)
}
```

**Regla:**
- ✅ Usar `@RequiredArgsConstructor` + `final` para servicios
- ✅ Constructor injection > Field injection
- ✅ Inmutabilidad de dependencias

---

#### **@Builder - Construcción de Objetos**
```java
// ✅ CORRECTO (en entidades)
@Entity
@Builder
public class Marca {
    // ...
}

// Uso en controllers
Marca nuevaMarca = Marca.builder()
    .nombre(nombre)
    .descripcion(descripcion)
    .estado(true)
    .build();
```

**Regla:**
- ✅ Usar `@Builder` en entidades para NUEVO
- ✅ Facilita creación de objetos con muchos campos
- ✅ Evita constructores con 10+ parámetros

---

### Objects vs Optional - Null Safety

#### **Objects.requireNonNullElse() - Valor default simple**
```java
// ✅ CORRECTO - Null-check simple con valor default
perfilUsuario = Objects.requireNonNullElse(sessionManager.getUserPerfil(), "");
txtDescripcion.setText(Objects.requireNonNullElse(marca.getDescripcionMarca(), ""));
```

#### **Optional<T> - Retornos que pueden no existir**
```java
// ✅ CORRECTO - Retorno de búsqueda + chain de operaciones
Optional<Marca> findById(Long id);

// Uso con orElseThrow
Marca marca = marcaService.findById(id)
    .orElseThrow(() -> new IllegalStateException("No encontrada"));

// Uso con chain (filter/map)
String nombre = Optional.ofNullable(sessionManager)
    .filter(SessionManager::isAutenticado)
    .map(SessionManager::getUserName)
    .orElse("Invitado");
```

#### **Cuándo usar cada uno**

| Escenario | Usar | Ejemplo |
|-----------|------|---------|
| Null-check simple con default | `Objects` | `Objects.requireNonNullElse(x, "")` |
| Retorno de findById/buscar | `Optional` | `Optional<T> findById(id)` |
| Chain filter/map/flatMap | `Optional` | `.filter().map().orElse()` |
| Parámetro de método | ❌ Ninguno | Usar overloads o validación |

**Regla:**
- ✅ `Objects` para asignación simple de valor default
- ✅ `Optional` para retornos que pueden no existir + transformaciones
- ❌ NO usar `Optional.ofNullable(x).orElse("")` para null-check simple (verbose)
- ❌ NO usar `Optional` como parámetro de método

---

### Gestión de CSS

#### **Cuándo cargar CSS en Java vs FXML**

| Caso | Solución | Razón |
|------|----------|-------|
| **Helper crea componentes dinámicos** | ✅ Cargar en Java | Toast, DialogHelper crean Stage/Scene sin FXML |
| **Helper trabaja con FXML existente** | ✅ Cargar en FXML | TableViewHelper, SearchHelper solo aplican clases |
| **Componente reutilizable** | ✅ CSS modular | Cada helper tiene su archivo CSS |

**Ejemplo correcto:**
```java
// ✅ Toast - Crea VBox dinámicamente
public class Toast {
    private static final String CSS_PATH = "/css/toast.css";
    
    private static VBox crearVBox() {
        VBox vbox = new VBox();
        URL cssUrl = Toast.class.getResource(CSS_PATH);
        vbox.getStylesheets().add(cssUrl.toExternalForm());
        return vbox;
    }
}

// ✅ TableViewHelper - Solo aplica clases CSS
public class TableViewHelper {
    // NO carga CSS, se carga en FXML
    public void configurarColumnaBooleana(...) {
        cell.getStyleClass().add("table-cell-activo");
    }
}
```

---

### Manejo de Focus en Contenedores Compuestos

#### **Problema: JavaFX no soporta `:focused-within`**

```css
/* ❌ NO funciona en JavaFX */
.search-container:focused-within {
    -fx-border-color: blue;
}
```

#### **Solución: Clase CSS dinámica desde Java**

**1. Agregar fx:id al contenedor (FXML):**
```xml
<HBox fx:id="searchContainer" styleClass="search-container">
    <TextField fx:id="txtBusqueda" />
</HBox>
```

**2. Listener en Controller:**
```java
@FXML private HBox searchContainer;
@FXML private TextField txtBusqueda;

private void configurarFocus() {
    txtBusqueda.focusedProperty().addListener((obs, wasFocused, isFocused) -> {
        if (isFocused) {
            searchContainer.getStyleClass().add("focused");
        } else {
            searchContainer.getStyleClass().remove("focused");
        }
    });
}
```

**3. CSS con clase dinámica:**
```css
/* ✅ Funciona */
.search-container.focused {
    -fx-border-color: #3B82F6 !important;
    -fx-background-color: #F0F9FF;
}
```

**Regla:**
- ✅ Usar clases CSS dinámicas para efectos en contenedores padre
- ✅ Solo para casos donde el foco está en hijo pero queremos efecto en padre
- ❌ NO necesario para TextField/TextArea simples (`:focused` funciona)

---

### Validación de Warnings del IDE

**Regla general:**
1. ✅ **Eliminar warnings** que puedan causar bugs futuros
2. ✅ **Ignorar** (con comentario justificado) si eliminar rompe funcionalidad
3. ❌ **NO ignorar** sin analizar primero

**Ejemplos de esta sesión:**

```java
// ✅ Warning corregido: "Calls to boolean method always inverted"
// ANTES
if (!dialogHelper.showConfirmacion(...)) return;

// DESPUÉS
if (dialogHelper.showConfirmacion(...)) {
    // lógica aquí
}

// ✅ Warning corregido: "Duplicated code fragment"
// Extraído a método privado reutilizable

// ✅ Warning corregido: "Parameter never used"
// Eliminado parámetro innecesario
```

---

### Efectos Visuales Consistentes

**Todos los inputs deben tener:**
1. ✅ Estado **normal** (borde gris claro)
2. ✅ Estado **hover** (borde gris oscuro + sombra sutil)
3. ✅ Estado **focus** (borde azul + sombra azul)
4. ✅ Estado **disabled** (fondo gris + opacidad)

**Todos los botones deben tener:**
1. ✅ Estado **normal** (color base + dropshadow)
2. ✅ Estado **hover** (color más oscuro + sombra más intensa)
3. ✅ Estado **pressed** (color aún más oscuro + translate 1px)
4. ✅ Estado **disabled** (gris + sin efectos)

---

### Documentación al Implementar

**Regla:**
- ✅ **Actualizar** `farmafx_resumen.md` al completar features grandes
- ✅ **Documentar** decisiones técnicas importantes
- ✅ **Registrar** bugs conocidos en sección "Pendiente de Revisión"
- ✅ **Mantener** fecha de última actualización

**Ejemplo de esta sesión:**
- ✅ CSS Modular v2.0 documentado
- ✅ Carga CSS por Helper documentada
- ✅ Bugs identificados registrados
- ✅ Flujo pendiente de revisión documentado

---

### Gestión de Focus Global ✅ (Implementado 23/11/2025)

#### **FocusHelper v1.0 - Helper estático**

```java
// FocusHelper.java - Quita focus de inputs al click fuera
public class FocusHelper {
    public static void configurarClickFuera(Node root, TextInputControl... inputs) {
        root.addEventFilter(MouseEvent.MOUSE_CLICKED, event -> {
            for (TextInputControl input : inputs) {
                if (debeQuitarFocus(input, event)) {
                    root.requestFocus();
                    return;
                }
            }
        });
    }
}

// Uso en Controller
FocusHelper.configurarClickFuera(root, txtPagina, txtBusqueda, txtNombre, txtDescripcion);
```

#### **Comportamiento implementado**

| Área clickeada | Focus final |
|----------------|-------------|
| **Tabla** (fila) | Tabla (selecciona fila) |
| **Área vacía** | Root (quita focus) |
| **Input/Button** | Mantiene focus |

---

### Helpers Estáticos vs Instancias

| Patrón | Cuándo usar | Ejemplo |
|--------|-------------|---------|
| **Estático** | Sin estado, configura una vez | `FocusHelper`, `CharCounterHelper` |
| **Instancia** | Mantiene estado, se consulta después | `PaginationHelper`, `ValidationHelper` |
| **Spring DI** | Acceso a BD, otros servicios | `MarcaService`, `SessionManager` |

---

**Versión:** 2.2
**Última actualización:** 2025-11-23 01:30
**Cambios sesión 23/11/2025:**
- Objects vs Optional - Null Safety (cuándo usar cada uno)
- FocusHelper implementado (ya no pendiente)
- Helpers estáticos vs instancias
- @Slf4j migrado a LoginController y MainGuiController