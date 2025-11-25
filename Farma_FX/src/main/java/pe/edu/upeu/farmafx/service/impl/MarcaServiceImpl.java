package pe.edu.upeu.farmafx.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.upeu.farmafx.dto.ComboBoxOption;
import pe.edu.upeu.farmafx.model.Marca;
import pe.edu.upeu.farmafx.repository.ICrudGenericRepository;
import pe.edu.upeu.farmafx.repository.MarcaRepository;
import pe.edu.upeu.farmafx.service.IMarcaService;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class MarcaServiceImpl extends CrudGenericServiceImpl<Marca, Long> implements IMarcaService {

    private final MarcaRepository marcaRepository;
    private final pe.edu.upeu.farmafx.service.IProductoService productoService;

    @Override
    protected ICrudGenericRepository<Marca, Long> getRepository() {
        return marcaRepository;
    }

    @Override
    @Transactional
    public Marca save(Marca marca) {
        return marcaRepository.save(marca);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        marcaRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Marca> listar(Boolean estadoFiltro, Pageable pageable) {
        return marcaRepository.findByEstado(estadoFiltro, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Marca> buscarPaginado(String filtro, Boolean estadoFiltro, Pageable pageable) {
        return marcaRepository.findByNombreOrDescripcion(filtro, estadoFiltro, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ComboBoxOption> listarCombobox() {
        List<ComboBoxOption> opciones = new ArrayList<>();
        for (Marca marca : marcaRepository.findAll()) {
            ComboBoxOption option = new ComboBoxOption();
            option.setKey(String.valueOf(marca.getIdMarca()));
            option.setValue(marca.getNombreMarca());
            opciones.add(option);
        }
        return opciones;
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByNombreMarca(String nombre) {
        return marcaRepository.existsByNombreMarca(nombre);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByNombreMarcaAndNotId(String nombre, Long id) {
        return marcaRepository.existsByNombreMarcaAndNotId(nombre, id);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean tieneProductosAsociados(Long idMarca) {
        return marcaRepository.countProductosByMarca(idMarca) > 0;
    }

    @Override
    @Transactional(readOnly = true)
    public void validarMarca(Marca marca) throws IllegalArgumentException {
        if (marca == null) {
            throw new IllegalArgumentException("Marca no puede ser nula");
        }

        String nombre = marca.getNombreMarca();
        if (nombre == null || nombre.trim().isEmpty()) {
            throw new IllegalArgumentException("Nombre es obligatorio");
        }

        // Validar duplicado: si es NUEVO o si es EDICIÓN con ID diferente
        if (marca.getIdMarca() == null) {
            // Es NUEVO
            if (existsByNombreMarca(nombre.trim())) {
                throw new IllegalArgumentException("Nombre ya existe");
            }
        } else {
            // Es EDICIÓN
            if (existsByNombreMarcaAndNotId(nombre.trim(), marca.getIdMarca())) {
                throw new IllegalArgumentException("Nombre ya existe");
            }
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Long obtenerProximoId() {
        return marcaRepository.findMaxId()
            .map(maxId -> maxId + 1)
            .orElse(1L);
    }

    // ============ CASCADA MARCA → PRODUCTOS ============

    @Override
    @Transactional(readOnly = true)
    public long contarProductosActivosPorMarca(Long idMarca) {
        return productoService.contarPorMarcaYEstado(idMarca, true);
    }

    @Override
    @Transactional(readOnly = true)
    public long contarProductosInactivosPorMarca(Long idMarca) {
        return productoService.contarPorMarcaYEstado(idMarca, false);
    }

    @Override
    @Transactional
    public void inactivarMarcaYProductos(Long idMarca) {
        // Validar que existe
        Marca marca = findById(idMarca)
            .orElseThrow(() -> new IllegalArgumentException("Marca no encontrada con ID: " + idMarca));

        // Inactivar marca
        marca.setEstadoMarca(false);
        save(marca);

        // Inactivar productos activos (cascada)
        int productosInactivados = productoService.inactivarPorMarca(idMarca);

        // Log para auditoría
        if (productosInactivados > 0) {
            log.info("Marca ID {} inactivada con {} productos en cascada", idMarca, productosInactivados);
        }
    }

    @Override
    @Transactional
    public void activarMarcaYProductos(Long idMarca) {
        // Validar que existe
        Marca marca = findById(idMarca)
            .orElseThrow(() -> new IllegalArgumentException("Marca no encontrada con ID: " + idMarca));

        // Activar marca
        marca.setEstadoMarca(true);
        save(marca);

        // Activar productos inactivos (cascada)
        int productosActivados = productoService.activarPorMarca(idMarca);

        // Log para auditoría
        if (productosActivados > 0) {
            log.info("Marca ID {} activada con {} productos en cascada", idMarca, productosActivados);
        }
    }
}
