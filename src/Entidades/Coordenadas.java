package Entidades;

import java.time.LocalDateTime;

public class Coordenadas {
	
	private LocalDateTime hora ;
	private String longitud;
	private String latitud;
	
	public Coordenadas(String latitud, String longitud, LocalDateTime hora){
		
		this.longitud = longitud;
		this.latitud  = latitud;
		this.hora 	  = hora;
		
	}

	public LocalDateTime getHora() {
		return hora;
	}

	public void setHora(LocalDateTime hora) {
		this.hora = hora;
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
