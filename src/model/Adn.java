package model;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;

import model.*;
import evolutivo.*;

public class Adn{
    private final int NUM_ACCIONES=4;
    private final int NUM_INDICADORES=5;

    private float[] adn; //30 fisico + 5x4 comportamiento
    private int HP;
    private int AT;
    private int AR;
    private int EN;
    private int AG;
    private float[] pesosAtacar;
    private float[] pesosBloquear;
    private float[] pesosEsquivar;
    private float[] pesosPasar;


    public Adn(float[] adn){
        this.HP=0;
        this.AT=0;
        this.AR=0;
        this.EN=0;
        this.AG=0;

        if(adn.length!=50){
            //error
        }

        this.adn=adn;
        for(int i=0;i<30;i++){
            int valor = (int)adn[i];
            switch(valor){
                case 0:
                    this.HP++;
                    break;
                case 1:
                    this.AT++;
                    break;
                case 2:
                    this.AR++;
                    break;
                case 3:
                    this.EN++;
                    break;
                case 4:
                    this.AG++;
                    break;
                default:
                    break;
            }
        }

        for(int i=0;i<NUM_ACCIONES;i++){
            float[] pesos = new float[5];
            for(int j=0;j<NUM_INDICADORES;j++){
                pesos[j]=adn[30+NUM_ACCIONES*i+j];
            }
            switch(i){
                case 0:
                    this.pesosAtacar=pesos;
                    break;
                case 1:
                    this.pesosBloquear=pesos;
                    break;
                case 2:
                    this.pesosEsquivar=pesos;
                    break;
                case 3:
                    this.pesosPasar=pesos;
                    break;
            }
        }
    }

    public int getHP(){
        return this.HP;
    }

    public int getAT(){
        return this.AT;
    }

    public int getAR(){
        return this.AR;
    }

    public int getEN(){
        return this.EN;
    }

    public int getAG(){
        return this.AG;
    }

    public float[] getPesosAtacar(){
        return this.pesosAtacar;
    }

    public float[] getPesosBloquear(){
        return this.pesosBloquear;
    }

    public float[] getPesosEsquivar(){
        return this.pesosEsquivar;
    }

    public float[] getPesosPasar(){
        return this.pesosPasar;
    }

    public float[] getAdn(){
        return this.adn;
    }

}