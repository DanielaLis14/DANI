package pe.edu.upeu.farmafx.service.impl;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import pe.edu.upeu.farmafx.model.Proveedor;
import pe.edu.upeu.farmafx.repository.ICrudGenericRepository;
import pe.edu.upeu.farmafx.repository.ProveedorRepository;
import pe.edu.upeu.farmafx.service.IProveedorService;

@Service
@RequiredArgsConstructor
public class ProveedorServiceImpl extends CrudGenericServiceImpl<Proveedor, Long> implements IProveedorService {

    private static final Logger logger = LoggerFactory.getLogger(ProveedorServiceImpl.class);

    private final ProveedorRepository proveedorRepository;

    @Override
    protected ICrudGenericRepository<Proveedor, Long> getRepository() {
        return proveedorRepository;
    }
}
