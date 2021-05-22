package Peticiones;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import Entidades.LittleEndian;
import Entidades.TipoMensaje;

public class Puntuacion {
	
	private String 		puntuacion;
	private TipoMensaje identificadorPeticion;
	
	public Puntuacion(String puntuacion){
		
		this.puntuacion			   = puntuacion;
		this.identificadorPeticion = TipoMensaje.Puntuacion;
	
	}
	
	public void aplanar(OutputStream salida){
		
		int longitud;
		
		try {

			int tipoOrdinal = this.identificadorPeticion.ordinal();
			salida.write(LittleEndian.empaquetar(tipoOrdinal));
						
			longitud = this.puntuacion.getBytes("UTF-8").length;
			salida.write(LittleEndian.empaquetar(longitud));
			salida.write(this.puntuacion.getBytes("UTF-8"));
									
			salida.flush();
			
		} catch (IOException e) {
			e.printStackTrace();
		}
	
	}
	
	public static Puntuacion desaplanar(InputStream entrada){
		
		Puntuacion peticion = null;
		int longitud;
		
		try {	
			
			byte[] tamanoBytes = new byte[4];
			entrada.read(tamanoBytes);
			longitud = LittleEndian.desempaquetar(tamanoBytes);
			
			byte[] peticionBytes = new byte[longitud];
			entrada.read(peticionBytes);
			String puntuacion = new String(peticionBytes, "UTF-8");
		
			
			peticion = new Puntuacion(puntuacion);
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return peticion;
	}

	public String getPuntuacion() {
		return puntuacion;
	}

	public void setPuntuacion(String puntuacion) {
		this.puntuacion = puntuacion;
	}
	
	
}
