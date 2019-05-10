package evolutivo;

public class EvolverConfig{
	public static enum TIPO_SELECCION {
		RULETA,
		TORNEO
	}
	
    int tamanoPoblacion;
    double ratioMutacion;
    double tasaMutacion;
    TIPO_SELECCION tipoSeleccion;

    public EvolverConfig(int tamanoPoblacion){
        this.tamanoPoblacion=tamanoPoblacion;
        this.ratioMutacion=4;
        this.tasaMutacion=0.05;
        this.tipoSeleccion=TIPO_SELECCION.RULETA;
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