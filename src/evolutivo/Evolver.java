package evolutivo;

import java.util.List;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;
import java.util.Map.Entry;
import java.util.HashMap;

import model.*;

public class Evolver{
        
    List<Criatura> poblacion;
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
            System.out.println(Arrays.toString(c.getAdn()));
        }
    }

    public void run(){
        //Comienza la ejecucion del algoritmo
        int generaciones=0;
        while(generaciones<50){//Condicion de parada
        	System.out.println("Empezando ronda "+generaciones);
        	
            //Simula los emparejamientos de todos los individuos con todos y obtiene puntuaciones
            simularPartidos();

            printResultadosPartidos();

            //Dependiendo del record de victorias/derrotas y otras clasificaciones, 
            //otorga probabilidades de seleccion
            System.out.println("Entrando a fase ranking");
            realizarRanking();

            System.out.println("Entrando a fase seleccion"); 
            //Selecciona individuos para la siguiente generacion dependiendo de probabilidades del ranking
            seleccion();
            
            printResumenPoblacion();
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
            for(Criatura c2 : this.poblacion){
                if(c1.equals(c2))continue;
                
                System.out.println("PARTIDO: "+c1.getNombre()+" contra "+c2.getNombre());

                //[victoria, hpA, hpB, dmgA, dmgB]
                int[] resultado = (new CampoBatalla()).combate(c1,c2);
                
                switch(resultado[0]){
                    case -1:
                        sumar(victorias, c1, 1);
                        break;
                    case 1:
                        sumar(victorias, c2, 1);
                        break;
                }

                sumar(diffHP, c1, resultado[1]-resultado[2]);
                sumar(diffHP, c2, resultado[2]-resultado[1]);

                sumar(dmgDone, c1, resultado[3]);
                sumar(dmgDone, c2, resultado[4]);
            }
        }
    }

    private void realizarRanking(){
        tablaProbs.clear();

        Map<Criatura, Double> puntuacion = new HashMap<Criatura, Double>();
        double total=0;
        for(Criatura c : this.poblacion){
        	Integer victorias = this.victorias.get(c);
        	if(victorias==null)victorias=0;
        	Double valor=(double) victorias+2*dmgDone.get(c)+diffHP.get(c);
        	if(valor<0)valor=0.0;
            puntuacion.put(c, valor);
            total+=valor;
        }


		if (total != 0) {
			double acumulado = 0;
			for (Criatura c : this.poblacion) {
				tablaProbs.put(c, new Intervalo(acumulado, acumulado + puntuacion.get(c)/total));
				acumulado += puntuacion.get(c)/total;
			}
		}else {
			double acumulado = 0;
			for (Criatura c : this.poblacion) {
				tablaProbs.put(c, new Intervalo(acumulado, acumulado + ((double)1/this.config.getTamanoPoblacion())));
				acumulado += ((double)1/this.config.getTamanoPoblacion());
			}
		}
        //tablaProbs.sortByValue ASC
    }

    private void seleccion(){
        List<Criatura> generacionPrevia = this.poblacion;

        List<Criatura> nuevaGeneracion = new ArrayList<Criatura>();
        
        System.out.println("Imprimiendo tabla probs");
        for(Entry<Criatura, Intervalo> e : this.tablaProbs.entrySet()) {
        	System.out.println("Entrada "+e.getKey()+"->("+e.getValue().inf+","+e.getValue().sup+")");
        }

        for(int i=0;i<this.config.getTamanoPoblacion();i++){
            Criatura a=null;
            Criatura b=null;

            double rnd = Math.random();
            for(Criatura c : this.poblacion){
                if(tablaProbs.get(c).belongsTo(rnd)){
                    a=c;
                    break;
                }
            }

            rnd = Math.random();
            for(Criatura c : this.poblacion){
                if(tablaProbs.get(c).belongsTo(rnd)){
                    b=c;
                    break;
                }
            }

            if(a!= null && b != null){
                Criatura res = cruce(a,b);
                nuevaGeneracion.add(res);
                System.out.println("Seleccionamos criatura ");
                System.out.println(res.getNombre());
                System.out.println("A partir de ");
                System.out.println("A: "+a.getNombre());
                System.out.println("B: "+b.getNombre());
            }else{
                i--;
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

    private void printResultadosPartidos(){
        System.out.print("{");
        for(Criatura c : this.poblacion){
            System.out.print("[ "+this.victorias.get(c)+", "+this.dmgDone.get(c)+"],");
        }
        System.out.println("}");
    }
    
    
    private void printResumenPoblacion() {
    	System.out.println("----------------------");
    	System.out.println("Resumen de poblacion");
    	System.out.println("----------------------");
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
    		
    		System.out.println("Criatura: "+c.getNombre()+" adnHash: "+adnHash);
    	}
    	
    	System.out.println("----------------------");
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

