package pe.edu.upeu.farmafx.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.upeu.farmafx.dto.ComboBoxOption;
import pe.edu.upeu.farmafx.model.UnidadMedida;
import pe.edu.upeu.farmafx.repository.ICrudGenericRepository;
import pe.edu.upeu.farmafx.repository.UnidadMedidaRepository;
import pe.edu.upeu.farmafx.service.IUnidadMedidaService;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UnidadMedidaServiceImpl extends CrudGenericServiceImpl<UnidadMedida, Long> implements IUnidadMedidaService {

    private final UnidadMedidaRepository unidadMedidaRepository;

    @Override
    protected ICrudGenericRepository<UnidadMedida, Long> getRepository() {
        return unidadMedidaRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<ComboBoxOption> listarCombobox() {
        List<ComboBoxOption> opciones = new ArrayList<>();
        for (UnidadMedida unidad : unidadMedidaRepository.findAll()) {
            ComboBoxOption option = new ComboBoxOption();
            option.setKey(String.valueOf(unidad.getIdUmedida()));
            option.setValue(unidad.getNombreUmedida());
            opciones.add(option);
        }
        return opciones;
    }
}
