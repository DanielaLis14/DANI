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
import pe.edu.upeu.farmafx.model.Compra;
import pe.edu.upeu.farmafx.repository.CompraRepository;
import pe.edu.upeu.farmafx.repository.ICrudGenericRepository;
import pe.edu.upeu.farmafx.service.ICompraService;

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
public class CompraServiceImpl extends CrudGenericServiceImpl<Compra, Long> implements ICompraService {

    private static final Logger logger = LoggerFactory.getLogger(CompraServiceImpl.class);
    private static final String JASPER_BASE_DIR = "jasper";

    private final CompraRepository compraRepository;
    private final DataSource dataSource;

    @Override
    protected ICrudGenericRepository<Compra, Long> getRepository() {
        return compraRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public Compra buscarPorComprobante(TipoComprobante tipo, String serie, String numero) {
        if (tipo == null || estaVacio(serie) || estaVacio(numero)) {
            logger.debug("Buscar compra por comprobante omitido por datos vacíos: tipo={}, serie={}, numero={}", tipo, serie, numero);
            return null;
        }
        return compraRepository.buscarPorComprobante(tipo.name(), serie.trim(), numero.trim());
    }

    @Override
    public File getFile(String fileName) {
        Path basePath = Paths.get(JASPER_BASE_DIR).toAbsolutePath();
        Path completePath = basePath.resolve(fileName);
        return completePath.toFile();
    }

    @Override
    public JasperPrint runReport(Long idCompra) throws JRException, SQLException {
        if (idCompra == null || !compraRepository.existsById(idCompra)) {
            throw new IllegalArgumentException("La compra con id " + idCompra + " no existe");
        }

        Map<String, Object> parameters = new HashMap<>();
        parameters.put("idcompra", idCompra);
        parameters.put("imagenurl", getFile("logoupeu.png").getAbsolutePath());
        parameters.put("urljasper", getFile("detallec.jasper").getAbsolutePath());

        JasperDesign design = JRXmlLoader.load(getFile("comprobante_compra.jrxml"));
        JasperReport report = JasperCompileManager.compileReport(design);

        try (Connection connection = dataSource.getConnection()) {
            return JasperFillManager.fillReport(report, parameters, connection);
        } catch (SQLException ex) {
            logger.error("Error al obtener la conexión para el reporte de compra {}", idCompra, ex);
            throw ex;
        }
    }

    private boolean estaVacio(String valor) {
        return valor == null || valor.isBlank();
    }
}
