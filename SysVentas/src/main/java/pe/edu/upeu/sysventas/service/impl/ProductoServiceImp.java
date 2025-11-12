package pe.edu.upeu.sysventas.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pe.edu.upeu.sysventas.dto.ModeloDataAutocomplet;
import pe.edu.upeu.sysventas.model.Producto;
import pe.edu.upeu.sysventas.repository.ProductoRepository;
import pe.edu.upeu.sysventas.service.ProductoIService;

import java.util.ArrayList;
import java.util.List;

@Service
public class ProductoServiceImp implements ProductoIService {
    private static final Logger logger = LoggerFactory.getLogger(ProductoServiceImp.class);
    @Autowired
    ProductoRepository pRepo;
    @Override
    public Producto save(Producto producto) {
        return pRepo.save(producto);
    }
    @Override
    public List<Producto> findAll() {
        return pRepo.findAll();
    }
    @Override
    public Producto update(Producto producto) {
        return pRepo.save(producto);
    }
    @Override
    public void delete(Long id) {
        pRepo.deleteById(id);
    }
    @Override
    public Producto findById(Long id) {
        return pRepo.findById(id).orElse(null);
    }
    @Override
    public List<ModeloDataAutocomplet> listAutoCompletProducto(String
                                                                       nombre) {
        List<ModeloDataAutocomplet> listarProducto = new ArrayList<>();
        try {
            for (Producto producto :
                    pRepo.listAutoCompletProducto(nombre + "%")) {
                ModeloDataAutocomplet data = new ModeloDataAutocomplet();
                data.setIdx(producto.getNombre());

                data.setNameDysplay(String.valueOf(producto.getIdProducto()));
                data.setOtherData(producto.getPu() + ":" +
                        producto.getStock());
                listarProducto.add(data);
            }
        } catch (Exception e) {
            logger.error("Error al realizar la busqueda", e);
        }
        return listarProducto;
    }


    @Override
    public List<ModeloDataAutocomplet> listAutoCompletProducto() {
        List<ModeloDataAutocomplet> listarProducto = new ArrayList<>();
        try {
            for (Producto producto : pRepo.findAll())
            {ModeloDataAutocomplet data = new ModeloDataAutocomplet();
                data.setIdx(String.valueOf(producto.getIdProducto()));
                data.setNameDysplay(producto.getNombre());
                data.setOtherData(producto.getPu() + ":" +
                        producto.getStock());
                listarProducto.add(data);
            }
        } catch (Exception e) {
            logger.error("Error al realizar la busqueda", e);
        }
        return listarProducto;
    }
}