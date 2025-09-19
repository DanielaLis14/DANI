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
import pe.edu.upeu.farmafx.enums.RolUsuario;
import pe.edu.upeu.farmafx.modelo.Usuario;
import pe.edu.upeu.farmafx.servicio.UsuarioServicioI;
import pe.edu.upeu.farmafx.utils.NavegadorVistas;
import pe.edu.upeu.farmafx.utils.Vistas;

import java.util.Optional;

@Controller
public class UsuarioController {

    @Autowired
    private UsuarioServicioI usuarioServicio;
    @Autowired
    private NavegadorVistas navegador;

    @FXML private TableView<Usuario> tablaUsuarios;
    @FXML private TableColumn<Usuario, String> colDni;
    @FXML private TableColumn<Usuario, RolUsuario> colRol;
    @FXML private TableColumn<Usuario, Estado> colEstado;

    @FXML private VBox panelEdicion;
    @FXML private TextField dniTextField;
    @FXML private PasswordField clavePasswordField;
    @FXML private ComboBox<RolUsuario> rolComboBox;
    @FXML private CheckBox estadoCheckBox;

    private Usuario usuarioSeleccionado;

    @FXML
    public void initialize() {
        configurarTabla();
        cargarUsuarios();
        panelEdicion.setDisable(true);
        rolComboBox.setItems(FXCollections.observableArrayList(RolUsuario.values()));
    }

    private void configurarTabla() {
        colDni.setCellValueFactory(new PropertyValueFactory<>("dni"));
        colRol.setCellValueFactory(new PropertyValueFactory<>("rol"));
        colEstado.setCellValueFactory(new PropertyValueFactory<>("estado"));

        tablaUsuarios.getSelectionModel().selectedItemProperty().addListener((obs, old, sel) -> {
            this.usuarioSeleccionado = sel;
            if (sel != null) {
                mostrarDatosEnPanel(sel);
                panelEdicion.setDisable(false);
            } else {
                limpiarPanel();
                panelEdicion.setDisable(true);
            }
        });
    }

    private void mostrarDatosEnPanel(Usuario usuario) {
        dniTextField.setText(usuario.getDni());
        clavePasswordField.clear();
        rolComboBox.setValue(usuario.getRol());
        estadoCheckBox.setSelected(usuario.getEstado() == Estado.ACTIVO);
        dniTextField.setDisable(true);
        clavePasswordField.setDisable(true);
    }

    private void limpiarPanel() {
        dniTextField.clear();
        clavePasswordField.clear();
        rolComboBox.setValue(null);
        estadoCheckBox.setSelected(false);
        dniTextField.setDisable(false);
        clavePasswordField.setDisable(false);
    }

    private void cargarUsuarios() {
        ObservableList<Usuario> lista = FXCollections.observableArrayList(usuarioServicio.listarTodosLosUsuarios());
        tablaUsuarios.setItems(lista);
        tablaUsuarios.refresh();
    }

    @FXML
    void nuevoAccion(ActionEvent event) {
        tablaUsuarios.getSelectionModel().clearSelection();
        this.usuarioSeleccionado = null;
        limpiarPanel();
        rolComboBox.setValue(RolUsuario.CLIENTE);
        estadoCheckBox.setSelected(true);
        panelEdicion.setDisable(false);
        dniTextField.requestFocus();
    }

    @FXML
    void guardarAccion(ActionEvent event) {
        Estado estadoSeleccionado = estadoCheckBox.isSelected() ? Estado.ACTIVO : Estado.INACTIVO;

        if (this.usuarioSeleccionado != null) {
            usuarioSeleccionado.setRol(rolComboBox.getValue());
            usuarioSeleccionado.setEstado(estadoSeleccionado);
            usuarioServicio.guardarUsuario(usuarioSeleccionado);
            mostrarAlerta("Éxito", "Usuario actualizado correctamente.");
        } else {
            if (dniTextField.getText().isEmpty() || clavePasswordField.getText().isEmpty() || rolComboBox.getValue() == null) {
                mostrarAlerta("Error", "DNI, Contraseña y Rol son obligatorios.");
                return;
            }
            Usuario nuevoUsuario = new Usuario();
            nuevoUsuario.setDni(dniTextField.getText());
            nuevoUsuario.setClave(clavePasswordField.getText());
            nuevoUsuario.setRol(rolComboBox.getValue());
            nuevoUsuario.setEstado(estadoSeleccionado);

            String resultado = usuarioServicio.registrarUsuario(nuevoUsuario);
            mostrarAlerta(resultado.startsWith("Error") ? "Error" : "Éxito", resultado);
        }
        cargarUsuarios();
        tablaUsuarios.getSelectionModel().clearSelection();
    }

    @FXML
    void eliminarAccion(ActionEvent event) {
        if (this.usuarioSeleccionado == null) {
            mostrarAlerta("Error", "Debe seleccionar un usuario para eliminar.");
            return;
        }
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmar Eliminación");
        alert.setHeaderText("¿Eliminar al usuario con DNI: " + usuarioSeleccionado.getDni() + "?");
        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                usuarioServicio.eliminarUsuarioPorDni(usuarioSeleccionado.getDni());
                cargarUsuarios();
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