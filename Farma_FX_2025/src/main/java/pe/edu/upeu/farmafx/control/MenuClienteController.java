package pe.edu.upeu.farmafx.control;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import pe.edu.upeu.farmafx.FarmaFXApplication;
import pe.edu.upeu.farmafx.modelo.Usuario;
import pe.edu.upeu.farmafx.utils.NavegadorVistas;
import pe.edu.upeu.farmafx.utils.Vistas;

@Controller
public class MenuClienteController {

    @Autowired
    private NavegadorVistas navegador;

    @FXML
    private BorderPane panelPrincipal;

    @FXML
    private Label dniUsuarioLabel;

    @FXML
    public void initialize() {
        Usuario usuario = FarmaFXApplication.usuarioLogueado;
        if (usuario != null) {
            dniUsuarioLabel.setText("Usuario: " + usuario.getDni());
        }
    }

    @FXML
    void cerrarSesionAccion(ActionEvent event) {
        FarmaFXApplication.usuarioLogueado = null;
        navegador.cambiarEscena(panelPrincipal, Vistas.LOGIN, "Inicio de Sesión");
    }
    @FXML
    void salirAccion(ActionEvent event) {
        Platform.exit();
    }
}