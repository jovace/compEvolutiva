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
    private final int EN_ESQUIVAR = 2;
    private final int EN_BLOQUEAR = 3;
    private final int EN_ATACAR=1;

    private final double PESO_ARMADURA = 0.3;


    //Simula el  enfrentamiento entre dos criaturas

    Criatura a,b;

    //En estos mapas guardaremos informacion de las dos criaturas, como vida, dano inflingido o energia
    Map<String, Integer> estadoA;
    Map<String, Integer> estadoB;

    public CampoBatalla(){}

    public int[] combate(Criatura a, Criatura b){
        //Inicializamos los mapas
        estadoA = new HashMap<String, Integer>();
        estadoB = new HashMap<String, Integer>();
        this.a=a;
        this.b=b;

        //Asignamos a cada criatura su vida inicial y su energia inicial, asi como ponemos dano inflingido a cero
        this.estadoA.put("HP", Integer.valueOf(a.getHP()));
        this.estadoB.put("HP", Integer.valueOf(b.getHP()));
        this.estadoA.put("EN", Integer.valueOf(a.getEN()));
        this.estadoB.put("EN", Integer.valueOf(b.getEN()));
        this.estadoA.put("dmg_done", 0);
        this.estadoB.put("dmg_done", 0);

        //Mientras que ambos esten con vida, y no hayan pasado mas de 50 rondas
        int cRonda=0;
        while(estadoA.get("HP")>0 && estadoB.get("HP")>0 && cRonda<50){
        	Logger.INFO("Ronda "+cRonda+". "
        			+ "{"+this.estadoA.get("HP")+","+this.estadoA.get("EN")+"} - "
        			+ "{"+this.estadoB.get("HP")+","+this.estadoB.get("EN")+"}", 0);

        	//Funcion que gestiona las acciones del turno
            turno();
            cRonda++;
        }

        //Vamos a devolver el resultado del combate como un array de ints que son lo siguiente:
        //[victoria, hpA, hpB, dmgA, dmgB], donde victoria es -1 si A gana, 0 si empatan o 1 si B gana
        //Ahora distinguimos cada caso en funcion de la vida que les quede a cada uno
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
        //Estos indicadores son los que utilizaran las criaturas para tomar la decision de la accion en este turno
        int[] indicadores=new int[NUM_INDICADORES];
        indicadores[0]=this.estadoA.get("HP")-this.estadoB.get("HP");
        indicadores[1]=this.estadoA.get("EN")-this.estadoB.get("EN");
        indicadores[2]=this.a.getAtaque()-this.b.getAtaque();
        indicadores[3]=this.a.getArmadura()-this.b.getArmadura();
        indicadores[4]=0;

        //Le pedimos a A que tome una decision
        String accionA=this.a.realizarAccion(indicadores);

        //Invertimos los indicadores para B
        for(int i=0;i<NUM_INDICADORES-1;i++){
            indicadores[i]*=-1;
        }

        //Le pedimos a B que tome una decision
        String accionB=this.b.realizarAccion(indicadores);

        
        //Comprobamos que las criaturas tienen la energia necesaria para llevar a cabo la accion. Si no es asi, PASAR
        if(accionA.equals("ESQUIVAR") && this.estadoA.get("EN")<EN_ESQUIVAR) accionA="PASAR";
        else if(accionA.equals("BLOQUEAR") && this.estadoA.get("EN")<EN_BLOQUEAR) accionA="PASAR";
        else if(accionA.equals("ATACAR") && this.estadoA.get("EN")<EN_ATACAR) accionA="PASAR";
        
        if(accionB.equals("ESQUIVAR") && this.estadoB.get("EN")<EN_ESQUIVAR) accionB="PASAR";
        else if(accionB.equals("BLOQUEAR") && this.estadoB.get("EN")<EN_BLOQUEAR) accionB="PASAR";
        else if(accionB.equals("ATACAR") && this.estadoB.get("EN")<EN_ATACAR) accionB="PASAR";

        Logger.INFO("A->"+accionA+"; B->"+accionB, 0);


        //Dependiendo de la accion que tenga cada uno, actualizamos la vida de uno u otro
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