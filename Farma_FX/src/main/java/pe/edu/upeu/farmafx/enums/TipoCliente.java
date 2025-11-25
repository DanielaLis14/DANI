package pe.edu.upeu.farmafx.enums;

public enum TipoCliente {
    NATURAL("Persona natural"),
    JURIDICO("Persona jurídica");

    private final String descripcion;

    TipoCliente(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getDescripcion() {
        return descripcion;
    }
}
