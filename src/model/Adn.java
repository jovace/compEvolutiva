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

    private int[] adn; //30 fisico + 5x4 comportamiento


    private int HP;
    private int AT;
    private int AR;
    private int EN;
    private int AG;
    private int[] pesosAtacar;
    private int[] pesosBloquear;
    private int[] pesosEsquivar;
    private int[] pesosPasar;


    public Adn(int[] adn){
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
            int[] pesos = new int[5];
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

    public int[] getPesosAtacar(){
        return this.pesosAtacar;
    }

    public int[] getPesosBloquear(){
        return this.pesosBloquear;
    }

    public int[] getPesosEsquivar(){
        return this.pesosEsquivar;
    }

    public int[] getPesosPasar(){
        return this.pesosPasar;
    }

    public int[] getAdn(){
        return this.adn;
    }





}