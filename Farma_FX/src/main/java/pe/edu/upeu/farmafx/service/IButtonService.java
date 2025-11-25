package pe.edu.upeu.farmafx.service;

import pe.edu.upeu.farmafx.dto.ButtonDto;
import java.util.Map;
import java.util.Properties;

/**
 * Servicio para verificación centralizada de permisos de botones por perfil.
 *
 * Controla qué botones están habilitados para cada perfil en cada módulo.
 * Provee configuración completa de botones (label, icon, tooltip, shortcut).
 *
 * @see pe.edu.upeu.farmafx.service.impl.ButtonServiceImpl
 * @since 2025-11-17
 */
public interface IButtonService {

    /**
     * Obtiene Map de botones autorizados con configuración completa.
     *
     * @param perfil nombre del perfil (ej: "Root", "Administrador")
     * @param modulo nombre del módulo (ej: "MARCAS", "PRODUCTOS")
     * @param idioma Properties de i18n para localización
     * @return Map de botones autorizados (clave = id, solo los permitidos)
     */
    Map<String, ButtonDto> obtenerBotones(String perfil, String modulo, Properties idioma);

    /**
     * Verifica si un perfil puede acceder a un botón en un módulo específico.
     *
     * @param perfil nombre del perfil (ej: "Root", "Administrador", "Cajero", "Cliente")
     * @param modulo nombre del módulo (ej: "PRODUCTOS", "CATEGORIAS", "USUARIOS")
     * @param botonId ID del botón (ej: "nuevo", "editar", "inactivar", "eliminar")
     * @return true si el botón está habilitado, false en caso contrario
     */
    boolean isEnabled(String perfil, String modulo, String botonId);

    /**
     * Verifica rápidamente si un perfil puede ver (acceder) un botón.
     * Alias corto de isEnabled().
     *
     * @param perfil nombre del perfil
     * @param modulo nombre del módulo
     * @param botonId ID del botón
     * @return true si el botón es visible/habilitado
     */
    boolean puede(String perfil, String modulo, String botonId);
}
