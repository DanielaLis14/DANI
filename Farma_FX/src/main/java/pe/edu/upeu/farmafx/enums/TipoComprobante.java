package pe.edu.upeu.farmafx.enums;

public enum TipoComprobante {
    FACTURA("Factura"),
    BOLETA("Boleta"),
    NOTA_CREDITO("Nota de crédito"),
    NOTA_DEBITO("Nota de débito");

    private final String descripcion;

    TipoComprobante(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getDescripcion() {
        return descripcion;
    }
}
