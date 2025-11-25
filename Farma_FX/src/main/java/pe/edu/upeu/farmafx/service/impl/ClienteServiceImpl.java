package pe.edu.upeu.farmafx.service.impl;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import pe.edu.upeu.farmafx.model.Cliente;
import pe.edu.upeu.farmafx.repository.ClienteRepository;
import pe.edu.upeu.farmafx.repository.ICrudGenericRepository;
import pe.edu.upeu.farmafx.service.IClienteService;

@Service
@RequiredArgsConstructor
public class ClienteServiceImpl extends CrudGenericServiceImpl<Cliente, Long> implements IClienteService {

    private static final Logger logger = LoggerFactory.getLogger(ClienteServiceImpl.class);

    private final ClienteRepository clienteRepository;

    @Override
    protected ICrudGenericRepository<Cliente, Long> getRepository() {
        return clienteRepository;
    }

}
