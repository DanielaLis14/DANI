package pe.edu.upeu.sysventas.service;

import pe.edu.upeu.sysventas.dto.ComboBoxOption;
import pe.edu.upeu.sysventas.model.UnidadMedida;

import java.util.List;

public interface IUnidadMedidaService extends  ICrudGenericService<UnidadMedida,Long> {
    List<ComboBoxOption> listarCombobox();
}
