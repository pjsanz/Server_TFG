package Cliente;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.net.UnknownHostException;

import Peticiones.Autenticacion;
import Peticiones.EnvioCoordServidor;
import Peticiones.InicioPartida;
import Peticiones.RespuestaAutenticacion;


public class Cliente {
	
	private Socket s = null;
	private BufferedReader bf;
	private String idSesion;
	
	public Cliente(String maquina, int puerto){
		
		try {
			this.s = new Socket(maquina,puerto);
		} catch (UnknownHostException e) {
			System.out.println(e);
			e.printStackTrace();
		} catch (IOException e) {
			System.out.println(e);
			e.printStackTrace();
		}
	}
	
	public void inicio(){
		
		boolean logeado         = false;
		String  miUsuario       = null;
		boolean peticionEnviada = false;
		
		byte [] bytes;
		int n;
		
		
		System.out.println("Tipos de Peticiones cliente (App Android):");
		
		System.out.println("0 - Autenticación en el sistema: 0 + usuario + contraseña");
		System.out.println("1 - Inicio partida: 			 2 + sesion ");
		System.out.println("2 - Envio coordenadas: 			 4 + sesion ");
		System.out.println("3 - Histórico de puntuaciones: 	 7 + sesion");
				
		
		while(true){
			
			try {
				
				bf = new BufferedReader(new InputStreamReader(System.in));
				
				BufferedOutputStream bos = new BufferedOutputStream(s.getOutputStream());
				DataOutputStream dos = new DataOutputStream(bos);
				
				BufferedInputStream bis = new BufferedInputStream(this.s.getInputStream());
				DataInputStream dis = new DataInputStream(bis);
				
				String linea = bf.readLine();
				
				if (linea==null){
					
					bos.close();
					dos.close();
					bis.close();
					dis.close();
					this.s.close();
					
					break;
				}
				
				String mensaje[] = linea.split(" ");
				
				int longitud = mensaje.length;
				
				try {
					if (mensaje[0].equals("0")&&(!logeado)){
						
						if (longitud==3){
							
							Autenticacion peticion = new Autenticacion(mensaje[1], mensaje[2]);
							peticion.aplanar(dos);
							miUsuario = mensaje[1];
							peticionEnviada=true;
							
						}else{
							System.out.println("El nick debe ser una unica palabra sin espacios");
						}
					}else{
						
						if (mensaje[0].equals("login")){
							System.out.println("Ya estas logeado");
						}
					}
					
					if (mensaje[0].equals("2")&&(logeado)){
									
						String ejemploCoord = "40.4165,-3.70256";
						
						InicioPartida peticion = new InicioPartida(mensaje[1], ejemploCoord);											
						
						peticion.aplanar(dos);						
						
						peticionEnviada=true;
					}
					
				} catch (ArrayIndexOutOfBoundsException e) {
					System.out.println("Petición realizada incorrectamente");
				}
								
				if ((peticionEnviada)&&(!logeado)&&(mensaje[0].equals("0"))){ // Si el mensaje es 0 se que he el login
					
					bytes = new byte[4];
					n = -1;
					
					n = dis.read(bytes);
					
					if (n!=-1){
						
						RespuestaAutenticacion respuestaPeticion = RespuestaAutenticacion.desaplanar(dis);
						
						if (respuestaPeticion.getRespuesta().equals("OK")){
							
							System.out.println("Autenticación completada correctamente!!!");

							logeado=true;
							miUsuario = mensaje[1];
							idSesion = respuestaPeticion.getIdSesion();
							System.out.println("Usuario: " + miUsuario);
							System.out.println("Id Sesion: " + idSesion);
							
						}
						else if (respuestaPeticion.getRespuesta().equals("KO")){
							
							System.out.println("Contraseña incorrecta, nick ya existente!");					
						}
						else {
							System.out.println("Error al realizar la autenticación");		
						}
						
					}
				}
				
				//Aqui tengo que recibir las coordenadas por parte del servidor 
					
				else if(logeado && peticionEnviada && (mensaje[0].equals("2"))) {
					
					bytes = new byte[4];
					n = -1;
					
					n = dis.read(bytes);
					
					if (n!=-1){
						
						EnvioCoordServidor respuestaPeticion = EnvioCoordServidor.desaplanar(dis);
						System.out.println(respuestaPeticion);						//Aqui hago la logica
						//Lo tendre que guardar en una estructura de datos también no? tengo que ver como hacer eso 
						//Como comparar las colisiones
										
					}
					}else{
						if (!logeado&&linea!=null){
							System.out.println("Lo primero debes autenticarte para interactuar");
						}else{
							if(!peticionEnviada&&linea!=null){
								System.out.println("Comando no vÃ¡lido");
							}
						}
					}
				
				} catch (IOException e) {
					System.out.println(e);
					e.printStackTrace();
				}
				peticionEnviada=false;
		}
		
		
	}
}
