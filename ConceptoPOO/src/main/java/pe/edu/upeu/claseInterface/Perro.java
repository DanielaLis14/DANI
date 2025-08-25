package pe.edu.upeu.claseInterface;

public class Perro implements Animal{
    @Override
    public void emitiSonido() {
        System.out.println("Guau..guau");

    }

    @Override
    public void dormir() {
        System.out.println("zzz.z.z.");

    }
}
