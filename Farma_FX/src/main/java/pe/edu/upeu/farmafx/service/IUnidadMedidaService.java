package pe.edu.upeu.farmafx.service;

import pe.edu.upeu.farmafx.dto.ComboBoxOption;
import pe.edu.upeu.farmafx.model.UnidadMedida;

import java.util.List;

public interface IUnidadMedidaService extends ICrudGenericService<UnidadMedida, Long> {

    List<ComboBoxOption> listarCombobox();
}
