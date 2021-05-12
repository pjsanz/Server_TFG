package Peticiones;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import Entidades.LittleEndian;
import Entidades.TipoMensaje;

public class Autenticacion {
	
	private String contrase�a;
	private String usuario;
	private TipoMensaje    identificadorPeticion;
	
	public Autenticacion(String usuario, String contrase�a){
		
		this.usuario			   = usuario;
		this.contrase�a 		   = contrase�a;
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
			
			longitud = this.contrase�a.getBytes("UTF-8").length;
			salida.write(LittleEndian.empaquetar(longitud));
			salida.write(this.contrase�a.getBytes("UTF-8"));
						
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
			String contrase�a = new String(peticionBytes2, "UTF-8");
			
			peticion = new Autenticacion(nick, contrase�a);
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return peticion;
	}
	
	public String getUsuario() {
		return usuario;
	}
	
	public String getContrase�a() {
		return contrase�a;
	}
	
}

