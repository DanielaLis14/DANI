package pe.edu.upeu.farmafx.utils;

import lombok.Data;

public enum Vistas {
    LOGIN("/fxml/login.fxml"),
    REGISTRO("/fxml/registro.fxml"),
    MENU_ADMIN("/fxml/menu_admin.fxml"),
    GESTION_MARCAS("/fxml/gestion_marcas.fxml"),
    GESTION_CATEGORIAS("/fxml/gestion_categorias.fxml"),
    GESTION_USUARIOS("/fxml/gestion_usuarios.fxml"),
    MENU_CLIENTE("/fxml/menu_cliente.fxml");

    private final String ruta;

    Vistas(String ruta) {
        this.ruta = ruta;
    }

    public String getRuta() {
        return ruta;
    }
}