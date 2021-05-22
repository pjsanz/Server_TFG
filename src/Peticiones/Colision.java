package Peticiones;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import Entidades.LittleEndian;
import Entidades.TipoMensaje;

public class Colision {
		
	private String idSesion;
	private String usuarioColision;	
	private TipoMensaje    identificadorPeticion;
	
	public Colision(String idSesion, String usuarioColision){
		
		this.idSesion			   = idSesion;
		this.usuarioColision 	    = usuarioColision;
		this.identificadorPeticion = TipoMensaje.Colision;
	
	}
	
	public void aplanar(OutputStream salida){
		
		int longitud;
		
		try {

			int tipoOrdinal = this.identificadorPeticion.ordinal();
			salida.write(LittleEndian.empaquetar(tipoOrdinal));
						
			longitud = this.idSesion.getBytes("UTF-8").length;
			salida.write(LittleEndian.empaquetar(longitud));
			salida.write(this.idSesion.getBytes("UTF-8"));
			
			longitud = this.usuarioColision.getBytes("UTF-8").length;
			salida.write(LittleEndian.empaquetar(longitud));
			salida.write(this.usuarioColision.getBytes("UTF-8"));
						
			salida.flush();
			
		} catch (IOException e) {
			e.printStackTrace();
		}
	
	}
	
	public static Colision desaplanar(InputStream entrada){
		
		Colision peticion = null;
		int longitud;
		
		try {	
			
			byte[] tamanoBytes = new byte[4];
			entrada.read(tamanoBytes);
			longitud = LittleEndian.desempaquetar(tamanoBytes);
			
			byte[] peticionBytes = new byte[longitud];
			entrada.read(peticionBytes);
			String idSesion = new String(peticionBytes, "UTF-8");
			
			byte[] tamanoBytes2 = new byte[4];
			entrada.read(tamanoBytes2);
			longitud = LittleEndian.desempaquetar(tamanoBytes2);
			
			byte[] peticionBytes2 = new byte[longitud];
			entrada.read(peticionBytes2);
			String usuarioColision = new String(peticionBytes2, "UTF-8");
			
			peticion = new Colision(idSesion, usuarioColision);
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return peticion;
	}
	
	public String getIdSesion() {
		return idSesion;
	}

	public void setIdSesion(String idSesion) {
		this.idSesion = idSesion;
	}

	public String getUsuarioColision() {
		return usuarioColision;
	}

	public void setUsuarioColision(String usuarioColision) {
		this.usuarioColision = usuarioColision;
	}

}
