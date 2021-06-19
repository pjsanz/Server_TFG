package Peticiones;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.MessageDigest;
import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import Entidades.LittleEndian;
import Entidades.TipoMensaje;

public class Autenticacion {
	
	private String password;
	private String usuario;
	private TipoMensaje identificadorPeticion;
	
	public Autenticacion(String usuario, String password){
		
		this.usuario = usuario;
		this.password = password;
		this.identificadorPeticion = TipoMensaje.Autenticacion;
	
	}
	
	public void aplanar(OutputStream salida) throws Exception{
		
		int longitud;
		
		try {

			int tipoOrdinal = this.identificadorPeticion.ordinal();
			salida.write(LittleEndian.empaquetar(tipoOrdinal));
						
			longitud = this.usuario.getBytes("UTF-8").length;
			salida.write(LittleEndian.empaquetar(longitud));
			salida.write(this.usuario.getBytes("UTF-8"));
			
			longitud = this.password.getBytes("UTF-8").length;
			salida.write(LittleEndian.empaquetar(longitud));	
			
			salida.write(cifra(this.password));
						
			salida.flush();
			
		} catch (IOException e) {
			e.printStackTrace();
		}
	
	}
	
	public static Autenticacion desaplanar(InputStream entrada) throws Exception{
		
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
			
			byte[] peticionBytes2 = new byte[16];
			entrada.read(peticionBytes2);
			String password = descifra(peticionBytes2);					
			
			peticion = new Autenticacion(nick, password);
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return peticion;
	}
	
	public String getUsuario() {
		return usuario;
	}
	
	public String getPassword() {
		return password;
	}
	

	public byte[] cifra(String sinCifrar) throws Exception {
		final byte[] bytes = sinCifrar.getBytes("UTF-8");
		final Cipher aes = obtieneCipher(true);
		final byte[] cifrado = aes.doFinal(bytes);
		return cifrado;
	}

	public static String descifra(byte[] cifrado) throws Exception {
		final Cipher aes = obtieneCipher(false);
		final byte[] bytes = aes.doFinal(cifrado);
		final String sinCifrar = new String(bytes, "UTF-8");
		return sinCifrar;
	}

	private static Cipher obtieneCipher(boolean paraCifrar) throws Exception {
		
		final String frase = "Est0yR3@liz@nd0##TFG@ndr0id";
		final MessageDigest digest = MessageDigest.getInstance("SHA");
		digest.update(frase.getBytes("UTF-8"));
		final SecretKeySpec key = new SecretKeySpec(digest.digest(), 0, 16, "AES");

		final Cipher aes = Cipher.getInstance("AES/ECB/PKCS5Padding");
		
		if (paraCifrar) {
			aes.init(Cipher.ENCRYPT_MODE, key);
		} else {
			aes.init(Cipher.DECRYPT_MODE, key);
		}

		return aes;
	}
	
}

