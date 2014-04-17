import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;


public class Main {

	public static void main(String[] args) {
		File file = new File("postulantes.txt");
		
		Map<Integer, Postulante> Postulantes = new HashMap<Integer, Postulante>();
		
		Scanner sc;
		try{
			sc = new Scanner(file);
		}
		catch(Exception e){System.out.println("Archivo de postulantes no encontrado");}

	}

}
