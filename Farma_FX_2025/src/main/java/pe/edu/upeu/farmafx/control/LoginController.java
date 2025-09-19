package pe.edu.upeu.farmafx.control;

import pe.edu.upeu.farmafx.FarmaFXApplication;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import pe.edu.upeu.farmafx.enums.RolUsuario;
import pe.edu.upeu.farmafx.modelo.Usuario;
import pe.edu.upeu.farmafx.servicio.UsuarioServicioI;
import pe.edu.upeu.farmafx.utils.NavegadorVistas;
import pe.edu.upeu.farmafx.utils.Vistas;

@Controller
public class LoginController {

    @Autowired
    private NavegadorVistas navegador;
    @Autowired
    private UsuarioServicioI usuarioServicio;

    @FXML
    private TextField dniTextField;
    @FXML
    private PasswordField clavePasswordField;
    @FXML
    private Label mensajeLabel;

    @FXML
    void ingresarAccion(ActionEvent event) {
        String dni = dniTextField.getText();
        String clave = clavePasswordField.getText();

        if (dni.isEmpty() || clave.isEmpty()) {
            mensajeLabel.setText("El DNI y la contraseña son obligatorios.");
            return;
        }

        Usuario usuario = usuarioServicio.autenticarUsuario(dni, clave);

        if (usuario != null) {
            FarmaFXApplication.usuarioLogueado = usuario;
            if (usuario.getRol() == RolUsuario.ADMINISTRADOR) {
                navegador.cambiarEscena((Node) event.getSource(), Vistas.MENU_ADMIN, "Panel de Administrador");
            }
            else if (usuario.getRol() == RolUsuario.CLIENTE) {
                navegador.cambiarEscena((Node) event.getSource(), Vistas.MENU_CLIENTE, "Bienvenido Cliente");
            }
        }
        else {
            mensajeLabel.setText("DNI o contraseña incorrectos.");
        }
    }

    @FXML
    void abrirVentanaRegistro(ActionEvent event) {
        Node sourceNode = (Node) event.getSource();
        navegador.cambiarEscena(sourceNode, Vistas.REGISTRO, "Registro de Nuevo Usuario");
    }

    @FXML
    void salirAccion(ActionEvent event) {
        Platform.exit();
    }
}
