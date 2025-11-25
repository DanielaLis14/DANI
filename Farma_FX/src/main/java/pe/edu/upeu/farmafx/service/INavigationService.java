package pe.edu.upeu.farmafx.service;

import pe.edu.upeu.farmafx.dto.NavigationItemDto;
import java.util.Map;
import java.util.Properties;

/**
 * Servicio para gestión de items de navegación por perfil de usuario.
 *
 * Controla qué elementos de navegación puede ver cada perfil.
 * Similar a ButtonService pero para navegación, NO para botones CRUD.
 *
 * Flujo típico:
 * 1. Usuario hace login → SessionManager guarda perfil
 * 2. MainGuiModernController.initialize() llama obtenerNavegacion(perfil, idioma)
 * 3. Service filtra items según permisos del perfil
 * 4. Controller construye sidebar dinámicamente con los items retornados
 *
 * @see pe.edu.upeu.farmafx.service.impl.NavigationServiceImpl
 * @see pe.edu.upeu.farmafx.dto.NavigationItemDto
 * @since 2025-11-17
 */
public interface INavigationService {

    /**
     * Obtiene Map de items de navegación autorizados para un perfil.
     *
     * El Map retornado:
     * - Ya está filtrado según permisos del perfil
     * - Usa LinkedHashMap para mantener orden de inserción
     * - Incluye labels localizados según idioma
     * - Map vacío si perfil es null/inválido
     *
     * Ejemplos:
     * - Root: retorna TODOS los items (~11)
     * - Administrador: retorna mayoría de items (~10)
     * - Cajero: retorna solo items de ventas/lectura (~7)
     * - Cliente: retorna solo dashboard (~1)
     *
     * @param perfil Nombre del perfil (ej: "Root", "Administrador", "Cajero", "Cliente")
     * @param idioma Properties de i18n para localización (puede ser null, usa defaults)
     * @return Map ordenado de items autorizados (clave = id, nunca null)
     */
    Map<String, NavigationItemDto> obtenerNavegacion(String perfil, Properties idioma);
}