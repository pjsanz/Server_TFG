package Peticiones;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import Entidades.LittleEndian;
import Entidades.TipoMensaje;

public class RespuestaHistoricoPuntuaciones {
	
	private String puntuaciones;	
	private TipoMensaje identificadorPeticion;
	
	public RespuestaHistoricoPuntuaciones(String puntuaciones){
		
		this.puntuaciones = puntuaciones;
		this.identificadorPeticion = TipoMensaje.HistoricoPuntuaciones;
	
	}
	
	public void aplanar(OutputStream salida){
		
		int longitud;
		
		try {

			int tipoOrdinal = this.identificadorPeticion.ordinal();
			salida.write(LittleEndian.empaquetar(tipoOrdinal));
									
			longitud = this.puntuaciones.getBytes("UTF-8").length;
			salida.write(LittleEndian.empaquetar(longitud));
			salida.write(this.puntuaciones.getBytes("UTF-8"));
						
			salida.flush();
			
		} catch (IOException e) {
			e.printStackTrace();
		}
	
	}
	
	public static RespuestaHistoricoPuntuaciones desaplanar(InputStream entrada){
		
		RespuestaHistoricoPuntuaciones peticion = null;
		int longitud;
		
		try {	
			

			
			byte[] tamanoBytes = new byte[4];
			entrada.read(tamanoBytes);
			longitud = LittleEndian.desempaquetar(tamanoBytes);
			
			byte[] peticionBytes = new byte[longitud];
			entrada.read(peticionBytes);
			String puntuaciones = new String(peticionBytes, "UTF-8");
			
			peticion = new RespuestaHistoricoPuntuaciones(puntuaciones);
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return peticion;
	}	

	public String getPuntuaciones() {
		return puntuaciones;
	}

	public void setPuntuaciones(String puntuaciones) {
		this.puntuaciones = puntuaciones;
	}
	

}
