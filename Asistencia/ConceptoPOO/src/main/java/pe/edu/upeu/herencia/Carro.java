package pe.edu.upeu.herencia;

public class Carro extends Vehiculo{
    String modelo="Mustang";
    String color="blanco";
    String placa ="PE-3244";

    void caracteristicas(){
        System.out.println("Las caracteristicas de este carro");
        System.out.println("Modelo: "+modelo);
        System.out.println("Placa: "+ placa);
        System.out.println("Color: "+color);
        System.out.println("Marca: "+marca);
    }

    public static void  main(String[] args) {
        Carro carro = new Carro();
        carro.caracteristicas();
        carro.sonido();
    }
}
