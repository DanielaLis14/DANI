package pe.edu.upeu.asistencia.control;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.util.Callback;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import pe.edu.upeu.asistencia.enums.Carrera;
import pe.edu.upeu.asistencia.enums.TipoParticipante;
import pe.edu.upeu.asistencia.modelo.Participante;
import pe.edu.upeu.asistencia.servicio.ParticipanteServicioI;


@Controller
public class ParticipanteController {
    @FXML
    private ComboBox<Carrera> cbxCarrera;
    @FXML
    private ComboBox<TipoParticipante> cbxTipoParticipante;
    @FXML
    private TextField txtNombres,txtApellidos,txtDNI;
    @FXML
    private TableView<Participante> tableRegPart;
    ObservableList<Participante> participantes;
    @Autowired
    ParticipanteServicioI ps;
    TableColumn<Participante, String> dniCol,nombreCol,apellidoCol,carreraCol,tipoParticipanteCol;
    TableColumn<Participante, Void> opcCol;
    int indexEdit=-1;
    @FXML
    public void initialize(){
        cbxCarrera.getItems().addAll(Carrera.values());
        cbxTipoParticipante.getItems().addAll(TipoParticipante.values());
        cbxCarrera.getSelectionModel().select(4);
        Carrera carrera = cbxCarrera.getSelectionModel().getSelectedItem();
        System.out.println(carrera.name());
        definirColumnas();
        listarPartipantes();
    }
    public void limpiarFormulario(){
        txtNombres.setText("");
        txtDNI.setText("");
        txtApellidos.setText("");
        cbxCarrera.getSelectionModel().clearSelection();
        cbxTipoParticipante.getSelectionModel().clearSelection();
    }
    @FXML
    public void registrarParticipante(){
        Participante p=new Participante();
        p.setDni(new SimpleStringProperty(txtDNI.getText()));
        p.setNombre(new SimpleStringProperty(txtNombres.getText()));
        p.setApellido(new SimpleStringProperty(txtApellidos.getText()));
        p.setCarrera(cbxCarrera.getSelectionModel().getSelectedItem());
        p.setTipoParticipante(cbxTipoParticipante.getSelectionModel().getSelectedItem());
        if(indexEdit==-1){
            ps.save(p);
        }
        else{
            ps.update(p,indexEdit);
            indexEdit=-1;
        }

        limpiarFormulario();
        listarPartipantes();
    }
    public void definirColumnas(){
        dniCol=new TableColumn<>("DNI");
        nombreCol=new TableColumn<>("Nombre");
        apellidoCol= new TableColumn<>("Apellido");
        carreraCol=new TableColumn<>("Carrera");
        tipoParticipanteCol=new TableColumn<>("Tipo Participante");
        opcCol=new TableColumn<>("Opcines");
        tableRegPart.getColumns().addAll(dniCol,nombreCol,apellidoCol,carreraCol,tipoParticipanteCol,opcCol);
    }
    public void listarPartipantes(){
        dniCol.setCellValueFactory(cellData -> cellData.getValue().getDni());
        nombreCol.setCellValueFactory(cellData -> cellData.getValue().getNombre());
        apellidoCol.setCellValueFactory(cellData-> cellData.getValue().getApellido());
        carreraCol.setCellValueFactory(celData-> new SimpleStringProperty(celData.getValue().getCarrera().toString()));
        agregarAccionesButton();
        tipoParticipanteCol.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getTipoParticipante().toString()));
        participantes = FXCollections.observableList(ps.findAll());
        tableRegPart.setItems(participantes);
    }
    public void eliminarParticipante(int index){
        ps.delete(index);
        listarPartipantes();
    }
    public void editarParticipante(Participante p,int index){
        txtDNI.setText(p.getDni().getValue());
        txtApellidos.setText(p.getApellido().getValue());
        txtNombres.setText(p.getNombre().getValue());
        cbxTipoParticipante.getSelectionModel().select(p.getTipoParticipante());
        cbxCarrera.getSelectionModel().select(p.getCarrera());
        indexEdit=index;

    }
    public void agregarAccionesButton(){
        Callback<TableColumn<Participante, Void>, TableCell<Participante,Void>> cellFactory= param->new TableCell<>(){
            private final Button btnEdit=new Button("Editar");
            private final Button btnDelete=new Button("Eliminar");
            {
                btnEdit.setOnAction(actionEvent -> {
                    Participante p=getTableView().getItems().get(getIndex());
                    editarParticipante(p,getIndex());
                });
                btnDelete.setOnAction(actionEvent -> {
                    eliminarParticipante(getIndex());
                });
            }
            @Override
            public void updateItem(Void item, boolean empty){
                super.updateItem(item,empty);
                if(empty){
                    setGraphic(null);
                }
                else{
                    HBox hbox =new HBox(btnEdit,btnDelete);
                    hbox.setSpacing(10);
                    setGraphic(hbox);
                }
            }
        };
        opcCol.setCellFactory(cellFactory);
    }
}