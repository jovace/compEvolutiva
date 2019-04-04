package launcher;

import evolutivo.*;

public class Launcher{

    public static void main(String[] args){
        Evolver e = new Evolver();

        //Crear configuracion
        EvolverConfig conf = new EvolverConfig(10);

        e.setConfig(conf);
        
        e.inicializar();
        e.run();

    }
}