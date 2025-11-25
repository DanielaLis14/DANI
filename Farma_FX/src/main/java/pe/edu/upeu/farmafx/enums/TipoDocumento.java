package pe.edu.upeu.farmafx.enums;

public enum TipoDocumento {
    DNI("DNI"),
    RUC("RUC");

    private final String descripcion;

    TipoDocumento(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getDescripcion() {
        return descripcion;
    }
}
