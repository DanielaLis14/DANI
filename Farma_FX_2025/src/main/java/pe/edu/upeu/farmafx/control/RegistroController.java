package pe.edu.upeu.farmafx.control;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.paint.Color;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import pe.edu.upeu.farmafx.enums.RolUsuario;
import pe.edu.upeu.farmafx.modelo.Usuario;
import pe.edu.upeu.farmafx.servicio.UsuarioServicioI;
import pe.edu.upeu.farmafx.utils.NavegadorVistas;
import pe.edu.upeu.farmafx.utils.Vistas;

@Controller
public class RegistroController {

    @Autowired
    private NavegadorVistas navegador;
    @Autowired
    private UsuarioServicioI usuarioServicio;

    @FXML
    private TextField dniTextField;
    @FXML
    private PasswordField clavePasswordField;
    @FXML
    private PasswordField confirmarClavePasswordField;
    @FXML
    private Label mensajeLabel;

    @FXML
    void registrarAccion(ActionEvent event) {
        String dni = dniTextField.getText();
        String clave = clavePasswordField.getText();
        String confirmarClave = confirmarClavePasswordField.getText();

        if (dni.isEmpty() || clave.isEmpty() || confirmarClave.isEmpty()) {
            mensajeLabel.setTextFill(Color.RED);
            mensajeLabel.setText("Todos los campos son obligatorios.");
            return;
        }

        if (!clave.equals(confirmarClave)) {
            mensajeLabel.setTextFill(Color.RED);
            mensajeLabel.setText("Las contraseñas no coinciden.");
            return;
        }

        Usuario nuevoUsuario = new Usuario();
        nuevoUsuario.setDni(dni);
        nuevoUsuario.setClave(clave);
        nuevoUsuario.setRol(RolUsuario.CLIENTE);

        String resultado = usuarioServicio.registrarUsuario(nuevoUsuario);

        if (resultado.equals("Registro exitoso.")) {
            mensajeLabel.setTextFill(Color.GREEN);
        } else {
            mensajeLabel.setTextFill(Color.RED);
        }
        mensajeLabel.setText(resultado);
    }

    @FXML
    void volverAlLoginAccion(ActionEvent event) {
        Node sourceNode = (Node) event.getSource();
        navegador.cambiarEscena(sourceNode, Vistas.LOGIN, "Inicio de Sesión");
    }
}
