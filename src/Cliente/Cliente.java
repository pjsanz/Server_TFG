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
import java.util.ArrayList;
import java.util.Random;

import Entidades.DatosCliente;
import Peticiones.Autenticacion;
import Peticiones.Colision;
import Peticiones.EnvioCoordCliente;
import Peticiones.EnvioCoordServidor;
import Peticiones.InicioPartida;
import Peticiones.Puntuacion;
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
		String  usuarioColision = null;
		boolean peticionEnviada = false;
		boolean juegoIniciado   = false;
		ArrayList<DatosCliente> listaDatosRivales = new ArrayList<DatosCliente>();
		
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
											
						//Parte de coordenadas sustituir en app por las coordenadas actuales
						
						Integer latitudCoord = new Random().nextInt(5);
						Integer longitudCoord = new Random().nextInt(5);
						
						String ejemploCoord = latitudCoord.toString() + "," +  longitudCoord.toString();
						
						InicioPartida peticion = new InicioPartida(idSesion, ejemploCoord);											
						
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
					
				else if(!juegoIniciado && logeado && peticionEnviada && (mensaje[0].equals("2"))) {
					
					bytes = new byte[4];
					n = -1;
					
					n = dis.read(bytes);
					
					if (n!=-1){
						
						EnvioCoordServidor respuestaPeticion = EnvioCoordServidor.desaplanar(dis);
												
						System.out.println(respuestaPeticion.getUsuarios());
						System.out.println(respuestaPeticion.getCoordenadas());
						
						//Descompongo y las añado a la lista de usuarios ya que la tengo vacia porque acabo de iniciar 
						
						//Añadimos usuarios menos nosotros mismos!
						
						DatosCliente datos = null;
						
						String[] usuarios = respuestaPeticion.getUsuarios().split("&");
						String[] coordenadas = respuestaPeticion.getCoordenadas().split("@");
						
						String misUltimasCoordenadasEnviadas = "";
						
						for(int i = 0; i < usuarios.length; i++) {
							if(!usuarios[i].equals(miUsuario)) {
								 datos = new DatosCliente(usuarios[i]);
								 datos.añadirCoordenadas(coordenadas[i]);
								 listaDatosRivales.add(datos);
							}
							else 
							{
								misUltimasCoordenadasEnviadas = coordenadas[i];
							}
							
						}
						
						//PINTAMOS COORDENADAS EN EL MAPA EN LA APP DE ANDROID
						
						//Realizamos la comparacion por si hubiera colision
						
						Boolean colision = false;
						
						for (DatosCliente datosCliente : listaDatosRivales) {
							if(!datosCliente.getUsuario().equals(miUsuario)) {
								if(datosCliente.buscarCoordenada(misUltimasCoordenadasEnviadas)){
									colision = true;
									usuarioColision = datosCliente.getUsuario();
								}
							}
					    }
						
						System.out.println(colision);
						
						//Si hay colision envio el mensaje de colision al servidor 
						//Sino le vuelvo a mandar mis coordenadas esta vez ya con un mensaje de envioCoordCliente
						
						if(colision) {
							Colision peticion = new Colision(idSesion, usuarioColision);																		
							peticion.aplanar(dos);	
							
							juegoIniciado = false;
						}
						else { 
							
							Integer latitudCoord = new Random().nextInt(5);
							Integer longitudCoord = new Random().nextInt(5);
							
							String ejemploCoord = latitudCoord.toString() + "," +  longitudCoord.toString();
							
							EnvioCoordCliente peticion = new EnvioCoordCliente(idSesion, ejemploCoord);											
							
							peticion.aplanar(dos);						
							
						}
						
						//Hemos iniciado el juego 
						
						juegoIniciado = true;
										
					}
					
				else if(juegoIniciado) {
					//Aqui recibimos el resto de coordenadas que son envios de cliente y responderemos con nuestras nuevas coordenadas
					//a no ser que haya colision y enviemos el mensaje de colision
					
					bytes = new byte[4];
					n = -1;
					
					n = dis.read(bytes);
					
					if (n!=-1){
						
						EnvioCoordServidor respuestaPeticion = EnvioCoordServidor.desaplanar(dis);
						
						System.out.println(respuestaPeticion.getUsuarios());
						System.out.println(respuestaPeticion.getCoordenadas());
						
						//Descompongo y las añado a la lista de usuarios ya que la tengo vacia porque acabo de iniciar 
						
						//Añadimos usuarios menos nosotros mismos!
						
						DatosCliente datos = null;
						
						String[] usuarios = respuestaPeticion.getUsuarios().split("&");
						String[] coordenadas = respuestaPeticion.getCoordenadas().split("@");
						
						String misUltimasCoordenadasEnviadas = "";
						Boolean existeUsuario = false;
						int     indiceUsuario = 0;
						
						for(int i = 0; i < usuarios.length; i++) {
							if(!usuarios[i].equals(miUsuario)) {
								
								//Comprobamos si el usuario ya estaba añadido para no añadirle de nuevo
								//Solamente introducimos su ultima coordenada
								
								for (DatosCliente datosClienteUsu : listaDatosRivales) {
									if(datosClienteUsu.getUsuario().equals(usuarios[i])) {
										existeUsuario = true;
										break;
									}
									indiceUsuario++;
								}
								
								if(!existeUsuario) {
									 datos = new DatosCliente(usuarios[i]);
									 datos.añadirCoordenadas(coordenadas[i]);
									 listaDatosRivales.add(datos);
								}
								else {
									datos = listaDatosRivales.get(indiceUsuario);
									datos.añadirCoordenadas(coordenadas[i]);
									
									listaDatosRivales.set(indiceUsuario, datos);
								}
								
							}
							else 
							{
								misUltimasCoordenadasEnviadas = coordenadas[i];
							}
							
						}
						
						//PINTAMOS COORDENADAS EN EL MAPA EN LA APP DE ANDROID
						
						//Realizamos la comparacion por si hubiera colision
						
						Boolean colision = false;
						
						for (DatosCliente datosCliente : listaDatosRivales) {
							if(!datosCliente.getUsuario().equals(miUsuario)) {
								if(datosCliente.buscarCoordenada(misUltimasCoordenadasEnviadas)){
									colision = true;
									usuarioColision = datosCliente.getUsuario();
								}
							}
					    }
						
						System.out.println(colision);
						
						//Si hay colision envio el mensaje de colision al servidor, sino vuelvo a mandar mis coordenadas actuales
						
						if(colision) {
							
							Colision peticion = new Colision(idSesion, usuarioColision);																		
							peticion.aplanar(dos);	
							juegoIniciado = false;
						}
						else { 
							
							Integer latitudCoord = new Random().nextInt(5);
							Integer longitudCoord = new Random().nextInt(5);
							
							String ejemploCoord = latitudCoord.toString() + "," +  longitudCoord.toString();
							
							EnvioCoordCliente peticion = new EnvioCoordCliente(idSesion, ejemploCoord);											
							
							peticion.aplanar(dos);					
						}
						
					}
															
				}
				else if(!juegoIniciado) //Recibo respuesta de puntuacion
				{
					
					bytes = new byte[4];
					n = -1;
					
					n = dis.read(bytes);
					
					if (n!=-1){		
						
						Puntuacion respuestaPuntuacion = Puntuacion.desaplanar(dis);
						System.out.println(respuestaPuntuacion.getPuntuacion() + " Chocaste con:" + usuarioColision);

					}
					
					//Recibimos el mensaje de puntuacion y le mostramos
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
