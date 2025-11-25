package pe.edu.upeu.farmafx.repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import pe.edu.upeu.farmafx.model.CompraDetalle;

import java.util.List;

public interface CompraDetalleRepository extends ICrudGenericRepository<CompraDetalle, Long> {

    @Query("SELECT cd FROM CompraDetalle cd "
            + "JOIN FETCH cd.compra c "
            + "JOIN FETCH cd.productoCompra p "
            + "WHERE c.idCompra = :idCompra")
    List<CompraDetalle> listarPorCompra(@Param("idCompra") Long idCompra);
}
