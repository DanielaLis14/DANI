package pe.edu.upeu.encapsulamiento;

public class ClaseGeneral {
    public static void probarJugador() {
        Jugador jugador = new Jugador();
        jugador.setNombre("Dani");
        jugador.setApellido("Cóndor");
        jugador.setEdad(20);
        jugador.setPosicion("2");
        jugador.setNumeroCam(1);
        System.out.println(jugador);
    }


    public static void  probar(){
        Estudiante estudiante = new Estudiante();
        estudiante.setCarrera("Ing sistemas");
        estudiante.setCodigo("234144");
        estudiante.trabajo();
    }


    public static void main(String[] args) {
        Persona persona = new Persona();
        persona.nombre="Dani";
        persona.edad=20;
        persona.saludo();

        probar();
        probarJugador();
    }
}
