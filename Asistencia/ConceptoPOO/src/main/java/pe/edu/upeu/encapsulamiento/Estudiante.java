package pe.edu.upeu.encapsulamiento;

public class Estudiante {
    private String codigo;
    private String  carrera;


    public void trabajo(){
        System.out.println("Estudiar,investigar en la carrera de: "+carrera
        +codigo);
    }

    public String getCarrera() {
        return carrera;
    }

    public void setCarrera(String carrera) {
        this.carrera = carrera;
    }

    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }
}
