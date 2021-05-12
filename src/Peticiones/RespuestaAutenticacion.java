package Peticiones;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

import Entidades.LittleEndian;
import Entidades.TipoMensaje;


public class RespuestaAutenticacion {

	private String respuesta;
	private String idSesion;
	private TipoMensaje identificadorPeticion;
	
	public RespuestaAutenticacion(String respuesta){
		
		this.respuesta			   = respuesta;
		this.idSesion 			   = "0";
		this.identificadorPeticion = TipoMensaje.RespuestaAutenticacion;
	
	}
		
	public void aplanar(OutputStream salida){
		
		int longitud;
		
		try {
				int tipoOrdinal = this.identificadorPeticion.ordinal();
				salida.write(LittleEndian.empaquetar(tipoOrdinal));

				longitud = this.respuesta.getBytes("UTF-8").length;
				salida.write(LittleEndian.empaquetar(longitud));
				salida.write(this.respuesta.getBytes("UTF-8"));					
				salida.flush();
				
				longitud = this.idSesion.getBytes("UTF-8").length;
				salida.write(LittleEndian.empaquetar(longitud));
				salida.write(this.idSesion.getBytes("UTF-8"));					
				salida.flush();
				
			} catch (IOException e) {
			e.printStackTrace();
		}
	
	}
	
	public static RespuestaAutenticacion desaplanar(InputStream entrada){
		
		RespuestaAutenticacion peticion = null;
		int longitud;
		
		try {	
			
			byte[] tamanoBytes = new byte[4];
			entrada.read(tamanoBytes);
			longitud = LittleEndian.desempaquetar(tamanoBytes);
			
			byte[] peticionBytes = new byte[longitud];
			entrada.read(peticionBytes);
			String respuesta = new String(peticionBytes, "UTF-8");
			
			byte[] tamanoBytes2 = new byte[4];
			entrada.read(tamanoBytes2);
			int longitud2 = LittleEndian.desempaquetar(tamanoBytes2);
			
			byte[] peticionBytes2 = new byte[longitud2];
			entrada.read(peticionBytes2);
			String idSesion = new String(peticionBytes2, "UTF-8");
			
			peticion = new RespuestaAutenticacion(respuesta);
			peticion.setIdSesion(idSesion);
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return peticion;
	}
	
	public String getRespuesta() {
		return respuesta;
	}
	
	public String getIdSesion() {
		return this.idSesion;
	}
	
	public void setIdSesion(String idSesion) {
		this.idSesion = idSesion;
	}
		
	public void generarIdSesion() {
	    this.idSesion = UUID.randomUUID().toString();	    
	} 
}
