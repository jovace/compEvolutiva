package model;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;

import model.*;
import tools.Logger;
import evolutivo.*;
import java.lang.Math;
 
public class CampoBatalla{
    private final int NUM_INDICADORES=5;


    //PARAMETROS DE COMBATE
    private final double PESO_ARMADURA = 0.3;


    //Simula el  enfrentamiento entre dos criaturas

    Criatura a,b;

    Map<String, Integer> estadoA;
    Map<String, Integer> estadoB;

    public CampoBatalla(){}

    public int[] combate(Criatura a, Criatura b){
        estadoA = new HashMap<String, Integer>();
        estadoB = new HashMap<String, Integer>();
        this.a=a;
        this.b=b;

        this.estadoA.put("HP", Integer.valueOf(a.getHP()));
        this.estadoB.put("HP", Integer.valueOf(b.getHP()));
        this.estadoA.put("EN", Integer.valueOf(a.getEN()));
        this.estadoB.put("EN", Integer.valueOf(b.getEN()));
        this.estadoA.put("dmg_done", 0);
        this.estadoB.put("dmg_done", 0);

        int cRonda=0;
        while(estadoA.get("HP")>0 && estadoB.get("HP")>0 && cRonda<50){
        	Logger.INFO("Ronda "+cRonda+". "
        			+ "{"+this.estadoA.get("HP")+","+this.estadoA.get("EN")+"} - "
        			+ "{"+this.estadoB.get("HP")+","+this.estadoB.get("EN")+"}", 0);
            turno();
            cRonda++;
        }

        int[] resultado={0,0,0,0,0};
		if (estadoA.get("HP") > 0 && estadoB.get("HP") > 0) {
			Logger.INFO("EMPATE. Criatura A: " + this.estadoA.get("HP") + "HP. Criatura B: "
					+ this.estadoB.get("HP") + "HP.", 2);
			resultado[0] = 0;
		} else if (estadoA.get("HP") <= 0 && estadoB.get("HP") <= 0 && estadoA.get("HP").equals(estadoB.get("HP"))) {
			Logger.INFO("EMPATE. Criatura A: " + this.estadoA.get("HP") + "HP. Criatura B: "
					+ this.estadoB.get("HP") + "HP.", 2);
			resultado[0] = 0;
		} else if (estadoA.get("HP") <= 0 && estadoA.get("HP")<estadoB.get("HP")) {
			Logger.INFO("GANA B. Criatura A: " + this.estadoA.get("HP") + "HP. Criatura B: "
					+ this.estadoB.get("HP") + "HP.", 2);
			resultado[0] = 1;
		} else if (estadoB.get("HP") <= 0 && estadoB.get("HP")<estadoA.get("HP")) {
			Logger.INFO("GANA A. Criatura A: " + this.estadoA.get("HP") + "HP. Criatura B: "
					+ this.estadoB.get("HP") + "HP.", 2);
			resultado[0] = -1;
		}

        resultado[1]=this.estadoA.get("HP");
        resultado[2]=this.estadoB.get("HP");
        resultado[3]=this.estadoA.get("dmg_done");
        resultado[4]=this.estadoA.get("dmg_done");

        return resultado;
    }

    private void turno(){
        //Primero calculamos los indicadores para criatura A. Si positivo, A tiene mas que B.
        int[] indicadores=new int[NUM_INDICADORES];
        indicadores[0]=this.estadoA.get("HP")-this.estadoB.get("HP");
        indicadores[1]=this.estadoA.get("EN")-this.estadoB.get("EN");
        indicadores[2]=this.a.getAtaque()-this.b.getAtaque();
        indicadores[3]=this.a.getArmadura()-this.b.getArmadura();
        indicadores[4]=0;

        String accionA=this.a.realizarAccion(indicadores);

        for(int i=0;i<NUM_INDICADORES-1;i++){
            indicadores[i]*=-1;
        }

        String accionB=this.b.realizarAccion(indicadores);

        
        
        if(accionA.equals("ESQUIVAR") && this.estadoA.get("EN")<2) accionA="PASAR";
        else if(accionA.equals("BLOQUEAR") && this.estadoA.get("EN")<3) accionA="PASAR";
        else if(accionA.equals("ATACAR") && this.estadoA.get("EN")<1) accionA="PASAR";
        
        if(accionB.equals("ESQUIVAR") && this.estadoB.get("EN")<2) accionB="PASAR";
        else if(accionB.equals("BLOQUEAR") && this.estadoB.get("EN")<3) accionB="PASAR";        
        else if(accionB.equals("ATACAR") && this.estadoB.get("EN")<1) accionB="PASAR";

        Logger.INFO("A->"+accionA+"; B->"+accionB, 0);
        
        if(accionA.equals("PASAR")){
            this.estadoA.put("EN",this.estadoA.get("EN")+1);

            if(accionB.equals("PASAR")){                
                this.estadoB.put("EN",this.estadoB.get("EN")+1);
            }else if(accionB.equals("ATACAR")){
                int dano = this.b.getAtaque()-(int)(this.a.getArmadura()*PESO_ARMADURA);
                if(dano<0)dano=0;
                this.estadoA.put("HP",this.estadoA.get("HP")-dano);
                this.estadoB.put("dmg_done",this.estadoB.get("dmg_done")+dano);
                sumar(estadoB, "EN", -1);
            }else if(accionB.equals("BLOQUEAR")){
                sumar(estadoB, "EN", -3);
            }else if(accionB.equals("ESQUIVAR")){
                sumar(estadoB, "EN", -2);
            }
        }else if(accionA.equals("ATACAR")){
            sumar(estadoA, "EN", -1);

            if(accionB.equals("PASAR")){
                int dano = this.a.getAtaque()-(int)(this.b.getArmadura()*PESO_ARMADURA);
                if(dano<0)dano=0;
                sumar(estadoB, "HP", -dano);
                sumar(estadoA, "dmg_done", dano);
                
                this.estadoB.put("EN",this.estadoB.get("EN")+1);
            }else if(accionB.equals("ATACAR")){
            	int dano = this.a.getAtaque()-(int)(this.b.getArmadura()*PESO_ARMADURA);
                if(dano<0)dano=0;
                sumar(estadoB, "HP", -dano);
                sumar(estadoA, "dmg_done", dano);

                dano = this.b.getAtaque()-(int)(this.a.getArmadura()*PESO_ARMADURA);
                if(dano<0)dano=0;
                this.estadoA.put("HP",this.estadoA.get("HP")-dano);
                this.estadoB.put("dmg_done",this.estadoB.get("dmg_done")+dano);

                sumar(estadoB, "EN", -1);
            }else if(accionB.equals("BLOQUEAR")){
                sumar(estadoB, "EN", -3);
            }else if(accionB.equals("ESQUIVAR")){
            	int dano = this.a.getAtaque()-(int)(this.b.getArmadura()*PESO_ARMADURA);

                double probEsquivar = calcProbabilidadEsquivar(this.b.getAgilidad());
                if(Math.random()>probEsquivar){
                    if(dano<0)dano=0;
                    sumar(estadoB, "HP", -dano);
                    sumar(estadoA, "dmg_done", dano);
                }
                sumar(estadoB, "EN", -2);
            }
        }else if(accionA.equals("BLOQUEAR")){
            sumar(estadoA, "EN", -3);

            if(accionB.equals("PASAR")){
                sumar(estadoB, "EN", 1);
            }else if(accionB.equals("ATACAR")){
                sumar(estadoB, "EN", -1);
            }else if(accionB.equals("BLOQUEAR")){
                sumar(estadoB, "EN", -3);
            }else if(accionB.equals("ESQUIVAR")){
                sumar(estadoB, "EN", -2);
            }
        }else if(accionA.equals("ESQUIVAR")){
            sumar(estadoA, "EN", -2);

            if(accionB.equals("PASAR")){
                sumar(estadoB, "EN", 1);
            }else if(accionB.equals("ATACAR")){
                int dano = this.b.getAtaque()-(int)(this.a.getArmadura()*PESO_ARMADURA);

                double probEsquivar = calcProbabilidadEsquivar(this.a.getAgilidad());
                if(Math.random()>probEsquivar){
                    if(dano<0)dano=0;
                    sumar(estadoA, "HP", -dano);
                    sumar(estadoB, "dmg_done", dano);
                }

                sumar(estadoB, "EN", -1);
            }else if(accionB.equals("BLOQUEAR")){
                sumar(estadoB, "EN", -3);
            }else if(accionB.equals("ESQUIVAR")){
                sumar(estadoB, "EN", -2);
            }
        }
    }

    private void sumar(Map<String,Integer> mapa, String clave, int valor){
        mapa.put(clave, mapa.get(clave)+valor);
    }

    private double calcProbabilidadEsquivar(int AG){
        return 0.3+(0.022*AG)+(-0.00085*AG*AG)+(0.000015*(AG^3));
    }
}