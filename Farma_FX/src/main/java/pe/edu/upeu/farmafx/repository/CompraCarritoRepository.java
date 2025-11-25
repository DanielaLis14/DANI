package pe.edu.upeu.farmafx.repository;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import pe.edu.upeu.farmafx.model.CompraCarrito;

import java.util.List;

public interface CompraCarritoRepository extends ICrudGenericRepository<CompraCarrito, Long> {

    @Query("SELECT cc FROM CompraCarrito cc "
            + "JOIN FETCH cc.proveedorCarrito "
            + "JOIN FETCH cc.productoCarritoCompra "
            + "JOIN FETCH cc.usuarioCarritoCompra "
            + "WHERE cc.usuarioCarritoCompra.idUsuario = :idUsuario "
            + "AND cc.estadoCarritoCompra = true "
            + "ORDER BY cc.idCompraCarrito")
    List<CompraCarrito> listarPorUsuario(@Param("idUsuario") Long idUsuario);

    @Modifying
    @Query("DELETE FROM CompraCarrito cc WHERE cc.usuarioCarritoCompra.idUsuario = :idUsuario")
    void eliminarPorUsuario(@Param("idUsuario") Long idUsuario);
}
