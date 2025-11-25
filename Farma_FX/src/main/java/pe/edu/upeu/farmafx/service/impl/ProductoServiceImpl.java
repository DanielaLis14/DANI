package pe.edu.upeu.farmafx.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.upeu.farmafx.model.Producto;
import pe.edu.upeu.farmafx.repository.ICrudGenericRepository;
import pe.edu.upeu.farmafx.repository.ProductoRepository;
import pe.edu.upeu.farmafx.service.IProductoService;


@Slf4j
@Service
@RequiredArgsConstructor
public class ProductoServiceImpl extends CrudGenericServiceImpl<Producto, Long> implements IProductoService {

    private final ProductoRepository productoRepository;

    @Override
    protected ICrudGenericRepository<Producto, Long> getRepository() {
        return productoRepository;
    }

    @Override
    @Transactional
    public Producto save(Producto producto) {
        return productoRepository.save(producto);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        productoRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Producto> listar(Pageable pageable) {
        return productoRepository.findAll(pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Producto> buscarPaginado(String filtro, Pageable pageable) {
        return productoRepository.findByCodigoNombreMarca(filtro, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Producto> listar(Boolean estado, Pageable pageable) {
        log.debug("Listando productos con estado: {}", estado);
        return productoRepository.findAllByEstado(estado, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Producto> buscarPaginado(String filtro, Boolean estado, Pageable pageable) {
        log.debug("Buscando productos con filtro: '{}', estado: {}", filtro, estado);
        return productoRepository.findByFiltroAndEstado(filtro, estado, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Producto findByCodigoProducto(String codigoProducto) {
        return productoRepository.findByCodigoProducto(codigoProducto);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByCodigoProducto(String codigoProducto) {
        return productoRepository.existsByCodigoProducto(codigoProducto);
    }

    @Override
    @Transactional(readOnly = true)
    public String generarSiguienteCodigo() {
        try {
            String ultimoCodigo = productoRepository.findUltimoCodigoSecuencial();
            
            int siguiente;
            if (ultimoCodigo == null || ultimoCodigo.isEmpty()) {
                siguiente = 1;
                log.info("Generando primer código secuencial: PROD-0001");
            } else {
                String numeroStr = ultimoCodigo.substring(5);
                int numero = Integer.parseInt(numeroStr);
                siguiente = numero + 1;
                
                log.debug("Último código: {}, generando siguiente: PROD-{}", 
                    ultimoCodigo, String.format("%04d", siguiente));
            }
            
            return String.format("PROD-%04d", siguiente);
            
        } catch (Exception e) {
            log.error("Error al generar código secuencial", e);
            return "PROD-" + String.format("%04d", System.currentTimeMillis() % 10000);
        }
    }

    // ============ CASCADA MARCA/CATEGORÍA → PRODUCTOS ============

    @Override
    @Transactional(readOnly = true)
    public long contarPorMarcaYEstado(Long idMarca, Boolean estado) {
        log.debug("Contando productos de marca ID: {}, estado: {}", idMarca, estado);
        return productoRepository.countByMarcaIdAndEstado(idMarca, estado);
    }

    @Override
    @Transactional(readOnly = true)
    public long contarPorCategoriaYEstado(Long idCategoria, Boolean estado) {
        log.debug("Contando productos de categoría ID: {}, estado: {}", idCategoria, estado);
        return productoRepository.countByCategoriaIdAndEstado(idCategoria, estado);
    }

    @Override
    @Transactional
    public int inactivarPorMarca(Long idMarca) {
        log.info("Inactivando productos de marca ID: {}", idMarca);
        int cantidad = productoRepository.inactivarPorMarca(idMarca);
        log.info("{} productos inactivados de marca ID: {}", cantidad, idMarca);
        return cantidad;
    }

    @Override
    @Transactional
    public int activarPorMarca(Long idMarca) {
        log.info("Activando productos de marca ID: {}", idMarca);
        int cantidad = productoRepository.activarPorMarca(idMarca);
        log.info("{} productos activados de marca ID: {}", cantidad, idMarca);
        return cantidad;
    }

    @Override
    @Transactional
    public int inactivarPorCategoria(Long idCategoria) {
        log.info("Inactivando productos de categoría ID: {}", idCategoria);
        int cantidad = productoRepository.inactivarPorCategoria(idCategoria);
        log.info("{} productos inactivados de categoría ID: {}", cantidad, idCategoria);
        return cantidad;
    }

    @Override
    @Transactional
    public int activarPorCategoria(Long idCategoria) {
        log.info("Activando productos de categoría ID: {}", idCategoria);
        int cantidad = productoRepository.activarPorCategoria(idCategoria);
        log.info("{} productos activados de categoría ID: {}", cantidad, idCategoria);
        return cantidad;
    }

}
