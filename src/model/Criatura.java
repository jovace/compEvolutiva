package model;

import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;
import java.util.HashMap;

import model.*;
import evolutivo.*;

public class Criatura{
    private final int NUM_INDICADORES=5;
    //Criatura que se origina a partir de un "ADN", y de ahi se sacan los atributos.
    //De hecho, no tiene setters para atributos, sino que solo se modifican en el constructor

    //Asignar un nombre para la criatura que refleje de alguna manera sus antecesores
    private Adn adn;
    private String nombre;


    public Criatura(int[] adn){
        this.adn = new Adn(adn);
        this.nombre=Arrays.toString(adn).hashCode()+"";
    }

    public String printGenoma() {
        String genoma=printCFisicas();
        genoma+="Pesos={ \n";
        genoma+="HPDiff, ENDiff, ATDiff, ARDiff \n";
        genoma+="Atacar: "+Arrays.toString(this.adn.getPesosAtacar()) + " \n";
        genoma+="Bloquear: "+Arrays.toString(this.adn.getPesosBloquear()) + " \n";
        genoma+="Esquivar: "+Arrays.toString(this.adn.getPesosEsquivar()) + " \n";
        genoma+="Pasar: "+Arrays.toString(this.adn.getPesosPasar()) + " \n";
        genoma+="}";

        return genoma;
    }

    public String printCFisicas(){
        String genoma="CF={";
        genoma+="AT:"+this.getAtaque()+", ";
        genoma+="AR:"+this.getArmadura()+", ";
        genoma+="AG:"+this.getAgilidad()+", ";
        genoma+="EN:"+this.getEN()+", ";
        genoma+="HP:"+this.getHP()+"} \n";
        return genoma;
    }

    public String getNombre(){
        return this.nombre;
    }

    public int getAtaque(){
        return this.adn.getAT();
    }

    public int getArmadura(){
        return this.adn.getAR();
    }

    public int getHP(){
        return this.adn.getHP();
    }

    public int getEN(){
        return this.adn.getEN();
    }

    public int getAgilidad(){
        return this.adn.getAG();
    }

    public int[] getAdn(){
        return this.adn.getAdn();
    }



    public String realizarAccion(int[] indicadores){
        if(indicadores.length!=NUM_INDICADORES){
            //error
            return "PASAR";
        }

        float valMax=Float.NEGATIVE_INFINITY;
        String accMax="";

        int[] pesos;


        pesos=this.adn.getPesosAtacar();
        int valor=0;
        for(int i=0;i<NUM_INDICADORES-1;i++){
            valor+=indicadores[i]*pesos[i];
        }
        valor*=pesos[NUM_INDICADORES-1];
        if(valor>valMax){
            accMax="ATACAR";
            valMax=valor;
        }


        pesos=this.adn.getPesosBloquear();
        valor=0;
        for(int i=0;i<NUM_INDICADORES-1;i++){
            valor+=indicadores[i]*pesos[i];
        }
        valor*=pesos[NUM_INDICADORES-1];
        if(valor>valMax){
            accMax="BLOQUEAR";
            valMax=valor;
        }


        pesos=this.adn.getPesosEsquivar();
        valor=0;
        for(int i=0;i<NUM_INDICADORES-1;i++){
            valor+=indicadores[i]*pesos[i];
        }
        valor*=pesos[NUM_INDICADORES-1];
        if(valor>valMax){
            accMax="ESQUIVAR";
            valMax=valor;
        }


        pesos=this.adn.getPesosPasar();
        valor=0;
        for(int i=0;i<NUM_INDICADORES-1;i++){
            valor+=indicadores[i]*pesos[i];
        }
        valor*=pesos[NUM_INDICADORES-1];
        if(valor>valMax){
            accMax="PASAR";
            valMax=valor;
        }


        return accMax;
    }

}