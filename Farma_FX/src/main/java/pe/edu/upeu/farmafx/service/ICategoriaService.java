package pe.edu.upeu.farmafx.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import pe.edu.upeu.farmafx.dto.ComboBoxOption;
import pe.edu.upeu.farmafx.model.Categoria;

import java.util.List;

public interface ICategoriaService extends ICrudGenericService<Categoria, Long> {

    Categoria save(Categoria categoria);
    void delete(Long id);

    // ============ LISTAR CON FILTRO DE ESTADO ============
    // Justificación: Permite filtrar categorías por estado + paginación
    Page<Categoria> listar(Boolean estadoFiltro, Pageable pageable);

    // Justificación: Búsqueda + filtro de estado combinados para tabla con filtros activos
    Page<Categoria> buscarPaginado(String filtro, Boolean estadoFiltro, Pageable pageable);

    List<ComboBoxOption> listarCombobox();

    // ============ VALIDACIONES ============
    // Justificación: Prevenir duplicados al CREAR categoría
    boolean existsByNombreCategoria(String nombre);

    // Justificación: Prevenir duplicados al EDITAR categoría (excluye su propio ID)
    boolean existsByNombreCategoriaAndNotId(String nombre, Long id);

    /**
     * Obtener todas las categorías hijas (directas e indirectas) de una categoría padre.
     * @param idPadre ID de la categoría padre
     * @return Lista de todas las categorías hijas recursivamente
     */
    List<Categoria> obtenerCategoriasHijas(Long idPadre);

    // ============ PRÓXIMO ID y CASCADA ============
    // Justificación: obtenerProximoId para mostrar ID sugerido en modo NUEVO
    Long obtenerProximoId();

    // Justificación: Contar productos DIRECTOS para dialog de confirmación cascada
    long contarProductosActivosPorCategoria(Long idCategoria);
    long contarProductosInactivosPorCategoria(Long idCategoria);

    // Justificación: Contar productos TOTALES (categoría + todas las hijas) para cascada completa
    long contarProductosActivosTotales(Long idCategoria);
    long contarProductosInactivosTotales(Long idCategoria);

    // Justificación: Contar subcategorías por estado para dialog de confirmación
    long contarCategoriasHijasActivas(Long idCategoria);
    long contarCategoriasHijasInactivas(Long idCategoria);

    // Justificación: Cascada COMPLETA de inactivación/activación (categoría + hijas + productos)
    void inactivarCategoriaYProductos(Long idCategoria);
    void activarCategoriaYProductos(Long idCategoria);

    // ============ VALIDACIÓN COMPLETA ============
    // Justificación: Validar categoría antes de guardar (nombre, duplicados, etc.)
    void validarCategoria(Categoria categoria) throws IllegalArgumentException;

    // ============ ELIMINACIÓN CON CASCADA ============
    long contarCategoriasHijas(Long idCategoria);
    void eliminarCategoriaConCascada(Long idCategoria);
}
