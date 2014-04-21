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
		Poblacion = 80;
		Crossover = 0.9;
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
			if(esMejorCase(Generacion[i], mejorCaso)) mejorCaso = Generacion[i];
			//if(caseValue(Generacion[i]) > caseValue(mejorCaso)) mejorCaso = Generacion[i];
			//else if(caseValue(Generacion[i]) == caseValue(mejorCaso) && caseSecondValue(Generacion[i]) > caseSecondValue(mejorCaso)) mejorCaso = Generacion[i];
		}
		
		for(int i = 1; i < Generaciones; i++){//i parte en 1 porque ya hubo una generacion
			HashMap<Integer, Integer>[] Hijos = new HashMap[Poblacion];
			
			int hijosCrossover = (int)(Crossover * Poblacion);
			
			//Hijos generados por crossover
			int agregados = 0;
			while(agregados < hijosCrossover){
				//Estrategia de torneo para elegir los dos padres:
				int postulante1 = rng.nextInt(Poblacion);
				int postulante2 = rng.nextInt(Poblacion);
				while(postulante2 == postulante1) postulante2 = rng.nextInt(Poblacion);
				
				int padre1 = postulante1;
				if(esMejorCase(Generacion[postulante2], Generacion[postulante1])) padre1 = postulante2;
				
				int padre2 = padre1;
				while(padre2 == padre1){
					postulante1 = rng.nextInt(Poblacion);
					postulante2 = rng.nextInt(Poblacion);
					while(postulante2 == postulante1) postulante2 = rng.nextInt(Poblacion);
					padre2 = postulante1;
					if(esMejorCase(Generacion[postulante2], Generacion[postulante1])) padre2 = postulante2;
				}
				
				HashMap<Integer, Integer> Hijo1 = new HashMap<Integer, Integer>(Postulantes.size());
				HashMap<Integer, Integer> Hijo2 = new HashMap<Integer, Integer>(Postulantes.size());
				
				int indiceCambio = 1 + rng.nextInt(Postulantes.size() - 1);
				
				for(int k : Postulantes.keySet()){
					if(k <= indiceCambio){
						Hijo1.put(k, Generacion[padre1].get(k));
						Hijo2.put(k, Generacion[padre2].get(k));
					}
					else{
						Hijo1.put(k, Generacion[padre2].get(k));
						Hijo2.put(k, Generacion[padre1].get(k));
					}
				}
				
				Hijos[agregados] = fixCase(Hijo1);
				agregados++;
				Hijos[agregados] = fixCase(Hijo2);
				agregados++;
			}
			//Hijos generados por mutacion
			while(agregados < Poblacion){
				int postulante1 = rng.nextInt(Poblacion);
				int postulante2 = rng.nextInt(Poblacion);
				while(postulante2 == postulante1) postulante2 = rng.nextInt(Poblacion);
				
				int padre = postulante1;
				if(esMejorCase(Generacion[postulante2], Generacion[postulante1])) padre = postulante2;
				
				HashMap<Integer, Integer> Mutante = new HashMap<Integer, Integer>(Postulantes.size());
				
				for(int k : Generacion[padre].keySet()){
					if(rng.nextDouble() < Mutacion){//Entonces se muta por un valor al azar
						Mutante.put(k, rng.nextInt(3));
					}
					else{//Se pone el valor original
						Mutante.put(k, Generacion[padre].get(k));
					}
				}
				
				//Se agrega el mutante a los hijos
				Hijos[agregados] = fixCase(Mutante);
				agregados++;
			}
			
			//Se actualiza la generacion
			Generacion = Hijos;
			
			//Se actualiza el mejor caso
			for(int j = 0; j < Poblacion; j++){
				if(esMejorCase(Generacion[j], mejorCaso)) mejorCaso = Generacion[j];
			}
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
			//if(Postulantes.get(k).getPromedio() >= 5.0){
				Caso.put(k, rng.nextInt(3));
			//}
		}
		return Caso;
	}
	
	static HashMap<Integer, Integer> fixCase(HashMap<Integer,Integer> Case){
		for(int k : Postulantes.keySet()){
			if(Case.containsKey(k)){
				//Le quitamos la beca a quienes tengan promedio bajo 5 y que reciban ingresos sobre 1.600.000
				if(Postulantes.get(k).getPromedio() < 5.0 || Postulantes.get(k).getIngreso() > 1600000) Case.put(k, 0);
				//Quienes hallan recibido beca completa y tengan ingresos sobre 1.000.000 se cambian a beca parcial
				if(Case.get(k) == 2 && Postulantes.get(k).getIngreso() > 1000000)Case.put(k,1);
			}
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

	static boolean esMejorCase(HashMap<Integer, Integer> Case1, HashMap<Integer, Integer> Case2){
		if(caseValue(Case1) > caseValue(Case2)) return true;
		if(caseSecondValue(Case1) > caseSecondValue(Case2)) return true;
		
		//Si los dos tienen la misma cantidad de becas y de becas completas entonces es mejor en el que las mejores becas las tengan los mejores promedios
		double gradesValue1 = 0.0;
		double gradesValue2 = 0.0;
		
		for(int k : Case1.keySet()){
			//Las becas medias suman la mitad del ptomedio, las completas el puntaje entero y los sin nada.
			gradesValue1 += (Postulantes.get(k).getPromedio() * Case1.get(k))/2;
			gradesValue2 += (Postulantes.get(k).getPromedio() * Case2.get(k))/2;
		}
		
		return gradesValue1 > gradesValue2;
	}

}
