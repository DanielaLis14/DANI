package pe.edu.upeu.sysventas.controller;

import com.github.anastaciocintra.escpos.EscPos;
import com.github.anastaciocintra.escpos.EscPosConst;
import com.github.anastaciocintra.escpos.Style;
import com.github.anastaciocintra.escpos.barcode.QRCode;
import com.github.anastaciocintra.output.PrinterOutputStream;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import net.sf.jasperreports.engine.JasperPrint;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import pe.edu.upeu.sysventas.components.*;
import pe.edu.upeu.sysventas.dto.ModeloDataAutocomplet;
import pe.edu.upeu.sysventas.dto.PersonaDto;
import pe.edu.upeu.sysventas.dto.SessionManager;
import pe.edu.upeu.sysventas.enums.TipoDocumento;
import pe.edu.upeu.sysventas.exception.ModelNotFoundException;
import pe.edu.upeu.sysventas.model.Cliente;
import pe.edu.upeu.sysventas.model.VentCarrito;
import pe.edu.upeu.sysventas.model.Venta;
import pe.edu.upeu.sysventas.model.VentaDetalle;
import pe.edu.upeu.sysventas.service.*;
import pe.edu.upeu.sysventas.utils.ConsultaDNI;
import pe.edu.upeu.sysventas.utils.PrinterManager;

import javax.print.PrintService;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.Consumer;

@Controller
public class VentaController {
    @FXML
    TextField autocompCliente, dniRuc, razonSocial, txtDireccion,
            autocompProducto, nombreProducto, codigoPro, stockPro, cantidadPro, punitPro, preTPro,
            txtBaseImp, txtIgv, txtDescuento, txtImporteT;
    @FXML
    TableView<VentCarrito> tableView;

    @FXML
    Button btnRegCliente, btnRegCarrito, btnRegVenta, btnImprimirVenta;

    @FXML
    AnchorPane miContenedor;
    Stage stage;

    AutoCompleteTextField actfC;
    ModeloDataAutocomplet lastCliente;
    AutoCompleteTextField actf;
    ModeloDataAutocomplet lastProducto;

    @Autowired
    IClienteService cs;
    @Autowired
    ProductoIService ps;
    @Autowired
    ConsultaDNI cDni;
    @Autowired
    IVentCarritoService daoC;
    @Autowired
    IUsuarioService daoU;
    @Autowired
    IVentaService daoV;
    @Autowired
    IVentaDetalleService daoVD;

    private JasperPrint jasperPrint;

    private final SortedSet<ModeloDataAutocomplet> entries=new TreeSet<>((ModeloDataAutocomplet o1, ModeloDataAutocomplet o2) ->
            o1.toString().compareToIgnoreCase(o2.toString()));
    private final SortedSet<ModeloDataAutocomplet> entriesC=new TreeSet<>((ModeloDataAutocomplet o1, ModeloDataAutocomplet o2) ->
            o1.toString().compareToIgnoreCase(o2.toString()));

    public void listarProducto(){
        entries.addAll(ps.listAutoCompletProducto());
    }

    public void listarCliente(){
        entriesC.clear();
        entriesC.addAll(cs.listAutoCompletCliente());
    }


    public void autoCompletarCliente(){
        actfC=new AutoCompleteTextField<>(entriesC, autocompCliente);
        autocompCliente.setOnKeyReleased(e->{
            lastCliente=(ModeloDataAutocomplet) actfC.getLastSelectedObject();
            if(lastCliente!=null){
                dniRuc.setText(lastCliente.getIdx());
                razonSocial.setText(lastCliente.getNameDysplay());
                listar();
            }else{
                btnRegCliente.setDisable(true);
                limpiarFormCliente();
            }
        });
    }

    public void limpiarFormCliente(){
        razonSocial.clear();
        dniRuc.clear();
        txtDireccion.clear();
    }

    public void listar(){
        tableView.getItems().clear();
        List<VentCarrito> lista=daoC.listaCarritoCliente(dniRuc.getText());
        double impoTotal=0;
        for(VentCarrito dato:lista){
            impoTotal+=Double.parseDouble(String.valueOf(dato.getPtotal()));
        }
        txtImporteT.setText(String.valueOf(impoTotal));
        double pv=impoTotal/1.18;
        txtBaseImp.setText(String.valueOf(Math.round(pv*100.0/100.0)));
        txtIgv.setText(String.valueOf(Math.round((pv*0.18)*100.0)/100.0));
        tableView.getItems().addAll(lista);
    }

    public void consultarDNIReniec(double with){
        PersonaDto p=cDni.consultarDNI(autocompCliente.getText());
        if(p!=null){
            razonSocial.setText(p.getNombre()+" "+p.getApellidoPaterno()+" "+p.getApellidoMaterno());
            dniRuc.setText(p.getDni());
            btnRegCliente.setDisable(false);
            Toast.showToast(stage, "El cliente se encontró en RENIEC para registrar debe hacer clic en Add ", 2000, with, 50);
        }else{
            btnRegCliente.setDisable(true);
            Toast.showToast(stage, "El cliente no se encontró en RENIEC y debe registrar a través del formulario de cliente", 2000, with, 50);
        }
    }

    @FXML
    public void buscarClienteCdni(){
        limpiarFormCliente();
        Stage stage= StageManager.getPrimaryStage();
        double with=stage.getMaxWidth()/2;
        if(autocompCliente.getText().length()==8 || autocompCliente.getText().length()==11 ){
            try {
                if(cs.findById(autocompCliente.getText())!=null){
                    btnRegCliente.setDisable(true);
                    Toast.showToast(stage, "El cliente si existe", 2000, with, 50);
                    return;
                }
                consultarDNIReniec(with);
            }catch (ModelNotFoundException e){
                btnRegCliente.setDisable(true);
                Toast.showToast(stage, "El cliente no existe", 2000, with, 50);
                consultarDNIReniec(with);
            }
        }else{
            btnRegCliente.setDisable(true);
            Toast.showToast(stage, "El valor debe tener 8 o 11 digitos", 2000, with, 50);
        }
    }

    public void deleteReg(VentCarrito obj){
        Alert alert=new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmación");
        alert.setHeaderText("Confirmar acción");
        alert.setContentText("¿Estas seguro de eliminar el registro?");
        Optional<ButtonType> result=alert.showAndWait();
        if(result.isPresent() && result.get()==ButtonType.OK){
            daoC.deleteById(obj.getIdCarrito());
            Stage stage= StageManager.getPrimaryStage();
            double with=stage.getMaxWidth()/2;
            Toast.showToast(stage, "Accion completada", 2000, with, 50);
        }
    }

    public void personalizarTabla(){
        TableViewHelper<VentCarrito> tableViewHelper=new TableViewHelper<>();
        LinkedHashMap<String, ColumnInfo> columns=new LinkedHashMap<>();
        columns.put("ID Prod", new ColumnInfo("producto.idProducto", 100.0));
        columns.put("Nombre Producto", new ColumnInfo("nombreProducto", 300.0));
        columns.put("Cantidad", new ColumnInfo("cantidad", 60.0));
        columns.put("P. Unitario", new ColumnInfo("punitario", 100.0));
        columns.put("P. Total", new ColumnInfo("ptotal", 100.0));

        Consumer<VentCarrito> updateAction=(VentCarrito ventCarrito) ->{
            System.out.println("Actualizar: "+ventCarrito);
        };

        Consumer<VentCarrito> deleteAction=(VentCarrito ventCarrito) ->{
            deleteReg(ventCarrito);
        };

        tableViewHelper.addColumnsInOrderWithSize(tableView, columns, updateAction, deleteAction);
        tableView.setTableMenuButtonVisible(true);
    }


    @FXML
    public void initialize(){
        Platform.runLater(()->{
            stage=(Stage) miContenedor.getScene().getWindow();
        });

        listarCliente();
        autoCompletarCliente();

        listarProducto();
        actf=new AutoCompleteTextField<>(entries, autocompProducto);
        autocompProducto.setOnKeyReleased(e->{
            lastProducto=(ModeloDataAutocomplet) actf.getLastSelectedObject();
            if(lastProducto!=null){
                nombreProducto.setText(lastProducto.getNameDysplay());
                codigoPro.setText(lastProducto.getIdx());
                String[] dato=lastProducto.getOtherData().split(":");
                punitPro.setText(dato[0]);
                stockPro.setText(dato[1]);
            }
        });

        personalizarTabla();
        btnRegCliente.setDisable(true);
        btnRegCarrito.setDisable(true);
    }

    @FXML
    public void guardarCliente(){
        Stage stage= StageManager.getPrimaryStage();
        double with=stage.getMaxWidth()/2;
        try {
            Cliente c=Cliente.builder()
                    .dniruc(dniRuc.getText())
                    .nombres(razonSocial.getText())
                    .repLegal(razonSocial.getText())
                    .tipoDocumento(TipoDocumento.DNI)
                    .build();
            cs.save(c);
            btnRegCliente.setDisable(true);
            Toast.showToast(stage, "Cliente registrado", 2000, with, 50);
            listarCliente();
            listar();
        } catch (Exception e) {
            Toast.showToast(stage, "Error al guardar cliente", 2000, with, 50);
        }
    }

    @FXML
    public void calcularPT(){
        if(!cantidadPro.getText().isEmpty() || !cantidadPro.getText().equals("")){
            double dato=Double.parseDouble(cantidadPro.getText())*Double.parseDouble(punitPro.getText());
            preTPro.setText(String.valueOf(Math.round(dato*100.0)/100.0));
            if(Double.parseDouble(cantidadPro.getText())>0){
                btnRegCarrito.setDisable(false);
            }else{
                btnRegCarrito.setDisable(true);
            }
        }else{
            btnRegCarrito.setDisable(true);
        }
    }

    @FXML
    public void registrarCarrito(){
        try {
            VentCarrito vc=VentCarrito.builder()
                    .dniruc(dniRuc.getText())
                    .producto(ps.findById(Long.parseLong(codigoPro.getText())))
                    .nombreProducto(nombreProducto.getText())
                    .cantidad(Double.parseDouble(cantidadPro.getText()))
                    .punitario(Double.parseDouble(punitPro.getText()))
                    .ptotal(Double.parseDouble(preTPro.getText()))
                    .estado(1)
                    .usuario(daoU.findById(SessionManager.getInstance().getUserId()))
                    .build();

            daoC.save(vc);
            listar();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @FXML
    public void registrarVenta(){
        Locale locale=new Locale("es", "es-PE");
        LocalDateTime date=LocalDateTime.now();
        DateTimeFormatter formatter=DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss", locale);
        String fechaFormateada=date.format(formatter);
        Venta to=Venta.builder()
                .cliente(cs.findById(dniRuc.getText()))
                .precioBase(Double.parseDouble(txtBaseImp.getText()))
                .igv(Double.parseDouble(txtIgv.getText()))
                .precioTotal(Double.parseDouble(txtImporteT.getText()))
                .usuario(daoU.findById(SessionManager.getInstance().getUserId()))
                .serie("V")
                .tipoDoc("Factura")
                .fechaGener(date.parse(fechaFormateada, formatter))
                .numDoc("00")
                .build();
        Venta idx=daoV.save(to);
        List<VentCarrito> datosC=daoC.listaCarritoCliente(dniRuc.getText());
        if(idx.getIdVenta()!=0 && !datosC.isEmpty()){
            for(VentCarrito car:datosC){
                VentaDetalle vd=VentaDetalle.builder()
                        .venta(idx)
                        .producto(ps.findById(car.producto.getIdProducto()))
                        .cantidad(car.getCantidad())
                        .descuento(0.0)
                        .pu(car.getPunitario())
                        .subtotal(car.getPtotal())
                        .build();
                daoVD.save(vd);
            }
        }
        daoC.deleteCarAll(dniRuc.getText());
        listar();
        try {
            jasperPrint=daoV.runReport(idx.getIdVenta());
            Platform.runLater(()->{
                ReportAlert reportAlert=new ReportAlert(jasperPrint);
                reportAlert.show();
                print(idx.getIdVenta());
            });

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void print(Long idv) {
        Venta vt = daoV.findById(idv);
        try {
            PrinterManager printerManager = PrinterManager.getInstance();
            PrintService printService = printerManager.getPrintService();
            PrinterOutputStream printerOutputStream = new PrinterOutputStream(printService);
            EscPos escpos = new EscPos(printerOutputStream);

            Style titleStyle = new Style()
                    .setJustification(EscPosConst.Justification.Center)
                    .setBold(true)
                    .setFontSize(Style.FontSize._2, Style.FontSize._2);
            Style normal = new Style()
                    .setJustification(EscPosConst.Justification.Left_Default);
            Style center = new Style()
                    .setJustification(EscPosConst.Justification.Center);
            escpos.writeLF(titleStyle, "BOLETA DE VENTA");
            escpos.writeLF(center, "Tienda Demo S.A.C.");
            escpos.writeLF(center, "RUC: 12345678901");
            escpos.writeLF(center, "Av. Principal 123 - Lima");
            escpos.writeLF(center, "--------------------------------");
            //Datos del cliente
            escpos.writeLF(normal, "Cliente: " + vt.getCliente().getNombres());
            escpos.writeLF(normal, "DNI: " + vt.getCliente().getDniruc());
            escpos.writeLF(normal, "Fecha: " + vt.getFechaGener() + "");
            escpos.writeLF(normal, "--------------------------------");
            //Detalle
            escpos.writeLF(normal, "Cant Descripción Importe");
            int x = 1;
            for (VentaDetalle vd : vt.getVentaDetalles()) {
                String punit = x + " " + vd.getProducto().getNombre() + " S/ " + vd.getSubtotal() + "";
                escpos.writeLF(normal, punit);
            }
            escpos.writeLF(normal, "--------------------------------");
            escpos.writeLF(normal, "TOTAL: S/" + vt.getPrecioTotal() + "");
            escpos.writeLF(normal, "--------------------------------");
            //Agregar QR con los datos principales
            String qrData = "Boleta N°001-000123 | Total: S/ " + vt.getPrecioTotal() + " | Fecha: " + vt.getFechaGener();
            QRCode qrCode = new QRCode()
                    .setJustification(EscPosConst.Justification.Center)
                    .setErrorCorrectionLevel(QRCode.QRErrorCorrectionLevel.QR_ECLEVEL_M_Default)
                    .setModel(QRCode.QRModel._1_Default);
            escpos.write(qrCode, qrData);
            escpos.feed(2);
            escpos.writeLF(center, "Gracias por su compra!");
            escpos.feed(6);
            //Corte total
            escpos.cut(EscPos.CutMode.FULL);
            escpos.close();
            System.out.println("Boleta impresa correctamente.");
            System.out.println("Impresión completada correctamente.");
        } catch (IOException e) {
            System.err.println("Error al inicializar la impresora: " + e.getMessage());
        }
    }


}
