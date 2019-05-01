package launcher;

import evolutivo.*;
import model.Criatura;

public class Launcher{


    private static final int MAX_GENERACIONES = 100;
    private static final int NUM_ISLAS = 10;
    private static final int INTERVALO_INTERCAMBIO = 5;
    private static final int NUM_INTERCAMBIOS = 1;

    public static void main(String[] args){
        Evolver[] e = new Evolver[NUM_ISLAS];
        for(int i=0;i<NUM_ISLAS;i++){
            e[i]= new Evolver("P"+i);
        }

        //Crear configuracion
        EvolverConfig conf = new EvolverConfig(20);

        for(int i=0;i<NUM_ISLAS;i++){
            e[i].setConfig(conf);
            e[i].inicializar();
        }
        

        int generaciones=0;
        while(generaciones<MAX_GENERACIONES) {
            for(int i=0;i<NUM_ISLAS;i++) {
                e[i].run();
            }

            if(generaciones%INTERVALO_INTERCAMBIO==0) {
                for(int i=0;i<NUM_INTERCAMBIOS;i++) {
                    int origen = (int) Math.random() * 15;
                    int destino = (int) Math.random() * 15;
                    Criatura a = e[origen].extraerCriaturaRnd();
                    Criatura b = e[destino].extraerCriaturaRnd();
                    e[destino].insertarCriatura(a);
                    e[origen].insertarCriatura(b);
                }
            }
            generaciones++;
        }

    }
}