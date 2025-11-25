package pe.edu.upeu.farmafx.repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import pe.edu.upeu.farmafx.model.Emisor;

import java.util.List;

public interface EmisorRepository extends ICrudGenericRepository<Emisor, Long> {

    @Query("SELECT e FROM Emisor e WHERE e.rucEmisor = :ruc")
    Emisor buscarPorRuc(@Param("ruc") String ruc);

    @Query("SELECT e FROM Emisor e WHERE e.estadoEmisor = :estado")
    List<Emisor> listarPorEstado(@Param("estado") Boolean estado);
}
