package pe.edu.upeu.sysventas.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import pe.edu.upeu.sysventas.model.Proveedor;
import pe.edu.upeu.sysventas.repository.ICrudGenericRepository;
import pe.edu.upeu.sysventas.repository.ProveedorRepository;
import pe.edu.upeu.sysventas.service.IProveedorService;

@RequiredArgsConstructor
@Service
public class ProveedorServiceImp extends CrudGenericServiceImp<Proveedor, Long> implements IProveedorService {
    private final ProveedorRepository proveedorRepository;
    @Override
    protected ICrudGenericRepository<Proveedor, Long> getRepo() {
        return proveedorRepository;
    }
}
