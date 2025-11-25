package pe.edu.upeu.farmafx.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import pe.edu.upeu.farmafx.dto.ButtonDto;
import pe.edu.upeu.farmafx.service.IButtonService;

import jakarta.annotation.PostConstruct;
import java.util.*;

/**
 * Implementación eficaz y simple del servicio de permisos de botones.
 *
 * Usa estructura Map<Perfil, Map<Modulo, Set<Botones>>> para verificación O(1).
 * Sin DTOs innecesarios, solo lógica de permisos.
 *
 * Perfiles: Root, Administrador, Cajero, Cliente
 * Módulos: PRODUCTOS, CATEGORIAS, MARCAS, USUARIOS, CLIENTES, PROVEEDORES
 * Botones: nuevo, editar, inactivar, eliminar
 */
@Service
public class ButtonServiceImpl implements IButtonService {

    private static final Logger logger = LoggerFactory.getLogger(ButtonServiceImpl.class);

    /**
     * Estructura: {perfil} -> {módulo} -> Set<botonId permitidos>
     * Permite verificación rápida de permisos sin búsquedas lineales.
     */
    private final Map<String, Map<String, Set<String>>> permisos = new HashMap<>();

    @PostConstruct
    private void init() {
        initRoot();
        initAdmin();
        initCajero();
        initCliente();
        logger.info("Permisos de botones inicializados");
    }

    private static final String[] TODOS_MODULOS =
        {"PRODUCTOS", "CATEGORIAS", "MARCAS", "USUARIOS", "CLIENTES", "PROVEEDORES"};

    /**
     * Root: Acceso total a todo.
     */
    private void initRoot() {
        agregarPerfilConBotones("Root",
            Set.of("nuevo", "editar", "inactivar", "eliminar"));
    }

    /**
     * Admin: Puede nuevo, editar, inactivar. NO puede eliminar.
     */
    private void initAdmin() {
        agregarPerfilConBotones("Administrador",
            Set.of("nuevo", "editar", "inactivar"));
    }

    /**
     * Cajero: Solo lectura en todos los módulos (sin botones).
     */
    private void initCajero() {
        agregarPerfilConBotones("Cajero", Set.of());
    }

    /**
     * Cliente: Sin botones en ningún módulo (lectura solamente).
     */
    private void initCliente() {
        agregarPerfilConBotones("Cliente", Set.of());
    }

    /**
     * Helper para agregar un perfil con sus botones en todos los módulos.
     */
    private void agregarPerfilConBotones(String perfil, Set<String> botones) {
        Map<String, Set<String>> modulos = new HashMap<>();
        for (String modulo : TODOS_MODULOS) {
            modulos.put(modulo, botones);
        }
        permisos.put(perfil, modulos);
    }

    @Override
    public boolean isEnabled(String perfil, String modulo, String botonId) {
        if (perfil == null || modulo == null || botonId == null) {
            return false;
        }

        Set<String> botonesPermitidos = permisos
                .getOrDefault(perfil, new HashMap<>())
                .getOrDefault(modulo, Set.of());

        return botonesPermitidos.contains(botonId);
    }

    @Override
    public boolean puede(String perfil, String modulo, String botonId) {
        return isEnabled(perfil, modulo, botonId);
    }

    @Override
    public Map<String, ButtonDto> obtenerBotones(String perfil, String modulo, Properties idioma) {
        if (perfil == null || modulo == null) {
            return new LinkedHashMap<>();
        }

        Set<String> autorizados = permisos
            .getOrDefault(perfil, new HashMap<>())
            .getOrDefault(modulo, Set.of());

        Map<String, ButtonDto> resultado = new LinkedHashMap<>();

        // Orden fijo de botones
        String[] ordenBotones = {"nuevo", "editar", "inactivar", "eliminar", "guardar", "cancelar"};

        for (String botonId : ordenBotones) {
            if (autorizados.contains(botonId) || botonId.equals("guardar") || botonId.equals("cancelar")) {
                ButtonDto dto = construirBoton(botonId, idioma);
                if (dto != null) {
                    // guardar/cancelar siempre enabled si el perfil tiene algún botón
                    if ((botonId.equals("guardar") || botonId.equals("cancelar")) && autorizados.isEmpty()) {
                        continue;
                    }
                    resultado.put(botonId, dto);
                }
            }
        }

        logger.debug("Botones para '{}' en '{}': {}", perfil, modulo, resultado.keySet());
        return resultado;
    }

    private ButtonDto construirBoton(String botonId, Properties idioma) {
        return switch (botonId) {
            case "nuevo" -> new ButtonDto(
                "nuevo",
                getProp(idioma, "button.label.nuevo", "Nuevo"),
                "PLUS",
                getProp(idioma, "button.tooltip.nuevo", "Crear nuevo registro"),
                null,
                true
            );

            case "editar" -> new ButtonDto(
                "editar",
                getProp(idioma, "button.label.editar", "Editar"),
                "EDIT",
                getProp(idioma, "button.tooltip.editar", "Editar registro seleccionado"),
                null,
                true
            );

            case "inactivar" -> new ButtonDto(
                "inactivar",
                getProp(idioma, "button.label.inactivar", "Inactivar"),
                "BAN",
                getProp(idioma, "button.tooltip.inactivar", "Cambiar estado del registro"),
                null,
                true
            );

            case "eliminar" -> new ButtonDto(
                "eliminar",
                getProp(idioma, "button.label.eliminar", "Eliminar"),
                "TRASH",
                getProp(idioma, "button.tooltip.eliminar", "Eliminar registro seleccionado"),
                null,
                true
            );

            case "guardar" -> new ButtonDto(
                "guardar",
                getProp(idioma, "button.label.guardar", "Guardar"),
                "SAVE",
                getProp(idioma, "button.tooltip.guardar", "Guardar cambios"),
                null,
                true
            );

            case "cancelar" -> new ButtonDto(
                "cancelar",
                getProp(idioma, "button.label.cancelar", "Cancelar"),
                "TIMES",
                getProp(idioma, "button.tooltip.cancelar", "Cancelar edición"),
                null,
                true
            );

            default -> null;
        };
    }

    private String getProp(Properties props, String key, String defaultValue) {
        if (props == null) {
            return defaultValue;
        }
        return props.getProperty(key, defaultValue);
    }
}
