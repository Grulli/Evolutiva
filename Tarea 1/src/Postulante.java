
public class Postulante {
	private int id;
	private String nombre;
	private int ingreso;
	private double promedio;
	private int valor;
	
	public Postulante(int _id, String _nombre, int _ingreso, double _promedio, int _valor){
		this.id = _id;
		this.nombre = _nombre;
		this.ingreso = _ingreso;
		this.promedio = _promedio;
		this.valor = _valor;
	}
	
	public int getId(){return id;}
	public String getNombre(){return nombre;}
	public int getIngreso(){return ingreso;}
	public double getPromedio(){return promedio;}
	public int getValor(){return valor;}
	
	
}
