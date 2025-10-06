package pe.edu.upeu.asistencia.conexion;

import javax.swing.*;
import java.sql.*;

public class ConDB {

    public static Connection conexion=null;

    public static Connection getConexion(){
        try {
            Class.forName("org.sqlite.JDBC");
            String url="jdbc:sqlite:data/asistenciadb.db?foreign_keys=on;";
            if(conexion==null){
                conexion= DriverManager.getConnection(url);
            }
            System.out.println("Coneccion exitosa");
        }catch( ClassNotFoundException | SQLException e){
            JOptionPane.showMessageDialog(null,"Error:"+e.getMessage());
        }
        return conexion;
    }

    public static void closeConexion(){
        if(conexion!=null){
            try {
                conexion.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] args) {
        PreparedStatement pst=null;
        ResultSet rs=null;
        Connection con=getConexion();
        try {
            pst=con.prepareStatement("SELECT * FROM participante");
            rs=pst.executeQuery();
            while(rs.next()){
                String nombre=rs.getString("nombre");
                String apellido=rs.getString("apellidos");
                System.out.println(nombre+" "+apellido);
            }

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}