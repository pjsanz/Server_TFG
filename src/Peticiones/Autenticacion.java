package Peticiones;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import Entidades.LittleEndian;
import Entidades.TipoMensaje;

public class Autenticacion {
	
	private String contraseña;
	private String usuario;
	private TipoMensaje    identificadorPeticion;
	
	public Autenticacion(String usuario, String contraseña){
		
		this.usuario			   = usuario;
		this.contraseña 		   = contraseña;
		this.identificadorPeticion = TipoMensaje.Autenticacion;
	
	}
	
	public void aplanar(OutputStream salida){
		
		int longitud;
		
		try {

			int tipoOrdinal = this.identificadorPeticion.ordinal();
			salida.write(LittleEndian.empaquetar(tipoOrdinal));
						
			longitud = this.usuario.getBytes("UTF-8").length;
			salida.write(LittleEndian.empaquetar(longitud));
			salida.write(this.usuario.getBytes("UTF-8"));
			
			longitud = this.contraseña.getBytes("UTF-8").length;
			salida.write(LittleEndian.empaquetar(longitud));
			salida.write(this.contraseña.getBytes("UTF-8"));
						
			salida.flush();
			
		} catch (IOException e) {
			e.printStackTrace();
		}
	
	}
	
	public static Autenticacion desaplanar(InputStream entrada){
		
		Autenticacion peticion = null;
		int longitud;
		
		try {	
			
			byte[] tamanoBytes = new byte[4];
			entrada.read(tamanoBytes);
			longitud = LittleEndian.desempaquetar(tamanoBytes);
			
			byte[] peticionBytes = new byte[longitud];
			entrada.read(peticionBytes);
			String nick = new String(peticionBytes, "UTF-8");
			
			byte[] tamanoBytes2 = new byte[4];
			entrada.read(tamanoBytes2);
			longitud = LittleEndian.desempaquetar(tamanoBytes2);
			
			byte[] peticionBytes2 = new byte[longitud];
			entrada.read(peticionBytes2);
			String contraseña = new String(peticionBytes2, "UTF-8");
			
			peticion = new Autenticacion(nick, contraseña);
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return peticion;
	}
	
	public String getUsuario() {
		return usuario;
	}
	
	public String getContraseña() {
		return contraseña;
	}
	
}

