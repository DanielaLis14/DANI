package pe.edu.upeu.farmafx.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import pe.edu.upeu.farmafx.model.Producto;

public interface ProductoRepository extends ICrudGenericRepository<Producto, Long> {

    @Query(value = "SELECT DISTINCT p FROM Producto p " +
           "LEFT JOIN FETCH p.marca " +
           "LEFT JOIN FETCH p.categoria " +
           "LEFT JOIN FETCH p.unidadMedida " +
           "WHERE LOWER(p.codigoProducto) LIKE LOWER(CONCAT('%', :filtro, '%')) " +
           "OR LOWER(p.nombreProducto) LIKE LOWER(CONCAT('%', :filtro, '%')) " +
           "OR LOWER(p.marca.nombreMarca) LIKE LOWER(CONCAT('%', :filtro, '%'))",
           countQuery = "SELECT COUNT(DISTINCT p) FROM Producto p " +
           "WHERE LOWER(p.codigoProducto) LIKE LOWER(CONCAT('%', :filtro, '%')) " +
           "OR LOWER(p.nombreProducto) LIKE LOWER(CONCAT('%', :filtro, '%')) " +
           "OR LOWER(p.marca.nombreMarca) LIKE LOWER(CONCAT('%', :filtro, '%'))")
    Page<Producto> findByCodigoNombreMarca(@Param("filtro") String filtro, Pageable pageable);

    @Query(value = "SELECT DISTINCT p FROM Producto p " +
           "LEFT JOIN FETCH p.marca " +
           "LEFT JOIN FETCH p.categoria " +
           "LEFT JOIN FETCH p.unidadMedida",
           countQuery = "SELECT COUNT(p) FROM Producto p")
    Page<Producto> findAll(Pageable pageable);

    /**
     * Lista productos paginados filtrando por estado.
     *
     * @param estado Estado a filtrar (true=activo, false=inactivo, null=todos)
     * @param pageable Paginación y ordenamiento
     * @return Página de productos
     */
    @Query(value = "SELECT DISTINCT p FROM Producto p " +
           "LEFT JOIN FETCH p.marca " +
           "LEFT JOIN FETCH p.categoria " +
           "LEFT JOIN FETCH p.unidadMedida " +
           "WHERE :estado IS NULL OR p.estadoProducto = :estado",
           countQuery = "SELECT COUNT(p) FROM Producto p " +
           "WHERE :estado IS NULL OR p.estadoProducto = :estado")
    Page<Producto> findAllByEstado(@Param("estado") Boolean estado, Pageable pageable);

    /**
     * Busca productos por código/nombre/marca filtrando por estado.
     *
     * @param filtro Texto a buscar en código, nombre o marca
     * @param estado Estado a filtrar (true=activo, false=inactivo, null=todos)
     * @param pageable Paginación y ordenamiento
     * @return Página de productos que coinciden con el filtro y estado
     */
    @Query(value = "SELECT DISTINCT p FROM Producto p " +
           "LEFT JOIN FETCH p.marca " +
           "LEFT JOIN FETCH p.categoria " +
           "LEFT JOIN FETCH p.unidadMedida " +
           "WHERE (:estado IS NULL OR p.estadoProducto = :estado) " +
           "AND (LOWER(p.codigoProducto) LIKE LOWER(CONCAT('%', :filtro, '%')) " +
           "OR LOWER(p.nombreProducto) LIKE LOWER(CONCAT('%', :filtro, '%')) " +
           "OR LOWER(p.marca.nombreMarca) LIKE LOWER(CONCAT('%', :filtro, '%')))",
           countQuery = "SELECT COUNT(DISTINCT p) FROM Producto p " +
           "WHERE (:estado IS NULL OR p.estadoProducto = :estado) " +
           "AND (LOWER(p.codigoProducto) LIKE LOWER(CONCAT('%', :filtro, '%')) " +
           "OR LOWER(p.nombreProducto) LIKE LOWER(CONCAT('%', :filtro, '%')) " +
           "OR LOWER(p.marca.nombreMarca) LIKE LOWER(CONCAT('%', :filtro, '%')))")
    Page<Producto> findByFiltroAndEstado(@Param("filtro") String filtro,
                                          @Param("estado") Boolean estado,
                                          Pageable pageable);

    boolean existsByCodigoProducto(String codigoProducto);

    Producto findByCodigoProducto(String codigoProducto);

    /**
     * Obtiene el último código generado con formato PROD-XXXX.
     * Necesario para generar siguiente código secuencial.
     * Ignora huecos (siempre retorna el máximo existente).
     *
     * @return Último código (ej: "PROD-0023") o null si no hay productos
     */
    @Query(value = "SELECT p.codigo_producto FROM farmafx_productos p " +
           "WHERE p.codigo_producto LIKE 'PROD-%' " +
           "ORDER BY p.codigo_producto DESC LIMIT 1", nativeQuery = true)
    String findUltimoCodigoSecuencial();

    // ============ MÉTODOS PARA CASCADA MARCA/CATEGORÍA → PRODUCTOS ============

    /**
     * Cuenta productos por marca y estado.
     *
     * @param idMarca ID de la marca
     * @param estado  Estado a filtrar (true=activo, false=inactivo, null=todos)
     * @return Cantidad de productos
     */
    @Query("SELECT COUNT(p) FROM Producto p WHERE p.marca.idMarca = :idMarca AND " +
           "(:estado IS NULL OR p.estadoProducto = :estado)")
    long countByMarcaIdAndEstado(@Param("idMarca") Long idMarca, @Param("estado") Boolean estado);

    /**
     * Cuenta productos por categoría y estado.
     *
     * @param idCategoria ID de la categoría
     * @param estado      Estado a filtrar (true=activo, false=inactivo, null=todos)
     * @return Cantidad de productos
     */
    @Query("SELECT COUNT(p) FROM Producto p WHERE p.categoria.idCategoria = :idCategoria AND " +
           "(:estado IS NULL OR p.estadoProducto = :estado)")
    long countByCategoriaIdAndEstado(@Param("idCategoria") Long idCategoria, @Param("estado") Boolean estado);

    /**
     * Inactiva todos los productos ACTIVOS de una marca.
     *
     * @param idMarca ID de la marca
     * @return Cantidad de productos inactivados
     */
    @Modifying
    @Query("UPDATE Producto p SET p.estadoProducto = false " +
           "WHERE p.marca.idMarca = :idMarca AND p.estadoProducto = true")
    int inactivarPorMarca(@Param("idMarca") Long idMarca);

    /**
     * Activa todos los productos INACTIVOS de una marca.
     *
     * @param idMarca ID de la marca
     * @return Cantidad de productos activados
     */
    @Modifying
    @Query("UPDATE Producto p SET p.estadoProducto = true " +
           "WHERE p.marca.idMarca = :idMarca AND p.estadoProducto = false")
    int activarPorMarca(@Param("idMarca") Long idMarca);

    /**
     * Inactiva todos los productos ACTIVOS de una categoría.
     *
     * @param idCategoria ID de la categoría
     * @return Cantidad de productos inactivados
     */
    @Modifying
    @Query("UPDATE Producto p SET p.estadoProducto = false " +
           "WHERE p.categoria.idCategoria = :idCategoria AND p.estadoProducto = true")
    int inactivarPorCategoria(@Param("idCategoria") Long idCategoria);

    /**
     * Activa todos los productos INACTIVOS de una categoría.
     *
     * @param idCategoria ID de la categoría
     * @return Cantidad de productos activados
     */
    @org.springframework.data.jpa.repository.Modifying
    @Query("UPDATE Producto p SET p.estadoProducto = true " +
           "WHERE p.categoria.idCategoria = :idCategoria AND p.estadoProducto = false")
    int activarPorCategoria(@Param("idCategoria") Long idCategoria);

    // ============ MÉTODOS PARA HUERFANIZACIÓN (SET NULL) ============

    /**
     * Huerfaniza productos de una categoría (SET categoria = NULL).
     * Usado cuando se elimina una categoría para no perder los productos.
     *
     * @param idCategoria ID de la categoría a huerfanizar
     * @return Cantidad de productos huerfanizados
     */
    @Modifying
    @Query("UPDATE Producto p SET p.categoria = null WHERE p.categoria.idCategoria = :idCategoria")
    int huerfanizarPorCategoria(@Param("idCategoria") Long idCategoria);

    // ============ NOTA: NO EXISTE huerfanizarPorMarca() ============
    // Marca es NOT NULL (obligatoria), por lo que NO se puede hacer SET marca = null.
    // Si se intenta eliminar una marca con productos, debe validarse en MarcasController
    // y prohibir la eliminación mostrando un error al usuario.

}
