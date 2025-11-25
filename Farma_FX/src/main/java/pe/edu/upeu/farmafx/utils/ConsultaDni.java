package pe.edu.upeu.farmafx.utils;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pe.edu.upeu.farmafx.dto.PersonaDto;

import java.io.IOException;

/**
 * Realiza una consulta simple de datos de DNI consumiendo el sitio público https://eldni.com.
 * Considerar que el formato puede cambiar y la red puede requerir permisos adicionales.
 */
public class ConsultaDni {

    private static final Logger logger = LoggerFactory.getLogger(ConsultaDni.class);
    private static final String URL = "https://eldni.com/pe/buscar-datos-por-dni";

    public PersonaDto consultarDni(String dni) {
        if (dni == null || dni.isBlank()) {
            throw new IllegalArgumentException("El DNI es obligatorio");
        }

        PersonaDto personaDto = new PersonaDto();
        try {
            Connection.Response getResponse = Jsoup.connect(URL)
                    .method(Connection.Method.GET)
                    .execute();

            Document document = getResponse.parse();
            String token = document.select("input[name=_token]").attr("value");

            Connection.Response postResponse = Jsoup.connect(URL)
                    .cookies(getResponse.cookies())
                    .data("_token", token)
                    .data("dni", dni.trim())
                    .method(Connection.Method.POST)
                    .ignoreContentType(true)
                    .execute();

            Document resultDoc = Jsoup.parse(postResponse.body());
            Element row = resultDoc.selectFirst("table tbody tr");
            if (row != null) {
                Elements cells = row.select("td");
                if (cells.size() >= 4) {
                    personaDto.setDni(cells.get(0).text());
                    personaDto.setNombre(cells.get(1).text());
                    personaDto.setApellidoPaterno(cells.get(2).text());
                    personaDto.setApellidoMaterno(cells.get(3).text());
                }
            } else {
                logger.warn("No se encontraron datos para el DNI {}", dni);
            }
        } catch (IOException ex) {
            logger.error("Error consultando DNI {}", dni, ex);
        }
        return personaDto;
    }
}
