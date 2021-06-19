package Peticiones;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import Entidades.LittleEndian;
import Entidades.TipoMensaje;

public class EnvioCoordCliente {
	private String idSesion;
	private String coordenadas;	
	private TipoMensaje identificadorPeticion;
	
	public EnvioCoordCliente(String idSesion, String coordenadas){
		
		this.idSesion = idSesion;
		this.coordenadas = coordenadas;
		this.identificadorPeticion = TipoMensaje.EnvioCoordCliente;
	
	}
	
	public void aplanar(OutputStream salida){
		
		int longitud;
		
		try {

			int tipoOrdinal = this.identificadorPeticion.ordinal();
			salida.write(LittleEndian.empaquetar(tipoOrdinal));
						
			longitud = this.idSesion.getBytes("UTF-8").length;
			salida.write(LittleEndian.empaquetar(longitud));
			salida.write(this.idSesion.getBytes("UTF-8"));
			
			longitud = this.coordenadas.getBytes("UTF-8").length;
			salida.write(LittleEndian.empaquetar(longitud));
			salida.write(this.coordenadas.getBytes("UTF-8"));
						
			salida.flush();
			
		} catch (IOException e) {
			e.printStackTrace();
		}
	
	}
	
	public static EnvioCoordCliente desaplanar(InputStream entrada){
		
		EnvioCoordCliente peticion = null;
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
			String coordenadas = new String(peticionBytes2, "UTF-8");
			
			peticion = new EnvioCoordCliente(idSesion, coordenadas);
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return peticion;
	}
	
	public String getIdSesion() {
		return this.idSesion;
	}
	
	public String getCoordenadas() {
		return this.coordenadas;
	}
	
	public void SetCoordenadas(String coordenadas) {
		this.coordenadas = coordenadas;
	}
}


