package pe.edu.upeu.farmafx.utils;

import org.springframework.stereotype.Component;
import org.springframework.context.annotation.Scope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Gestor de sesión del usuario autenticado.
 * @Component: Spring lo inyecta automáticamente
 * @Scope("singleton"): Una sola instancia en toda la app
 */
@Component
@Scope("singleton")
public class SessionManager {

    private static final Logger logger = LoggerFactory.getLogger(SessionManager.class);

    private Long userId;
    private String userName;
    private String userPerfil;
    private boolean loginExitoso = false;  // Flag temporal para Toast de bienvenida

    // Getters
    public Long getUserId() {
        return userId;
    }

    public String getUserName() {
        return userName;
    }

    public String getUserPerfil() {
        return userPerfil;
    }

    // Setters con validación
    public void setUserId(Long userId) {
        if (userId == null) {
            throw new IllegalArgumentException("userId no puede ser null");
        }
        this.userId = userId;
    }

    public void setUserName(String userName) {
        if (userName == null || userName.trim().isEmpty()) {
            throw new IllegalArgumentException("userName no puede ser vacío");
        }
        this.userName = userName;
        logger.info("Usuario autenticado: {}", userName);
    }

    public void setUserPerfil(String userPerfil) {
        if (userPerfil == null || userPerfil.trim().isEmpty()) {
            throw new IllegalArgumentException("userPerfil no puede ser null");
        }
        this.userPerfil = userPerfil;
        logger.info("Perfil asignado: {} para usuario: {}", userPerfil, userName);
    }

    /**
     * Retorna perfil o defecto si es null.
     */
    public String getPerfilOrDefault(String defecto) {
        return userPerfil != null ? userPerfil : defecto;
    }

    /**
     * Verifica si la sesión es válida.
     */
    public boolean isAutenticado() {
        return userId != null && userName != null && userPerfil != null;
    }

    /**
     * Marca que el usuario acaba de hacer login exitoso.
     * MainGui consumirá este flag para mostrar Toast de bienvenida.
     */
    public void setLoginExitoso() {
        this.loginExitoso = true;
        logger.info("Login exitoso marcado para usuario: {}", userName);
    }

    /**
     * Verifica si acaba de hacer login (una sola vez).
     * Consume el flag (se resetea automáticamente después de consultar).
     */
    public boolean consumirLoginExitoso() {
        if (loginExitoso) {
            loginExitoso = false;  // Resetear flag
            logger.info("Login exitoso consumido - Toast de bienvenida mostrado");
            return true;
        }
        return false;
    }

    /**
     * Limpia la sesión (logout).
     */
    public void clear() {
        if (isAutenticado()) {
            logger.info("Sesión cerrada para usuario: {}", userName);
        }
        userId = null;
        userName = null;
        userPerfil = null;
        loginExitoso = false;  // Limpiar flag también
    }
}
