package pe.edu.upeu.sysventas.controller;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import pe.edu.upeu.sysventas.components.ColumnInfo;
import pe.edu.upeu.sysventas.components.ComboBoxAutoComplete;
import pe.edu.upeu.sysventas.components.TableViewHelper;
import pe.edu.upeu.sysventas.components.Toast;
import pe.edu.upeu.sysventas.dto.ComboBoxOption;
import pe.edu.upeu.sysventas.model.Producto;
import pe.edu.upeu.sysventas.service.ICategoriaService;
import pe.edu.upeu.sysventas.service.IMarcaService;
import pe.edu.upeu.sysventas.service.IUnidadMedidaService;
import pe.edu.upeu.sysventas.service.ProductoIService;

import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

@Controller
public class ProductoController {
    @FXML
    TextField txtNombreProducto, txtPUnit,
            txtPUnitOld, txtUtilidad, txtStock, txtStockOld,
            txtFiltroDato;
    @FXML
    ComboBox<ComboBoxOption> cbxMarca;
    @FXML
    ComboBox<ComboBoxOption> cbxCategoria;
    @FXML
    ComboBox<ComboBoxOption> cbxUnidMedida;
    @FXML
    private TableView<Producto> tableView;
    @FXML
    Label lbnMsg, idPrueba;
    @FXML
    private AnchorPane miContenedor;
    Stage stage;

    @Autowired
    IMarcaService ms;

    @Autowired
    ICategoriaService cs;

    @Autowired
    ProductoIService ps;

    @Autowired
    IUnidadMedidaService ums;

    private Validator validator;

    ObservableList<Producto> listarProducto;
    Producto formulario;
    Long idProductoCE=0L;

    private void filtrarProductos(String filtro) {
        if (filtro == null || filtro.isEmpty()) {
            tableView.getItems().clear();
            tableView.getItems().addAll(listarProducto);
        } else {
            String lowerCaseFilter = filtro.toLowerCase();
            List<Producto> productosFiltrados = listarProducto.stream()
                    .filter(producto -> {
                        if (producto.getNombre().toLowerCase().contains(lowerCaseFilter)) {
                            return true;
                        }
                        if (String.valueOf(producto.getPu()).contains(lowerCaseFilter)) {
                            return true;
                        }
                        if (String.valueOf(producto.getUtilidad()).contains(lowerCaseFilter)) {
                            return true;
                        }
                        if
                        (producto.getMarca().getNombre().toLowerCase().contains(lowerCaseFilter)) {
                            return true;
                        }
                        if
                        (producto.getCategoria().getNombre().toLowerCase().contains(lowerCaseFilter)) {
                            return true;
                        }
                        return false; // Si no coincide con ningún campo
                    })
                    .collect(Collectors.toList());
            tableView.getItems().clear();
            tableView.getItems().addAll(productosFiltrados);
        }
    }

    public void listar(){
        try {
            tableView.getItems().clear();
            listarProducto = FXCollections.observableArrayList(ps.findAll());
            tableView.getItems().addAll(listarProducto);
            txtFiltroDato.textProperty().addListener((observable, oldValue,
                                                      newValue) -> {
                filtrarProductos(newValue);
            });
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }


    @FXML
    public void initialize() {
        Timeline timeline = new Timeline(new KeyFrame(Duration.millis(2000),
                event -> {
                    stage = (Stage) miContenedor.getScene().getWindow();
                    if (stage != null) {
                        System.out.println("El título del stage es: " +
                                stage.getTitle());
                    } else {
                        System.out.println("Stage aún no disponible.");
                    }
                }));
        timeline.setCycleCount(1);
        timeline.play();

        cbxMarca.getItems().addAll(ms.listarCombobox());
        cbxMarca.setOnAction(event -> {
            ComboBoxOption selectedProduct =
                    cbxMarca.getSelectionModel().getSelectedItem();
            if (selectedProduct != null) {
                String selectedId = selectedProduct.getKey(); // Obtener el ID
                System.out.println("ID del producto seleccionado: " +
                        selectedId);
            }
        });
        new ComboBoxAutoComplete<>(cbxMarca);

        cbxCategoria.getItems().addAll(cs.listarCombobox());
        cbxCategoria.setOnAction(event -> {
            ComboBoxOption selectedProduct =
                    cbxCategoria.getSelectionModel().getSelectedItem();
            if (selectedProduct != null) {
                String selectedId = selectedProduct.getKey(); // Obtener el ID
                System.out.println("ID del producto seleccionado: " +
                        selectedId);
            }
        });
        new ComboBoxAutoComplete<>(cbxCategoria);

        cbxUnidMedida.getItems().addAll(ums.listarCombobox());
        cbxUnidMedida.setOnAction(event -> {
            ComboBoxOption selectedProduct =
                    cbxUnidMedida.getSelectionModel().getSelectedItem();
            if (selectedProduct != null) {
                String selectedId = selectedProduct.getKey(); // Obtener el ID
                System.out.println("ID del producto seleccionado: " +
                        selectedId);
            }
        });
        new ComboBoxAutoComplete<>(cbxUnidMedida);


        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();

        TableViewHelper<Producto> tableViewHelper = new TableViewHelper<>();
        LinkedHashMap<String, ColumnInfo> columns = new LinkedHashMap<>();
        columns.put("ID Pro.", new ColumnInfo("idProducto", 60.0));
        columns.put("Nombre Producto", new ColumnInfo("nombre", 200.0));
        columns.put("P. Unitario", new ColumnInfo("pu", 150.0));
        columns.put("Utilidad", new ColumnInfo("utilidad", 100.0));
        columns.put("Marca", new ColumnInfo("marca.nombre", 200.0));
        columns.put("Categoria", new ColumnInfo("categoria.nombre", 200.0));

        Consumer<Producto> updateAction = (Producto producto) -> {
            System.out.println("Actualizar: " + producto);
            editForm(producto);
        };
        Consumer<Producto> deleteAction = (Producto producto) -> {
            System.out.println("Actualizar: " + producto);
            ps.delete(producto.getIdProducto());
            double with=stage.getWidth()/1.5;
            double h=stage.getHeight()/2;
            Toast.showToast(stage, "Se eliminó correctamente!!", 2000, with,
                    h);
            listar();
        };

        tableViewHelper.addColumnsInOrderWithSize(tableView,
                columns,updateAction, deleteAction );

        tableView.setTableMenuButtonVisible(true);


        listar();
    }


    public void limpiarError() {
        List<Control> controles = List.of(
                txtNombreProducto, txtPUnit, txtPUnitOld,
                txtUtilidad, txtStock, txtStockOld,
                cbxMarca, cbxCategoria, cbxUnidMedida
        );
        controles.forEach(c -> c.getStyleClass().remove("text-field-error"));
    }


    public void clearForm() {
        txtNombreProducto.clear();
        txtPUnit.clear();
        txtPUnitOld.clear();
        txtUtilidad.clear();
        txtStock.clear();
        txtStockOld.clear();
        cbxMarca.getSelectionModel().clearSelection();
        cbxCategoria.getSelectionModel().clearSelection();
        cbxUnidMedida.getSelectionModel().clearSelection();
        idProductoCE = 0L;
        limpiarError();
    }

    public void editForm(Producto producto){
        txtNombreProducto.setText(producto.getNombre());
        txtPUnit.setText(producto.getPu().toString());
        txtPUnitOld.setText(producto.getPuOld().toString());
        txtUtilidad.setText(producto.getUtilidad().toString());
        txtStock.setText(producto.getStock().toString());
        txtStockOld.setText(producto.getStockOld().toString());
        cbxMarca.getSelectionModel().select(
                cbxMarca.getItems().stream()
                        .filter(marca ->
                                Long.parseLong(marca.getKey())==producto.getMarca().getIdMarca())
                        .findFirst()
                        .orElse(null)
        );
        cbxCategoria.getSelectionModel().select(
                cbxCategoria.getItems().stream()
                        .filter(categoria ->
                                Long.parseLong(categoria.getKey())==producto.getCategoria().getIdCategoria(
                                ))
                        .findFirst()
                        .orElse(null)
        );
        cbxUnidMedida.getSelectionModel().select(
                cbxUnidMedida.getItems().stream()
                        .filter(unidad ->
                                Long.parseLong(unidad.getKey())==producto.getUnidadMedida().getIdUnidad())
                        .findFirst()
                        .orElse(null)
        );
        idProductoCE=producto.getIdProducto();
        limpiarError();
    }

    private double parseDoubleSafe(String value) {
        if (value == null || value.trim().isEmpty()) return 0.0;
        try {
            return Double.parseDouble(value.trim());
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }



    private void mostrarErroresValidacion(List<ConstraintViolation<Producto>> violaciones) {
        limpiarError();
        //Mantiene el orden de los campos del formulario
        Map<String, Control> campos = new LinkedHashMap<>();
        campos.put("nombre", txtNombreProducto);
        campos.put("pu", txtPUnit);
        campos.put("puOld", txtPUnitOld);
        campos.put("utilidad", txtUtilidad);
        campos.put("stock", txtStock);
        campos.put("stockOld", txtStockOld);
        campos.put("marca", cbxMarca);
        campos.put("categoria", cbxCategoria);
        campos.put("unidadMedida", cbxUnidMedida);

        //Guarda los errores siguiendo el orden del formulario
        LinkedHashMap<String, String> erroresOrdenados = new LinkedHashMap<>();
        final Control[] primerControlConError = {null};
        for (String campo : campos.keySet()) {
            violaciones.stream()
                    .filter(v -> v.getPropertyPath().toString().equals(campo))
                    .findFirst()
                    .ifPresent(v -> {
                        erroresOrdenados.put(campo, v.getMessage());
                        Control control = campos.get(campo);
                        //Aplica el estilo de error si no lo tiene
                        if (control != null && !control.getStyleClass().contains("text-field-error")) {
                            control.getStyleClass().add("text-field-error");
                        }
                        //Guarda el primer control con error para enfocar después
                        if (primerControlConError[0] == null) {
                            primerControlConError[0] = control;
                        }
                    });
        }
        //Muestra el primer mensaje de error y enfoca el control correspondiente
        if (!erroresOrdenados.isEmpty()) {
            var primerError = erroresOrdenados.entrySet().iterator().next();
            lbnMsg.setText(primerError.getValue());
            lbnMsg.setStyle("-fx-text-fill: red; -fx-font-size: 16px;");

            //Enfocar el primer campo con error
            if (primerControlConError[0] != null) {
                Control finalPrimerControl = primerControlConError[0];
                Platform.runLater(finalPrimerControl::requestFocus);
            }
        }
    }


    @FXML
    public void validarFormulario() {
        formulario = new Producto();
        formulario.setNombre(txtNombreProducto.getText());
        formulario.setPu(parseDoubleSafe(txtPUnit.getText()));
        formulario.setPuOld(parseDoubleSafe(txtPUnitOld.getText()));
        formulario.setUtilidad(parseDoubleSafe(txtUtilidad.getText()));
        formulario.setStock(parseDoubleSafe(txtStock.getText()));
        formulario.setStockOld(parseDoubleSafe(txtStockOld.getText()));
        String idxM=cbxMarca.getSelectionModel().getSelectedItem()==null?"0":cbxMarca.getSelectionModel().getSelectedItem().getKey();
        formulario.setMarca(idxM=="0"?null:ms.findById(Long.parseLong(idxM)));
        String idxC=cbxCategoria.getSelectionModel().getSelectedItem()==null?"0":cbxCategoria.getSelectionModel().getSelectedItem().getKey();
        formulario.setCategoria(idxC=="0"?null:cs.findById(Long.parseLong(idxC)));
        String idxUM=cbxUnidMedida.getSelectionModel().getSelectedItem()==null?"0":cbxUnidMedida.getSelectionModel().getSelectedItem().getKey();
        formulario.setUnidadMedida(idxUM=="0"?null:ums.findById(Long.parseLong(idxUM)));

        Set<ConstraintViolation<Producto>> violaciones = validator.validate(formulario);

        List<ConstraintViolation<Producto>> violacionesOrdenadas = violaciones.stream()
                .sorted(Comparator.comparing(v -> v.getPropertyPath().toString()))
                .toList();


        if (violacionesOrdenadas.isEmpty()) {
            procesarFormulario();
        } else {
            mostrarErroresValidacion(violacionesOrdenadas);
        }


    }




    private void procesarFormulario() {
        lbnMsg.setText("Formulario válido");
        lbnMsg.setStyle("-fx-text-fill: green; -fx-font-size: 16px;");
        limpiarError();
        double width = stage.getWidth() / 1.5;
        double height = stage.getHeight() / 2;
        if (idProductoCE > 0L) {
            formulario.setIdProducto(idProductoCE);
            ps.update(formulario);
            Toast.showToast(stage, "Se actualizó correctamente!!", 2000, width, height);
        } else {
            ps.save(formulario);
            Toast.showToast(stage, "Se guardó correctamente!!", 2000, width, height);
        }
        clearForm();
        listar();
    }

}
