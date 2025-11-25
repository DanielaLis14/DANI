package pe.edu.upeu.farmafx.components;

import javafx.application.Platform;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.scene.input.KeyCode;
import lombok.extern.slf4j.Slf4j;

import java.util.function.IntConsumer;
import java.util.regex.Pattern;

/**
 * Helper reutilizable para paginación con TextField editable.
 * Layout: [Primera] [Anterior] Página [_3_] de 10 [Siguiente] [Última]
 *
 * <p>Uso típico:</p>
 * <pre>{@code
 * paginationHelper = new PaginationHelper(
 *     txtPagina, lblTotal,
 *     btnPrimera, btnAnterior, btnSiguiente, btnUltima,
 *     pagina -> {
 *         paginaActual = pagina;
 *         cargarDatos();
 *     }
 * ).configure();
 *
 * // Después de cargar datos:
 * paginationHelper.sincronizar(page.getNumber(), page.getTotalPages());
 * }</pre>
 *
 * @author Claude Code
 * @version 2.0 - Limpieza API (eliminado @Getter sin uso)
 */
@Slf4j
public class PaginationHelper {

    private static final Pattern SOLO_NUMEROS = Pattern.compile("\\d*");

    private final TextField txtPagina;
    private final Label lblTotal;
    private final Button btnPrimera;
    private final Button btnAnterior;
    private final Button btnSiguiente;
    private final Button btnUltima;
    private final IntConsumer onPageChange;

    private int paginaActual = 0;
    private int totalPaginas = 1;

    /**
     * Constructor del helper de paginación.
     *
     * @param txtPagina    TextField para escribir número de página
     * @param lblTotal     Label que muestra "de X"
     * @param btnPrimera   Botón ir a primera página
     * @param btnAnterior  Botón ir a página anterior
     * @param btnSiguiente Botón ir a página siguiente
     * @param btnUltima    Botón ir a última página
     * @param onPageChange Callback cuando cambia la página (recibe índice 0-based)
     */
    public PaginationHelper(TextField txtPagina, Label lblTotal,
                           Button btnPrimera, Button btnAnterior,
                           Button btnSiguiente, Button btnUltima,
                           IntConsumer onPageChange) {
        this.txtPagina = txtPagina;
        this.lblTotal = lblTotal;
        this.btnPrimera = btnPrimera;
        this.btnAnterior = btnAnterior;
        this.btnSiguiente = btnSiguiente;
        this.btnUltima = btnUltima;
        this.onPageChange = onPageChange;
    }

    /**
     * Configura el helper: listeners de botones y TextField.
     * @return this para encadenar
     */
    public PaginationHelper configure() {
        configurarTextField();
        configurarBotones();
        actualizarUI();
        log.debug("PaginationHelper configurado");
        return this;
    }

    /**
     * Sincroniza el helper con el estado actual de paginación.
     * Llamar después de cargar datos del servidor.
     *
     * @param paginaActual Página actual (0-based)
     * @param totalPaginas Total de páginas
     */
    public void sincronizar(int paginaActual, int totalPaginas) {
        this.totalPaginas = Math.max(0, totalPaginas);
        this.paginaActual = totalPaginas == 0 ? 0 : Math.min(paginaActual, totalPaginas - 1);
        Platform.runLater(this::actualizarUI);
    }

    /**
     * Reinicia la paginación a página 0.
     */
    public void reset() {
        sincronizar(0, 1);
    }

    /**
     * Deshabilita TODOS los controles de paginación.
     * Usar cuando se entra en modo edición/nuevo.
     */
    public void disable() {
        txtPagina.setDisable(true);
        btnPrimera.setDisable(true);
        btnAnterior.setDisable(true);
        btnSiguiente.setDisable(true);
        btnUltima.setDisable(true);
    }

    /**
     * Habilita controles según estado actual de paginación.
     * Usar al salir de modo edición.
     */
    public void enable() {
        actualizarUI();
    }

    /**
     * Configura el TextField con validación numérica y navegación por Enter.
     */
    private void configurarTextField() {
        // Prevenir auto-focus cuando botones se deshabilitan (focus traversal)
        // pero permitir clic manual del usuario
        txtPagina.setFocusTraversable(false);
        
        // Solo permite números
        txtPagina.setTextFormatter(new TextFormatter<>(change -> {
            String newText = change.getControlNewText();
            if (SOLO_NUMEROS.matcher(newText).matches()) {
                return change;
            }
            return null;
        }));

        // Auto-seleccionar todo el texto al recibir focus (como Ctrl+A)
        txtPagina.focusedProperty().addListener((obs, wasFocused, isNowFocused) -> {
            if (isNowFocused) {
                Platform.runLater(txtPagina::selectAll);
            }
        });

        // Enter para navegar (ESC se maneja a nivel Scene en el Controller)
        txtPagina.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                navegarDesdeTextField();
                event.consume();
            }
        });

        // Click fuera simplemente pierde foco (NO navega automáticamente)
        // Solo Enter navega de forma explícita
    }

    /**
     * Configura los listeners de los botones.
     */
    private void configurarBotones() {
        btnPrimera.setOnAction(e -> irAPagina(0));
        btnAnterior.setOnAction(e -> irAPagina(paginaActual - 1));
        btnSiguiente.setOnAction(e -> irAPagina(paginaActual + 1));
        btnUltima.setOnAction(e -> irAPagina(totalPaginas - 1));
    }

    /**
     * Navega a la página escrita en el TextField.
     */
    private void navegarDesdeTextField() {
        String texto = txtPagina.getText().trim();
        if (texto.isEmpty()) {
            actualizarUI(); // Restaurar valor actual
            return;
        }

        try {
            int paginaIngresada = Integer.parseInt(texto);
            int paginaDestino = paginaIngresada - 1; // Convertir a 0-based

            // Validar rango
            if (paginaDestino < 0) {
                paginaDestino = 0;
            } else if (paginaDestino >= totalPaginas) {
                paginaDestino = totalPaginas - 1;
            }

            if (paginaDestino != paginaActual) {
                irAPagina(paginaDestino);
            } else {
                actualizarUI(); // Restaurar valor si es la misma página
            }
        } catch (NumberFormatException e) {
            actualizarUI(); // Restaurar valor actual si hay error
        }
    }

    /**
     * Navega a una página específica si está en rango válido.
     * @param pagina Índice de página (0-based)
     */
    private void irAPagina(int pagina) {
        if (pagina >= 0 && pagina < totalPaginas && pagina != paginaActual) {
            paginaActual = pagina;
            onPageChange.accept(pagina);
            log.debug("Navegando a página {} de {}", pagina + 1, totalPaginas);
        }
    }

    /**
     * Actualiza la UI: TextField, Label y estados de botones.
     */
    private void actualizarUI() {
        // Caso sin resultados: mostrar "0 de 0" y deshabilitar todo
        if (totalPaginas == 0) {
            txtPagina.setText("0");
            txtPagina.setDisable(true);
            lblTotal.setText("de 0");
            btnPrimera.setDisable(true);
            btnAnterior.setDisable(true);
            btnSiguiente.setDisable(true);
            btnUltima.setDisable(true);
            return;
        }

        // Habilitar TextField cuando hay páginas
        txtPagina.setDisable(false);

        // TextField muestra página 1-based
        txtPagina.setText(String.valueOf(paginaActual + 1));

        // Label "de X"
        lblTotal.setText("de " + totalPaginas);

        // Estados de botones
        boolean esPrimera = paginaActual == 0;
        boolean esUltima = paginaActual >= totalPaginas - 1;

        btnPrimera.setDisable(esPrimera);
        btnAnterior.setDisable(esPrimera);
        btnSiguiente.setDisable(esUltima);
        btnUltima.setDisable(esUltima);

        // Si el campo tiene focus, seleccionar todo para fácil edición
        if (txtPagina.isFocused()) {
            Platform.runLater(txtPagina::selectAll);
        }
    }
}