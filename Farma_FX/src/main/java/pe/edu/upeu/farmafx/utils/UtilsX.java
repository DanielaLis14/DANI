package pe.edu.upeu.farmafx.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.*;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Locale;
import java.util.Properties;

/**
 * Utilidades misceláneas para acceso a archivos externos e internacionalización.
 */
@Component
public class UtilsX {

    private static final Logger logger = LoggerFactory.getLogger(UtilsX.class);
    private static final String CONFIG_FILE = "FarmaFx.properties";
    private static final String LANGUAGE_FOLDER = "language";

    public URL getFile(String ruta)  {
        return getClass().getResource("/" + ruta);
    }

    public File getFileExterno(String carpeta, String fileName) {
        Path folderPath = Paths.get(carpeta).toAbsolutePath();
        return folderPath.resolve(fileName).toFile();
    }

    public File getFolderExterno(String carpeta) {
        return Paths.get(carpeta).toAbsolutePath().toFile();
    }

    public Properties detectLanguage(String idioma) {
        Properties properties = new Properties();
        File target = getFileExterno(LANGUAGE_FOLDER, "idiomas-" + idioma + ".properties");
        if (!target.exists()) {
            logger.warn("Archivo de idioma no encontrado: {}", target.getAbsolutePath());
            return properties;
        }
        try (FileInputStream inputStream = new FileInputStream(target)) {
            properties.load(inputStream);
        } catch (IOException ex) {
            logger.error("Error cargando archivo de idioma {}", target.getAbsolutePath(), ex);
        }
        return properties;
    }

    /**
     * Lee el idioma actual desde FarmaFx.properties.
     * Si no existe el archivo o la clave, detecta idioma del SO.
     *
     * @return Código de idioma: "es", "en" o "fr"
     */
    public String cargarIdiomaActual() {
        File config = getFileExterno(LANGUAGE_FOLDER, CONFIG_FILE);

        // Si no existe el archivo, crear con idioma del SO
        if (!config.exists()) {
            String idiomaOS = detectarIdiomaDelSO();
            guardarIdioma(idiomaOS);
            return idiomaOS;
        }

        // Leer archivo existente
        Properties properties = new Properties();
        try (FileInputStream inputStream = new FileInputStream(config)) {
            properties.load(inputStream);
            String idioma = properties.getProperty("FarmaFx.idioma", "");

            if (idioma.isEmpty()) {
                String idiomaOS = detectarIdiomaDelSO();
                guardarIdioma(idiomaOS);
                return idiomaOS;
            }

            return idioma;
        } catch (IOException ex) {
            logger.error("Error leyendo configuración de idioma {}", config.getAbsolutePath(), ex);
            return detectarIdiomaDelSO();
        }
    }

    /**
     * Guarda el idioma seleccionado en FarmaFx.properties.
     *
     * @param idioma Código de idioma: "es", "en" o "fr"
     */
    public void guardarIdioma(String idioma) {
        File config = getFileExterno(LANGUAGE_FOLDER, CONFIG_FILE);
        Properties properties = new Properties();

        // Crear directorio si no existe
        try {
            Files.createDirectories(config.getParentFile().toPath());
        } catch (IOException ex) {
            logger.error("Error creando directorio de configuración", ex);
        }

        // Guardar propiedad
        properties.setProperty("FarmaFx.idioma", idioma);
        try (FileOutputStream outputStream = new FileOutputStream(config)) {
            properties.store(outputStream, "Configuracion de idioma FarmaFx");
            logger.info("Idioma guardado: {}", idioma);
        } catch (IOException ex) {
            logger.error("Error guardando idioma en {}", config.getAbsolutePath(), ex);
        }
    }

    /**
     * Detecta el idioma del sistema operativo.
     *
     * @return "es" para español, "fr" para francés, "en" por defecto
     */
    private String detectarIdiomaDelSO() {
        String idiomaOS = Locale.getDefault().getLanguage();
        return switch (idiomaOS) {
            case "es" -> "es";
            case "fr" -> "fr";
            default -> "en";
        };
    }

}
