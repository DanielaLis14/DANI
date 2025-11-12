package pe.edu.upeu.sysventas.utils;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Component;
import pe.edu.upeu.sysventas.dto.PersonaDto;

import java.io.IOException;

@Component
public  class ConsultaDNI {

    public PersonaDto consultarDNI(String dni){

        PersonaDto personaDto=new PersonaDto();
        String url = "https://eldni.com/pe/buscar-datos-por-dni";
        try {

            // Primero: hacer GET para obtener el _token
            Connection.Response getResponse = Jsoup.connect(url)
                    .method(Connection.Method.GET)
                    .execute();

            Document doc = getResponse.parse();
            String token = doc.select("input[name=_token]").attr("value");

            // Ahora hacer POST con el DNI y el token
            Connection.Response postResponse = Jsoup.connect(url)
                    .cookies(getResponse.cookies()) // mantener la sesión
                    .data("_token", token)
                    .data("dni", dni)
                    .method(Connection.Method.POST)
                    .ignoreContentType(true)
                    .execute();
            // Paso 3: parsear respuesta
            Document resultDoc = Jsoup.parse(postResponse.body());
            Element fila = resultDoc.select("table tbody tr").first();
            if (fila != null) {
                Elements celdas = fila.select("td");
                personaDto.setDni(celdas.get(0).text());
                personaDto.setNombre(celdas.get(1).text());
                personaDto.setApellidoPaterno(celdas.get(2).text());
                personaDto.setApellidoMaterno(celdas.get(3).text());
            }
        }catch (IOException e) {
            e.printStackTrace();
        }
        return personaDto;
    }


    public static void main(String[] args) {
        ConsultaDNI p=new ConsultaDNI();
        PersonaDto a=  p.consultarDNI("73454203");
        System.out.println(a.toString());
    }

}
