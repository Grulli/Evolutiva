import java.io.File;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Random;
import java.util.Scanner;


public class Main {

	static int Generaciones;
	static int Poblacion;
	static double Crossover;
	static double Mutacion;
	static int PresupuestoMax;
	static Map<Integer, Postulante> Postulantes;
	static Random rng;
	
	public static void main(String[] args) {
		File file_postulantes = new File("postulantes.txt");
		
		//Valores por defecto
		Generaciones = 10000;
		Poblacion = 40;
		Crossover = 0.5;
		Mutacion = 0.1;
		PresupuestoMax = 600000000;
		
		Postulantes = new HashMap<Integer, Postulante>();
		
		rng = new Random();
		
		Scanner sc;
		try{
			sc = new Scanner(file_postulantes);
			int indice = 1;
			while(sc.hasNext()){
				String line = sc.nextLine();
				String[] datos = line.split(";");
				String nombre = datos[0];
				int ingreso = Integer.parseInt(datos[1].trim());
				double promedio = Double.parseDouble(datos[2].replace(',','.'));
				int valor = Integer.parseInt(datos[3]);
				
				Postulante p = new Postulante(indice, nombre, ingreso, promedio, valor);
				Postulantes.put(indice, p);
				indice++;
			}
		}
		catch(Exception e){System.out.println("Archivo de postulantes no encontrado");}
	
		Map<String, String> Opciones = new HashMap<String, String>();
		if(args.length > 0){
			File file_opciones = new File(args[0]);
			try{
				sc = new Scanner(file_opciones);
				while(sc.hasNext()){
					String line = sc.nextLine();
					String[] datos = line.split("=");
					Opciones.put(datos[0].trim(), datos[1].trim());
				}
			}
			catch(Exception e){System.out.println("Archivo de opciones no encontrado");}
		}
		
		if (Opciones.containsKey("Generaciones")) Generaciones = Integer.parseInt(Opciones.get("Generaciones"));
		if (Opciones.containsKey("Poblacion")) Poblacion = Integer.parseInt(Opciones.get("Poblacion"));
		if (Opciones.containsKey("Crossover")) Crossover = Double.parseDouble(Opciones.get("Crossover").replace(',','.'));
		if (Opciones.containsKey("Mutacion")) Mutacion = Double.parseDouble(Opciones.get("Mutacion").replace(',','.'));
		if (Opciones.containsKey("PresupuestoMax")) PresupuestoMax = Integer.parseInt(Opciones.get("PresupuestoMax").replaceAll(".", ""));
	
		HashMap<Integer, Integer> mejorCaso = null;
		
		HashMap<Integer, Integer>[] Generacion = new HashMap[Poblacion]; 
		for(int i = 0; i < Poblacion; i++){
			Generacion[i] = fixCase(getRandomCase());
			if(caseValue(Generacion[i]) > caseValue(mejorCaso)) mejorCaso = Generacion[i];
			else if(caseValue(Generacion[i]) == caseValue(mejorCaso) && caseSecondValue(Generacion[i]) > caseSecondValue(mejorCaso)) mejorCaso = Generacion[i];
		}
		
		
		System.out.println("ASIGNACION OPTIMA");
		for(int k : mejorCaso.keySet()){
			String linea = "Postulante " + k + ", ";
			if (mejorCaso.get(k) == 0) linea += "Nada";
			else if(mejorCaso.get(k) == 1) linea += "Media";
			else linea += "Total";
			
			System.out.println(linea);
		}
		System.out.println("Valor total asignado: $" + String.format("%,d", getCaseCost(mejorCaso)).replace(',', '.'));
	}
	
	static HashMap<Integer, Integer> getRandomCase(){
		HashMap<Integer, Integer> Caso = new HashMap<Integer, Integer>(Postulantes.size());
		for(int k : Postulantes.keySet()){
			Caso.put(k, rng.nextInt(3));
		}
		return Caso;
	}
	
	static HashMap<Integer, Integer> fixCase(HashMap<Integer,Integer> Case){
		for(int k : Postulantes.keySet()){
			//Le quitamos la beca a quienes tengan promedio bajo 5 y que reciban ingresos sobre 1.600.000
			if(Postulantes.get(k).getPromedio() < 5.0 || Postulantes.get(k).getIngreso() > 1600000) Case.put(k, 0);
			//Quienes hallan recibido beca completa y tengan ingresos sobre 1.000.000 se cambian a beca parcial
			if(Case.get(k) == 2 && Postulantes.get(k).getIngreso() > 1000000)Case.put(k,1);
		}
		//Revisamos que el caso no se pase del costo
		while(getCaseCost(Case) > PresupuestoMax){
			//Busco el mas caro de los becados totales y parciales
			
			boolean FoundCompleto = false;
			int maxCompleto = 1;
			boolean FoundParcial = false;
			int maxParcial = 1;
			
			for(int k : Case.keySet()){
				if(Case.get(k) == 2 && (!FoundCompleto || Postulantes.get(maxCompleto).getValor() < Postulantes.get(k).getValor())){
					FoundCompleto = true;
					maxCompleto = k;
				}
				else if(Case.get(k) == 1 && (!FoundParcial || Postulantes.get(maxParcial).getValor() < Postulantes.get(k).getValor())){
					FoundParcial = true;
					maxParcial = k;
				}
			}
			
			//Al mas caro de beca completa lo bajamos a media
			if (FoundCompleto) Case.put(maxCompleto, 1);
			//Si no hay beca completa entonces al mas caro parcial lo bajamos a nada
			else if(FoundParcial) Case.put(maxParcial, 0);
			//Si nadie tiene beca salimos, solo puede pasar si el presupuesto es negativo
			else break;
			
		}
		return Case;
	}
	
	static int getCaseCost(HashMap<Integer, Integer> Case){
		int costo = 0;
		
		for(int k : Case.keySet()){
			if (Case.get(k) == 2) costo += Postulantes.get(k).getValor();
			else if (Case.get(k) == 1) costo += (Postulantes.get(k).getValor() / 2);
		}
		
		return costo;
	}

	static int caseValue(HashMap<Integer, Integer> Case){
		if (Case == null) return 0;
		
		int value = 0;
		for(int i : Case.keySet()){
			if(Case.get(i) > 0) value++;
		}
		
		return value;
	}
	
	static int caseSecondValue(HashMap<Integer, Integer> Case){
		if (Case == null) return 0;
		
		int value = 0;
		for(int i : Case.keySet()){
			if(Case.get(i) == 2) value++;
		}
		
		return value;
	}
}
