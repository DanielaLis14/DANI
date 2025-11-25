package pe.edu.upeu.farmafx.repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import pe.edu.upeu.farmafx.model.VentaDetalle;

import java.util.List;

public interface VentaDetalleRepository extends ICrudGenericRepository<VentaDetalle, Long> {

    @Query("SELECT vd FROM VentaDetalle vd "
            + "JOIN FETCH vd.venta v "
            + "JOIN FETCH vd.productoVenta p "
            + "WHERE v.idVenta = :idVenta")
    List<VentaDetalle> listarPorVenta(@Param("idVenta") Long idVenta);
}
