package Entidades;


import java.net.Socket;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Date;
import java.util.Random;

public class DatosCliente {
	
	private String usuario;
	private String sesion; 
	private String estado;
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
		
		Coordenadas coord = new Coordenadas(arrayCoord[0].toString(),arrayCoord[1].toString(), new Date().toString());
		
		this.coordenadas.add(coord);
		
	}

	public Boolean buscarCoordenada(String coordenadaBusqueda) {
		
		String[] arrayCoord = coordenadaBusqueda.split(",", -1);
		Boolean retorno = false;
		
		for (Coordenadas coordenadaCliente : this.coordenadas) 
		{
			if(coordenadaCliente.getLatitud().equals(arrayCoord[0]) && coordenadaCliente.getLongitud().equals(arrayCoord[1])) {
				retorno = true;
				break;
			}
		}
		
		return retorno;
					
	}
	
	public ArrayList<Coordenadas> getCoordenadas() {
		
		return this.coordenadas;
		
	}
	
}


