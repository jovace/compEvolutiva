package evolutivo;

import java.util.List;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;
import java.util.Map.Entry;

import evolutivo.EvolverConfig.TIPO_SELECCION;

import java.util.HashMap;

import model.*;
import tools.Logger;

public class Evolver{
        
    List<Criatura> poblacion;
    List<Criatura> poblacionPrueba;
    EvolverConfig config;   //Ajustas los parametros del algoritmo evolutivo

    Map<Criatura, Integer> victorias = new HashMap<Criatura, Integer>();
    Map<Criatura, Integer> diffHP = new HashMap<Criatura, Integer>();
    Map<Criatura, Integer> dmgDone = new HashMap<Criatura, Integer>();
    Map<Criatura, Intervalo> tablaProbs = new HashMap<Criatura, Intervalo>();
    
    public Evolver(){}

    public void setConfig(EvolverConfig config){
        this.config=config;
    }

    public void inicializar(){
        //Genera poblacion inicial. Sobrecarga con poblacion especifica.
        int tamanoPoblacion = this.config.getTamanoPoblacion();
        this.poblacion=new ArrayList<Criatura>();
        this.poblacionPrueba = new ArrayList<Criatura>();

        for(int i=0;i<tamanoPoblacion;i++){
            float[] adn = new float[50];

            for(int j=0;j<30;j++){
                int valor = (int)(Math.random()*5);

                adn[j]=valor;
            }

            for(int j=30;j<50;j++){
                adn[j]=(float)(Math.random()-0.5)*20;
            }
            Criatura c = new Criatura(adn);
            this.poblacion.add(c);
            Logger.INFO(Arrays.toString(c.getAdn()), 7);
        }
        
        for(int i=0;i<tamanoPoblacion;i++){
            float[] adn = new float[50];

            for(int j=0;j<30;j++){
                int valor = (int)(Math.random()*5);

                adn[j]=valor;
            }

            for(int j=30;j<50;j++){
                adn[j]=(float)(Math.random()-0.5)*20;
            }
            Criatura c = new Criatura(adn);
            this.poblacionPrueba.add(c);
            Logger.INFO(Arrays.toString(c.getAdn()), 7);
        }
    }

    public void run(){
        //Comienza la ejecucion del algoritmo
        int generaciones=0;
        while(generaciones<500){//Condicion de parada
        	Logger.INFO("Empezando ronda "+generaciones, 4);
        	
            //Simula los emparejamientos de todos los individuos con todos y obtiene puntuaciones
            simularPartidos();

            printResultadosPartidos();

            //Dependiendo del record de victorias/derrotas y otras clasificaciones, 
            //otorga probabilidades de seleccion
            Logger.INFO("Entrando a fase ranking",4);
            realizarRanking();

            Logger.INFO("Entrando a fase seleccion",4); 
            
            
            printResumenPoblacion();
            //Selecciona individuos para la siguiente generacion dependiendo de probabilidades del ranking          
            seleccion();
            
            
            //cruce();    //Quiza dentro de seleccion, o a la que haces la seleccion
            //mutacion(); //Idem

            //Rinse and repeat
            generaciones++;
        }
    }

    private void simularPartidos(){
        victorias.clear();
        diffHP.clear();
        dmgDone.clear();

        for(Criatura c1 : this.poblacion){
            for(Criatura c2 : this.poblacionPrueba){
                if(c1.equals(c2))continue;
                
                Logger.INFO("PARTIDO: "+c1.getNombre()+" contra "+c2.getNombre(), 5);

                //[victoria, hpA, hpB, dmgA, dmgB]
                int[] resultado = (new CampoBatalla()).combate(c1,c2);
                
                switch(resultado[0]){
                    case -1:
                        sumar(victorias, c1, 1);
                        break;
                    case 1:
                        sumar(victorias, c1, 0);
                        break;
                }

                sumar(diffHP, c1, resultado[1]-resultado[2]);

                sumar(dmgDone, c1, resultado[3]);
            }
        }
    }

    private void realizarRanking(){
        tablaProbs.clear();

        Map<Criatura, Double> puntuacion = new HashMap<Criatura, Double>();
        double total=0.0;
        for(Criatura c : this.poblacion){
        	Double valor=evaluar(c);
        	if(valor<0)valor=0.0;
            puntuacion.put(c, valor);
            total+=valor;
        }


		if (total != 0 && this.config.tipoSeleccion!=TIPO_SELECCION.TORNEO) {
			double acumulado = 0;
			for (Criatura c : this.poblacion) {
				tablaProbs.put(c, new Intervalo(acumulado, acumulado + puntuacion.get(c) / total));
				acumulado += puntuacion.get(c) / total;
			}
		} else {

			double acumulado = 0;
			for (Criatura c : this.poblacion) {
				tablaProbs.put(c,
						new Intervalo(acumulado, acumulado + ((double) 1 / this.config.getTamanoPoblacion())));
				acumulado += ((double) 1 / this.config.getTamanoPoblacion());
			}
		}
    }

    private void seleccion(){
        List<Criatura> generacionPrevia = this.poblacion;

        List<Criatura> nuevaGeneracion = new ArrayList<Criatura>();
        
        Logger.INFO("Imprimiendo tabla probs", 8);
        for(Entry<Criatura, Intervalo> e : this.tablaProbs.entrySet()) {
        	Logger.INFO("Entrada "+e.getKey()+"->("+e.getValue().inf+","+e.getValue().sup+")", 8);
        }
        
		if (this.config.tipoSeleccion == TIPO_SELECCION.RULETA) {
			for (int i = 0; i < this.config.getTamanoPoblacion(); i++) {
				Criatura a = null;
				Criatura b = null;

				double rnd = Math.random();
				for (Criatura c : this.poblacion) {
					if (tablaProbs.get(c).belongsTo(rnd)) {
						a = c;
						break;
					}
				}

				rnd = Math.random();
				for (Criatura c : this.poblacion) {
					if (tablaProbs.get(c).belongsTo(rnd)) {
						b = c;
						break;
					}
				}

				if (a != null && b != null) {
					Criatura res = cruce(a, b);
					nuevaGeneracion.add(res);
					Logger.INFO("Seleccionamos criatura ", 8);
					Logger.INFO(res.getNombre(), 8);
					Logger.INFO("A partir de ", 8);
					Logger.INFO("A: " + a.getNombre(), 8);
					Logger.INFO("B: " + b.getNombre(), 8);
				} else {
					i--;
				}
			}
		} else if (this.config.tipoSeleccion == TIPO_SELECCION.TORNEO) {
			for (int i = 0; i < this.config.getTamanoPoblacion(); i++) {
				Criatura a = null;
				Criatura b = null;
				Criatura c = null;

				double rnd = Math.random();
				for (Criatura d : this.poblacion) {
					if (tablaProbs.get(d).belongsTo(rnd)) {
						a = d;
						break;
					}
				}

				rnd = Math.random();
				for (Criatura d : this.poblacion) {
					if (tablaProbs.get(d).belongsTo(rnd)) {
						b = d;
						break;
					}
				}
				
				if (a != null && b != null) {
					if(this.victorias.get(a)==null && this.victorias.get(a)==this.victorias.get(b)) {
						if(this.diffHP.get(a)>this.diffHP.get(b)) {
							c=a;
						}else if(this.diffHP.get(b)>this.diffHP.get(a)) {
							c=b;
						}else {
							if(this.dmgDone.get(a)>=this.dmgDone.get(b)) {
								c=a;
							}else if(this.dmgDone.get(b)>this.dmgDone.get(a)) {
								c=b;
							}
						}
					}else if(this.victorias.get(a) == null) {
						c=b;
					}else if(this.victorias.get(b) == null) {
						c=a;
					}else {
						if(this.victorias.get(a)>this.victorias.get(b)) {
							c=a;
						}else if(this.victorias.get(b)>this.victorias.get(a)) {
							c=b;
						}else {
							if(this.dmgDone.get(a)>=this.dmgDone.get(b)) {
								c=a;
							}else if(this.dmgDone.get(b)>this.dmgDone.get(a)) {
								c=b;
							}
						}
					}
					nuevaGeneracion.add(new Criatura(c.getAdn()));
					Logger.INFO("Seleccionamos criatura ", 8);
					Logger.INFO(c.getNombre(), 8);
					Logger.INFO("A partir de ", 8);
					Logger.INFO("A: " + a.getNombre(), 8);
					Logger.INFO("B: " + b.getNombre(), 8);
				} else {
					i--;
				}
				
			}
		}

        this.poblacion = nuevaGeneracion;
    }

    private void sumar(Map<Criatura, Integer> map, Criatura c, Integer valor){
        Integer val = map.get(c);
        if(val!=null)map.put(c, map.get(c)+valor);
        else map.put(c, valor);
    	
    }

    private Criatura cruce(Criatura a, Criatura b){
        double ratioMutacion=this.config.getRatioMutacion();
        double tasaMutacion=this.config.getTasaMutacion();
        float[] adnA=a.getAdn();
        float[] adnB=b.getAdn();
        float[] adnR=new float[50];

        for(int i=0;i<30;i++){
            double rng = Math.random();
            if(rng<0.5){
                adnR[i]=adnA[i];
            }else{
                adnR[i]=adnB[i];
            }
        }

        for(int i=30;i<50;i++){
            double rng=Math.random();
            adnR[i]=(float) (adnA[i]*rng + adnB[i]*(1-rng));

            double muta=Math.random();
            if(muta<tasaMutacion){
                muta=Math.random()-0.5;

                adnR[i]*=(ratioMutacion*muta);
            }
        }

        return new Criatura(adnR);
    }
    
    public Double evaluar(Criatura c) {
    	Integer victorias = this.victorias.get(c);
    	if(victorias==null)victorias=0;
    	return (double) 10*victorias+2*dmgDone.get(c)+diffHP.get(c);
    }

    private void printResultadosPartidos(){
    	Logger.INFO("{", 7);
        for(Criatura c : this.poblacion){
        	Logger.INFO("[ "+Arrays.toString(Arrays.copyOfRange(c.getAdn(), 0, 30))+", "+this.victorias.get(c)+", "+this.dmgDone.get(c)+"],", 7);
        }
        Logger.INFO("}", 7);
    }
    
    
    private void printResumenPoblacion() {
    	Logger.INFO("----------------------", 9);
    	Logger.INFO("Resumen de poblacion", 9);
    	Logger.INFO("----------------------", 9);
    	for(Criatura c : this.poblacion) {
    		String adnHash="";
    		try {
				MessageDigest md = MessageDigest.getInstance("MD5");
				md.update(Arrays.toString(c.getAdn()).getBytes());
				byte[] digest=md.digest();
				StringBuffer sb = new StringBuffer();
				for(byte b : digest) {
					sb.append(String.format("%02x", b & 0xff));
				}
				adnHash=sb.toString();
			} catch (NoSuchAlgorithmException e) {
				e.printStackTrace();
			}
    		
    		Logger.INFO("Criatura: "+c.getNombre()+" adnHash: "+adnHash+" victorias:"+this.victorias.get(c), 9);
    	}
    	
    	Logger.INFO("----------------------", 9);
    }


    private class Intervalo{
        double inf;
        double sup;

        public Intervalo(double i, double s){
            this.inf=i;
            this.sup=s;
        }

        public boolean belongsTo(double val){
            return (this.inf<val && val<=this.sup);
        }
    }
}

