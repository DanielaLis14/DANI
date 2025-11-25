package pe.edu.upeu.farmafx.repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import pe.edu.upeu.farmafx.model.Compra;

public interface CompraRepository extends ICrudGenericRepository<Compra, Long> {

    @Query("SELECT c FROM Compra c "
            + "JOIN FETCH c.proveedor "
            + "JOIN FETCH c.usuarioRegistro "
            + "WHERE c.tipoComprobante = :tipo "
            + "AND c.serieComprobante = :serie "
            + "AND c.numeroComprobante = :numero")
    Compra buscarPorComprobante(@Param("tipo") String tipo, @Param("serie") String serie, @Param("numero") String numero);
}
