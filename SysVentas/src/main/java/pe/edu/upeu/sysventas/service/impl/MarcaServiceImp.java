package pe.edu.upeu.sysventas.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import pe.edu.upeu.sysventas.dto.ComboBoxOption;
import pe.edu.upeu.sysventas.model.Marca;
import pe.edu.upeu.sysventas.repository.ICrudGenericRepository;
import pe.edu.upeu.sysventas.repository.MarcaRepository;
import pe.edu.upeu.sysventas.service.IMarcaService;

import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
@Service
public class MarcaServiceImp extends CrudGenericServiceImp<Marca, Long> implements IMarcaService {
    private final MarcaRepository marcaRepository;

    @Override
    protected ICrudGenericRepository<Marca, Long> getRepo() {
        return marcaRepository;
    }
    @Override
    public List<ComboBoxOption> listarCombobox() {
        List<ComboBoxOption> listar=new ArrayList<>();
        ComboBoxOption cb;
        for(Marca cate : marcaRepository.findAll()) {
            cb=new ComboBoxOption();
            cb.setKey(String.valueOf(cate.getIdMarca()));
            cb.setValue(cate.getNombre());
            listar.add(cb);
        }
        return listar;
    }

}
