package Entidades;


import java.net.Socket;
import java.nio.charset.Charset;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Random;

public class DatosCliente {
	
	private String usuario;
	private String sesion; 
	private String estado;
	private LocalDateTime hora;

	private ArrayList<Coordenadas> coordenadas;
	private Socket s;
	
	public DatosCliente(String usuario){
		
		this.usuario =  usuario;
		this.estado  = "Inactivo";
		this.coordenadas = new ArrayList<Coordenadas>();
		
	}
	
	public void anadirSocket(Socket s){
		this.s=s;
	}
	
	public Socket getSocket() {
		return s;
	}
	
	public void anadirSesion(String sesion){
		this.setSesion(sesion);
	}	

	public String getUsuario() {
		return usuario;
	}
	
	public void setUsuario(String usuario) {
		this.usuario = usuario;
	}
	
	public String getSesion() {
		return sesion;
	}

	public void setSesion(String sesion) {
		this.sesion = sesion;
	}
	
	public void generarSesion() {
		byte[] array = new byte[20]; 
	    new Random().nextBytes(array);
	    this.sesion = new String(array, Charset.forName("UTF-8"));	    
	}
	
	public String getEstado() {
		return estado;
	}
	
	public void setEstado(String estado) {
		this.estado = estado;
	}
	
	public void añadirCoordenadas(String coordenadas) {
		
		String[] arrayCoord = coordenadas.split(",", -1);
		
		//Metemos la fecha para el orden? o mejor un orden de entrada?
		
		Coordenadas coord = new Coordenadas(arrayCoord[0].toString(),arrayCoord[1].toString(), LocalDateTime.now());
		
		this.coordenadas.add(coord);
		
	}

	public Boolean buscarCoordenada(String coordenadaBusqueda, LocalDateTime horaInicio) {
		
		String[] arrayCoord = coordenadaBusqueda.split(",", -1);
		Boolean retorno = false;
		
		for (Coordenadas coordenadaCliente : this.coordenadas) 
		{			
			if(EsHoraMayorIgual(coordenadaCliente.getHora(), horaInicio)){							
			
				if(coordenadaCliente.getLatitud().equals(arrayCoord[0]) && coordenadaCliente.getLongitud().equals(arrayCoord[1])) {
					retorno = true;
					break;
				}
			}
		}
		
		return retorno;
					
	}
	
	public void setCoordenadas(ArrayList<Coordenadas> coordenadas) {
		this.coordenadas = coordenadas;
	}

	public ArrayList<Coordenadas> getCoordenadas() {
		
		return this.coordenadas;
		
	}

	public LocalDateTime getHora() {
		return hora;
	}

	public void setHora(LocalDateTime hora) {
		this.hora = hora;
	}
	
	public boolean EsHoraMayorIgual(LocalDateTime hora1, LocalDateTime hora2) {
		
		int horas1 = hora1.getHour();
		int minutos1 = hora1.getMinute();
		int segundos1 = hora1.getSecond();
		
		int horas2 = hora2.getHour();
		int minutos2 = hora2.getMinute();
		int segundos2 = hora2.getSecond();
		
		if(horas1 > horas2) {
			return true;
		}
		else if(horas1 < horas2) {
			return false;
		}
		else {
			if(minutos1 == minutos2) {
				if(segundos1 < segundos2) {
					return false;
				}
				else {
					return true;
				}
			}
			else if(minutos1 > minutos2) {
				return true;
			}
			else {
				return false;
			}
		}		
	}
	
}


