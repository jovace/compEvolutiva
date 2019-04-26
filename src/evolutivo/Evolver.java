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

    private static final float PESO_SD = 5;
    private static final int NUM_SUSTITUIDOS_POR_RONDA = 3;
    private Poblacion poblacion;
    private Poblacion poblacionPrueba;
    private EvolverConfig config;   //Ajustas los parametros del algoritmo evolutivo

    private Map<Criatura, Integer> victorias = new HashMap<>();
    private Map<Criatura, Integer> diffHP = new HashMap<>();
    private Map<Criatura, Integer> dmgDone = new HashMap<>();
    private Map<Criatura, Intervalo> tablaProbs = new HashMap<>();

    private int generaciones=0;

    
    public Evolver(){}

    public void setConfig(EvolverConfig config){
        this.config=config;
    }

    public void inicializar(){
        //Genera poblacion inicial. Sobrecarga con poblacion especifica.

        //Obtenemos el tamano de poblacion de la configuracion
        int tamanoPoblacion = this.config.getTamanoPoblacion();
        List<Criatura> poblacionL=new ArrayList<>();
        List<Criatura> poblacionPruebaL = new ArrayList<>();

        //Genero poblacion principal (la que se va a evolucionar)
        for(int i=0;i<tamanoPoblacion;i++){
            Criatura c = generarCriaturaAleatoria();
            poblacionL.add(c);
            Logger.INFO(Arrays.toString(c.getAdn()), 7);
        }

        //Genero poblacion de prueba (la que se va a utilizar para combatir)
        /*for(int i=0;i<tamanoPoblacion;i++){
            Criatura c = generarCriaturaAleatoria();
            poblacionPruebaL.add(c);
            Logger.INFO(Arrays.toString(c.getAdn()), 7);
        }*/

        //Cargamos en la clase Poblacion la lista. La clase poblacion permite operaciones teniendo en cuenta
        //el conjunto entero de la poblacion (media, sd, distancia...)
        poblacion=new Poblacion(poblacionL);
        //poblacionPrueba=new Poblacion(poblacionPruebaL);
    }


    //Metodo para generar criatura con ADN aleatorio
    private Criatura generarCriaturaAleatoria(){
        float[] adn = new float[50];

        //Generamos los 30 genes fisicos
        for(int j=0;j<30;j++){
            int valor = (int)(Math.random()*5);

            adn[j]=valor;
        }

        //Generamos el resto de genes de comportamiento
        for(int j=30;j<50;j++){
            adn[j]=(float)(Math.random()-0.5)*20;
        }

        return new Criatura(adn);
    }

    public int run(){
        //Condicion de parada
        Logger.INFO("Empezando ronda "+generaciones, 9);

        //Simula los emparejamientos de todos los individuos con todos y obtiene puntuaciones
        simularPartidos();

        printResultadosPartidos();

        //Dependiendo del record de victorias/derrotas y otras clasificaciones,
        //otorga probabilidades de seleccion
        Logger.INFO("Entrando a fase ranking",4);
        realizarRanking();


        printResumenPoblacion();


        //Selecciona individuos para la siguiente generacion dependiendo de probabilidades del ranking
        Logger.INFO("Entrando a fase seleccion",4);
        seleccion();


        //Rinse and repeat
        generaciones++;
        return generaciones;
    }

    private void simularPartidos(){
        //Utilizamos el mapa de victorias para relacionar cada criatura con su numero de victorias
        //diffHP para relaccionar cada criatura con el ratio dano inflingido/dano recibido
        //dmgDone para relacionar cada criatura con el dano inflingido total
        victorias.clear();
        diffHP.clear();
        dmgDone.clear();

        for(Criatura c1 : this.poblacion.getPoblacion()){
            for(Criatura c2 : this.poblacion.getPoblacion()){
                if(c1.equals(c2))continue;
                
                Logger.INFO("PARTIDO: "+c1.getNombre()+" contra "+c2.getNombre(), 5);

                //El metodo combate nos va a devolver un array con el siguiente contenido:
                // [victoria, hpA, hpB, dmgA, dmgB], donde victoria es -1 si A gana, 0 si empatan o 1 si B gana
                int[] resultado = (new CampoBatalla()).combate(c1,c2);

                //Actualizamos record de victorias
                switch(resultado[0]){
                    case -1:
                        sumar(victorias, c1, 1);
                        break;
                    case 1:
                        sumar(victorias, c1, 0);
                        break;
                }

                //Actualizamos record de diffHP
                sumar(diffHP, c1, resultado[1]-resultado[2]);

                //Actualizamos record de dmgDone
                sumar(dmgDone, c1, resultado[3]);
            }
        }
    }

    private void realizarRanking(){
        //Para realizar el ranking vamos a asignar a los individuos un intervalo contenido en 0-1 de acorde a
        //la proporcion de su puntuacion con respecto a la suma de todas las puntuaciones. Para la seleccion, elegiremos
        //un numero del 0-1 y en el intervalo que caiga, escogemos esa criatura

        tablaProbs.clear();

        Map<Criatura, Double> puntuacion = new HashMap<>();

        //Guardamos en la variable total la suma de todas las puntuaciones
        double total=0.0;
        for(Criatura c : this.poblacion.getPoblacion()){
            //Llamamos a la funcion evaluar, que tiene la formula del calculo de la puntuacion segun las victorias y demas
        	Double valor=evaluar(c);
        	if(valor<0)valor=0.0;
            puntuacion.put(c, valor);
            total+=valor;
        }


		if (total != 0 && this.config.tipoSeleccion!=TIPO_SELECCION.TORNEO) {
		    //Si utilizamos ruleta, asignamos a cada criatura el intervalo proporcional

			double acumulado = 0;
			for (Criatura c : this.poblacion.getPoblacion()) {
				tablaProbs.put(c, new Intervalo(acumulado, acumulado + puntuacion.get(c) / total));
				acumulado += puntuacion.get(c) / total;
			}
		} else {
            //Si utilizamos torneo asignamos a todas las criaturas intervalos iguales
			double acumulado = 0;
			for (Criatura c : this.poblacion.getPoblacion()) {
				tablaProbs.put(c,
						new Intervalo(acumulado, acumulado + ((double) 1 / this.config.getTamanoPoblacion())));
				acumulado += ((double) 1 / this.config.getTamanoPoblacion());
			}
		}
    }

    private void seleccion(){
        List<Criatura> nuevaGeneracion = new ArrayList<>();
        
        Logger.INFO("Imprimiendo tabla probs", 8);
        for(Entry<Criatura, Intervalo> e : this.tablaProbs.entrySet()) {
        	Logger.INFO("Entrada "+e.getKey()+"->("+e.getValue().inf+","+e.getValue().sup+")", 8);
        }
        
		if (this.config.tipoSeleccion == TIPO_SELECCION.RULETA) {

		    //Sustituimos en la poblacion de combate el numero de criaturas indicado. Seleccionamos criatura mediante
            //ruleta, y la sustituimos por la mas cercana en la poblacion de combate.
		    /*for(int i=0;i<NUM_SUSTITUIDOS_POR_RONDA;i++) {
                double rnd = Math.random();
                for (Criatura c : this.poblacion.getPoblacion()) {
                    if (tablaProbs.get(c).belongsTo(rnd)) {
                        this.poblacionPrueba.sustituirCercano(c);
                        break;
                    }
                }
            }*/

		    //Construimos la siguiente generacion
			for (int i = 0; i < this.config.getTamanoPoblacion(); i++) {
				Criatura a = null;
				Criatura b = null;

				//Seleccionamos progenitor1
				double rnd = Math.random();
				for (Criatura c : this.poblacion.getPoblacion()) {
					if (tablaProbs.get(c).belongsTo(rnd)) {
						a = c;
						break;
					}
				}

				//seleccionamos progenitor2
				rnd = Math.random();
				for (Criatura c : this.poblacion.getPoblacion()) {
					if (tablaProbs.get(c).belongsTo(rnd)) {
						b = c;
						break;
					}
				}

				//Dejo el if con el else por si por algun motivo uno de los progenitores no ha sido correctamente
                //seleccionado y se ha quedado a null
				if (a != null && b != null) {
				    //Las cruzamos
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
		} else if (this.config.tipoSeleccion == TIPO_SELECCION.TORNEO) { //En principio solo utilizaremos ruleta.
			for (int i = 0; i < this.config.getTamanoPoblacion(); i++) {
				Criatura a = null;
				Criatura b = null;
				Criatura c = null;

				double rnd = Math.random();
				for (Criatura d : this.poblacion.getPoblacion()) {
					if (tablaProbs.get(d).belongsTo(rnd)) {
						a = d;
						break;
					}
				}

				rnd = Math.random();
				for (Criatura d : this.poblacion.getPoblacion()) {
					if (tablaProbs.get(d).belongsTo(rnd)) {
						b = d;
						break;
					}
				}


				//Metodo muy largo y tedioso para ver que criatura tiene mejor puntuacion.
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

        this.poblacion.setPoblacion(nuevaGeneracion);
    }

    private void sumar(Map<Criatura, Integer> map, Criatura c, Integer valor){
        //metodo de apoyo que si existe el valor en la coleccion lo suma en esa cantidad y si no lo pone en la coleccion
        //con ese valor
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

        //Para los genes fisicos elegimos o bien de la madre o bien del padre, al ser valores discretos
        for(int i=0;i<30;i++){
            double rng = Math.random();
            if(rng<0.5){
                adnR[i]=adnA[i];
            }else{
                adnR[i]=adnB[i];
            }
        }

        //Para los genes de comportamiento cogemos un numero aleatorio en el intervalo 0-1 y le damos el gen al hijo
        //en esa proporcion con respecto a los genes de los progenitores
        for(int i=30;i<50;i++){
            double rng=Math.random();
            adnR[i]=(float) (adnA[i]*rng + adnB[i]*(1-rng));

            //Si este valor aleatorio es menor que la probabilidad de mutacion, el gen muta
            double muta=Math.random();
            if(muta<tasaMutacion){
                //El gen muta en una proporcion (-0.5<->+0.5)*ratio
                muta=Math.random()-0.5;

                adnR[i]*=(ratioMutacion*muta);
            }

            //Ademas, si la mutacion salta a valores muy altos, la restringimos dentro de limites.
            if(Math.abs(adnR[i])>10)
                if(adnR[i]>0){adnR[i]=10;}else{adnR[i]=-10;}
        }

        return new Criatura(adnR);
    }
    
    private Double evaluar(Criatura c) {
    	Integer victorias = this.victorias.get(c);
    	if(victorias==null)victorias=0;
    	//Utilizamos desviacion estandard de la criatura con respecto a la media de la poblacion para penalizar a las
        //criaturas que esten cerca de otras mejores pero parecidas (pues tendrán sd baja, y por ende su puntuacion sera peor)
    	return (double) (10*victorias+2*dmgDone.get(c)+diffHP.get(c))*(Math.abs(this.poblacion.calcSD(c))*PESO_SD);
    }

    private void printResultadosPartidos(){
    	Logger.INFO("{", 7);
        for(Criatura c : this.poblacion.getPoblacion()){
        	Logger.INFO("[ "+Arrays.toString(Arrays.copyOfRange(c.getAdn(), 0, 30))+", "+this.victorias.get(c)+", "+this.dmgDone.get(c)+"],", 7);
        }
        Logger.INFO("}", 7);
    }
    
    
    private void printResumenPoblacion() {
    	Logger.INFO("----------------------", 9);
    	Logger.INFO("Resumen de poblacion", 9);
    	Logger.INFO("----------------------", 9);
    	for(Criatura c : this.poblacion.getPoblacion()) {
    		String adnHash="";
    		try {
				MessageDigest md = MessageDigest.getInstance("MD5");
				md.update(Arrays.toString(c.getAdn()).getBytes());
				byte[] digest=md.digest();
				StringBuilder sb = new StringBuilder();
				for(byte b : digest) {
					sb.append(String.format("%02x", b & 0xff));
				}
				adnHash=sb.toString();
			} catch (NoSuchAlgorithmException e) {
				e.printStackTrace();
			}
    		
    		Logger.INFO("Criatura: "+c.getNombre()+" "+c.printCFisicas()+" Victorias:"+this.victorias.get(c) + " SD:"+this.poblacion.calcSD(c), 9);
    	}
    	
    	Logger.INFO("----------------------", 9);
    }


    //Clase de apoyo que representa un intervalo
    private class Intervalo{
        double inf;
        double sup;

        Intervalo(double i, double s){
            this.inf=i;
            this.sup=s;
        }

        boolean belongsTo(double val){
            return (this.inf<val && val<=this.sup);
        }
    }
}

