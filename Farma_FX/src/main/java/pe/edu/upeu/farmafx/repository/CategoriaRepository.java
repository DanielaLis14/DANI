package pe.edu.upeu.farmafx.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import pe.edu.upeu.farmafx.model.Categoria;

import java.util.List;
import java.util.Optional;

public interface CategoriaRepository extends ICrudGenericRepository<Categoria, Long> {

    // ============ BÚSQUEDA CON FILTRO DE ESTADO ============
    // Justificación: Permite filtrar categorías por estado (activo/inactivo/todos)
    // Usa LEFT JOIN FETCH para evitar N+1 con categoría padre

    @Query("SELECT DISTINCT c FROM Categoria c LEFT JOIN FETCH c.categoriaPadre " +
           "WHERE (:estado IS NULL OR c.estadoCategoria = :estado)")
    Page<Categoria> findAll(@Param("estado") Boolean estado, Pageable pageable);

    @Query(value = "SELECT DISTINCT c FROM Categoria c LEFT JOIN FETCH c.categoriaPadre " +
           "WHERE (:estado IS NULL OR c.estadoCategoria = :estado) AND " +
           "(LOWER(c.nombreCategoria) LIKE LOWER(CONCAT('%', :filtro, '%')) " +
           "OR LOWER(c.descripcionCategoria) LIKE LOWER(CONCAT('%', :filtro, '%')))",
           countQuery = "SELECT COUNT(DISTINCT c) FROM Categoria c " +
           "WHERE (:estado IS NULL OR c.estadoCategoria = :estado) AND " +
           "(LOWER(c.nombreCategoria) LIKE LOWER(CONCAT('%', :filtro, '%')) " +
           "OR LOWER(c.descripcionCategoria) LIKE LOWER(CONCAT('%', :filtro, '%')))")
    Page<Categoria> findByNombreOrDescripcion(@Param("filtro") String filtro,
                                               @Param("estado") Boolean estado,
                                               Pageable pageable);

    // ============ VALIDACIONES ============
    // Justificación: existsByNombreCategoria para NUEVO, existsByNombreCategoriaAndNotId para EDICIÓN
    // Evita permitir nombres duplicados al crear/editar

    boolean existsByNombreCategoria(String nombreCategoria);

    @Query("SELECT COUNT(c) > 0 FROM Categoria c " +
           "WHERE LOWER(c.nombreCategoria) = LOWER(:nombre) AND c.idCategoria <> :id")
    boolean existsByNombreCategoriaAndNotId(@Param("nombre") String nombre, @Param("id") Long id);

    // ============ CASCADA CATEGORÍA → PRODUCTOS ============
    // Justificación: Contar productos por estado para diálogo de cascada

    @Query("SELECT COUNT(p) FROM Producto p " +
           "WHERE p.categoria.idCategoria = :idCategoria AND p.estadoProducto = :estado")
    long countProductosByCategoriaAndEstado(@Param("idCategoria") Long idCategoria,
                                             @Param("estado") Boolean estado);

    // ============ CASCADA CATEGORÍA → CATEGORÍAS HIJAS ============
    // Justificación: Obtener todas las hijas directas para gestión de jerarquía

    @Query("SELECT c FROM Categoria c WHERE c.categoriaPadre.idCategoria = :idPadre")
    List<Categoria> findByCategoriaPadreId(@Param("idPadre") Long idPadre);

    // ============ UTILIDADES ============
    // Justificación: Obtener próximo ID disponible para mostrar en modo NUEVO

    @Query("SELECT MAX(c.idCategoria) FROM Categoria c")
    Optional<Long> findMaxId();
}
