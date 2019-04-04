package evolutivo;

public class EvolverConfig{
    int tamanoPoblacion;
    double ratioMutacion;
    double tasaMutacion;

    public EvolverConfig(int tamanoPoblacion){
        this.tamanoPoblacion=tamanoPoblacion;
        this.ratioMutacion=1.5;
        this.tasaMutacion=0.1;
    }

    public int getTamanoPoblacion(){
        return this.tamanoPoblacion;
    }

    public void setTamanoPoblacion(int tam){
        this.tamanoPoblacion=tam;
    }

    public double getRatioMutacion(){
        return this.ratioMutacion;
    }

    public double getTasaMutacion(){
        return this.tasaMutacion;
    }

    /*
    Ratio mutacion
    Tipo mutacion
    Tipo cruce
    Elitismo
    Tipo seleccion
    Parametros de cruce
    etc
    */
}