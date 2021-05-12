package Peticiones;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import Entidades.LittleEndian;
import Entidades.TipoMensaje;

public class EnvioCoordServidor {

	private String 		   coordenadasUsuarios;	
	private TipoMensaje    identificadorPeticion;
	
	public EnvioCoordServidor(String coordenadasUsuarios){
		
		this.coordenadasUsuarios   = coordenadasUsuarios;
		this.identificadorPeticion = TipoMensaje.EnvioCoordServidor;
	
	}
	
	public void aplanar(OutputStream salida){
		
		int longitud;
		
		try {

			int tipoOrdinal = this.identificadorPeticion.ordinal();
			salida.write(LittleEndian.empaquetar(tipoOrdinal));
									
			longitud = this.coordenadasUsuarios.getBytes("UTF-8").length;
			salida.write(LittleEndian.empaquetar(longitud));
			salida.write(this.coordenadasUsuarios.getBytes("UTF-8"));
						
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
			String coordenadasUsuarios = new String(peticionBytes, "UTF-8");
			
			peticion = new EnvioCoordServidor(coordenadasUsuarios);
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return peticion;
	}

	public String getCoordenadasUsuarios() {
		return this.coordenadasUsuarios;
	}
	
	public void SetCoordenadasUsuarios(String coordenadasUsuarios) {
		this.coordenadasUsuarios = coordenadasUsuarios;
	}
	
}
