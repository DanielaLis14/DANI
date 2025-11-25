package pe.edu.upeu.farmafx.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import pe.edu.upeu.farmafx.model.Marca;

import java.util.Optional;

public interface MarcaRepository extends ICrudGenericRepository<Marca, Long> {

    @Query("SELECT m FROM Marca m WHERE " +
           "(:estado IS NULL OR m.estadoMarca = :estado) AND " +
           "(LOWER(m.nombreMarca) LIKE LOWER(CONCAT('%', :filtro, '%')) " +
           "OR LOWER(m.descripcionMarca) LIKE LOWER(CONCAT('%', :filtro, '%')))")
    Page<Marca> findByNombreOrDescripcion(@Param("filtro") String filtro,
                                          @Param("estado") Boolean estado,
                                          Pageable pageable);

    @Query("SELECT m FROM Marca m WHERE :estado IS NULL OR m.estadoMarca = :estado")
    Page<Marca> findByEstado(@Param("estado") Boolean estado, Pageable pageable);

    @Query("SELECT COUNT(m) > 0 FROM Marca m WHERE LOWER(m.nombreMarca) = LOWER(:nombre)")
    boolean existsByNombreMarca(@Param("nombre") String nombreMarca);

    @Query("SELECT COUNT(m) > 0 FROM Marca m WHERE LOWER(m.nombreMarca) = LOWER(:nombre) AND m.idMarca <> :id")
    boolean existsByNombreMarcaAndNotId(@Param("nombre") String nombre, @Param("id") Long id);

    @Query("SELECT COUNT(p) FROM Producto p WHERE p.marca.idMarca = :idMarca")
    long countProductosByMarca(@Param("idMarca") Long idMarca);

    @Query("SELECT MAX(m.idMarca) FROM Marca m")
    Optional<Long> findMaxId();
}
