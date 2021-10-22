package proxy;

public class RunProxy {
    public static void main(String[] args) {
        Proxy prueba=new Proxy();
        System.out.println("Configuracion:"+prueba.toString());
        prueba.atenderPeticiones();
    }
}
