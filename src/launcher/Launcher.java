package launcher;

import evolutivo.*;

public class Launcher{


    private static final int MAX_GENERACIONES = 500;

    public static void main(String[] args){
        Evolver e = new Evolver();

        //Crear configuracion
        EvolverConfig conf = new EvolverConfig(20);

        e.setConfig(conf);
        
        e.inicializar();
        int generaciones=0;
        while(generaciones<MAX_GENERACIONES) {
            e.run();
            generaciones++;
        }

    }
}