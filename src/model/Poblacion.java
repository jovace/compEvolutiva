package model;

import tools.Logger;

import java.util.List;
import java.util.Random;

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

    public float[] media(){
        float[] acumulado= new float[]{0, 0, 0, 0, 0};
        for (Criatura criatura : poblacion) {
            acumulado[0]+=criatura.getHP();
            acumulado[1]+=criatura.getAtaque();
            acumulado[2]+=criatura.getArmadura();
            acumulado[3]+=criatura.getEN();
            acumulado[4]+=criatura.getAgilidad();
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
            float[] c =
                    new float[]{criatura.getHP(), criatura.getAtaque(), criatura.getArmadura(),
                            criatura.getEN(), criatura.getAgilidad()};
            float dist=calculateDistance(media, c);
            acumulado+=dist;
        }
        acumulado/=this.poblacion.size();
        acumulado=(float) Math.sqrt((double) acumulado);
        //Logger.INFO("-------------------- \n SD="+acumulado+"\n ------------ \n\n\n",10);
        return acumulado;
    }

    public float calcSD(Criatura criatura){
        float[] media= media();
        float[] c =
                new float[]{criatura.getHP(), criatura.getAtaque(), criatura.getArmadura(),
                        criatura.getEN(), criatura.getAgilidad()};
        float dist=calculateDistance(media, c);
        dist/=this.stdDeviation();
        return dist;
    }

    private float calculateDistance(float[] a, float[] b){
        float dist=0;
        for(int i=0;i<a.length;i++){
            float coordDiff=a[i]-b[i];
            dist+= coordDiff*coordDiff;
        }
        dist=(float) Math.sqrt((double) dist);
        return dist;
    }

    /*private float calculateDistance(Criatura a, Criatura b){
        int diffHP=Math.abs(a.getHP()-b.getHP());
        int diffAT=Math.abs(a.getAtaque()-b.getAtaque());
        int diffAR=Math.abs(a.getArmadura()-b.getArmadura());
        int diffEN=Math.abs(a.getEN()-b.getEN());
        int diffAG=Math.abs(a.getAgilidad()-b.getAgilidad());
        double suma=diffHP^2 + diffAT^2 + diffAR^2 + diffEN^2 + diffAG^2;
        return (float)Math.sqrt(suma);
    }*/

    public void sustituirCercano(Criatura criatura){
        float minDist=Float.MAX_VALUE;
        Criatura minC=null;

        for(Criatura c : this.poblacion){
            float[] cf =
                    new float[]{criatura.getHP(), criatura.getAtaque(), criatura.getArmadura(),
                            criatura.getEN(), criatura.getAgilidad()};
            float[] cf1 =
                    new float[]{c.getHP(), c.getAtaque(), c.getArmadura(),
                            c.getEN(), c.getAgilidad()};
            float d=calculateDistance(cf1, cf);
            if(d<minDist){
                minDist=d;
                minC=c;
            }
        }

        if(minC!=null){
            this.poblacion.remove(minC);
            this.poblacion.add(criatura);
        }
    }


    public Criatura extraerCriaturaRnd() {
        Random rand = new Random();
        Criatura criatura = this.poblacion.get(rand.nextInt(this.poblacion.size()));
        this.poblacion.remove(criatura);
        return criatura;
    }

    public void insertarCriatura(Criatura c) {
        this.poblacion.add(c);
    }
}
