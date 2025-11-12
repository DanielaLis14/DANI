package pe.edu.upeu.sysventas.service.impl;


import lombok.RequiredArgsConstructor;
import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.design.JasperDesign;
import net.sf.jasperreports.engine.xml.JRXmlLoader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pe.edu.upeu.sysventas.model.Venta;
import pe.edu.upeu.sysventas.repository.ICrudGenericRepository;
import pe.edu.upeu.sysventas.repository.VentaRepository;
import pe.edu.upeu.sysventas.service.IVentaService;

import javax.sql.DataSource;
import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;

@RequiredArgsConstructor
@Service
public class VentaServiceImp extends CrudGenericServiceImp<Venta, Long> implements IVentaService {
    @Autowired
    private DataSource dataSource;

    private final VentaRepository ventaRepository;

    @Override
    protected ICrudGenericRepository<Venta, Long> getRepo() {
        return ventaRepository;
    }

    @Override
    public File getFile(String filex) {
        File newFolder = new File("jasper");
        String ruta = newFolder.getAbsolutePath();
        //CAMINO = Paths.get(ruta+"/"+"reporte1.jrxml");
        Path CAMINO = Paths.get(ruta + "/" + filex);
        System.out.println("Llegasss Ruta 2:" +
                CAMINO.toAbsolutePath().toFile());
        return CAMINO.toFile();
    }


    @Override
    public JasperPrint runReport(Long idv) throws JRException, SQLException
    {
        // Verificar si la venta existe
        if (!ventaRepository.existsById(idv)) {
            throw new IllegalArgumentException("La venta con id " + idv + " no existe");
        }
        HashMap<String, Object> param = new HashMap<>();
        // Obtener ruta de la imagen
        String imgen = getFile("logoupeu.png").getAbsolutePath();
        String urljasper=getFile("detallev.jasper").getAbsolutePath();
        // Agregar parámetros
        param.put("idventa", idv);
        param.put("imagenurl", imgen);
        param.put("urljasper", urljasper);
        // Cargar el diseño del informe
        JasperDesign jdesign = JRXmlLoader.load(getFile("comprobante.jrxml"));
        JasperReport jreport = JasperCompileManager.compileReport(jdesign);
        // Llenar el informe

        try (Connection conn = dataSource.getConnection()) {
            return JasperFillManager.fillReport(jreport, param, conn);
        }

    }
}
