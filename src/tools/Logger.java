package tools;

public abstract class Logger {
	public static int level=6;
	//0-> menos restrictivo, 10->Mas restrictivo
	//0->Muestra estado y acciones de cada criatura en cada turno
	//1->Muestra Resultado final del combate
	//2->
	
	
	
	//8->
	//9->Muestra poblaciones en cada generacion
	//10->Solo muestra resultado final
	
	public static void INFO(String mensaje, int nivel) {
		if(nivel>=level) {
			System.out.println(mensaje);
		}
	}
}
