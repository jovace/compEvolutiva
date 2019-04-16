package model;

import java.util.List;

public class Poblacion {
    private List<Criatura> poblacion;

    public Poblacion(){}

    public Poblacion(List<Criatura> poblacion){
        this.poblacion=poblacion;
    }

    public List<Criatura> getPoblacion(){
        return this.poblacion;
    }

    public void setPoblacion(List<Criatura> poblacion){
        this.poblacion=poblacion;
    }

    private float[] media(){
        float[] acumulado=null;
        for (Criatura criatura : poblacion) {
            if (acumulado == null) {
                acumulado = criatura.getAdn();
            } else {
                for (int j = 0; j < acumulado.length; j++) {
                    acumulado[j] += criatura.getAdn()[j];
                }
            }
        }

        for(int i=0;i<acumulado.length;i++){
            acumulado[i]/=poblacion.size();
        }

        return acumulado;
    }

    private float stdDeviation(){
        float[] media= media();
        float acumulado=0;
        for (Criatura criatura : poblacion) {
            float dist=calculateDistance(media, criatura.getAdn());
            acumulado+=dist;
        }
        acumulado/=this.poblacion.size();
        acumulado=(float) Math.sqrt((double) acumulado);
        return acumulado;
    }

    public float calcSD(Criatura criatura){
        float dist=calculateDistance(this.media(), criatura.getAdn());
        dist/=this.stdDeviation();
        return dist;
    }

    private float calculateDistance(float[] a, float[] b){
        float dist=0;
        for(int i=0;i<30;i++){
            float coordDiff=a[i]-b[i];
            dist+= coordDiff*coordDiff;
        }
        dist=(float) Math.sqrt((double) dist);
        return dist;
    }


}
