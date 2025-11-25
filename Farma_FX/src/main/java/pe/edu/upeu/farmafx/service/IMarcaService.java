package pe.edu.upeu.farmafx.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import pe.edu.upeu.farmafx.dto.ComboBoxOption;
import pe.edu.upeu.farmafx.model.Marca;

import java.util.List;

public interface IMarcaService extends ICrudGenericService<Marca, Long> {

    Marca save(Marca marca);
    void delete(Long id);

    Page<Marca> listar(Boolean estadoFiltro, Pageable pageable);
    Page<Marca> buscarPaginado(String filtro, Boolean estadoFiltro, Pageable pageable);

    List<ComboBoxOption> listarCombobox();

    boolean existsByNombreMarca(String nombre);
    boolean existsByNombreMarcaAndNotId(String nombre, Long id);
    boolean tieneProductosAsociados(Long idMarca);

    /**
     * Valida una marca antes de guardarla.
     * Lanza excepción si hay problemas (nombre vacío, duplicado, etc)
     */
    void validarMarca(Marca marca) throws IllegalArgumentException;

    /**
     * Obtiene el próximo ID que se asignará a una nueva marca.
     * Calcula MAX(idMarca) + 1 de la base de datos.
     *
     * @return Próximo ID disponible (1 si no hay marcas)
     */
    Long obtenerProximoId();

    // ============ CASCADA MARCA → PRODUCTOS ============

    /**
     * Cuenta productos ACTIVOS de una marca.
     *
     * @param idMarca ID de la marca
     * @return Cantidad de productos activos
     */
    long contarProductosActivosPorMarca(Long idMarca);

    /**
     * Cuenta productos INACTIVOS de una marca.
     *
     * @param idMarca ID de la marca
     * @return Cantidad de productos inactivos
     */
    long contarProductosInactivosPorMarca(Long idMarca);

    /**
     * Inactiva una marca Y todos sus productos activos en cascada.
     * Operación transaccional: si falla, se hace rollback completo.
     *
     * @param idMarca ID de la marca
     * @throws IllegalArgumentException si la marca no existe
     */
    void inactivarMarcaYProductos(Long idMarca);

    /**
     * Activa una marca Y todos sus productos inactivos en cascada.
     * Operación transaccional: si falla, se hace rollback completo.
     *
     * @param idMarca ID de la marca
     * @throws IllegalArgumentException si la marca no existe
     */
    void activarMarcaYProductos(Long idMarca);
}
