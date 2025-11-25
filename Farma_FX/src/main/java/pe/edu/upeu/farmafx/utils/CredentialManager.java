package pe.edu.upeu.farmafx.utils;

import lombok.extern.slf4j.Slf4j;
import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.prefs.Preferences;

/**
 * Gestor de credenciales persistentes para función "Recordar usuario".
 * Usa Preferences API de Java (almacenamiento seguro por usuario del SO).
 * Las contraseñas se encriptan con AES-128 antes de persistir.
 * <p>
 * Ubicación automática:
 * - Windows: Registry HKEY_CURRENT_USER\Software\JavaSoft\Prefs
 * - Linux: ~/.java/.userPrefs/pe/edu/upeu/farmafx/utils/prefs.xml
 * - Mac: ~/Library/Preferences/com.apple.java.util.prefs.plist
 *
 * @author FarmaFx Team
 * @version 2.0 (AES-128 encryption)
 */
@Slf4j
public class CredentialManager {

    // Preferences node (almacenamiento persistente)
    private static final Preferences prefs = Preferences.userNodeForPackage(CredentialManager.class);

    // Claves para almacenar en Preferences
    private static final String KEY_USERNAME = "last_username";
    private static final String KEY_PASSWORD_HASH = "password_hash";
    private static final String KEY_REMEMBER = "remember_me";

    // Configuración AES-128
    private static final String ALGORITHM = "AES";
    private static final String SECRET_KEY = "FarmaFx2025!Sec#"; // 16 caracteres = 128 bits (AES-128)

    /**
     * Guarda las credenciales del usuario de forma persistente.
     *
     * @param username Nombre de usuario
     * @param password Contraseña (se encripta antes de guardar)
     * @param remember Si es true, guarda las credenciales; si es false, las borra
     */
    public static void saveCredentials(String username, String password, boolean remember) {
        try {
            if (remember) {
                prefs.put(KEY_USERNAME, username);
                prefs.put(KEY_PASSWORD_HASH, encryptPassword(password));
                prefs.putBoolean(KEY_REMEMBER, true);
                log.info("Credenciales guardadas para usuario: {}", username);
            } else {
                clearCredentials();
            }
            prefs.flush(); // Forzar escritura inmediata
        } catch (Exception e) {
            log.error("Error al guardar credenciales", e);
        }
    }

    /**
     * Obtiene el último nombre de usuario guardado.
     *
     * @return Nombre de usuario, o cadena vacía si no existe
     */
    public static String getLastUsername() {
        return prefs.get(KEY_USERNAME, "");
    }

    /**
     * Obtiene la última contraseña guardada (desencriptada).
     *
     * @return Contraseña desencriptada, o cadena vacía si no existe
     */
    public static String getLastPassword() {
        String encrypted = prefs.get(KEY_PASSWORD_HASH, "");
        return encrypted.isEmpty() ? "" : decryptPassword(encrypted);
    }

    /**
     * Verifica si el usuario activó "Recordar usuario".
     *
     * @return true si debe recordar, false en caso contrario
     */
    public static boolean shouldRemember() {
        return prefs.getBoolean(KEY_REMEMBER, false);
    }

    /**
     * Borra todas las credenciales guardadas.
     */
    public static void clearCredentials() {
        try {
            prefs.remove(KEY_USERNAME);
            prefs.remove(KEY_PASSWORD_HASH);
            prefs.putBoolean(KEY_REMEMBER, false);
            prefs.flush();
            log.info("Credenciales borradas correctamente");
        } catch (Exception e) {
            log.error("Error al borrar credenciales", e);
        }
    }

    /**
     * Encripta la contraseña usando AES-128.
     * El resultado se codifica en Base64 para almacenamiento seguro.
     *
     * @param password Contraseña en texto plano
     * @return Contraseña encriptada en Base64, o cadena vacía si falla
     */
    private static String encryptPassword(String password) {
        try {
            // Crear clave secreta AES
            SecretKeySpec keySpec = new SecretKeySpec(SECRET_KEY.getBytes(StandardCharsets.UTF_8), ALGORITHM);

            // Inicializar cipher en modo encriptación
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, keySpec);

            // Encriptar y codificar en Base64
            byte[] encryptedBytes = cipher.doFinal(password.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(encryptedBytes);

        } catch (Exception e) {
            log.error("Error al encriptar contraseña con AES-128", e);
            return "";
        }
    }

    /**
     * Desencripta la contraseña desde AES-128.
     *
     * @param encrypted Contraseña encriptada en Base64
     * @return Contraseña en texto plano, o cadena vacía si falla
     */
    private static String decryptPassword(String encrypted) {
        try {
            // Crear clave secreta AES
            SecretKeySpec keySpec = new SecretKeySpec(SECRET_KEY.getBytes(StandardCharsets.UTF_8), ALGORITHM);

            // Inicializar cipher en modo desencriptación
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, keySpec);

            // Decodificar Base64 y desencriptar
            byte[] decodedBytes = Base64.getDecoder().decode(encrypted);
            byte[] decryptedBytes = cipher.doFinal(decodedBytes);
            return new String(decryptedBytes, StandardCharsets.UTF_8);

        } catch (Exception e) {
            log.error("Error al desencriptar contraseña con AES-128", e);
            return "";
        }
    }

    /**
     * Verifica si existen credenciales guardadas.
     *
     * @return true si hay credenciales, false en caso contrario
     */
    public static boolean hasStoredCredentials() {
        return shouldRemember() && !getLastUsername().isEmpty();
    }
}