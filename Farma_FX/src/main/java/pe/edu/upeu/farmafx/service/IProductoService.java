package pe.edu.upeu.farmafx.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import pe.edu.upeu.farmafx.model.Producto;

public interface IProductoService extends ICrudGenericService<Producto, Long> {

    Producto save(Producto producto);
    void delete(Long id);
    Producto findByCodigoProducto(String codigoProducto);
    boolean existsByCodigoProducto(String codigoProducto);

    /**
     * Genera el siguiente código secuencial automático.
     * Formato: PROD-XXXX (ej: PROD-0001, PROD-0024)
     * Ignora huecos en la secuencia.
     * 
     * @return Código único generado
     */
    String generarSiguienteCodigo();

    Page<Producto> listar(Pageable pageable);
    Page<Producto> buscarPaginado(String filtro, Pageable pageable);

    /**
     * Lista productos paginados filtrando por estado.
     *
     * @param estado Estado a filtrar (true=activo, false=inactivo, null=todos)
     * @param pageable Paginación y ordenamiento
     * @return Página de productos
     */
    Page<Producto> listar(Boolean estado, Pageable pageable);

    /**
     * Busca productos paginados filtrando por estado.
     *
     * @param filtro Texto a buscar en código, nombre o marca
     * @param estado Estado a filtrar (true=activo, false=inactivo, null=todos)
     * @param pageable Paginación y ordenamiento
     * @return Página de productos que coinciden con el filtro y estado
     */
    Page<Producto> buscarPaginado(String filtro, Boolean estado, Pageable pageable);

    // ============ CASCADA MARCA/CATEGORÍA → PRODUCTOS ============

    /**
     * Cuenta productos por marca y estado.
     *
     * @param idMarca ID de la marca
     * @param estado  Estado (true=activo, false=inactivo, null=todos)
     * @return Cantidad de productos
     */
    long contarPorMarcaYEstado(Long idMarca, Boolean estado);

    /**
     * Cuenta productos por categoría y estado.
     *
     * @param idCategoria ID de la categoría
     * @param estado      Estado (true=activo, false=inactivo, null=todos)
     * @return Cantidad de productos
     */
    long contarPorCategoriaYEstado(Long idCategoria, Boolean estado);

    /**
     * Inactiva todos los productos activos de una marca.
     *
     * @param idMarca ID de la marca
     * @return Cantidad de productos inactivados
     */
    int inactivarPorMarca(Long idMarca);

    /**
     * Activa todos los productos inactivos de una marca.
     *
     * @param idMarca ID de la marca
     * @return Cantidad de productos activados
     */
    int activarPorMarca(Long idMarca);

    /**
     * Inactiva todos los productos activos de una categoría.
     *
     * @param idCategoria ID de la categoría
     * @return Cantidad de productos inactivados
     */
    int inactivarPorCategoria(Long idCategoria);

    /**
     * Activa todos los productos inactivos de una categoría.
     *
     * @param idCategoria ID de la categoría
     * @return Cantidad de productos activados
     */
    int activarPorCategoria(Long idCategoria);

}
