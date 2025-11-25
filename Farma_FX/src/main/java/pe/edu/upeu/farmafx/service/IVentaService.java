package pe.edu.upeu.farmafx.service;

import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperPrint;
import pe.edu.upeu.farmafx.enums.TipoComprobante;
import pe.edu.upeu.farmafx.model.Venta;

import java.io.File;
import java.sql.SQLException;

public interface IVentaService extends ICrudGenericService<Venta, Long> {

    Venta buscarPorComprobante(TipoComprobante tipo, String serie, String numero);

    File getFile(String fileName);

    JasperPrint runReport(Long idVenta) throws JRException, SQLException;
}
