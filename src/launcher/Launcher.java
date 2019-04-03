public class Launcher{

    public static void main(String[] args){
        Evolver e = new Evolver();

        //Crear configuracion
        EvolverConfig conf = new EvolverConfig();

        e.setConfig(config);

        Resutado = e.run();

        System.out.println(resultado);
    }
}