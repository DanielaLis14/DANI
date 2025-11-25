package pe.edu.upeu.farmafx.repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import pe.edu.upeu.farmafx.model.Venta;

public interface VentaRepository extends ICrudGenericRepository<Venta, Long> {

    @Query("SELECT v FROM Venta v "
            + "JOIN FETCH v.clienteVenta "
            + "JOIN FETCH v.usuarioVenta "
            + "WHERE v.tipoComprobanteVenta = :tipo "
            + "AND v.serieComprobanteVenta = :serie "
            + "AND v.numeroComprobanteVenta = :numero")
    Venta buscarPorComprobante(@Param("tipo") String tipo, @Param("serie") String serie, @Param("numero") String numero);
}
