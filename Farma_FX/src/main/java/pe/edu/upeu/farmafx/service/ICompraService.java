package pe.edu.upeu.farmafx.service;

import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperPrint;
import pe.edu.upeu.farmafx.enums.TipoComprobante;
import pe.edu.upeu.farmafx.model.Compra;

import java.io.File;
import java.sql.SQLException;

public interface ICompraService extends ICrudGenericService<Compra, Long> {

    Compra buscarPorComprobante(TipoComprobante tipo, String serie, String numero);

    File getFile(String fileName);

    JasperPrint runReport(Long idCompra) throws JRException, SQLException;
}
