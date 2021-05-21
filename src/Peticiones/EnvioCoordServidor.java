package Peticiones;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import Entidades.LittleEndian;
import Entidades.TipoMensaje;

public class EnvioCoordServidor {

	private String 		   usuarios;
	private String 		   coordenadas;	
	private TipoMensaje    identificadorPeticion;
	
	public EnvioCoordServidor(String usuarios, String coordenadas){
		
		this.usuarios    = usuarios;
		this.coordenadas = coordenadas;

		this.identificadorPeticion = TipoMensaje.EnvioCoordServidor;
	
	}
	
	public void aplanar(OutputStream salida){
		
		int longitud;
		
		try {

			int tipoOrdinal = this.identificadorPeticion.ordinal();
			salida.write(LittleEndian.empaquetar(tipoOrdinal));
									
			longitud = this.usuarios.getBytes("UTF-8").length;
			salida.write(LittleEndian.empaquetar(longitud));
			salida.write(this.usuarios.getBytes("UTF-8"));
			
			longitud = this.coordenadas.getBytes("UTF-8").length;
			salida.write(LittleEndian.empaquetar(longitud));
			salida.write(this.coordenadas.getBytes("UTF-8"));
			
			salida.flush();
			
		} catch (IOException e) {
			e.printStackTrace();
		}
	
	}	

	public static EnvioCoordServidor desaplanar(InputStream entrada){
		
		EnvioCoordServidor peticion = null;
		int longitud;
		
		try {	
			
			byte[] tamanoBytes = new byte[4];
			entrada.read(tamanoBytes);
			longitud = LittleEndian.desempaquetar(tamanoBytes);
			
			byte[] peticionBytes = new byte[longitud];
			entrada.read(peticionBytes);
			String usuarios = new String(peticionBytes, "UTF-8");
			
			byte[] tamanoBytes2 = new byte[4];
			entrada.read(tamanoBytes2);
			longitud = LittleEndian.desempaquetar(tamanoBytes2);
			
			byte[] peticionBytes2 = new byte[longitud];
			entrada.read(peticionBytes2);
			String coordenadas = new String(peticionBytes2, "UTF-8");
						
			peticion = new EnvioCoordServidor(usuarios, coordenadas);
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return peticion;
	}

	public String getUsuarios() {
		return usuarios;
	}

	public void setUsuarios(String usuarios) {
		this.usuarios = usuarios;
	}

	public String getCoordenadas() {
		return coordenadas;
	}

	public void setCoordenadas(String coordenadas) {
		this.coordenadas = coordenadas;
	}

	
}
