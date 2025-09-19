package pe.edu.upeu.farmafx.utils;

import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class NavegadorVistas {

    @Autowired
    private ApplicationContext context;

    public void cambiarEscena(Node node, Vistas vista, String titulo) {
        try {
            Stage stage = (Stage) node.getScene().getWindow();

            FXMLLoader loader = new FXMLLoader(getClass().getResource(vista.getRuta()));
            loader.setControllerFactory(context::getBean);

            Parent root = loader.load();

            stage.setScene(new Scene(root));
            stage.setTitle(titulo);
            stage.centerOnScreen();
            stage.show();
        } catch (IOException e) {
            System.err.println("Error al cargar la vista: " + vista.getRuta());
            e.printStackTrace();
        }
    }
}
