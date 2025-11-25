package pe.edu.upeu.farmafx.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import pe.edu.upeu.farmafx.dto.NavigationItemDto;
import pe.edu.upeu.farmafx.enums.ViewRoute;
import pe.edu.upeu.farmafx.service.INavigationService;

import java.util.*;

/**
 * Implementación del servicio de navegación con permisos por perfil.
 * - Map para verificación O(1)
 * - Constructor para inicializar permisos
 * - Switch expression para construcción de items
 *
 * @since 2025-11-17
 */
@Service
@Slf4j
public class NavigationServiceImpl implements INavigationService {

    /**
     * Orden fijo de items en el sidebar.
     * Se respeta al construir el Map retornado (LinkedHashMap).
     */
    private static final String[] ORDEN_NAVEGACION = {
        "dashboard",
        "productos", "marcas", "categorias",
        "pos", "historial_ventas",
        "clientes", "usuarios",
        "reportes",
        "configuracion", "cerrar_sesion"
    };

    /**
     * Estructura de permisos: {perfil} -> Set<itemId autorizados>
     */
    private final Map<String, Set<String>> permisos = new HashMap<>();

    public NavigationServiceImpl() {
        inicializarPermisos();
    }

    private void inicializarPermisos() {
        initRoot();
        initAdmin();
        initCajero();
        initCliente();
        log.info("Permisos de navegación inicializados para 4 perfiles");
    }

    private void initRoot() {
        permisos.put("Root", new HashSet<>(Arrays.asList(ORDEN_NAVEGACION)));
    }

    private void initAdmin() {
        permisos.put("Administrador", Set.of(
            "dashboard",
            "productos", "marcas", "categorias",
            "pos", "historial_ventas",
            "clientes", "usuarios",
            "reportes",
            "configuracion", "cerrar_sesion"
        ));
    }

    private void initCajero() {
        permisos.put("Cajero", Set.of(
            "dashboard",
            "productos", "marcas", "categorias",
            "pos", "historial_ventas",
            "cerrar_sesion"
        ));
    }

    private void initCliente() {
        permisos.put("Cliente", Set.of(
            "dashboard",
            "cerrar_sesion"
        ));
    }

    @Override
    public Map<String, NavigationItemDto> obtenerNavegacion(String perfil, Properties idioma) {
        if (perfil == null || perfil.isEmpty()) {
            log.warn("Perfil null/vacío - retornando Map vacío");
            return new LinkedHashMap<>();
        }

        Set<String> autorizados = permisos.getOrDefault(perfil, Set.of());
        Map<String, NavigationItemDto> resultado = new LinkedHashMap<>();

        for (String itemId : ORDEN_NAVEGACION) {
            if (autorizados.contains(itemId)) {
                NavigationItemDto dto = construirItem(itemId, idioma);
                if (dto != null) {
                    resultado.put(itemId, dto);
                }
            }
        }

        log.info("Navegación para '{}': {} items cargados", perfil, resultado.size());
        return resultado;
    }

    private NavigationItemDto construirItem(String itemId, Properties idioma) {
        return switch (itemId) {
            // ========== ITEM ESPECIAL: DASHBOARD ==========
            case "dashboard" -> new NavigationItemDto(
                "dashboard",
                getProp(idioma, "nav.label.dashboard", "Dashboard"),
                "HOME",
                null,
                null,
                "VIEW",
                "Ctrl+D",
                true
            );

            // ========== GRUPO: INVENTARIO ==========
            case "productos" -> new NavigationItemDto(
                "productos",
                getProp(idioma, "nav.label.productos", "Productos"),
                "CUBES",
                ViewRoute.PRODUCTOS.getPath(),  // TODO: ViewRoute.PRODUCTOS_MODERN.getPath()
                "INVENTARIO",
                "VIEW",
                "Ctrl+1",
                true
            );

            case "marcas" -> new NavigationItemDto(
                "marcas",
                getProp(idioma, "nav.label.marcas", "Marcas"),
                "TAG",
                ViewRoute.MARCAS.getPath(),
                "INVENTARIO",
                "VIEW",
                "Ctrl+2",
                true
            );

            case "categorias" -> new NavigationItemDto(
                "categorias",
                getProp(idioma, "nav.label.categorias", "Categorías"),
                "SITEMAP",
                ViewRoute.CATEGORIAS.getPath(),
                "INVENTARIO",
                "VIEW",
                "Ctrl+3",
                true
            );

            // ========== GRUPO: VENTAS ==========
            case "pos" -> new NavigationItemDto(
                "pos",
                getProp(idioma, "nav.label.pos", "Punto de Venta"),
                "CREDIT_CARD",
                null,
                "VENTAS",
                "VIEW",
                "Ctrl+4",
                true
            );

            case "historial_ventas" -> new NavigationItemDto(
                "historial_ventas",
                getProp(idioma, "nav.label.historial_ventas", "Historial Ventas"),
                "HISTORY",
                null,
                "VENTAS",
                "VIEW",
                "Ctrl+5",
                true
            );

            // ========== GRUPO: GESTION ==========
            case "clientes" -> new NavigationItemDto(
                "clientes",
                getProp(idioma, "nav.label.clientes", "Clientes"),
                "USERS",
                null,
                "GESTION",
                "VIEW",
                "Ctrl+6",
                true
            );

            case "usuarios" -> new NavigationItemDto(
                "usuarios",
                getProp(idioma, "nav.label.usuarios", "Usuarios"),
                "USER",
                null,
                "GESTION",
                "VIEW",
                "Ctrl+7",
                true
            );

            // ========== GRUPO: ANALISIS ==========
            case "reportes" -> new NavigationItemDto(
                "reportes",
                getProp(idioma, "nav.label.reportes", "Reportes"),
                "BAR_CHART",
                null,
                "ANALISIS",
                "VIEW",
                "Ctrl+8",
                true
            );

            // ========== GRUPO: SISTEMA ==========
            case "configuracion" -> new NavigationItemDto(
                "configuracion",
                getProp(idioma, "nav.label.configuracion", "Configuración"),
                "COG",
                null,
                "SISTEMA",
                "VIEW",
                "Ctrl+9",
                true
            );

            case "cerrar_sesion" -> new NavigationItemDto(
                "cerrar_sesion",
                getProp(idioma, "nav.label.cerrar_sesion", "Cerrar Sesión"),
                "SIGN_OUT",
                null,
                "SISTEMA",
                "EXIT",
                "Ctrl+Q",
                true
            );

            default -> {
                log.warn("Item desconocido: '{}'", itemId);
                yield null;
            }
        };
    }

    private String getProp(Properties props, String key, String defaultValue) {
        if (props == null) {
            return defaultValue;
        }
        return props.getProperty(key, defaultValue);
    }
}