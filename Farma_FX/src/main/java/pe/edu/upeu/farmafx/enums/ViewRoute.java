package pe.edu.upeu.farmafx.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ViewRoute {

    // ========== AUTENTICACIÓN ==========
    // LOGIN("/view/auth/login.fxml"),  //  DEPRECADO - Usar TEST_LOGIN
    TEST_LOGIN("/view/auth/login.fxml"),  //  Login moderno con "Recordar usuario" (ACTIVO)
    // REGISTER("/view/auth/register.fxml"),  // COMENTADO - No se usa actualmente

    // ========== VISTAS PRINCIPALES ==========
    // MAIN_GUI("/view/main/main_gui.fxml"),  //  DEPRECADO - Usar MAIN_GUI_MODERN
    MAIN_GUI_MODERN("/view/main/main_gui.fxml"),  //  MainGui moderno (sidebar + header)

    // ========== GESTIÓN PRODUCTOS ==========
    PRODUCTOS("/view/productos/productos.fxml"),  // Vista GOLD moderna para MainGui
    CATEGORIAS("/view/productos/categorias.fxml"),
    MARCAS("/view/productos/marcas.fxml"),  // Vista GOLD moderna para MainGui

    // DEPRECADOS - Eliminados
    // MARCAS_MODERN - archivo no existe
    // MARCAS_PANEL - alias redundante de MARCAS_MODERN

    // GESTIÓN USUARIOS
    ADMINS_LISTA("/view/usuarios/admins_lista.fxml"),
    CAJEROS_LISTA("/view/usuarios/cajeros_lista.fxml"),
    CLIENTES_LISTA("/view/usuarios/clientes_lista.fxml"),
    CLIENTES("/view/usuarios/clientes_lista.fxml"),  // Alias corto

    // VENTAS
    NUEVA_VENTA("/view/ventas/nueva_venta.fxml"),
    HISTORIAL_VENTAS("/view/ventas/historial_ventas.fxml"),
    ANULAR_VENTA("/view/ventas/anular_venta.fxml"),

    // COMPRAS
    NUEVA_COMPRA("/view/compras/nueva_compra.fxml"),
    HISTORIAL_COMPRAS("/view/compras/historial_compras.fxml"),

    // PROVEEDORES
    PROVEEDORES_LISTA("/view/proveedores/proveedores_lista.fxml"),

    // PERFIL Y DIRECCIÓN
    MI_PERFIL("/view/perfil/mi_perfil.fxml"),
    MI_DIRECCION("/view/perfil/mi_direccion.fxml"),

    // CLIENTE - CATÁLOGO
    CATALOGO_PRODUCTOS("/view/cliente/catalogo_productos.fxml"),
    MI_CARRITO("/view/cliente/mi_carrito.fxml"),
    MIS_COMPRAS("/view/cliente/mis_compras.fxml"),

    // CONTROL
    EXIT("/exit");  // Para acciones que salen de la app (no se usa, solo documentación)

    private final String path;
}
