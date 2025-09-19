package pe.edu.upeu.farmafx.control;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import pe.edu.upeu.farmafx.enums.Estado;
import pe.edu.upeu.farmafx.modelo.Marca;
import pe.edu.upeu.farmafx.servicio.MarcaServicioI;
import pe.edu.upeu.farmafx.utils.NavegadorVistas;
import pe.edu.upeu.farmafx.utils.Vistas;

@Controller
public class MarcaController {

    @Autowired
    private MarcaServicioI marcaServicio;
    @Autowired
    private NavegadorVistas navegador;

    @FXML private TableView<Marca> tablaMarcas;
    @FXML private TableColumn<Marca, Integer> colId;
    @FXML private TableColumn<Marca, String> colNombre;
    @FXML private TableColumn<Marca, Estado> colEstado;

    @FXML private TextField nombreTextField;
    @FXML private CheckBox activoCheckBox;
    @FXML private VBox panelEdicion;

    private Marca marcaSeleccionada;

    @FXML
    public void initialize() {
        configurarTabla();
        cargarMarcas();
        panelEdicion.setDisable(true);
    }

    private void configurarTabla() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colNombre.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        colEstado.setCellValueFactory(new PropertyValueFactory<>("estado"));

        colEstado.setCellFactory(c -> new TableCell<>() {
            @Override
            protected void updateItem(Estado item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.toString());
            }
        });

        tablaMarcas.getSelectionModel().selectedItemProperty().addListener((obs, old, sel) -> {
            this.marcaSeleccionada = sel;
            if (sel != null) {
                mostrarDatosEnPanel(sel);
                panelEdicion.setDisable(false);
            } else {
                limpiarPanel();
                panelEdicion.setDisable(true);
            }
        }   );
    }

    private void mostrarDatosEnPanel(Marca marca) {
        nombreTextField.setText(marca.getNombre());
        activoCheckBox.setSelected(marca.getEstado() == Estado.ACTIVO);
    }

    private void limpiarPanel() {
        nombreTextField.clear();
        activoCheckBox.setSelected(false);
    }

    private void cargarMarcas() {
        ObservableList<Marca> listaObservableMarcas = FXCollections.observableArrayList(marcaServicio.listarMarcas());
        tablaMarcas.setItems(listaObservableMarcas);
        tablaMarcas.refresh();
    }

    @FXML
    void nuevoAccion(ActionEvent event) {
        tablaMarcas.getSelectionModel().clearSelection();
        this.marcaSeleccionada = null;
        limpiarPanel();
        activoCheckBox.setSelected(true);
        panelEdicion.setDisable(false);
        nombreTextField.requestFocus();
    }

    @FXML
    void guardarAccion(ActionEvent event) {
        String nombre = nombreTextField.getText();
        if (nombre == null || nombre.trim().isEmpty()) {
            mostrarAlerta("Error", "El nombre no puede estar vacío.");
            return;
        }

        Estado estadoSeleccionado = activoCheckBox.isSelected() ? Estado.ACTIVO : Estado.INACTIVO;

        if (this.marcaSeleccionada != null) {
            marcaSeleccionada.setNombre(nombre);
            marcaSeleccionada.setEstado(estadoSeleccionado);
            marcaServicio.guardarMarca(marcaSeleccionada);
            mostrarAlerta("Éxito", "Marca actualizada correctamente.");
        } else {
            Marca nuevaMarca = new Marca();
            nuevaMarca.setNombre(nombre);
            nuevaMarca.setEstado(estadoSeleccionado);
            marcaServicio.guardarMarca(nuevaMarca);
            mostrarAlerta("Éxito", "Marca creada correctamente.");
        }

        cargarMarcas();
        tablaMarcas.getSelectionModel().clearSelection();
    }

    @FXML
    void eliminarAccion(ActionEvent event) {
        Marca seleccionada = tablaMarcas.getSelectionModel().getSelectedItem();
        if (seleccionada == null) {
            mostrarAlerta("Error", "Debe seleccionar una marca para eliminar.");
            return;
        }

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmar Eliminación");
        alert.setHeaderText("¿Eliminar la marca: " + seleccionada.getNombre() + "?");

        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                marcaServicio.eliminarMarca(seleccionada.getId());
                cargarMarcas();
            }
        });
    }

    @FXML
    void volverAccion(ActionEvent event) {
        navegador.cambiarEscena((Node) event.getSource(), Vistas.MENU_ADMIN, "Panel de Administrador");
    }

    private void mostrarAlerta(String titulo, String mensaje) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }
}