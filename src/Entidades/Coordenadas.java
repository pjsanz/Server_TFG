package Entidades;

public class Coordenadas {
	
	private String fecha ;
	private String longitud;
	private String latitud;
	
	public Coordenadas(String latitud, String longitud, String fecha){
		
		this.longitud = longitud;
		this.latitud  = latitud;
		this.fecha 	  = fecha;
		
	}

	public String getFecha() {
		return fecha;
	}

	public void setFecha(String fecha) {
		this.fecha = fecha;
	}

	public String getLongitud() {
		return longitud;
	}

	public void setLongitud(String longitud) {
		this.longitud = longitud;
	}

	public String getLatitud() {
		return latitud;
	}

	public void setLatitud(String latitud) {
		this.latitud = latitud;
	}
	
}
