public class Evolver{
        
    List<Criatura> poblacion;
    EvolverConfig config;   //Ajustas los parametros del algoritmo evolutivo
    
    public Evolver(){}

    public setConfig(EvolverConfig config){
        this.config=config;
    }

    public inicializar(){
        //Genera poblacion inicial. Sobrecarga con poblacion especifica.
    }

    public run(){
        //Comienza la ejecucion del algoritmo

        while(true){//Condicion de parada
            //Simula los emparejamientos de todos los individuos con todos
            simularPartidos();

            //Dependiendo del record de victorias/derrotas y otras clasificaciones, 
            //otorga probabilidades de seleccion
            realizarRanking();


            //Selecciona individuos para la siguiente generacion dependiendo de probabilidades del ranking
            seleccion();
            cruce();    //Quiza dentro de seleccion, o a la que haces la seleccion
            mutacion(); //Idem

            //Rinse and repeat
        }
    }

}

