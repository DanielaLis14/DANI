package pe.edu.upeu.farmafx.service.impl;

import lombok.RequiredArgsConstructor;
import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.design.JasperDesign;
import net.sf.jasperreports.engine.xml.JRXmlLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.upeu.farmafx.enums.TipoComprobante;
import pe.edu.upeu.farmafx.model.Venta;
import pe.edu.upeu.farmafx.repository.ICrudGenericRepository;
import pe.edu.upeu.farmafx.repository.VentaRepository;
import pe.edu.upeu.farmafx.service.IVentaService;

import javax.sql.DataSource;
import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class VentaServiceImpl extends CrudGenericServiceImpl<Venta, Long> implements IVentaService {

    private static final Logger logger = LoggerFactory.getLogger(VentaServiceImpl.class);
    private static final String JASPER_BASE_DIR = "jasper";

    private final VentaRepository ventaRepository;
    private final DataSource dataSource;

    @Override
    protected ICrudGenericRepository<Venta, Long> getRepository() {
        return ventaRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public Venta buscarPorComprobante(TipoComprobante tipo, String serie, String numero) {
        if (tipo == null || estaVacio(serie) || estaVacio(numero)) {
            logger.debug("Buscar venta por comprobante omitido por datos vacíos: tipo={}, serie={}, numero={}", tipo, serie, numero);
            return null;
        }
        return ventaRepository.buscarPorComprobante(tipo.name(), serie.trim(), numero.trim());
    }

    @Override
    public File getFile(String fileName) {
        Path basePath = Paths.get(JASPER_BASE_DIR).toAbsolutePath();
        Path completePath = basePath.resolve(fileName);
        return completePath.toFile();
    }

    @Override
    public JasperPrint runReport(Long idVenta) throws JRException, SQLException {
        if (idVenta == null || !ventaRepository.existsById(idVenta)) {
            throw new IllegalArgumentException("La venta con id " + idVenta + " no existe");
        }

        Map<String, Object> parameters = new HashMap<>();
        parameters.put("idventa", idVenta);
        parameters.put("imagenurl", getFile("logoupeu.png").getAbsolutePath());
        parameters.put("urljasper", getFile("detallev.jasper").getAbsolutePath());

        JasperDesign design = JRXmlLoader.load(getFile("comprobante_venta.jrxml"));
        JasperReport report = JasperCompileManager.compileReport(design);

        try (Connection connection = dataSource.getConnection()) {
            return JasperFillManager.fillReport(report, parameters, connection);
        } catch (SQLException ex) {
            logger.error("Error al obtener la conexión para el reporte de venta {}", idVenta, ex);
            throw ex;
        }
    }

    private boolean estaVacio(String value) {
        return value == null || value.isBlank();
    }
}
