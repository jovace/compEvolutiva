package launcher;

import evolutivo.*;
import model.Criatura;
import tools.Logger;

import java.util.ArrayList;
import java.util.List;

public class Launcher{


    private static final int MAX_GENERACIONES = 100;
    private static final int NUM_ISLAS = 6;
    private static final int INTERVALO_INTERCAMBIO = 10;
    private static final int NUM_INTERCAMBIOS = 5;

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
            List<Criatura> poblacionPrueba = new ArrayList<Criatura>();
            for(int i=0;i<conf.getTamanoPoblacion();i++){
                int origen = (int) Math.random() * NUM_ISLAS;
                Criatura a = e[origen].getCriaturaRnd();
                poblacionPrueba.add(a);
            }

            for(int i=0;i<NUM_ISLAS;i++) {
                e[i].setPoblacionPrueba(poblacionPrueba);
                e[i].run();
            }

            if(generaciones%INTERVALO_INTERCAMBIO==0) {
                for(int i=0;i<NUM_INTERCAMBIOS;i++) {
                    int origen = (int) Math.random() * NUM_ISLAS;
                    int destino = (int) Math.random() * NUM_ISLAS;
                    Criatura a = e[origen].extraerCriaturaRnd();
                    Criatura b = e[destino].extraerCriaturaRnd();
                    e[destino].insertarCriatura(a);
                    e[origen].insertarCriatura(b);
                }
            }
            generaciones++;
        }

        Logger.INFO("\n \n-----------------------------", 10);
        Logger.INFO("Resumen criaturas", 10);
        Logger.INFO("-----------------------------", 10);
        for(int i=0;i<NUM_ISLAS;i++){
            int j=0;
            for(Criatura c : e[i].getPoblacion()){
                String s="P"+i+";"+"C"+j+";";
                int[] adn = c.getAdn();
                for(int z=0;z<adn.length-1;z++){
                    s+=adn[z]+";";
                }
                s+=adn[adn.length-1];
                Logger.INFO(s,10);
                j++;
            }
        }

    }
}