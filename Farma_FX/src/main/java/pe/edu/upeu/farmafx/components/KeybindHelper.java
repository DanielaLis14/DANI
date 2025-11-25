package pe.edu.upeu.farmafx.components;

import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;

import javafx.event.EventHandler;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.function.BooleanSupplier;

/**
 * Helper para gestión centralizada de atajos de teclado en controllers CRUD.
 * Proporciona una API fluent para registrar keybinds de forma declarativa.
 *
 * <p>Características:</p>
 * <ul>
 *   <li>Teclas simples (F5, Delete, ESC)</li>
 *   <li>Modificadores (Ctrl, Shift, Alt)</li>
 *   <li>Condiciones opcionales (solo ejecutar si botón está enabled)</li>
 *   <li>ESC genérico para CRUDs con 3 prioridades</li>
 * </ul>
 *
 * <p>Uso típico:</p>
 * <pre>{@code
 * keybindHelper = new KeybindHelper(tableMarcas.getScene())
 *     .onEscapeCrud(txtPagina, tableMarcas,
 *                   this::estaFormularioEditable,
 *                   this::cancelar,
 *                   this::limpiarFormulario,
 *                   this::actualizarBotones)
 *     .on(KeyCode.F5, this::limpiar)
 *     .on(KeyCode.DELETE, this::eliminarSeleccionado, () -> isEnabled(btnEliminar))
 *     .onCtrl(KeyCode.N, this::nuevo, () -> isEnabled(btnNuevo))
 *     .onCtrl(KeyCode.S, this::guardar, () -> isEnabled(btnGuardar))
 *     .onCtrl(KeyCode.E, this::editar, () -> isEnabled(btnEditar))
 *     .configure();
 * }</pre>
 *
 * @version 1.0
 * @since 2025-11-22
 */
public class KeybindHelper {

    /**
     * Registro de handlers previos por Scene.
     * WeakHashMap permite que Scenes garbage-collected se limpien automáticamente.
     */
    private static final Map<Scene, EventHandler<KeyEvent>> activeHandlers = new WeakHashMap<>();

    /**
     * Representa un keybind registrado.
     *
     * @param keyCode Tecla (F5, DELETE, N, etc.)
     * @param ctrl true si requiere Ctrl presionado
     * @param shift true si requiere Shift presionado
     * @param alt true si requiere Alt presionado
     * @param action Acción a ejecutar
     * @param condition Condición opcional (null = siempre ejecutar)
     */
    private record Keybind(
        KeyCode keyCode,
        boolean ctrl,
        boolean shift,
        boolean alt,
        Runnable action,
        BooleanSupplier condition
    ) {
        boolean matches(KeyEvent event) {
            return event.getCode() == keyCode
                && event.isControlDown() == ctrl
                && event.isShiftDown() == shift
                && event.isAltDown() == alt;
        }

        boolean canExecute() {
            return condition == null || condition.getAsBoolean();
        }
    }

    private final Scene scene;
    private final List<Keybind> keybinds = new ArrayList<>();

    /**
     * Constructor del helper.
     *
     * @param scene Scene donde se registrarán los atajos (obtenida de cualquier Node)
     */
    public KeybindHelper(Scene scene) {
        this.scene = scene;
    }

    // ============ REGISTRO DE KEYBINDS ============

    /**
     * Registra un atajo de tecla simple (sin modificadores).
     *
     * @param keyCode Tecla a escuchar
     * @param action Acción a ejecutar
     * @return this (para encadenamiento)
     */
    public KeybindHelper on(KeyCode keyCode, Runnable action) {
        return on(keyCode, action, null);
    }

    /**
     * Registra un atajo de tecla simple con condición.
     *
     * @param keyCode Tecla a escuchar
     * @param action Acción a ejecutar
     * @param condition Condición que debe cumplirse (null = siempre)
     * @return this (para encadenamiento)
     */
    public KeybindHelper on(KeyCode keyCode, Runnable action, BooleanSupplier condition) {
        keybinds.add(new Keybind(keyCode, false, false, false, action, condition));
        return this;
    }

    /**
     * Registra un atajo con Ctrl + tecla.
     *
     * @param keyCode Tecla a escuchar
     * @param action Acción a ejecutar
     * @return this (para encadenamiento)
     */
    public KeybindHelper onCtrl(KeyCode keyCode, Runnable action) {
        return onCtrl(keyCode, action, null);
    }

    /**
     * Registra un atajo con Ctrl + tecla y condición.
     *
     * @param keyCode Tecla a escuchar
     * @param action Acción a ejecutar
     * @param condition Condición que debe cumplirse
     * @return this (para encadenamiento)
     */
    public KeybindHelper onCtrl(KeyCode keyCode, Runnable action, BooleanSupplier condition) {
        keybinds.add(new Keybind(keyCode, true, false, false, action, condition));
        return this;
    }

    /**
     * Registra un atajo con Shift + tecla.
     *
     * @param keyCode Tecla a escuchar
     * @param action Acción a ejecutar
     * @return this (para encadenamiento)
     */
    public KeybindHelper onShift(KeyCode keyCode, Runnable action) {
        return onShift(keyCode, action, null);
    }

    /**
     * Registra un atajo con Shift + tecla y condición.
     *
     * @param keyCode Tecla a escuchar
     * @param action Acción a ejecutar
     * @param condition Condición que debe cumplirse
     * @return this (para encadenamiento)
     */
    public KeybindHelper onShift(KeyCode keyCode, Runnable action, BooleanSupplier condition) {
        keybinds.add(new Keybind(keyCode, false, true, false, action, condition));
        return this;
    }

    /**
     * Registra un atajo con Alt + tecla.
     *
     * @param keyCode Tecla a escuchar
     * @param action Acción a ejecutar
     * @return this (para encadenamiento)
     */
    public KeybindHelper onAlt(KeyCode keyCode, Runnable action) {
        return onAlt(keyCode, action, null);
    }

    /**
     * Registra un atajo con Alt + tecla y condición.
     *
     * @param keyCode Tecla a escuchar
     * @param action Acción a ejecutar
     * @param condition Condición que debe cumplirse
     * @return this (para encadenamiento)
     */
    public KeybindHelper onAlt(KeyCode keyCode, Runnable action, BooleanSupplier condition) {
        keybinds.add(new Keybind(keyCode, false, false, true, action, condition));
        return this;
    }

    // ============ ESCAPE GENÉRICO PARA CRUDs ============

    /**
     * Registra comportamiento ESC estándar para CRUDs con 3 niveles de prioridad:
     * <ol>
     *   <li>Si TextField paginación tiene focus → quitar focus</li>
     *   <li>Si formulario está en edición → cancelar</li>
     *   <li>Si hay item seleccionado en tabla → deseleccionar y limpiar</li>
     * </ol>
     *
     * @param txtPagina TextField de paginación (puede ser null si no hay)
     * @param tabla TableView del CRUD
     * @param estaEditando Supplier que indica si el formulario está en modo edición
     * @param cancelar Acción para cancelar edición
     * @param limpiarFormulario Acción para limpiar campos del formulario
     * @param actualizarBotones Acción para actualizar estado de botones
     * @return this (para encadenamiento)
     */
    public KeybindHelper onEscapeCrud(
            TextField txtPagina,
            TableView<?> tabla,
            BooleanSupplier estaEditando,
            Runnable cancelar,
            Runnable limpiarFormulario,
            Runnable actualizarBotones) {

        return on(KeyCode.ESCAPE, () -> {
            // Prioridad 1: Si TextField paginación tiene focus, quitarlo
            if (txtPagina != null && txtPagina.isFocused()) {
                txtPagina.getParent().requestFocus();
                return;
            }

            // Prioridad 2: Si está editando, cancelar
            if (estaEditando.getAsBoolean()) {
                cancelar.run();
                return;
            }

            // Prioridad 3: Si hay item seleccionado, deseleccionar
            if (tabla.getSelectionModel().getSelectedItem() != null) {
                tabla.getSelectionModel().clearSelection();
                limpiarFormulario.run();
                actualizarBotones.run();
            }
        });
    }

    // ============ CONFIGURACIÓN ============

    /**
     * Configura el listener de teclado en la Scene.
     * Debe llamarse después de registrar todos los keybinds.
     * <p>
     * IMPORTANTE: Remueve automáticamente cualquier KeybindHelper previo
     * registrado en la misma Scene para evitar duplicación de listeners.
     *
     * @return this (para encadenamiento)
     * @throws IllegalStateException si Scene es null o no hay keybinds registrados
     */
    public KeybindHelper configure() {
        if (scene == null) {
            throw new IllegalStateException("Scene es null - verificar que el Node tenga Scene asignada");
        }

        if (keybinds.isEmpty()) {
            throw new IllegalStateException("No hay keybinds registrados - llamar on()/onCtrl()/etc. antes de configure()");
        }

        // Remover handler previo si existe (evita acumulación de listeners)
        EventHandler<KeyEvent> previousHandler = activeHandlers.get(scene);
        if (previousHandler != null) {
            scene.removeEventFilter(KeyEvent.KEY_PRESSED, previousHandler);
        }

        // Crear y registrar nuevo handler
        EventHandler<KeyEvent> newHandler = this::handleKeyEvent;
        scene.addEventFilter(KeyEvent.KEY_PRESSED, newHandler);
        activeHandlers.put(scene, newHandler);

        return this;
    }

    /**
     * Manejador interno de eventos de teclado.
     * Busca el primer keybind que coincida y ejecuta su acción.
     */
    private void handleKeyEvent(KeyEvent event) {
        for (Keybind keybind : keybinds) {
            if (keybind.matches(event) && keybind.canExecute()) {
                event.consume();
                keybind.action().run();
                return; // Solo ejecutar el primer match
            }
        }
    }

    // ============ UTILIDADES ============

    /**
     * Verifica si un botón está visible y habilitado.
     * Útil para condiciones de keybinds.
     *
     * <p>Uso:</p>
     * <pre>{@code
     * .onCtrl(KeyCode.N, this::nuevo, () -> KeybindHelper.isEnabled(btnNuevo))
     * }</pre>
     *
     * @param button Botón a verificar
     * @return true si el botón es visible y no está deshabilitado
     */
    public static boolean isEnabled(Button button) {
        return button != null && button.isVisible() && !button.isDisabled();
    }
}
