package pe.edu.upeu.sysventas.service.impl;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import pe.edu.upeu.sysventas.dto.ModeloDataAutocomplet;
import pe.edu.upeu.sysventas.model.Cliente;
import pe.edu.upeu.sysventas.repository.ClienteRepository;
import pe.edu.upeu.sysventas.repository.ICrudGenericRepository;
import pe.edu.upeu.sysventas.service.IClienteService;

import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
@Service
public class ClienteServiceImp extends CrudGenericServiceImp<Cliente,String> implements IClienteService {
    private final ClienteRepository clienteRepository;

    Logger logger= LoggerFactory.getLogger(ClienteServiceImp.class);

    @Override
    protected ICrudGenericRepository<Cliente, String> getRepo() {
        return clienteRepository;
    }

    @Override
    public List<ModeloDataAutocomplet> listAutoCompletCliente() {
        List<ModeloDataAutocomplet> listarclientes = new ArrayList<>();
        try {
            for (Cliente cliente : clienteRepository.findAll()) {
                ModeloDataAutocomplet data = new ModeloDataAutocomplet();
                data.setIdx(cliente.getDniruc());
                data.setNameDysplay(cliente.getNombres());
                data.setOtherData(cliente.getTipoDocumento().name());
                listarclientes.add(data);
            }
        } catch (Exception e) {
            logger.error("Error durante la operación", e);
        }
        return listarclientes;
    }

}
