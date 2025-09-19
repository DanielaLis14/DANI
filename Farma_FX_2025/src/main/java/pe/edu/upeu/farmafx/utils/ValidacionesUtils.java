package pe.edu.upeu.farmafx.utils;

public class ValidacionesUtils {

    public static boolean validarDni(String dni) {
        return dni != null && dni.matches("\\d{8}");
    }

    public static boolean validarClave(String clave) {
        if (clave == null || clave.length() <= 8) {
            return false;
        }
        boolean tieneLetra = false;
        boolean tieneNumero = false;
        for (char c : clave.toCharArray()) {
            if (Character.isLetter(c)) {
                tieneLetra = true;
            } else if (Character.isDigit(c)) {
                tieneNumero = true;
            }
            if (tieneLetra && tieneNumero) {
                return true;
            }
        }
        return false;
    }
}