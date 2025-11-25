package pe.edu.upeu.farmafx.components;

import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Dialog;
import javafx.scene.control.DialogPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import net.sf.jasperreports.engine.JasperPrint;
import win.zqxu.jrviewer.JRViewerFX;

import java.util.Objects;

/**
 * Contenedor sencillo para visualizar un {@link JasperPrint} usando {@link JRViewerFX}.
 */

public class ReportDialog {

    private final JasperPrint jasperPrint;

    public ReportDialog(JasperPrint jasperPrint) {
        this.jasperPrint = Objects.requireNonNull(jasperPrint, "jasperPrint");
    }

    public void showAndWait() {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Visualizar Reporte");
        dialog.setHeaderText(null);
        dialog.setResizable(true);

        JRViewerFX viewerFX = new JRViewerFX(jasperPrint);
        viewerFX.setPrefSize(900, 600);

        Button closeButton = new Button("Cerrar");
        closeButton.setOnAction(event -> dialog.close());
        closeButton.setDefaultButton(true);

        VBox container = new VBox(10, viewerFX, closeButton);
        container.setPadding(new Insets(10));
        VBox.setVgrow(viewerFX, Priority.ALWAYS);

        DialogPane dialogPane = dialog.getDialogPane();
        dialogPane.getButtonTypes().clear();
        dialogPane.setContent(container);

        dialog.showAndWait();
    }
}
