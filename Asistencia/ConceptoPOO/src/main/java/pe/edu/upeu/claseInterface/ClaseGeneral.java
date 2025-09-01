package pe.edu.upeu.claseInterface;

public class ClaseGeneral {
    public static void main(String[] args) {
        Animal a=new Loro();
        a.dormir();
        a.emitiSonido();
        Animal b;
        b=new Perro();
        b.dormir();
        b.emitiSonido();


    }
}
