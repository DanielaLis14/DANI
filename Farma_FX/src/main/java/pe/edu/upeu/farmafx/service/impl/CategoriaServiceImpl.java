package pe.edu.upeu.farmafx.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.upeu.farmafx.dto.ComboBoxOption;
import pe.edu.upeu.farmafx.model.Categoria;
import pe.edu.upeu.farmafx.repository.CategoriaRepository;
import pe.edu.upeu.farmafx.repository.ICrudGenericRepository;
import pe.edu.upeu.farmafx.repository.ProductoRepository;
import pe.edu.upeu.farmafx.service.ICategoriaService;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class CategoriaServiceImpl extends CrudGenericServiceImpl<Categoria, Long> implements ICategoriaService {

    private final CategoriaRepository categoriaRepository;
    private final ProductoRepository productoRepository;

    @Override
    protected ICrudGenericRepository<Categoria, Long> getRepository() {
        return categoriaRepository;
    }

    @Override
    @Transactional
    public Categoria save(Categoria categoria) {
        return categoriaRepository.save(categoria);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        eliminarCategoriaConCascada(id);
    }


    @Override
    @Transactional(readOnly = true)
    public Page<Categoria> listar(Boolean estadoFiltro, Pageable pageable) {
        return categoriaRepository.findAll(estadoFiltro, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Categoria> buscarPaginado(String filtro, Boolean estadoFiltro, Pageable pageable) {
        return categoriaRepository.findByNombreOrDescripcion(filtro, estadoFiltro, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ComboBoxOption> listarCombobox() {
        List<ComboBoxOption> opciones = new ArrayList<>();
        for (Categoria categoria : categoriaRepository.findAll()) {
            ComboBoxOption option = new ComboBoxOption();
            option.setKey(String.valueOf(categoria.getIdCategoria()));
            option.setValue(categoria.getNombreCategoria());
            opciones.add(option);
        }
        return opciones;
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByNombreCategoria(String nombre) {
        return categoriaRepository.existsByNombreCategoria(nombre);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByNombreCategoriaAndNotId(String nombre, Long id) {
        return categoriaRepository.existsByNombreCategoriaAndNotId(nombre, id);
    }

    @Override
    @Transactional(readOnly = true)
    public Long obtenerProximoId() {
        return categoriaRepository.findMaxId()
            .map(maxId -> maxId + 1)
            .orElse(1L);
    }

    @Override
    @Transactional(readOnly = true)
    public long contarProductosActivosPorCategoria(Long idCategoria) {
        return categoriaRepository.countProductosByCategoriaAndEstado(idCategoria, true);
    }

    @Override
    @Transactional(readOnly = true)
    public long contarProductosInactivosPorCategoria(Long idCategoria) {
        return categoriaRepository.countProductosByCategoriaAndEstado(idCategoria, false);
    }

    @Override
    @Transactional(readOnly = true)
    public long contarProductosActivosTotales(Long idCategoria) {
        long total = contarProductosActivosPorCategoria(idCategoria);
        for (Categoria hija : obtenerCategoriasHijas(idCategoria)) {
            total += contarProductosActivosPorCategoria(hija.getIdCategoria());
        }
        return total;
    }

    @Override
    @Transactional(readOnly = true)
    public long contarProductosInactivosTotales(Long idCategoria) {
        long total = contarProductosInactivosPorCategoria(idCategoria);
        for (Categoria hija : obtenerCategoriasHijas(idCategoria)) {
            total += contarProductosInactivosPorCategoria(hija.getIdCategoria());
        }
        return total;
    }

    @Override
    @Transactional(readOnly = true)
    public long contarCategoriasHijasActivas(Long idCategoria) {
        return obtenerCategoriasHijas(idCategoria).stream()
            .filter(c -> Boolean.TRUE.equals(c.getEstadoCategoria()))
            .count();
    }

    @Override
    @Transactional(readOnly = true)
    public long contarCategoriasHijasInactivas(Long idCategoria) {
        return obtenerCategoriasHijas(idCategoria).stream()
            .filter(c -> Boolean.FALSE.equals(c.getEstadoCategoria()))
            .count();
    }

    @Override
    @Transactional
    public void inactivarCategoriaYProductos(Long idCategoria) {
        Categoria categoria = findById(idCategoria)
            .orElseThrow(() -> new IllegalArgumentException("Categoría no encontrada con ID: " + idCategoria));

        List<Categoria> hijas = obtenerCategoriasHijas(idCategoria);
        int categoriasInactivadas = 0;
        int productosInactivados = 0;

        // 1. Inactivar subcategorías activas y sus productos
        for (Categoria hija : hijas) {
            if (Boolean.TRUE.equals(hija.getEstadoCategoria())) {
                hija.setEstadoCategoria(false);
                save(hija);
                categoriasInactivadas++;
            }
            productosInactivados += productoRepository.inactivarPorCategoria(hija.getIdCategoria());
        }

        // 2. Inactivar categoría principal
        categoria.setEstadoCategoria(false);
        save(categoria);

        // 3. Inactivar productos de la categoría principal
        productosInactivados += productoRepository.inactivarPorCategoria(idCategoria);

        log.info("Categoría ID {} inactivada - {} subcategorías, {} productos en cascada",
                idCategoria, categoriasInactivadas, productosInactivados);
    }

    @Override
    @Transactional
    public void activarCategoriaYProductos(Long idCategoria) {
        Categoria categoria = findById(idCategoria)
            .orElseThrow(() -> new IllegalArgumentException("Categoría no encontrada con ID: " + idCategoria));

        List<Categoria> hijas = obtenerCategoriasHijas(idCategoria);
        int categoriasActivadas = 0;
        int productosActivados = 0;

        // 1. Activar subcategorías inactivas y sus productos
        for (Categoria hija : hijas) {
            if (Boolean.FALSE.equals(hija.getEstadoCategoria())) {
                hija.setEstadoCategoria(true);
                save(hija);
                categoriasActivadas++;
            }
            productosActivados += productoRepository.activarPorCategoria(hija.getIdCategoria());
        }

        // 2. Activar categoría principal
        categoria.setEstadoCategoria(true);
        save(categoria);

        // 3. Activar productos de la categoría principal
        productosActivados += productoRepository.activarPorCategoria(idCategoria);

        log.info("Categoría ID {} activada - {} subcategorías, {} productos en cascada",
                idCategoria, categoriasActivadas, productosActivados);
    }

    @Override
    @Transactional(readOnly = true)
    public void validarCategoria(Categoria categoria) throws IllegalArgumentException {
        if (categoria == null) {
            throw new IllegalArgumentException("Categoría no puede ser nula");
        }

        String nombre = categoria.getNombreCategoria();
        if (nombre == null || nombre.trim().isEmpty()) {
            throw new IllegalArgumentException("Nombre es obligatorio");
        }

        if (nombre.length() < 3) {
            throw new IllegalArgumentException("Nombre debe tener al menos 3 caracteres");
        }

        if (nombre.length() > 80) {
            throw new IllegalArgumentException("Nombre no puede exceder 80 caracteres");
        }

        String descripcion = categoria.getDescripcionCategoria();
        if (descripcion != null && descripcion.length() > 600) {
            throw new IllegalArgumentException("Descripción no puede exceder 600 caracteres");
        }

        // Validar duplicado: si es NUEVO o si es EDICIÓN con ID diferente
        if (categoria.getIdCategoria() == null) {
            // Es NUEVO
            if (existsByNombreCategoria(nombre.trim())) {
                throw new IllegalArgumentException("Nombre ya existe");
            }
        } else {
            // Es EDICIÓN
            if (existsByNombreCategoriaAndNotId(nombre.trim(), categoria.getIdCategoria())) {
                throw new IllegalArgumentException("Nombre ya existe");
            }
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<Categoria> obtenerCategoriasHijas(Long idPadre) {
        return obtenerCategoriasHijasRec(idPadre, new HashSet<>());
    }

    /**
     * Versión recursiva con protección contra ciclos.
     * @param idPadre ID de la categoría padre
     * @param visitados Set de IDs ya procesados para evitar ciclos infinitos
     */
    private List<Categoria> obtenerCategoriasHijasRec(Long idPadre, Set<Long> visitados) {
        List<Categoria> todasLasHijas = new ArrayList<>();

        // Protección contra ciclos: si ya visitamos este ID, no procesar
        if (visitados.contains(idPadre)) {
            return todasLasHijas;
        }
        visitados.add(idPadre);

        // Obtener hijas directas
        List<Categoria> hijasDirectas = categoriaRepository.findByCategoriaPadreId(idPadre);

        // Agregar cada hija y sus hijas recursivamente
        for (Categoria hija : hijasDirectas) {
            todasLasHijas.add(hija);
            todasLasHijas.addAll(obtenerCategoriasHijasRec(hija.getIdCategoria(), visitados));
        }

        return todasLasHijas;
    }

    // ============ MÉTODOS PARA ELIMINACIÓN CON CASCADA ============

    @Override
    @Transactional(readOnly = true)
    public long contarCategoriasHijas(Long idCategoria) {
        return obtenerCategoriasHijas(idCategoria).size();
    }

    @Override
    @Transactional
    public void eliminarCategoriaConCascada(Long idCategoria) {
        Categoria categoria = findById(idCategoria)
            .orElseThrow(() -> new IllegalArgumentException("Categoría no encontrada con ID: " + idCategoria));

        List<Categoria> hijas = obtenerCategoriasHijas(idCategoria);

        // 1. Huerfanizar productos (SET categoria = NULL)
        int totalProductosHuerfanizados = 0;

        // Huerfanizar productos de las hijas primero
        for (Categoria hija : hijas) {
            totalProductosHuerfanizados += productoRepository.huerfanizarPorCategoria(hija.getIdCategoria());
        }

        // Huerfanizar productos de la categoría principal
        totalProductosHuerfanizados += productoRepository.huerfanizarPorCategoria(idCategoria);

        // 2. Ordenar hijas por profundidad (más profundas primero) para evitar FK errors
        hijas.sort((c1, c2) -> {
            int prof1 = calcularProfundidad(c1);
            int prof2 = calcularProfundidad(c2);
            return Integer.compare(prof2, prof1); // Descendente
        });

        // 3. Eliminar hijas y categoría principal
        hijas.forEach(h -> categoriaRepository.deleteById(h.getIdCategoria()));
        categoriaRepository.deleteById(idCategoria);

        log.info("Categoría eliminada con cascada - ID: {}, Nombre: {}, Hijas: {}, Productos huerfanizados: {}",
                idCategoria, categoria.getNombreCategoria(), hijas.size(), totalProductosHuerfanizados);
    }

    /**
     * Calcula la profundidad de una categoría en la jerarquía.
     * Profundidad = número de niveles hasta la raíz.
     * 
     * @param categoria Categoría a calcular
     * @return Profundidad (0 = raíz, 1 = hija directa, 2 = nieta, etc.)
     */
    private int calcularProfundidad(Categoria categoria) {
        int profundidad = 0;
        Categoria actual = categoria;
        
        // Protección contra ciclos infinitos (máximo 100 niveles)
        int maxNiveles = 100;
        
        while (actual.getCategoriaPadre() != null && profundidad < maxNiveles) {
            profundidad++;
            actual = actual.getCategoriaPadre();
        }
        
        return profundidad;
    }
}
