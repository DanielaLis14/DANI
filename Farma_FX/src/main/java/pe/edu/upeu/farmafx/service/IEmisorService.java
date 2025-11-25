package pe.edu.upeu.farmafx.service;

import pe.edu.upeu.farmafx.model.Emisor;

import java.util.List;

public interface IEmisorService extends ICrudGenericService<Emisor, Long> {

    Emisor buscarPorRuc(String ruc);

    List<Emisor> listarActivos();
}
