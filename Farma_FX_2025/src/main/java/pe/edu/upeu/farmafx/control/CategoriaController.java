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
import pe.edu.upeu.farmafx.modelo.Categoria;
import pe.edu.upeu.farmafx.servicio.CategoriaServicioI;
import pe.edu.upeu.farmafx.utils.NavegadorVistas;
import pe.edu.upeu.farmafx.utils.Vistas;

@Controller
public class CategoriaController {

    @Autowired
    private CategoriaServicioI categoriaServicio;
    @Autowired
    private NavegadorVistas navegador;

    @FXML private TableView<Categoria> tablaCategorias;
    @FXML private TableColumn<Categoria, Integer> colId;
    @FXML private TableColumn<Categoria, String> colNombre;
    @FXML private TableColumn<Categoria, Estado> colEstado;

    @FXML private VBox panelEdicion;
    @FXML private TextField nombreTextField;
    @FXML private CheckBox activoCheckBox;

    private ObservableList<Categoria> listaObservableCategorias;
    private Categoria categoriaSeleccionada;

    @FXML
    public void initialize() {
        configurarTabla();
        cargarCategorias();
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

        tablaCategorias.getSelectionModel().selectedItemProperty().addListener((obs, old, sel) -> {
            this.categoriaSeleccionada = sel;
            if (sel != null) {
                mostrarDatosEnPanel(sel);
                panelEdicion.setDisable(false);
            } else {
                limpiarPanel();
                panelEdicion.setDisable(true);
            }
        });
    }

    private void mostrarDatosEnPanel(Categoria categoria) {
        nombreTextField.setText(categoria.getNombre());
        activoCheckBox.setSelected(categoria.getEstado() == Estado.ACTIVO);
    }

    private void limpiarPanel() {
        nombreTextField.clear();
        activoCheckBox.setSelected(false);
    }

    private void cargarCategorias() {
        listaObservableCategorias = FXCollections.observableArrayList(categoriaServicio.listarCategorias());
        tablaCategorias.setItems(listaObservableCategorias);
        tablaCategorias.refresh();
    }

    @FXML
    void nuevoAccion(ActionEvent event) {
        tablaCategorias.getSelectionModel().clearSelection();
        categoriaSeleccionada = null;
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
        if (this.categoriaSeleccionada != null) {
            categoriaSeleccionada.setNombre(nombre);
            Estado estadoSeleccionado = activoCheckBox.isSelected() ? Estado.ACTIVO : Estado.INACTIVO;
            categoriaSeleccionada.setEstado(estadoSeleccionado);
            categoriaServicio.guardarCategoria(categoriaSeleccionada);
            mostrarAlerta("Éxito", "Categoría actualizada correctamente.");
        }
        else {
            Categoria nuevaCategoria = new Categoria();
            nuevaCategoria.setNombre(nombre);
            Estado estadoSeleccionado = activoCheckBox.isSelected() ? Estado.ACTIVO : Estado.INACTIVO;
            nuevaCategoria.setEstado(estadoSeleccionado);
            categoriaServicio.guardarCategoria(nuevaCategoria);
            mostrarAlerta("Éxito", "Categoría creada correctamente.");
        }
        cargarCategorias();
        tablaCategorias.getSelectionModel().clearSelection();
    }

    @FXML
    void eliminarAccion(ActionEvent event) {
        Categoria seleccionada = tablaCategorias.getSelectionModel().getSelectedItem();
        if (seleccionada == null) {
            mostrarAlerta("Error","Debe seleccionar una categoría.");
            return;
        }

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmar Eliminación");
        alert.setHeaderText("¿Eliminar la categoría: " + seleccionada.getNombre() + "?");

        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                categoriaServicio.eliminarCategoria(seleccionada.getId());
                cargarCategorias();
            }
        });
    }

    @FXML
    void volverAccion(ActionEvent event) {
        Node sourceNode = (Node) event.getSource();
        navegador.cambiarEscena(sourceNode, Vistas.MENU_ADMIN, "Panel de Administrador");
    }

    private void mostrarAlerta(String titulo, String mensaje) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }
}